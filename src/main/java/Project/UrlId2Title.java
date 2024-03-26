package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import org.htmlparser.util.ParserException;

import java.io.IOException;

public class UrlId2Title {
    public RecordManager recman;
    public HTree convtable_urlIdtoTitle;


    public UrlId2Title() throws IOException
    {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2Title = recman.getNamedObject("UrlId2Title");
        if(recid_urlId2Title != 0)
        {
            convtable_urlIdtoTitle = HTree.load(recman,recid_urlId2Title);
        }
        else
        {
            convtable_urlIdtoTitle = HTree.createInstance(recman);
            recman.setNamedObject("UrlId2Title",convtable_urlIdtoTitle.getRecid());
        }

    }
    public void addUrl(String url, String urlId) throws ParserException, IOException
    {
        if(convtable_urlIdtoTitle.get(urlId) == null)
        {
            StringExtractor se = new StringExtractor(url);
            String title = se.getTitle();
            convtable_urlIdtoTitle.put(urlId,title);
            recman.commit();
        }
    }
    public String getTitle(String urlId) throws IOException
    {
        String str = "";
        if(convtable_urlIdtoTitle.get(urlId) != null)
        {
            str = convtable_urlIdtoTitle.get(urlId).toString();
        }
        return str;
    }
    public void deleteUrlId(String urlId) throws IOException
    {
        if(convtable_urlIdtoTitle.get(urlId) != null)
        {
            convtable_urlIdtoTitle.remove(urlId);
            recman.commit();
        }

    }
    public void update(String url, String urlId) throws ParserException, IOException
    {
        deleteUrlId(urlId);
        addUrl(url,urlId);
    }

    public static void main(String[] args)
    {
        try
        {
            UrlId2Title u2t = new UrlId2Title();
            u2t.addUrl("https://www.bilibili.com/","test urlId");
            String s = u2t.getTitle("test urlId");
            System.out.println(s);
            u2t.deleteUrlId("test urlId");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserException e)
        {
            e.printStackTrace();
        }

    }
}
