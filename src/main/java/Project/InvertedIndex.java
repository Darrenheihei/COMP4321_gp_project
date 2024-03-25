package Project;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Vector;
public class InvertedIndex {
    private RecordManager recman;
    private HTree convtable_keywordIdToUrlId; //HTree map urlId with the number of keywords to keywordId
    private HTree convtable_idToUrl; //convert urlId to url //not used

    //HTree for topic

    //HTree for size of page
    private ForwardIndex forward_index;


    public InvertedIndex() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("invertedIndex");
        if(recid_urlId2KeywordId != 0)
        {
            convtable_keywordIdToUrlId = HTree.load(recman,recid_urlId2KeywordId);
        }
        else
        {
            convtable_keywordIdToUrlId = HTree.createInstance(recman);
            recman.setNamedObject("keywordToId",convtable_keywordIdToUrlId.getRecid());
        }

         forward_index = new ForwardIndex();
    }

    /**
     * This function extract keyword from a webpage and insert all keywords to inverted table
     * @param urlId the id that correspond to the webpage
     * @param url the actual url coresspond to urlId
     */
    public void update(String urlId, String url) throws IOException { //include add new word AND update existed word from a document
        //updateConvtableIdToUrl(urlId, url);
        StringExtractor se = new StringExtractor(url);
        Vector<String> v = se.getString(true);
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        for(String str: v)
        {
            String url_list = convtable_keywordIdToUrlId.get(str).toString();
            if(url_list != null){ //the keyword exist in the inverted file already
                String[] url_array = url_list.split(" ");
                String new_keyword_list = "";

                boolean urlInList = false; //it is possible that the url id need to be appended in the end

                for (int i = 0; i < url_array.length; i += 2) { //loop over the url list to see whether the url exist,
                    if (url_array[i].equals(urlId)) { //If existed, add 1
                        new_keyword_list += url_array[i] + " " + (Integer.parseInt(url_array[i+1]) + 1) + " ";
                        urlInList = true;
                    }
                    else{
                        new_keyword_list += url_array[i] + " " + url_array[i+1] + " ";
                    }
                }

                if(urlInList){ //appned url id in the end with the number of keyword
                    new_keyword_list += str + " " + "1";
                }
                new_keyword_list = new_keyword_list.trim(); //remove whitespace in the end of string
                convtable_keywordIdToUrlId.put(str, new_keyword_list);
            }
            else{//the keyword does not exist in the inverted file
                String new_keyword_list = str + " " + "1";
                convtable_keywordIdToUrlId.put(str, new_keyword_list);
            }

            //update ForwardIndex
            //forward_index.getConvtableIdToUrl().put(urlId,url); //should be from darren
            forward_index.addUrlId(urlId);
            //forward_index.deleteUrlId("testing urlid");
            //forward_index.getConvtableIdToUrl().remove("testing urlid");

            recman.commit();
        }
    }

    public void delete(String urlId, String keywords) throws IOException{
        //String keywords = forward_index.getConvtableUrlIdToKeywordId().get(urlId);
        if(keywords != null){ //all keywords from the webpage
            String[] keyword_array = keywords.split(" ");
            for(String keyword : keyword_array){
                String url_list = convtable_keywordIdToUrlId.get(keyword).toString();//get all the urlId corresponding to the keyword
                if(url_list != null){
                    String[] url_array = url_list.split(" ");
                    String new_url_list = "";
                    for(int i = 0;i < url_array.length; i += 2){
                        if(!url_array[i].equals(urlId)){ //skip the url id that want to be removed
                            new_url_list += url_array[i] + " " + url_array[i+1] + " ";
                        }
                    }
                    new_url_list = new_url_list.trim();
                    if(!new_url_list.isEmpty()){
                        convtable_keywordIdToUrlId.put(keyword, new_url_list);
                    }


                }
            }
        }
        forward_index.deleteUrlId(urlId);
        //forward_index.getConvtableIdToUrl().remove("testing urlid"); //should be from darren

        recman.commit();
    }

    public void close() throws IOException {
        recman.close();
    }

}
