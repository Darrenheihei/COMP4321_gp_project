import sqlite3
import numpy as np
from StopStem import StopStem
from ContentExtractor import ContentExtractor


class resultItem:
    def __init__(self, score, title, url, date, size, keywords, parentLinks, childLinks, top5Words):
        self.score = score
        self.title = title
        self.url = url
        self.date = date
        self.size = size
        self.keywords = '; '.join(f"{key} {value}" for key, value in keywords.items())
        self.top5Words = top5Words
        if parentLinks != []:
            self.parentLinks = parentLinks
        else:
            self.parentLinks = ["This page has no parent links"]

        if childLinks != []:
            self.childLinks = childLinks
        else:
            self.childLinks = ["This page has no child links"]




class retrieval_function:
    def __init__(self):
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()
        documents = self.cur.execute("SELECT * FROM CrawledPage").fetchall()
        self.ss = StopStem()
        self.ce = ContentExtractor()
        self.N:int = len(documents)  # number of documents
        self.keyword2id = {}
        self.id2keyword = {}
        self.ForwardIndex = {}
        self.TitleInvertedIndex = {}
        self.BodyInvertedIndex = {}
        self.child_pages: dict = {}
        self.parent_pages: dict = {}
        self.Title: dict = {}
        self.id2url: dict = {}
        self.ModDate = {}
        self.PageSize = {}
        self.prepare()

    def prepare(self):
        result = self.cur.execute(f"SELECT * FROM keyword2id").fetchall()
        self.keyword2id = {i[0]: i[1] for i in result}
        result = self.cur.execute(f"SELECT * FROM id2keyword").fetchall()
        self.id2keyword = {i[0]: i[1] for i in result}
        result = self.cur.execute(f"SELECT * FROM ForwardIndex").fetchall()
        self.ForwardIndex = {i[0]: i[1].split(' ') for i in result}
        result = self.cur.execute(f"SELECT * FROM TitleInvertedIndex").fetchall()
        self.TitleInvertedIndex = {keywordId:
                                        {i.split(':')[0]: {'freq': int(i.split(':')[1]), 'pos': list(map(int, i.split(':')[2].split(',')))} for i in value.split(' ')}
                                    for keywordId, value in result}
        result = self.cur.execute(f"SELECT * FROM BodyInvertedIndex").fetchall()
        self.BodyInvertedIndex = {keywordId:
                                        {i.split(':')[0]: {'freq': int(i.split(':')[1]), 'pos': list(map(int, i.split(':')[2].split(',')))} for i in value.split(' ')}
                                    for keywordId, value in result}
        result = self.cur.execute(f"SELECT * FROM Title").fetchall()
        self.Title = {i[0]: i[1] for i in result}
        result = self.cur.execute(f"SELECT * FROM ParentUrl").fetchall()
        self.parent_pages = {i[0]: i[1].split(' ') for i in result}
        result = self.cur.execute(f"SELECT * FROM ChildUrl").fetchall()
        self.child_pages = {i[0]: i[1].split(' ') for i in result}
        result = self.cur.execute(f"SELECT * FROM id2url").fetchall()
        self.id2url = {i[0]: i[1] for i in result}
        result = self.cur.execute(f"SELECT * FROM ModDate").fetchall()
        self.ModDate = {i[0]: i[1] for i in result}
        result = self.cur.execute(f"SELECT * FROM PageSize").fetchall()
        self.PageSize = {i[0]: i[1] for i in result}


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
            if t in self.keyword2id.keys():
                terms.append(t)
        return terms

    def get_relevant_urlid(self,terms:list[str]) -> list:
        terms_copy = terms.copy()
        urlids:set = set()
        for term in terms_copy:
            if " " in term:
                split_term = term.split(" ")
                terms_copy.extend(split_term)

        for term in terms_copy:
            if term in self.keyword2id.keys():
                keywordId:str = self.keyword2id[term]
            else:
                continue
            if keywordId in self.TitleInvertedIndex.keys():
                docs = self.TitleInvertedIndex[keywordId].keys()
                for doc in docs:
                    urlids.add(doc)

            if keywordId in self.BodyInvertedIndex.keys():
                docs = self.BodyInvertedIndex[keywordId].keys()
                for doc in docs:
                    urlids.add(doc)
        return list(urlids)

    def calculate_phrase_df(self, phrase:list[str],title:bool=False) -> int:

        df:int = 0
        table = {}
        for word in phrase:
            inner_table = {}
            wordId:str = self.keyword2id[word]
            if title:
                if wordId not in self.TitleInvertedIndex.keys():
                    # print("not found word: ",word)
                    return 0
                doc_fre_pos:dict = self.TitleInvertedIndex[wordId]
            else:
                if wordId not in self.BodyInvertedIndex.keys():
                    return 0
                doc_fre_pos:dict = self.BodyInvertedIndex[wordId]
            for doc in doc_fre_pos.keys():
                inner_table[doc] = doc_fre_pos[doc]['pos']
            table[word] = inner_table
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
                    if (pos+i) not in inner_table[doc]:
                        flag = False
                        break
                if flag == True:
                    df += 1
                    break
        return df


    def checkAllWordsHaveId(self,phrase:list[str]) -> bool:
        for word in phrase:
            if word not in self.keyword2id:
                return False
        return True


    def term_fre_doc(self,urlid:str,keywords:list[str],title:bool = False) -> dict:
        term_fre = {}
        for keyword in keywords:
            if " " in keyword: # phrase
                fre:int = 0
                phrase:list[str] = keyword.split(" ")
                table = {}
                flag:bool = True
                for word in phrase:
                    inner_table = {}
                    wordId:str = self.keyword2id[word]
                    if title:
                        if wordId not in self.TitleInvertedIndex.keys():
                            flag = False
                            continue
                        doc_fre_pos:dict = self.TitleInvertedIndex[wordId]
                    else:
                        if wordId not in self.BodyInvertedIndex.keys():
                            flag = False
                            continue
                        doc_fre_pos:dict = self.BodyInvertedIndex[wordId]

                    if urlid not in doc_fre_pos.keys():
                        flag = False
                        break

                    table[word] = doc_fre_pos[urlid]['pos']

                if flag == False:
                    continue

                pos_of_first_word:list[int] = table[phrase[0]]
                for pos in pos_of_first_word:
                    for i in range(1,len(phrase)):
                        pos_ls:list[int] = table[phrase[i]]
                        if (pos+i) not in pos_ls:
                            break
                        if i == (len(phrase) - 1 ):
                            fre += 1

                term_fre[keyword] = fre

            else: # single word
                if keyword not in self.keyword2id.keys():
                    continue
                keywordId:str = self.keyword2id[keyword]

                if title:
                    if keywordId not in self.TitleInvertedIndex.keys():
                        continue
                    doc_fre_pos:dict = self.TitleInvertedIndex[keywordId]

                else:
                    if keywordId not in self.BodyInvertedIndex.keys():
                        continue
                    doc_fre_pos:dict = self.BodyInvertedIndex[keywordId]


                if urlid not in doc_fre_pos.keys():
                    continue
                term_fre[keyword] = doc_fre_pos[urlid]["freq"]
        return term_fre


    def get_doc_vector(self,urlid:str) -> dict:
        doc_vector = {}
        if urlid in self.ForwardIndex.keys():
            keywordIds:list[str] = self.ForwardIndex[urlid]
            for keywordId in keywordIds:
                if keywordId in self.BodyInvertedIndex.keys():
                    doc:dict = self.BodyInvertedIndex[keywordId]
                    if urlid not in doc.keys():
                        continue
                    fre:int = self.BodyInvertedIndex[keywordId][urlid]["freq"]
                    key:str = self.id2keyword[keywordId]
                    doc_vector[key] = fre
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
                if term not in self.keyword2id.keys():
                    continue
                if term not in term_fre:
                    term_fre[term] = 1
                else:
                    term_fre[term] = term_fre[term] + 1


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
                if df == 0:
                    continue
                idf = np.log2(self.N/df)
                weightList[term] = term_fre[term] * idf / tf_max
            else:   #single word

                keywordId = self.keyword2id[term]
                if title:
                    doc_fre_pos:dict = self.TitleInvertedIndex[keywordId]
                else:
                    doc_fre_pos:dict = self.BodyInvertedIndex[keywordId]

                df = len(doc_fre_pos.keys())
                # print(term,"df:",df)
                idf = np.log2(self.N/df)
                # print(term,"idf:",idf)
                # print(term,"N:",self.N)

                weightList[term] = term_fre[term] * idf / tf_max
                # if title:
                #     print('title')
                # else:
                #     print('body')
                # print(term,weightList[term])

        return weightList


    def calculate_cos_similarity(self,query_vector:dict, doc_vector:dict) -> float:

        #calculate the norm of query vector
        square_sum_query:float = 0
        for term in query_vector.keys():
            square_sum_query += query_vector[term]**2
        norm_query = square_sum_query**(0.5)
        # print("query norm: ",norm_query)
        #calculate the norm of doc vector
        square_sum_doc: float = 0
        for term in doc_vector.keys():
            square_sum_doc += doc_vector[term]**2
        norm_doc = square_sum_doc**(0.5)
        # print("doc_norm: ",norm_doc)
        # calculate dot product
        dot_product:float = 0
        for term in query_vector.keys():
            if term in doc_vector.keys():
                # print("query_vector[term]: ",term,query_vector[term])
                # print("doc_vector[term]: ",term, doc_vector[term])
                dot_product += query_vector[term]*doc_vector[term]

        # print("dot product: ",dot_product)
        # print("norm_query*norm_doc: ",norm_query*norm_doc)

        # print(doc_vector)

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
        # print("doc_vec: ",doc_vec)
        # calculate weighted vector
        weighted_body_vector:dict = self.calculate_doc_weights(urlid,doc_vec,False)
        body_sim:float = self.calculate_cos_similarity(query_vector,weighted_body_vector)


        #calculate title similarity
        title_vector:dict = self.term_fre_doc(urlid,query,True)
        # print("title vector: ",title_vector)
        # calculate weighted vector
        weighted_title_vector: dict = self.calculate_doc_weights(urlid,title_vector,True)
        title_sim:float = self.calculate_cos_similarity(query_vector,weighted_title_vector)
        # print("title: ",title_sim)
        # print("body: ",body_sim)
        # print("score: ",3*title_sim+body_sim)
        return (3*title_sim+body_sim)

    def get_result(self,urlid:str, query:list[str])->resultItem:
        score:float = self.get_score(urlid,query)
        title:str = self.Title[urlid]
        url:str = self.id2url[urlid]
        modDate = self.ModDate[urlid]
        pageSize = self.PageSize[urlid]
        keyword_fre = {}
        top5:list[str] = self.getTop5FreqWords(urlid)
        for word in top5:
            wordid = self.keyword2id[word]
            if wordid in self.TitleInvertedIndex.keys():

                if urlid in self.TitleInvertedIndex[wordid]:
                    keyword_fre[word] = self.TitleInvertedIndex[wordid][urlid]['freq']

            if wordid in self.BodyInvertedIndex.keys():

                if urlid in self.BodyInvertedIndex[wordid].keys():
                    if word in keyword_fre.keys():
                        keyword_fre[word] = keyword_fre[word] + self.BodyInvertedIndex[wordid][urlid]['freq']
                    else:
                        keyword_fre[word] = self.BodyInvertedIndex[wordid][urlid]['freq']


        if urlid in self.parent_pages.keys():
            parentLinks = []
            parents_links_ids = self.parent_pages[urlid]
            for id in parents_links_ids:
                parentLinks.append(self.id2url[id])
        else:
            parentLinks = ["This page has no parent links"]
        if urlid in self.child_pages.keys():
            childLinks= []
            child_links_ids = self.child_pages[urlid][:10]
            for id in child_links_ids:
                childLinks.append(self.id2url[id])
        else:
            childLinks = ["This page has no child links"]

        top5words = self.getTop5FreqWords(urlid)
        top5words = " ".join(top5words)

        return resultItem(score=score,title=title,url=url,date=modDate,size=pageSize,keywords=keyword_fre,parentLinks=parentLinks,childLinks=childLinks,top5Words=top5words)


    def getTop5FreqWords(self, urlId: str ) -> list[str]:
        if urlId not in self.ForwardIndex.keys():
            return None
        wordids = self.ForwardIndex[urlId]
        keyword_fre = {}
        for id in wordids:
            if id in self.BodyInvertedIndex.keys():
                if urlId in self.BodyInvertedIndex[id].keys():
                    keyword_fre[self.id2keyword[id]] = self.BodyInvertedIndex[id][urlId]['freq']
            if id in self.TitleInvertedIndex.keys():
                if urlId in self.TitleInvertedIndex[id]:
                    if self.id2keyword[id] in keyword_fre:
                        keyword_fre[self.id2keyword[id]] = keyword_fre[self.id2keyword[id]] + self.TitleInvertedIndex[id][urlId]['freq']
                    else:
                        keyword_fre[self.id2keyword[id]] = self.TitleInvertedIndex[id][urlId]['freq']

        keywords = list(keyword_fre.keys())
        keywords.sort(key= lambda x : keyword_fre[x],reverse=True)
        return keywords[:5]


    def get_AllResult(self,prompt:str,extraword:list[str] = []) :
        results = []
        query:list[str] = self.splitPrompt(prompt)
        query.extend(extraword)
        urlids:list[str] = self.get_relevant_urlid(query)
        for urlid in urlids:
            x:resultItem = self.get_result(urlid,query)
            if x.score > 0:
                results.append(x)
        results.sort(key = lambda x: x.score,reverse=True)
        return results[:50]




if __name__ == '__main__':

    rf = retrieval_function()
    # item = rf.get_result()

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
    # term_fre1 = rf.term_fre_doc("41096512194994237309005844220821308201",["test page","test","page"])
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

    # # test get_score()
    # score = rf.get_score("41096512194994237309005844220821308201",["test page","test","page"])
    # print(score)

    # terms = rf.splitPrompt('"hello world" super')
    # print(terms)

    # test term_fre_doc()
    # term_fre1 = rf.term_fre_doc("41096512194994237309005844220821308201",split)
    # print(term_fre1)

    # test get_result()
    # print(rf.Title.keys())

    # test getTop5FreqWords()
    # top5 = rf.getTop5FreqWords('247040276485521844002811826849338028230')
    # print(top5)

    # test
    # similar_page = rf.getSimilarPages("247040276485521844002811826849338028230")
    # for page in similar_page:
    #     print(page.score)
    # print(rf.getTop5FreqWords('120882259239556584285953046974544627605'))
    # results:list[resultItems] = rf.get_AllResult("hello")
    # for result in results:
    #     print(result.keywords)
    # r = rf.get_result('120882259239556584285953046974544627605',['disney', 'imdb', 'user', 'movi', 'rate'])
    # print(r.keywords)




    # from time import time
    # start = time()
    # results:list[resultItems] = rf.get_AllResult('hello world movie "test page"')
    # # results:list[resultItems] = rf.get_AllResult('')
    # end = time()
    # print("time: ",end-start)
    # print(len(results))
    # i = 1
    # for result in results:
    #     # print(i)
    #     i += 1
    #     print(result.score)
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
    # rf.prepare()
    # dic:dict = rf.TitleInvertedIndex["6668672268896612241852548415349727365"]
    # print(dic['156034502776903695555964597907068709917']['pos'])

    # for docid in rf.ForwardIndex.keys():
    #     print(docid)
    # print(len(rf.ForwardIndex.keys()))
    # similar = rf.getSimilarPages("hello world",'120882259239556584285953046974544627605')
    # scores = [x.score for x in similar]
    # print(scores)

    # results = rf.splitPrompt('"powerpuff girls movie"')
    # # print(results)
    # urlids = rf.get_relevant_urlid(results)
    # # print(urlids)
    # term_inPhrase = results[0].split(" ")
    # print(term_inPhrase)
    # df = rf.calculate_phrase_df(term_inPhrase,title=False)
    # print(df)
    # term_fre_Q = rf.term_fre_query(results)
    # print(term_fre_Q)
    # # term_fre_D = rf.term_fre_doc('307107437884666448576352896230635319098',results,True)
    # # print(term_fre_D)

    # re:list[resultItem] = rf.get_AllResult('"powerpuff girls movie"')
    # for i in re:
    #     print(i.)

    # uid = rf.cur.execute(f"SELECT urlId FROM url2id WHERE url='https://www.cse.ust.hk/~kwtleung/COMP4321/Movie/84.html'").fetchone()[0]
    # print(uid)
    # r = rf.getSimilarPages('"powerpuff girls movie"',"273581480638588959161243458125022993277")
    # for i in r:
    #     print(i.score)

    # st = rf.getTop5FreqWords("273581480638588959161243458125022993277")
    # long_str = " ".join(st)
    # print(long_str)
    # id = rf.cur.execute(f"SELECT urlId FROM url2id WHERE url='https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm'").fetchone()[0]
    # rf.get_result(id,["test page"])
    # rf.get_result(id,["test","page"])
    # rf.get_result(id,["test"])

    # print(rf.N)
    # rf.get_AllResult()
    # print(len(rf.cur.execute("SELECT * FROM CrawledPage").fetchall()))
    # print(len(rf.cur.execute("SELECT * FROM ForwardIndex").fetchall()))


    # cp_list = []
    # fi_list = []
    # cp = rf.cur.execute("SELECT * FROM CrawledPage").fetchall()
    # print("CrawledPage")
    # for url,id in cp:
    #     cp_list.append(url)
    #     # print(url)
    # cp_list.sort()
    # for i in cp_list:
    #     print(i)
    # print("ForwardIndex")
    # fi = rf.cur.execute("SELECT * FROM ForwardIndex").fetchall()
    # for urlid,words in fi:
    #     url = rf.cur.execute(f'SELECT url FROM id2url WHERE urlId="{urlid}"').fetchone()[0]
    #     fi_list.append(url)
    #     # print(url)
    # fi_list.sort()
    # for i in fi_list:
    #     print(i)
    # con = sqlite3.connect('project.db')
    # cur = con.cursor()
    # print(len(cur.execute("SELECT * From ForwardIndex").fetchall()))
