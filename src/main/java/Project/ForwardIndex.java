package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Vector;

public class ForwardIndex {
    private RecordManager recman;
    private HTree convtable_urlIdToKeywordId;
    private HTree convtable_idToUrl;
    private Keyword2Id k2i;


    public ForwardIndex() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("forwardIndex");
        if(recid_urlId2KeywordId != 0)
        {
            convtable_urlIdToKeywordId = HTree.load(recman,recid_urlId2KeywordId);
        }
        else
        {
            convtable_urlIdToKeywordId = HTree.createInstance(recman);
            recman.setNamedObject("keywordToId",convtable_urlIdToKeywordId.getRecid());
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
    public void addUrlId(String urlId) throws IOException
    {
        if(convtable_urlIdToKeywordId.get(urlId)==null)
        {
            String url = convtable_idToUrl.get(urlId).toString();
            StringExtractor se = new StringExtractor(url);
            Vector<String> v = se.getAllString(true);
            StopStem stop_stem = new StopStem();
            v = stop_stem.stopAndStem(v);


            String IDs = "";
            for(String str: v)
            {

                IDs = IDs + k2i.getId(str) + " ";
            }
            convtable_urlIdToKeywordId.put(urlId,IDs);

        }

    }

    public String getKeywordId(String urlId) throws IOException
    {
        String IDs = convtable_urlIdToKeywordId.get(urlId).toString();
        return IDs;
    }

    public void deleteUrlId(String urlId) throws IOException
    {
        if(convtable_urlIdToKeywordId.get(urlId)!=null)
        {
            convtable_urlIdToKeywordId.remove(urlId);
        }
    }

    public void update(String urlId) throws IOException
    {
        this.deleteUrlId(urlId);
        this.addUrlId(urlId);
    }

    public void finalize() throws IOException
    {
        recman.commit();
        recman.close();
    }

    public static void main(String[] args)
    {
        try
        {
            ForwardIndex fi = new ForwardIndex();
            fi.convtable_idToUrl.put("testing urlid","https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            fi.addUrlId("testing urlid");
            System.out.println(fi.convtable_urlIdToKeywordId.get("testing urlid"));
            fi.deleteUrlId("testing urlid");
            fi.convtable_idToUrl.remove("testing urlid");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
