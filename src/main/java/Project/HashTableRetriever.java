package Project;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

public class HashTableRetriever {
    public static HTree getHashTable(String recordId){
        try {
            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid = recman.getNamedObject(recordId);
            HTree hashtable;
            if (recid != 0){
                hashtable = HTree.load(recman, recid);
            } else {
                hashtable = HTree.createInstance(recman);
                recman.setNamedObject( recordId, hashtable.getRecid() );
            }
            return hashtable;
        }
        catch (java.io.IOException e){
            e.printStackTrace();
            System.out.println("Error!");
            return null;
        }
    }
}
