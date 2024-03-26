package Project;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import org.htmlparser.beans.StringBean;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
public class PageSize {
    private RecordManager recman;
    private HTree convtable_UrlIdToPageSize; //HTree map urlId to page size

    public PageSize() throws IOException {
        recman = RecordManagerFactory.createRecordManager("projectRM");
        long recid_urlId2KeywordId = recman.getNamedObject("invertedIndex");
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

    public void close(){
        try {
            if (recman != null) {
                recman.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
