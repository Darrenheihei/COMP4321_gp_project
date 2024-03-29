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
    private static RecordManager recman;
    private static HTree invertedIndex; //HTree map urlId with the number of keywords to keywordId, key: keywordId, value: <urlId1>:<freq> <urlId2>:<freq> ...
    private static HTree k2i;

    /**
     * The constructor of InvertedIndex
     */
    public BodyInvertedIndex() {
        updateHtrees();
    }

    private void updateHtrees(){
        try{
            recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid_urlId2KeywordId = recman.getNamedObject("bodyInvertedIndex");
            if(recid_urlId2KeywordId != 0) {
                invertedIndex = HTree.load(recman,recid_urlId2KeywordId);
            } else {
                invertedIndex = HTree.createInstance(recman);
                recman.setNamedObject("bodyInvertedIndex",invertedIndex.getRecid());
            }

            //handle keyword to id
            long recid_key2id = recman.getNamedObject("keywordToId");
            if(recid_key2id != 0) {
                k2i = HTree.load(recman,recid_key2id);
            } else {
                k2i = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId",k2i.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This function extract BODY keyword from a webpage and insert all keywords to inverted table
     */
    public void addEntry(String urlId, Vector<String> keywords) throws IOException { //include add new word AND update existed word from a document
        HashMap<String, Integer> hashMap = new HashMap<>();
        for(String str: keywords) {
            if(hashMap.get(str) == null) {
                hashMap.put(str, 1);
            } else {
                int freq = hashMap.get(str);
                hashMap.put(str, freq + 1);
            }
        }

        updateHtrees();

//        FastIterator it = k2i.keys();
//        String a;
//        while( (a = (String)it.next())!=null) {
//            System.out.println("Found: " + a + " " + k2i.get(a));
//        }

        for(String keyword: hashMap.keySet()) {
//            System.out.println("word: " + keyword);
            Object keywordId = k2i.get(keyword);
//            System.out.println("ID: " + k2i.get(keyword));

            String longStr = "";
            if(invertedIndex.get(keywordId)!=null) { // already have such keyword
                longStr = invertedIndex.get(keywordId).toString();
            }

            invertedIndex.put(keywordId, longStr + urlId + ":" + hashMap.get(keyword) + " ");
        }
        recman.commit();
    }

    public static void main(String[] args)
    {
        try {
            BodyInvertedIndex II = new BodyInvertedIndex();
            Vector<String> keywords = new Vector<>();
            keywords.add("hello");
            keywords.add("world");
            II.addEntry("testId", keywords);

            FastIterator it = II.invertedIndex.keys();
            String keywordId = "";
            while((keywordId=(String)it.next())!=null) {
                System.out.println(keywordId +" -> "+ II.invertedIndex.get(keywordId).toString());
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
