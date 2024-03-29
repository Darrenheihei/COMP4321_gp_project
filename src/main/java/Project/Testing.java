package Project;
import java.io.IOException;
import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.text.ParseException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;

public class Testing {
    private static RecordManager recman;
    private static HTree k2i;
    private int i = 0;
    public Testing() throws IOException {
        recman = RecordManagerFactory.createRecordManager("projectRM");

        // get record id of the object named "k2i"
        long recid = recman.getNamedObject("keywordToId");
        if (recid != 0){
            k2i = HTree.load(recman, recid);
        } else {
            k2i = HTree.createInstance(recman);
            recman.setNamedObject( "keywordToId", k2i.getRecid() );
        }
    }
    public void addKeys(Vector<String> keys) throws IOException {
        for(String key:keys){
            k2i.put(key, i);
            i++;
        }
        recman.commit();

        System.out.println("Testing");
        FastIterator it = k2i.keys();
        String key;
        while( (key = (String)it.next())!=null) {
            System.out.println(key + " : " + k2i.get(key));
        }
//        recman.close();
    }

    public void updateHtrees() throws IOException {
        recman = RecordManagerFactory.createRecordManager("projectRM");

        long recid = recman.getNamedObject("keywordToId");
        if (recid != 0){
            k2i = HTree.load(recman, recid);
        } else {
            k2i = HTree.createInstance(recman);
            recman.setNamedObject( "keywordToId", k2i.getRecid() );
        }
    }

    public static void main(String[] args) throws IOException {
        Testing testing = new Testing();
//        RecordManager recman2 = RecordManagerFactory.createRecordManager("projectRM");

//        HTree k2i;
        // get record id of the object named "urlToId"
        long recid = recman.getNamedObject("keywordToId");
        if (recid != 0){
            k2i = HTree.load(recman, recid);
        } else {
            k2i = HTree.createInstance(recman);
            recman.setNamedObject( "keywordToId", k2i.getRecid() );
        }


        Vector<String> keys = new Vector<>();
        keys.add("hello");
        keys.add("world");
        testing.addKeys(keys);

        testing.updateHtrees();

        Vector<String> keys2 = new Vector<>();
        keys2.add("bello");
        keys2.add("borld");
        testing.addKeys(keys2);

        testing.updateHtrees();

        System.out.println("\nTesting2");
        FastIterator it = k2i.keys();
        String key;
        while( (key = (String)it.next())!=null) {
            System.out.println(key + " : " + k2i.get(key));
        }
        System.out.println("Done");

    }
}
