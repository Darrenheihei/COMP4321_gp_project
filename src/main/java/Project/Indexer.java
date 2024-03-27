package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;

public class Indexer {
    private PageSize ps;
    private UrlId2Title urlid2title;
    private Keyword2Id k2i;
    private ForwardIndex forwardIndex;
    private  TitleInvertedIndex titleInvertedIndex;
    private  BodyInvertedIndex bodyInvertedIndex;



    public Indexer() throws IOException
    {
        ps = new PageSize();
        urlid2title = new UrlId2Title();
        k2i = new Keyword2Id();
        forwardIndex = new ForwardIndex();
//        titleInvertedIndex = new TitleInvertedIndex();
//        bodyInvertedIndex = new BodyInvertedIndex();

    }
    public void indexing(String url,String urlId) throws IOException, ParserException
    {
        ps.updatePageSize(urlId,url);
        urlid2title.addUrl(url,urlId);
        k2i.addKeywordFromUrl(url);
        forwardIndex.addUrlId(urlId);
//        titleInvertedIndex.update(urlId,url);
//        bodyInvertedIndex.update(urlId,url);
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

            convtable_urlToId.put("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm","testid");
            convtable_idToUrl.put("testid","https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            recman.commit();
            Indexer indexer = new Indexer();
            indexer.indexing("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm","testid");

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
            HTree convtable_pageSize;
            long recid_pageSize = recman.getNamedObject("pageSizeIndex");
            if(recid_pageSize != 0)
            {
                convtable_pageSize = HTree.load(recman,recid_pageSize);
            }
            else
            {
                convtable_pageSize = HTree.createInstance(recman);
                recman.setNamedObject("pageSizeIndex",convtable_pageSize.getRecid());
            }
            FastIterator it = convtable_pageSize.keys();
            String size = "";
            while((size=(String)it.next())!=null)
            {
                System.out.println(size +" "+ convtable_pageSize.get(size).toString());
            }

            // test title
            HTree convtable_title;
            long recid_title = recman.getNamedObject("urlIdToTitle");
            if(recid_title != 0)
            {
                convtable_title = HTree.load(recman,recid_title);
            }
            else
            {
                convtable_title = HTree.createInstance(recman);
                recman.setNamedObject("urlIdToTitle",convtable_title.getRecid());
            }
            FastIterator it_title = convtable_title.keys();
            String size_title = "";
            while((size_title=(String)it_title.next())!=null)
            {
                System.out.println(size_title +" "+ convtable_title.get(size_title).toString());
            }


            HTree convtable_keywordToId;
            HTree convtable_idToKeyword;
            long recid_keywordToId = recman.getNamedObject("keywordToId");
            if(recid_keywordToId != 0)
            {
                convtable_keywordToId = HTree.load(recman,recid_keywordToId);
            }
            else
            {
                convtable_keywordToId = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
            }
            long recid_idToKeyword = recman.getNamedObject("idToKeyword");
            if(recid_idToKeyword != 0)
            {
                convtable_idToKeyword = HTree.load(recman,recid_idToKeyword);
            }
            else
            {
                convtable_idToKeyword = HTree.createInstance(recman);
                recman.setNamedObject("idToKeyword",convtable_idToKeyword.getRecid());
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
            HTree convtable_urlIdToKeywordId;
            long recid_forwardIndex = recman.getNamedObject("forwardIndex");
            if(recid_forwardIndex != 0)
            {
                convtable_urlIdToKeywordId = HTree.load(recman,recid_forwardIndex);
            }
            else
            {
                convtable_urlIdToKeywordId = HTree.createInstance(recman);
                recman.setNamedObject("forwardIndex",convtable_urlIdToKeywordId.getRecid());
            }
            FastIterator it5 = convtable_urlIdToKeywordId.keys();
            String key5;
            while((key5 = (String)it5.next())!=null)
            {
                System.out.println(key5 + " = " + convtable_urlIdToKeywordId.get(key5));
            }

//             testing title inverted index
//            HTree convtable_keywordIdToUrlIdTitle;
//            long recid_titleInvertedIndex = recman.getNamedObject("titleInvertedIndex");
//            if(recid_titleInvertedIndex != 0)
//            {
//                convtable_keywordIdToUrlIdTitle = HTree.load(recman,recid_titleInvertedIndex);
//            }
//            else
//            {
//                convtable_keywordIdToUrlIdTitle = HTree.createInstance(recman);
//                recman.setNamedObject("titleInvertedIndex",convtable_keywordIdToUrlIdTitle.getRecid());
//            }
//            FastIterator it6 = convtable_keywordIdToUrlIdTitle.keys();
//            String key6;
//            while((key6 = (String)it6.next())!=null)
//            {
//                System.out.println(key6 + " = " + convtable_keywordIdToUrlIdTitle.get(key6));
//            }




//            recman.commit();
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
