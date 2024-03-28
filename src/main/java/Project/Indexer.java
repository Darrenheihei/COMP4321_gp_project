package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;

public class Indexer {
    private PageSize ps = null;
    private UrlId2Title urlid2title = null;
    private Keyword2Id k2i = null;
    private ForwardIndex forwardIndex = null;
    private  TitleInvertedIndex titleInvertedIndex = null;
    private  BodyInvertedIndex bodyInvertedIndex = null;



    public Indexer() throws IOException
    {


    }
    public void indexing(String url,String urlId) throws IOException, ParserException
    {
//        if(this.k2i == null)
        {
            this.k2i = new Keyword2Id();
        }
        this.k2i.addKeywordFromUrl(url);

//        if(this.ps == null)
        {
            this.ps = new PageSize();
        }
        this.ps.updatePageSize(urlId,url);

//        if(this.urlid2title == null)
        {
            this.urlid2title = new UrlId2Title();
        }
        this.urlid2title.addUrl(url,urlId);


//        if(this.titleInvertedIndex == null)
        {
            this.titleInvertedIndex = new TitleInvertedIndex();
        }
        this.titleInvertedIndex.update(urlId,url);
//        if(this.bodyInvertedIndex == null)
        {
            this.bodyInvertedIndex = new BodyInvertedIndex();
        }
        this.bodyInvertedIndex.update(urlId,url);

//        if(this.forwardIndex == null)
        {
            this.forwardIndex = new ForwardIndex();
        }
        this.forwardIndex.addUrlId(urlId);


    }

    public static void main(String[] args)
    {
        try
        {
            HTree convtable_urlToId;
            HTree convtable_idToUrl;
            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid_urlToId = recman.getNamedObject("urlToId");
            if(recid_urlToId != 0)
            {
                convtable_urlToId = HTree.load(recman,recid_urlToId);
            }
            else
            {
                convtable_urlToId = HTree.createInstance(recman);
                recman.setNamedObject("urlToId",convtable_urlToId.getRecid());
            }
            long recid_idToUrl = recman.getNamedObject("idToUrl");
            if(recid_idToUrl != 0)
            {
                convtable_idToUrl = HTree.load(recman,recid_idToUrl);
            }
            else
            {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject("idToUrl",convtable_idToUrl.getRecid());
            }

            convtable_urlToId.put("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book2.htm","testid");
            convtable_idToUrl.put("testid","https://www.cse.ust.hk/~kwtleung/COMP4321/books/book2.htm");
            convtable_urlToId.put("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book1.htm","testid2");
            convtable_idToUrl.put("testid2","https://www.cse.ust.hk/~kwtleung/COMP4321/books/book1.htm");
//            convtable_urlToId.put("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm","testid3");
//            convtable_idToUrl.put("testid3","https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm");
            recman.commit();


            Indexer indexer = new Indexer();
            indexer.indexing("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book2.htm","testid");
            indexer.indexing("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book1.htm","testid2");
//            indexer.indexing("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm","testid3");


            FastIterator it1 = convtable_urlToId.keys();
            String key1;
            while((key1 = (String)it1.next())!=null)
            {
                System.out.println(key1 + " = " + convtable_urlToId.get(key1));
            }

            FastIterator it2 = convtable_idToUrl.keys();
            String key2;
            while((key2 = (String)it2.next())!=null)
            {
                System.out.println(key2 + " = " + convtable_idToUrl.get(key2));
            }
            //test pageSize
            RecordManager recman2 = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_pageSize;
            long recid_pageSize = recman2.getNamedObject("pageSizeIndex");
            if(recid_pageSize != 0)
            {
                convtable_pageSize = HTree.load(recman2,recid_pageSize);
            }
            else
            {
                convtable_pageSize = HTree.createInstance(recman2);
                recman2.setNamedObject("pageSizeIndex",convtable_pageSize.getRecid());
            }
            FastIterator it = convtable_pageSize.keys();
            String size = "";
            while((size=(String)it.next())!=null)
            {
                System.out.println(size +" "+ convtable_pageSize.get(size).toString());
            }

            // test title
            HTree convtable_title;
            long recid_title = recman2.getNamedObject("urlIdToTitle");
            if(recid_title != 0)
            {
                convtable_title = HTree.load(recman2,recid_title);
            }
            else
            {
                convtable_title = HTree.createInstance(recman2);
                recman2.setNamedObject("urlIdToTitle",convtable_title.getRecid());
            }
            FastIterator it_title = convtable_title.keys();
            String size_title = "";
            while((size_title=(String)it_title.next())!=null)
            {
                System.out.println(size_title +" "+ convtable_title.get(size_title).toString());
            }


            HTree convtable_keywordToId;
            HTree convtable_idToKeyword;
            long recid_keywordToId = recman2.getNamedObject("keywordToId");
            if(recid_keywordToId != 0)
            {
                convtable_keywordToId = HTree.load(recman2,recid_keywordToId);
            }
            else
            {
                convtable_keywordToId = HTree.createInstance(recman2);
                recman2.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
            }
            long recid_idToKeyword = recman2.getNamedObject("idToKeyword");
            if(recid_idToKeyword != 0)
            {
                convtable_idToKeyword = HTree.load(recman2,recid_idToKeyword);
            }
            else
            {
                convtable_idToKeyword = HTree.createInstance(recman2);
                recman2.setNamedObject("idToKeyword",convtable_idToKeyword.getRecid());
            }
            FastIterator it3 = convtable_keywordToId.keys();
            String key3;
            while((key3 = (String)it3.next())!=null)
            {
                System.out.println(key3 + " = " + convtable_keywordToId.get(key3));
            }
            FastIterator it4 = convtable_idToKeyword.keys();
            String key4;
            while((key4 = (String)it4.next())!=null)
            {
                System.out.println(key4 + " = " + convtable_idToKeyword.get(key4));
            }

            //testing forwardIndex
            RecordManager recman3 = RecordManagerFactory.createRecordManager("projectRM");

            HTree convtable_urlIdToKeywordId;
            long recid_forwardIndex = recman3.getNamedObject("forwardIndex");
            if(recid_forwardIndex != 0)
            {
                convtable_urlIdToKeywordId = HTree.load(recman3,recid_forwardIndex);
            }
            else
            {
                convtable_urlIdToKeywordId = HTree.createInstance(recman3);
                recman3.setNamedObject("forwardIndex",convtable_urlIdToKeywordId.getRecid());
            }
            FastIterator it5 = convtable_urlIdToKeywordId.keys();
            String key5;
            while((key5 = (String)it5.next())!=null)
            {
                System.out.println(key5 + " = " + convtable_urlIdToKeywordId.get(key5));
            }

             //testing title inverted index
            System.out.println("title inverted index");
            RecordManager recman4 = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_keywordIdToUrlIdTitle;
            long recid_titleInvertedIndex = recman4.getNamedObject("titleInvertedIndex");
            if(recid_titleInvertedIndex != 0)
            {
                convtable_keywordIdToUrlIdTitle = HTree.load(recman4,recid_titleInvertedIndex);
            }
            else
            {
                convtable_keywordIdToUrlIdTitle = HTree.createInstance(recman4);
                recman4.setNamedObject("titleInvertedIndex",convtable_keywordIdToUrlIdTitle.getRecid());
            }
            FastIterator it6 = convtable_keywordIdToUrlIdTitle.keys();
            String key6;
            while((key6 = (String)it6.next())!=null)
            {
                System.out.println(key6 + " = " + convtable_keywordIdToUrlIdTitle.get(key6));
            }
            System.out.println();
            //testing body inverted index
            System.out.println("body inverted index");
            RecordManager recman5 = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_keywordIdToUrlIdBody;
            long recid_bodyInvertedIndex = recman5.getNamedObject("bodyInvertedIndex");
            if(recid_bodyInvertedIndex != 0)
            {
                convtable_keywordIdToUrlIdBody = HTree.load(recman5,recid_bodyInvertedIndex);
            }
            else
            {
                convtable_keywordIdToUrlIdBody = HTree.createInstance(recman5);
                recman5.setNamedObject("bodyInvertedIndex",convtable_keywordIdToUrlIdBody.getRecid());
            }
            FastIterator it7 = convtable_keywordIdToUrlIdBody.keys();
            String key7;
            while((key7 = (String)it7.next())!=null)
            {
                System.out.println(key7 + " = " + convtable_keywordIdToUrlIdBody.get(key7));
            }




            recman.commit();
//            recman.close();

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ParserException e)
        {
            e.printStackTrace();
        }


    }

}
