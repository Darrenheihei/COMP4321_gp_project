package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;

public class Testing3 {
    private RecordManager recman;
    private HTree k2i;
    public Testing3() throws IOException {
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
}
