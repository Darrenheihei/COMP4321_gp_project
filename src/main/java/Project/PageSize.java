package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import org.htmlparser.beans.StringBean;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

public class PageSize {
    private static RecordManager recman;
    private static HTree convtable_UrlIdToPageSize; //HTree map urlId to page size

    public PageSize() {
        updateHrees();
    }

    private void updateHrees() {
        try {
            recman = RecordManagerFactory.createRecordManager("projectRM");
            long recid_urlId2KeywordId = recman.getNamedObject("pageSizeIndex");
            if (recid_urlId2KeywordId != 0) {
                convtable_UrlIdToPageSize = HTree.load(recman, recid_urlId2KeywordId);
            } else {
                convtable_UrlIdToPageSize = HTree.createInstance(recman);
                recman.setNamedObject("pageSizeIndex", convtable_UrlIdToPageSize.getRecid());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addPageSize(String urlId, String url, String bodyText){
        try {
            URL urll = new URL(url);
            URLConnection connection = urll.openConnection();
            int pageSize = connection.getContentLength();
            if(pageSize == -1){
                pageSize = bodyText.length();
            }

            convtable_UrlIdToPageSize.put(urlId, Integer.toString(pageSize));
            recman.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try {
            PageSize II = new PageSize();
            String bodyText = "";
            II.addPageSize("testId", "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", bodyText);

            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_pageSize;
            long recid_pageSize = recman.getNamedObject("pageSizeIndex");
            if(recid_pageSize != 0) {
                convtable_pageSize = HTree.load(recman,recid_pageSize);
            } else {
                convtable_pageSize = HTree.createInstance(recman);
                recman.setNamedObject("pageSizeIndex",convtable_pageSize.getRecid());
            }

            FastIterator it = convtable_pageSize.keys();
            String size = "";
            while((size=(String)it.next())!=null) {
                System.out.println(size +" "+ convtable_pageSize.get(size).toString());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
