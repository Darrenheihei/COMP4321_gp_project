package Project;
<<<<<<< HEAD
=======

>>>>>>> eb7065dd28659d30db7b7a9449ff3d052bdd72b2
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.beans.StringBean;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
<<<<<<< HEAD
=======

>>>>>>> eb7065dd28659d30db7b7a9449ff3d052bdd72b2
public class PageSize {
    private RecordManager recman;
    private HTree convtable_UrlIdToPageSize; //HTree map urlId to page size

    public PageSize() throws IOException {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("pageSizeIndex");
        if (recid_urlId2KeywordId != 0) {
            convtable_UrlIdToPageSize = HTree.load(recman, recid_urlId2KeywordId);
        } else {
            convtable_UrlIdToPageSize = HTree.createInstance(recman);
            recman.setNamedObject("pageSizeIndex", convtable_UrlIdToPageSize.getRecid());
        }

    }

    public void updatePageSize(String urlId, String url){
        try {
            URL urll = new URL(url);
            URLConnection connection = urll.openConnection();
            int pageSize = connection.getContentLength();
            if(pageSize == -1){
                Vector<String> v = new Vector<>();
                StringBean sb = new StringBean();
                sb.setLinks(false);
                sb.setURL(url);
                String allString = sb.getStrings();
                pageSize = allString.length();
            }
                convtable_UrlIdToPageSize.put(urlId, pageSize);
            recman.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPageSize(String urlId) throws IOException {
        if(convtable_UrlIdToPageSize.get(urlId) == null) return 0;
        return Integer.parseInt(convtable_UrlIdToPageSize.get(urlId).toString());
    }

    public void printAll() throws IOException
    {
        // Print all the data in the hashtable
        FastIterator it = convtable_UrlIdToPageSize.keys();
        String key;
        while((key = (String)it.next())!=null)
        {
            System.out.println(key  + " " + convtable_UrlIdToPageSize.get(key));
        }

    }

    public void close(){
        try {
            if (recman != null) {
                recman.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            PageSize II = new PageSize();
            II.updatePageSize("123", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            II.printAll();
            II.close();
<<<<<<< HEAD
=======

            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_pageSize;
            long recid_pageSize = recman.getNamedObject("pageSizeIndex");
            if(recid_pageSize != 0)
            {
                convtable_pageSize = HTree.load(recman,recid_pageSize);
            }
            else
            {
                convtable_pageSize = HTree.createInstance(recman);
                recman.setNamedObject("pageSizeIndex",convtable_pageSize.getRecid());
            }
            FastIterator it = convtable_pageSize.keys();
            String size = "";
            while((size=(String)it.next())!=null)
            {
                System.out.println(size +" "+ convtable_pageSize.get(size).toString());
            }
>>>>>>> eb7065dd28659d30db7b7a9449ff3d052bdd72b2
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
