import sqlite3
import numpy as np
from StopStem import StopStem
from ContentExtractor import ContentExtractor
from multiprocessing import Pool
from itertools import repeat
from functools import partial

class resultItems:
    def __init__(self,score,title,url,keywords=None,parentLinks=None,childLinks=None):
        self.score = score
        self.title = title
        self.url = url
        self.keywords = keywords
        self.parentLinks = parentLinks
        self.childLinks = childLinks


class retrieval_function:
    def __init__(self):
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()
        documents = self.cur.execute("SELECT * FROM ForwardIndex").fetchall()
        self.ss = StopStem()
        self.ce = ContentExtractor()
        self.N:int = len(documents)  # number of documents

    def splitPrompt(self, prompt:str) -> list:
        terms = prompt.split('"')
        phrase:list[str] = terms[1::2]
        remain:list[str] = terms[::2]
        single_term = ""
        for part in remain:
            single_term = single_term + part
        terms = []
        #process phrase
        for ph in phrase:
            words:list[str] = self.ce.splitWords(ph)
            words = self.ss.process(words)
            # add single term in the phrase 
            for word in words:
                if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone() is not None:
                    terms.append(word)
            # combine the split terms to phrase
            if self.checkAllWordsHaveId(words):
                combine:str = ""
                for word in words:
                    combine = combine + word + " "
                combine = combine[:-1]
                # print("combine: ",combine)
                terms.append(combine)
        # process single term
        keywords = self.ce.splitWords(single_term)
        keywords = self.ss.process(keywords)
        # terms.extend(keywords)
        for t in keywords:
            if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(t,)).fetchone() is not None:
                terms.append(t)
        return terms
    
    def get_relevant_urlid(self,terms:list[str]) -> list:

        urlids:set = set()
        for term in terms:
            if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(term,)).fetchone() is not None:
                keywordId:str = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(term,)).fetchone()[0]
            else:
                continue
            
            if self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{keywordId}'").fetchone() is not None:
                bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                records:list[str] = bodyValue.split(" ")
                # print("len:", len(records))
                for record in records:
                    doc_fre_pos:list[str] = record.split(":")
                    urlids.add(doc_fre_pos[0])

            if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone() is not None:
                bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                records:list[str] = bodyValue.split(" ")
                # print("len:", len(records))
                for record in records:
                    doc_fre_pos:list[str] = record.split(":")
                    urlids.add(doc_fre_pos[0])

        return list(urlids)
    
    def calculate_phrase_df(self, phrase:list[str],title:bool=False) -> int:

        df:int = 0
        table = {}
        for word in phrase:
            inner_table = {}
            wordId:str = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone()[0]
            if title:
                if self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                    return 0
                bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]
            else:
                if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                    return 0
                bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]
            records = bodyValue.split(" ")

            for record in records:
                doc_fre_pos:list[str] = record.split(":")
                inner_table[doc_fre_pos[0]] = doc_fre_pos[2].split(",")
                # print(doc_fre_pos[0],":",inner_table[doc_fre_pos[0]])

            table[word] = inner_table
            # print(f"len of {word}: ",len(table[word]))
        
        first_inner_table:dict = table[phrase[0]]
        for doc in (first_inner_table.keys()):
            flag:bool = True
            for i in range(1,len(phrase)):
                if doc not in dict(table[phrase[i]]).keys():
                    flag = False 
                    break
            
            if flag == False: # the doc not have all words in phrase
                continue

            for pos in first_inner_table[doc]:
                flag = True
                for i in range(1,len(phrase)):
                    inner_table:dict = table[phrase[i]]
                    if str(int(pos)+1) not in inner_table[doc]:
                        flag = False 
                        break
                if flag == True:
                    df += 1
                    break
        return df

    
    def checkAllWordsHaveId(self,phrase:list[str]) -> bool:
        for word in phrase:
            if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone() is None:
                return False
        return True

    
    def check_phrase(self, urlid:str, phrase:list[str], len_phrase:int, check_word_index:int, check_pos:int,title:bool = False)->bool: 
        if check_word_index == len_phrase: # all words in phrase have been checked
            return True
        else:
            word:str = phrase[check_word_index]
            if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone() is None:
                # print("keyword2id")
                return False
            wordId:str = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone()[0]
            if title:
                if self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                    # print("invertedindex")
                    return False
                bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]
            else:
                if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                    # print("invertedindex")
                    return False
                bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]

            records:list[str] = bodyValue.split(" ")
            for record in records:
                # print(record)
                if urlid in record:
                    doc_fre_pos = record.split(":")
                    positions = doc_fre_pos[2].split(",")
                    if str(check_pos) in positions:
                        return self.check_phrase(urlid,phrase,len_phrase,check_word_index+1,check_pos+1)
                    else:
                        return False

    def term_fre_doc(self,urlid:str,keywords:list[str],title:bool = False) -> dict:
        term_fre = {}
        for keyword in keywords:
            if " " in keyword: # phrase
                fre:int = 0
                phrase:list[str] = keyword.split(" ")
                table = {}
                for word in phrase:
                    inner_table = {}
                    wordId:str = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(word,)).fetchone()[0]
                    if title:
                        if self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                            return 0
                        bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]
                    else:
                        if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone() is None:
                            return 0
                        bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{wordId}'").fetchone()[0]
                    records = bodyValue.split(" ")
                    for record in records:
                        if urlid in record:
                            doc_fre_pos:list[str] = record.split(":")
                            table[word] = doc_fre_pos[2].split(",")
                            break
                
                pos_of_first_word:list[str] = table[phrase[0]]
                for pos in pos_of_first_word:
                    for i in range(1,len(phrase)):
                        pos_ls:list[str] = table[phrase[i]]
                        if str(int(pos)+1) not in pos_ls:
                            break
                        if i == (len(phrase) - 1 ):
                            fre += 1
                
                term_fre[keyword] = fre

            else: # single word
                if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(keyword,)).fetchone() is None:
                    continue
                keywordId:str = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(keyword,)).fetchone()[0]

                if title:
                    if self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{keywordId}'").fetchone() is None:
                        continue
                    bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                else:
                    if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone() is None:
                        continue
                    bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                records:list[str] = bodyValue.split(" ")
                for doc in records:
                    if urlid in str(doc):
                        doc_fre_pos = doc.split(":")
                        term_fre[keyword] = int(doc_fre_pos[1])
        return term_fre
    

    def get_doc_vector(self,urlid:str) -> dict:
        doc_vector = {}
        if self.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='{urlid}'").fetchone() is not None:
            value:str = self.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='{urlid}'").fetchone()[0]
            keywordIds:list[str] = value.split(" ")
            for keywordId in keywordIds:
                if self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone() is not None:
                    bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                    records:list[str] = bodyValue.split(" ")
                    for record in records:
                        if urlid in record:
                            doc_fre_pos:list[str] = record.split(":")
                            key:str = self.cur.execute(f"SELECT keyword FROM Id2keyword WHERE keywordId='{keywordId}'").fetchone()[0]

                            doc_vector[key] = int(doc_fre_pos[1])
                            break

        return doc_vector


    # query vector
    def term_fre_query(self, keywords:list[str]) -> dict:
        
        term_fre = {}
        for term in keywords:
            if " " in term:
                single_term:list[str] = term.split(" ")
                if self.checkAllWordsHaveId(single_term):
                    if term not in term_fre:
                        term_fre[term] = 1
                    else:
                        term_fre[term] = term_fre[term] + 1
            else:
                if self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(term,)).fetchone() is None:
                    continue
                if term not in term_fre:
                    term_fre[term] = 1
                else:
                    term_fre[term] = term_fre[term] + 1
            
        # print(term_fre)
        return term_fre
    
    #calculate_doc_weight return whe weightlist(document vector)
    def calculate_doc_weights(self, urlid:str, term_fre:dict, title:bool=False) -> dict: 
        weightList = {} # (keyword : weight)
        if len(list(term_fre.values())) == 0:
            return {}
        tf_max = max(list(term_fre.values()))
        for term in term_fre.keys():
            if " " in term: #phrase
                words = str(term).split(" ")
                df = self.calculate_phrase_df(words,title)
                idf = np.log2(self.N/df)
                weightList[term] = term_fre[term] * idf / tf_max
            else:   #single word
                # print(term)
                keywordId = self.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(term,)).fetchone()[0]
                if title:
                    bodyValue:str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                else:
                    bodyValue:str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordId}'").fetchone()[0]
                records = bodyValue.split(" ")
                df = len(records)
                idf = np.log2(self.N/df)
                weightList[term] = term_fre[term] * idf / tf_max
        return weightList

    
    def calculate_cos_similarity(self,query_vector:dict, doc_vector:dict) -> float:

        #calculate the norm of query vector
        square_sum_query:float = 0
        for term in query_vector.keys():
            square_sum_query += query_vector[term]**2
        norm_query = square_sum_query**(0.5)

        #calculate the norm of doc vector
        square_sum_doc: float = 0
        for term in doc_vector.keys():
            square_sum_doc += doc_vector[term]**2
        norm_doc = square_sum_doc**(0.5)

        # calculate dot product
        dot_product:float = 0
        for term in query_vector.keys():
            if term in doc_vector.keys():
                dot_product += query_vector[term]*doc_vector[term]

        if norm_query*norm_doc == 0:
            return 0
        
        return dot_product/(norm_query*norm_doc)
    
    def get_score(self,urlid:str, query:list[str]) -> float:
        if len(query) == 0:
            return 0
        # get query vector
        query_vector:dict = self.term_fre_query(query)

        # calculate body similarity

        doc_vec:dict = self.get_doc_vector(urlid)  # doc vector without phrase
        term_fre_doc:dict = self.term_fre_doc(urlid,query,False) # get phrase fre
        for key in term_fre_doc.keys():
            if key not in doc_vec.keys():
                doc_vec[key] = term_fre_doc[key]
        # calculate weighted vector
        weighted_body_vector:dict = self.calculate_doc_weights(urlid,doc_vec,False)
        body_sim:float = self.calculate_cos_similarity(query_vector,weighted_body_vector)
        # print(body_sim)

        #calculate title similarity

        title_vector:dict = rf.term_fre_doc(urlid,query,True)
        # calculate weighted vector
        weighted_title_vector: dict = rf.calculate_doc_weights(urlid,title_vector,True)
        title_sim:float = rf.calculate_cos_similarity(weighted_title_vector,query_vector)
        # print(title_sim)

        return (5*title_sim+body_sim)
    
    def get_result(self,urlid:str, query:list[str])->resultItems:
        score:float = self.get_score(urlid,query)
        title:str = self.cur.execute(f"SELECT title FROM Title WHERE urlId='{urlid}'").fetchone()[0]
        url:str = self.cur.execute(f"SELECT url FROM id2url WHERE urlId='{urlid}'").fetchone()[0]
        tv = self.term_fre_doc(urlid,query,True)
        bv = self.term_fre_doc(urlid,query,False)
        for key in tv.keys():
            if key in bv.keys():
                bv[key] = bv[key] + tv[key]
            else:
                bv[key] = tv[key]
        # parentLinks
        if self.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone() is not None:
            childLinks= self.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone()[0]
            child= childLinks.split(" ")
        else:
            child = None
        
        return resultItems(score=score,title=title,url=url,keywords=bv,parentLinks=None,childLinks=child)

        # score:float = rf.get_score(urlid,query)
        # title:str = rf.cur.execute(f"SELECT title FROM Title WHERE urlId='{urlid}'").fetchone()[0]
        # url:str = rf.cur.execute(f"SELECT url FROM id2url WHERE urlId='{urlid}'").fetchone()[0]
        # tv = rf.term_fre_doc(urlid,query,True)
        # bv = rf.term_fre_doc(urlid,query,False)
        # for key in tv.keys():
        #     if key in bv.keys():
        #         bv[key] = bv[key] + tv[key]
        #     else:
        #         bv[key] = tv[key]
        # # parentLinks
        # if rf.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone() is not None:
        #     childLinks= rf.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone()[0]
        #     child= childLinks.split(" ")
        # else:
        #     child = None
        
        # return resultItems(score=score,title=title,url=url,keywords=bv,parentLinks=None,childLinks=child)
        




def get_AllResult(prompt:str):
    results = []
    rf = retrieval_function()
    query:list[str] = rf.splitPrompt(prompt)
    urlids:list[str] = rf.get_relevant_urlid(query)
    # for urlid in urlids:
    #     score:float = rf.get_score(urlid,query)
    #     title:str = rf.cur.execute(f"SELECT title FROM Title WHERE urlId='{urlid}'").fetchone()[0]
    #     url:str = rf.cur.execute(f"SELECT url FROM id2url WHERE urlId='{urlid}'").fetchone()[0]
    #     tv = rf.term_fre_doc(urlid,query,True)
    #     bv = rf.term_fre_doc(urlid,query,False)
    #     for key in tv.keys():
    #         if key in bv.keys():
    #             bv[key] = bv[key] + tv[key]
    #         else:
    #             bv[key] = tv[key]
    #     # parentLinks
    #     if rf.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone() is not None:
    #         childLinks= rf.cur.execute(f"SELECT value FROM ChildUrl WHERE urlId='{urlid}'").fetchone()[0]
    #         child= childLinks.split(" ")
    #     else:
    #         child = None
        
    #     results.append(resultItems(score=score,title=title,url=url,keywords=bv,parentLinks=None,childLinks=child))
    # with Pool(10) as pool:
    #     results = pool.map(retrieval_function.get_result)
    # return results


if __name__ == '__main__':
    
    rf = retrieval_function()

    #test prepare
    # for urlid in rf.table.keys():
    #     print(urlid)

    # test splitPrompt
    # split = rf.splitPrompt('"movie list" page')
    # print(split)

    #test get_relevant_urlid()
    # urlids = rf.get_relevant_urlid(["movi"])
    # for urlid in urlids:
    #     print(urlid)

    # res = rf.cur.execute("SELECT * FROM CrawledPage")
    # for url, urlid in res:
    #     print(url + ": " + urlid)
    # testpage : 41096512194994237309005844220821308201
    # urlid = 41096512194994237309005844220821308201
    # keywords = rf.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='{urlid}'").fetchone()[0]
    # print(keywords)


    # keywordIds = rf.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='41096512194994237309005844220821308201'").fetchone()[0]
    # print(keywordIds)


    # keywordid = "106942772400394457250452673493895238648"
    # doc_fre_pos = rf.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{keywordid}'").fetchone()[0]
    # # print(doc_fre_pos)

    # #test checkAllWordsHaveId
    # print(rf.checkAllWordsHaveId(split[0:2]))

    # test get_doc_vector()
    # v = rf.get_doc_vector("77974596340136639431944900385628818684")
    # print(v)

    # #test check_phrase
    # test = rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword='test'").fetchone()[0]
    # print(test) #232490620194322860220908183140824686486
    # pag = rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword='page'").fetchone()[0]
    # print(pag) #148461418161209252101072365524762817459
    # movi = rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword='movi'").fetchone()[0]
    # print(movi) #106942772400394457250452673493895238648
    # ls = rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword='list'").fetchone()[0]
    # print(ls) #206097062474549307732880462118285530663
    # test_phrase =[]
    # test_phrase.append(movi)
    # test_phrase.append(ls)

    # test_bodyValue = rf.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{test}'").fetchone()[0]
    # test_doc_fre_pos = str(test_bodyValue).split(" ")
    # for page in test_doc_fre_pos:
    #     if "41096512194994237309005844220821308201" in page:
    #         print(page)

    # page_bodyValue = rf.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId='{pag}'").fetchone()[0]
    # page_doc_fre_pos = str(page_bodyValue).split(" ")
    # for page in page_doc_fre_pos:
    #     # if "41096512194994237309005844220821308201" in pag:
    #         print(page)


    # movi_bodyValue = rf.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{movi}'").fetchone()[0]
    # movi_doc_fre_pos = str(movi_bodyValue).split(" ")
    # for page in movi_doc_fre_pos:
    #     if "41096512194994237309005844220821308201" in page:
    #         print(page)

    # ls_bodyValue = rf.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId='{ls}'").fetchone()[0]
    # ls_doc_fre_pos = str(ls_bodyValue).split(" ")
    # for page in ls_doc_fre_pos:
    #     if "41096512194994237309005844220821308201" in page:
    #         print(page)
    # print(rf.check_phrase("41096512194994237309005844220821308201",split[0:2],2,0,12))
    # print(rf.check_phrase("41096512194994237309005844220821308201",["test", "page"],2,0,0,True))

    # # test calculate_phrase_df
    # df = rf.calculate_phrase_df(split[0:2])
    # print("df:",df)
    # df = rf.calculate_phrase_df_fre1 = rf.term_fre_doc("41096512194994237309005844220821308201",["test page","test","page"])
    # print(term_fre1)
    # term_fre2 = rf.term_fre_doc("41096512194994237309005844220821308201",["test page","test","page"],True)
    # print(term_fre2)

    # #test term_fre_query()
    # term_fre_Q = rf.term_fre_query(split)
    # print(term_fre_Q)

    # # test get_doc_vector()
    # doc_vec = rf.get_doc_vector("41096512194994237309005844220821308201")
    # print(doc_vec)

    # # test calculate_doc_weights()
    # weight1 = rf.calculate_doc_weights("41096512194994237309005844220821308201",term_fre1)
    # print(weight1)
    # weight2 = rf.calculate_doc_weights("41096512194994237309005844220821308201",term_fre2,True)
    # print(weight2)
   
    # #test calculate_cos_similarity()
    # sim_body = rf.calculate_cos_similarity(term_fre_Q,weight1)
    # print(sim_body)
    # sim_title = rf.calculate_cos_similarity(term_fre_Q,weight2)
    # print(sim_title)

    # test get_score()
    # score = rf.get_score("41096512194994237309005844220821308201",["test page","test","page"])
    # print(score)
 
    # terms = rf.splitPrompt('"hello world" super')
    # print(terms)

    # test term_fre_doc()
    # term_fre1 = rf.term_fre_doc("41096512194994237309005844220821308201",split)
    # term

    # test get_result()
    from time import time
    start = time()
    results:list[resultItems] = get_AllResult("hello world")
    end = time()
    print(end-start)
    for result in results:
        print(result.score)
    # test:str = "hkust"
    # testSplit = test.split(" ")
    # print(testSplit)

    # hello = rf.splitPrompt("world")
    # print(hello)

    # if rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(hello[0],)).fetchone() is None:
    #     print("None")
    # else:
    #     keywordid:str = rf.cur.execute(f"SELECT keywordId FROM keyword2id WHERE keyword=?",(hello[0],)).fetchone()[0]
    
    # value:str = rf.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId=?",(keywordid,)).fetchone()[0]
    # docs = value.split(" ")
    # print("len: ",len(docs))
    # for record in docs:
    #     doc = record.split(":")[0]
    #     words:str = rf.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='{doc}'").fetchone()[0]
    #     print(len(words.split(" ")))

    # for doc in docs:

    # pageSize = rf.cur.execute("SELECT size FROM PageSize WHERE urlId='311859208299266180936519881020383222841'").fetchone()[0]
    # print(value)