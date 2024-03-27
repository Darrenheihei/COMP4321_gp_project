package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.util.Vector;
public class BodyInvertedIndex {
    private RecordManager recman;
    private HTree convtable_keywordIdToUrlId; //HTree map urlId with the number of keywords to keywordId
    private HTree bodyIndex_UrlIdRecord;
    private Keyword2Id k2i;

    /**
     * The constructor of InvertedIndex
     */
    public BodyInvertedIndex() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("bodyInvertedIndex");
        if(recid_urlId2KeywordId != 0)
        {
            convtable_keywordIdToUrlId = HTree.load(recman,recid_urlId2KeywordId);
        }
        else
        {
            convtable_keywordIdToUrlId = HTree.createInstance(recman);
            recman.setNamedObject("bodyInvertedIndex",convtable_keywordIdToUrlId.getRecid());
        }

        long recid_UrlIdRecord = recman.getNamedObject("titleIndex_UrlIdRecord");
        if(recid_UrlIdRecord != 0)
        {
            bodyIndex_UrlIdRecord = HTree.load(recman,recid_UrlIdRecord);
        }
        else
        {
            bodyIndex_UrlIdRecord = HTree.createInstance(recman);
            recman.setNamedObject("titleIndex_UrlIdRecord",convtable_keywordIdToUrlId.getRecid());
        }

        k2i = new Keyword2Id();
    }

    /**
     * This function extract BODY keyword from a webpage and insert all keywords to inverted table
     * @param urlId the id that correspond to the webpage
     * @param url the actual url coresspond to urlId
     */
    public void update(String urlId, String url) throws IOException, ParserException { //include add new word AND update existed word from a document
        //updateConvtableIdToUrl(urlId, url);
        StringExtractor se = new StringExtractor(url);
        Vector<String> v;
        v = se.getBodyTextArray();
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        //System.out.println("testing1");
        if(bodyIndex_UrlIdRecord.get(urlId) != null){
            //System.out.println("testing2");
            return;
        }
        //System.out.println("testing3");
        bodyIndex_UrlIdRecord.put(urlId,"1");
        recman.commit();

        for(String str: v)
        {
            //String id = forward_index.getK2i().getId(str); //covert keyword to id, not used
            String keyword_id = k2i.getId(str);

            //System.out.println("this string is " + str);
            if(convtable_keywordIdToUrlId.get(keyword_id) != null){ //the keyword exist in the inverted file already
                String url_list = convtable_keywordIdToUrlId.get(keyword_id).toString();
                String[] url_array = url_list.split(" ");
                String new_keyword_list = "";

                boolean urlInList = false; //it is possible that the url id need to be appended in the end

                for (int i = 0; i < url_array.length; i += 2) { //loop over the url list to see whether the url exist,
                    if (url_array[i].equals(urlId)) { //If existed, add 1
                        new_keyword_list += url_array[i] + " " + (Integer.parseInt(url_array[i+1]) + 1) + " ";
                        //System.out.println("testing");
                        urlInList = true;
                    }
                    else{
                        new_keyword_list += url_array[i] + " " + url_array[i+1] + " ";
                    }
                }

                if(!urlInList){ //appned url id in the end with the number of keyword
                    new_keyword_list += keyword_id + " " + "1";
                }
                new_keyword_list = new_keyword_list.trim(); //remove whitespace in the end of string
                convtable_keywordIdToUrlId.put(keyword_id, new_keyword_list);
            }
            else{//the keyword does not exist in the inverted file
                String new_keyword_list = urlId + " " + "1";
                convtable_keywordIdToUrlId.put(keyword_id, new_keyword_list);
            }

            //System.out.println(str + " : " + convtable_keywordIdToUrlId.get(keyword_id));


            recman.commit();
        }
    }

    /**
     * This function remove all the BODY keywords record of the url
     * @param urlId the id that correspond to the webpage that you want to remove from the index
     * @param keywordsId the whole string containing all stemmed keywords. e.g "compu hello new", suppose can be gotten from ForwardIndex
     */
    public void delete(String urlId, String keywordsId) throws IOException{
        //String keywords = forward_index.getConvtableUrlIdToKeywordId().get(urlId);
        if(bodyIndex_UrlIdRecord.get(urlId) == null) return;
        if(keywordsId != null){ //all keywords from the webpage
            String[] keywordIdArray = keywordsId.split(" ");

            for(String keyword_id : keywordIdArray){
                //String keyword_id = k2i.getId(keyword);
                String url_list = convtable_keywordIdToUrlId.get(keyword_id).toString();//get all the urlId corresponding to the keyword
                if(url_list != null){
                    String[] url_array = url_list.split(" ");
                    String new_url_list = "";
                    for(int i = 0;i < url_array.length; i += 2){
                        if(!url_array[i].equals(urlId)){ //skip the url id that want to be removed
                            new_url_list += url_array[i] + " " + url_array[i+1] + " ";
                        }
                    }

                    new_url_list = new_url_list.trim();
                    if(new_url_list.isEmpty()){
                        convtable_keywordIdToUrlId.remove(keyword_id);
                    }
                    else {
                        convtable_keywordIdToUrlId.put(keyword_id, new_url_list);
                    }
                }
            }
        }
        bodyIndex_UrlIdRecord.remove(urlId);
        //forward_index.deleteUrlId(urlId);
        //forward_index.getConvtableIdToUrl().remove("testing urlid"); //should be from darren

        recman.commit();
    }

    public void printAll() throws IOException
    {
        // Print all the data in the hashtable
        FastIterator it = convtable_keywordIdToUrlId.keys();
        String key;
        while((key = (String)it.next())!=null)
        {
            System.out.println(key  + " " + convtable_keywordIdToUrlId.get(key));
        }

    }

    public void close() {
        try {
            if (recman != null) {
                recman.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            BodyInvertedIndex II = new BodyInvertedIndex();
            II.update("123", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            II.printAll();
            //II.close();

//            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
//
//            HTree convtable_keywordIdToUrlIdTitle;
//            long recid_titleInvertedIndex = recman.getNamedObject("titleInvertedIndex");
//            if(recid_titleInvertedIndex != 0)
//            {
//                System.out.println("1");
//                convtable_keywordIdToUrlIdTitle = HTree.load(recman,recid_titleInvertedIndex);
//            }
//            else
//            {
//                System.out.println("2");
//                convtable_keywordIdToUrlIdTitle = HTree.createInstance(recman);
//                recman.setNamedObject("titleInvertedIndex",convtable_keywordIdToUrlIdTitle.getRecid());
//            }
//            FastIterator it6 = convtable_keywordIdToUrlIdTitle.keys();
//            String key6;
//            while((key6 = (String)it6.next())!=null)
//            {
//                System.out.println(key6 + " = " + convtable_keywordIdToUrlIdTitle.get(key6));
//            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
