package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

public class ForwardIndex {
    public RecordManager recman;
    public HTree forwardIndex;
    public HTree convtable_keywordToId; // conversion table: Keyword to ID

    public ForwardIndex() throws IOException {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("forwardIndex");
        if(recid_urlId2KeywordId != 0) {
            forwardIndex = HTree.load(recman,recid_urlId2KeywordId);
        } else {
            forwardIndex = HTree.createInstance(recman);
            recman.setNamedObject("forwardIndex",forwardIndex.getRecid());
        }

        // keyword to id
        long recid_key2id = recman.getNamedObject("keywordToId");
        if(recid_key2id != 0) {
            convtable_keywordToId = HTree.load(recman,recid_key2id);
        } else {
            convtable_keywordToId = HTree.createInstance(recman);
            recman.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
        }
    }

    public void addEntry(String urlId, Vector<String> keywords) throws IOException {
        HashSet<String> hs = new HashSet<>();
        for(String keyword:keywords) {
            hs.add(keyword);
        }

        String IDs = "";
        for(String keyword:hs) {
            IDs += convtable_keywordToId.get(keyword) + " ";
        }

        forwardIndex.put(urlId, IDs);
        recman.commit();
    }

    public static void main(String[] args) {
        try {
            ForwardIndex fi = new ForwardIndex();
            Vector<String> keywords = new Vector<>();
            keywords.add("hello");
            keywords.add("world");
            fi.addEntry("testId", keywords);
            System.out.println(fi.forwardIndex.get("testId"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
