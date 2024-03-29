package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Vector;

public class Testing2 {
    public static void main(String[] args) throws IOException {
        Testing3 testing3 = new Testing3();

        RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
        HTree k2i;
        // get record id of the object named "urlToId"
        long recid = recman.getNamedObject("keywordToId");
        if (recid != 0){
            k2i = HTree.load(recman, recid);
        } else {
            k2i = HTree.createInstance(recman);
            recman.setNamedObject( "keywordToId", k2i.getRecid() );
        }

        Testing testing = new Testing();
        Vector<String> keys = new Vector<>();
        keys.add("hello");
        keys.add("world");
        testing.addKeys(keys);

        System.out.println("\nTesting2");
        FastIterator it = k2i.keys();
        String key;
        while( (key = (String)it.next())!=null) {
            System.out.println(key + " : " + k2i.get(key));
        }
        System.out.println("Done");

    }
}
