package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
public class TitleInvertedIndex {
    private RecordManager recman;
    private HTree invertedIndex; //HTree map urlId with the number of keywords to keywordId, key: keywordId, value: <urlId1>:<freq> <urlId2>:<freq> ...
    private HTree k2i;

    /**
     * The constructor of InvertedIndex
     */
    public TitleInvertedIndex() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("titleInvertedIndex");
        if(recid_urlId2KeywordId != 0) {
            invertedIndex = HTree.load(recman,recid_urlId2KeywordId);
        } else {
            invertedIndex = HTree.createInstance(recman);
            recman.setNamedObject("titleInvertedIndex",invertedIndex.getRecid());
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

    /**
     * This function extract TITLE keyword from a webpage and insert all keywords to inverted table
     */
    public void addEntry(String urlId, Vector<String> title) throws IOException { //include add new word AND update existed word from a document
        HashMap<String, Integer> hashMap = new HashMap<>();
        for(String str: title) {
            if(hashMap.get(str) == null) {
                hashMap.put(str, 1);
            } else {
                int freq = hashMap.get(str);
                hashMap.put(str, freq + 1);
            }
        }

        for(String keyword: hashMap.keySet()) {
            String keywordId = k2i.get(keyword).toString();

            String longStr = "";
            if(invertedIndex.get(keywordId)!=null) { // already have such keyword
                longStr = invertedIndex.get(keywordId).toString();
            }

            invertedIndex.put(keywordId, longStr + urlId + ":" + hashMap.get(keyword) + " ");
        }
        recman.commit();
    }

    public static void main(String[] args) {
        try {
            TitleInvertedIndex II = new TitleInvertedIndex();
            Vector<String> title = new Vector<>();
            title.add("hello");
            title.add("hello");
            II.addEntry("testId", title);

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
