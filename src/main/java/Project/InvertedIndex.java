package Project;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Vector;
public class InvertedIndex {
    private RecordManager recman;
    private HTree convtable_keywordIdToUrlId;
    private HTree convtable_idToUrl; //convert urlId to url
    private Keyword2Id k2i; //make


    public InvertedIndex() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("forwardIndex");
        if(recid_urlId2KeywordId != 0)
        {
            convtable_keywordIdToUrlId = HTree.load(recman,recid_urlId2KeywordId);
        }
        else
        {
            convtable_keywordIdToUrlId = HTree.createInstance(recman);
            recman.setNamedObject("keywordToId",convtable_keywordIdToUrlId.getRecid());
        }

        long recid = recman.getNamedObject("idToUrl");
        if (recid != 0){
            convtable_idToUrl = HTree.load(recman, recid);
        } else {
            convtable_idToUrl = HTree.createInstance(recman);
            recman.setNamedObject( "idToUrl", convtable_idToUrl.getRecid() );
        }


        k2i = new Keyword2Id();
    }
    public void update(String wordId, String urlId) throws IOException {
        String url = convtable_idToUrl.get(urlId).toString();
        StringExtractor se = new StringExtractor(url);
        Vector<String> v = se.getString(true);
    }

    public void add() throws IOException {

    }

}
