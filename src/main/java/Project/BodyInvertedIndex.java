package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
public class BodyInvertedIndex {
    private RecordManager recman;
    private HTree convtable_keywordIdToUrlId; //HTree map urlId with the number of keywords to keywordId
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

        HashMap<String, Integer> hashMap = new HashMap<>();
        for(String str: v)
        {
            if(hashMap.get(str) == null)
            {
                hashMap.put(str,1);
            }
            else
            {
                int fre = hashMap.get(str);
                hashMap.put(str, fre+1);
            }
        }

        for(String keyword: hashMap.keySet())
        {
            String keywordId = this.k2i.getId(keyword);
            if(this.convtable_keywordIdToUrlId.get(keywordId)!=null)
            {
                String longStr = this.convtable_keywordIdToUrlId.get(keywordId).toString();
                if(longStr.contains(urlId))
                {
                    String[] strArray = longStr.split(" ");
                    String newStr = "";
                    for(int i=0; i < strArray.length;i+=2)
                    {
                        if(strArray[i].equals(urlId))
                        {
                            newStr = newStr+urlId+" "+hashMap.get(keyword)+" ";
                        }
                        else
                        {
                            newStr = newStr+strArray[i]+" "+strArray[i+1]+" ";
                        }
                    }
                    this.convtable_keywordIdToUrlId.put(keywordId,newStr);
                }
                else
                {

                    this.convtable_keywordIdToUrlId.put(keywordId,longStr+urlId+" "+hashMap.get(keyword).toString()+" ");
                }

            }
            else
            {

                this.convtable_keywordIdToUrlId.put(keywordId,urlId+" "+hashMap.get(keyword).toString()+" ");
            }
        }
        recman.commit();
    }

    /**
     * This function remove all the BODY keywords record of the url
     * @param urlId the id that correspond to the webpage that you want to remove from the index
     * @param keywordsId the whole string containing all stemmed keywords. e.g "compu hello new", suppose can be gotten from ForwardIndex
     */
    public void delete(String urlId, String keywordsId) throws IOException{
        //String keywords = forward_index.getConvtableUrlIdToKeywordId().get(urlId);
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
        //forward_index.deleteUrlId(urlId);
        //forward_index.getConvtableIdToUrl().remove("testing urlid"); //should be from darren

        recman.commit();
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
            II.update("123", "https://www.cse.ust.hk/~kwtleung/COMP4321/books/book2.htm");
//            II.close();
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

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
