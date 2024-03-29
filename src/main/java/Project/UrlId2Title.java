package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;

public class UrlId2Title {
    public static RecordManager recman;
    public static HTree convtable_urlIdtoTitle;

    public UrlId2Title() throws IOException {
        updateHtrees();
    }

    private void updateHtrees(){
        try {
            recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid_urlId2Title = recman.getNamedObject("urlIdToTitle");
            if (recid_urlId2Title != 0) {
                convtable_urlIdtoTitle = HTree.load(recman, recid_urlId2Title);
            } else {
                convtable_urlIdtoTitle = HTree.createInstance(recman);
                recman.setNamedObject("urlIdToTitle", convtable_urlIdtoTitle.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addTitle(String urlId, String title) throws IOException {
        convtable_urlIdtoTitle.put(urlId, title);
        recman.commit();
    }

    public static void main(String[] args) {
        try {
            UrlId2Title u2t = new UrlId2Title();
            u2t.addTitle("testId","test urlId");
            System.out.println(u2t.convtable_urlIdtoTitle.get("testId"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
