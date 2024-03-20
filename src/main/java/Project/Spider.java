package Project;

import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.text.ParseException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import java.util.UUID;

public class Spider {
    private String url;
    private int num_pages;

    private RecordManager recman;
    private HTree convtable_urlToId; // conversion table: URL to ID
    private HTree convtable_idToUrl; // conversion table: ID to URL
    private Spider(String _url, int _num_pages){
        url = _url;
        num_pages = _num_pages;

        try {
            // create record manager
            recman = RecordManagerFactory.createRecordManager("projectRM");

            // get record id of the object named "urlToId"
            convtable_urlToId = HashTableRetriever.getHashTable("urlToId");
            convtable_urlToId.put("key5", "context 5");
            System.out.println( convtable_urlToId.get("key5"));
            recman.commit();

            // get record id of the object named "idToUrl"
            convtable_idToUrl = HashTableRetriever.getHashTable("idToUrl");
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public Vector<String> extractSinglePageLinks(String link) throws ParseException{
        LinkBean lb;
        lb = new LinkBean();
        lb.setURL(link);
        URL[] links = lb.getLinks();

        Vector<String> vec_links = new Vector<>();
        for (int i = 0; i < links.length; i++){
            vec_links.add(links[i].toString());
        }
        return vec_links;
    }

    public Vector<String> extractLinks() throws ParseException{
        // initialize the list of url fetched
        Vector<String> vec_links = new Vector<>(); // use this as both the storage of all links and the queue
        vec_links.add(url);

        // start fetching recursively using BFS
        int cur_index = 0; // record the index of the current url to be crawled in the vec_links
        while (vec_links.size() < num_pages){
            // fetch all links in the url
            Vector<String> crawled_links = extractSinglePageLinks(vec_links.get(cur_index));
            // break out of the loop if there less pages that can be crawled than num_pages
//            if (...){
//                break;
//            }
            // add the result to vec_links
            vec_links.addAll(crawled_links);
            // continue BFS
            cur_index++;
        }

        Vector<String> returned_links = new Vector<>(vec_links.subList(0, num_pages));
        return returned_links;
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Vector<String> links = spider.extractLinks();
            System.out.println("Number of links: " + links.size());
            for(int i = 0; i < links.size(); i++){
                System.out.println(links.get(i));
            }

        }
        catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
