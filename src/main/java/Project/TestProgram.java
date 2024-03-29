package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;

public class TestProgram {
    private static RecordManager recman;
    private static HTree convtable_idToUrl;
    private static HTree id2title;
    private static HTree id2date;
    private static HTree id2size;
    private static HTree titleInvertedIndex;
    private static HTree bodyInvertedIndex;
    private static HTree forwardIndex;
    private static HTree id2childUrl;

    public TestProgram() {
        updateHtrees();
    }

    private void updateHtrees() {
        try {
            // create record manager
            recman = RecordManagerFactory.createRecordManager("projectRM");

            // get record id of the object named "idToUrl"
            long recid = recman.getNamedObject("idToUrl");
            if (recid != 0) {
                convtable_idToUrl = HTree.load(recman, recid);
            } else {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject("idToUrl", convtable_idToUrl.getRecid());
            }

            // get record id of the object named "urlIdToTitle"
            recid = recman.getNamedObject("urlIdToTitle");
            if (recid != 0) {
                id2title = HTree.load(recman, recid);
            } else {
                id2title = HTree.createInstance(recman);
                recman.setNamedObject("urlIdToTitle", id2title.getRecid());
            }

            // get record id of the object named "modDate"
            recid = recman.getNamedObject("modDate");
            if (recid != 0) {
                id2date = HTree.load(recman, recid);
            } else {
                id2date = HTree.createInstance(recman);
                recman.setNamedObject("modDate", id2date.getRecid());
            }

            // get record id of the object named "pageSizeIndex"
            recid = recman.getNamedObject("pageSizeIndex");
            if (recid != 0) {
                id2size = HTree.load(recman, recid);
            } else {
                id2size = HTree.createInstance(recman);
                recman.setNamedObject("pageSizeIndex", id2size.getRecid());
            }

            // get record id of the object named "titleInvertedIndex"
            recid = recman.getNamedObject("titleInvertedIndex");
            if (recid != 0) {
                titleInvertedIndex = HTree.load(recman, recid);
            } else {
                titleInvertedIndex = HTree.createInstance(recman);
                recman.setNamedObject("titleInvertedIndex", titleInvertedIndex.getRecid());
            }

            // get record id of the object named "bodyInvertedIndex"
            recid = recman.getNamedObject("bodyInvertedIndex");
            if (recid != 0) {
                bodyInvertedIndex = HTree.load(recman, recid);
            } else {
                bodyInvertedIndex = HTree.createInstance(recman);
                recman.setNamedObject("bodyInvertedIndex", bodyInvertedIndex.getRecid());
            }

            // get record id of the object named "forwardIndex"
            recid = recman.getNamedObject("forwardIndex");
            if (recid != 0) {
                forwardIndex = HTree.load(recman, recid);
            } else {
                forwardIndex = HTree.createInstance(recman);
                recman.setNamedObject("forwardIndex", forwardIndex.getRecid());
            }

            // get record id of the object named "childURL"
            recid = recman.getNamedObject("childURL");
            if (recid != 0) {
                id2childUrl = HTree.load(recman, recid);
            } else {
                id2childUrl = HTree.createInstance(recman);
                recman.setNamedObject("childURL", id2childUrl.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void getOutput() throws IOException {
        FastIterator IDs = convtable_idToUrl.keys();
        String id;
        while( (id = (String)IDs.next())!=null) {
            System.out.println(id);
        }
    }

    public static void main(String[] args) {
        TestProgram test = new TestProgram();

        try {
            test.getOutput();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
