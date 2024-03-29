package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

public class ForwardIndex {
    public static RecordManager recman;
    public static HTree forwardIndex;
    public static HTree convtable_keywordToId; // conversion table: Keyword to ID

    public ForwardIndex() {
        updateHtrees();
    }

    private void updateHtrees(){
        try {
            recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid_urlId2KeywordId = recman.getNamedObject("forwardIndex");
            if (recid_urlId2KeywordId != 0) {
                forwardIndex = HTree.load(recman, recid_urlId2KeywordId);
            } else {
                forwardIndex = HTree.createInstance(recman);
                recman.setNamedObject("forwardIndex", forwardIndex.getRecid());
            }

            // keyword to id
            long recid_key2id = recman.getNamedObject("keywordToId");
            if (recid_key2id != 0) {
                convtable_keywordToId = HTree.load(recman, recid_key2id);
            } else {
                convtable_keywordToId = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId", convtable_keywordToId.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addEntry(String urlId, Vector<String> keywords) throws IOException {
        HashSet<String> hs = new HashSet<>();
        for(String keyword:keywords) {
            hs.add(keyword);
        }

        updateHtrees();
        String IDs = "";
        System.out.println("\nForward index:");
//        FastIterator it = convtable_keywordToId.keys();
//        String a;
//        while( (a = (String)it.next())!=null) {
//            System.out.println("Found(after): " + a);
//        }
        System.out.println(hs);
        for(String keyword:hs) {
            System.out.println(keyword + " " + convtable_keywordToId.get(keyword));
            IDs += convtable_keywordToId.get(keyword) + " ";
//            System.out.println("ID: " + IDs);
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
