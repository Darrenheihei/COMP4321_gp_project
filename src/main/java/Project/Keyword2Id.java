package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

public class Keyword2Id {
    private static RecordManager recman;
    private static HTree convtable_keywordToId; // conversion table: Keyword to ID
    private static HTree convtable_idToKeyword; // conversion table: ID to Keyword

    public Keyword2Id() throws IOException {
        upateHtrees();
    }

    private void upateHtrees() {
        try {
            recman = RecordManagerFactory.createRecordManager("projectRM");

            //handle keyword to id
            long recid_key2id = recman.getNamedObject("keywordToId");
            if (recid_key2id != 0) {
                convtable_keywordToId = HTree.load(recman, recid_key2id);
            } else {
                convtable_keywordToId = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId", convtable_keywordToId.getRecid());
            }

            //handle id to keyword
            long recid_id2key = recman.getNamedObject("idToKeyword");
            if (recid_id2key != 0) {
                convtable_idToKeyword = HTree.load(recman, recid_id2key);
            } else {
                convtable_idToKeyword = HTree.createInstance(recman);
                recman.setNamedObject("idToKeyword", convtable_idToKeyword.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addKeywords(Vector<String> keywords) throws IOException {
//        System.out.println("Keyword received: " + keywords);
        System.out.println("Keyword2Id:");
        for (String keyword:keywords) {
            upateHtrees();
//            System.out.println("Current Word: " + keyword);
            if (convtable_keywordToId.get(keyword) == null) {
                String newId = UUID.randomUUID().toString();
                convtable_keywordToId.put(keyword, newId);
                convtable_idToKeyword.put(newId, keyword);

                System.out.println("Keyword added: " + keyword + " <=> " + convtable_keywordToId.get(keyword));
//                FastIterator it = convtable_keywordToId.keys();
//                String a;
//                while( (a = (String)it.next())!=null) {
//                    System.out.println("Found(immediately1): " + a);
//                }
//                System.out.println();

                recman.commit();
            }
        }

//        upateHtrees();
//
//        FastIterator it = convtable_keywordToId.keys();
//        String a;
//        while( (a = (String)it.next())!=null) {
//            System.out.println("Found(immediately2): " + a);
//        }
        System.out.println("thu " + convtable_keywordToId.get("thu"));
        System.out.println("inform " + convtable_keywordToId.get("inform"));
        System.out.println("data " + convtable_keywordToId.get("data"));
        System.out.println();
    }

    static public void main(String[] args)
    {
        try
        {
            Keyword2Id k2i = new Keyword2Id();
            Vector<String> keywords = new Vector<>();
            keywords.add("hello");
            keywords.add("world");
            System.out.println("First call");
            k2i.addKeywords(keywords);

            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_keywordToId;
            HTree convtable_idToKeyword;

            Vector<String> keywords2 = new Vector<>();
            keywords2.add("comp");
            keywords2.add("4211");
            System.out.println("Second call");
            k2i.addKeywords(keywords2);


            long recid_keywordToId = recman.getNamedObject("keywordToId");
            if(recid_keywordToId != 0) {
                convtable_keywordToId = HTree.load(recman,recid_keywordToId);
            } else {
                convtable_keywordToId = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
            }

            long recid_idToKeyword = recman.getNamedObject("idToKeyword");
            if(recid_idToKeyword != 0) {
                convtable_idToKeyword = HTree.load(recman,recid_idToKeyword);
            } else {
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

}