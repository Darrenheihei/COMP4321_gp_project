package Project;

import java.io.IOException;
import java.util.Vector;

import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.FilterBean;
import java.net.URL;
import java.text.ParseException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import org.htmlparser.filters.TagNameFilter;
import java.net.URLConnection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;

public class Spider {
    private String startUrl;
    private int num_pages;

    private RecordManager recman;
    private HTree convtable_urlToId; // conversion table: URL to ID
    private HTree convtable_idToUrl; // conversion table: ID to URL
    private HTree hashtable_modificationDate; // key is urlID, value is time in millisecond
    private HTree hashtable_parentURL; // key is child urlID, value is the parent urlID
    private HTree hashtable_childURL; // key is parent urlID, value is all child urlID

    public Spider(String url, int _num_pages){
        startUrl = url;
        num_pages = _num_pages;

        try {
            // create record manager
            recman = RecordManagerFactory.createRecordManager("projectRM");

            // get record id of the object named "urlToId"
            long recid = recman.getNamedObject("urlToId");
            if (recid != 0){
                convtable_urlToId = HTree.load(recman, recid);
            } else {
                convtable_urlToId = HTree.createInstance(recman);
                recman.setNamedObject( "urlToId", convtable_urlToId.getRecid() );
            }

            // get record id of the object named "idToUrl"
            recid = recman.getNamedObject("idToUrl");
            if (recid != 0){
                convtable_idToUrl = HTree.load(recman, recid);
            } else {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject( "idToUrl", convtable_idToUrl.getRecid() );
            }

            // get record id of the object named "modDate"
            recid = recman.getNamedObject("modDate");
            if (recid != 0){
                convtable_idToUrl = HTree.load(recman, recid);
            } else {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject( "modDate", convtable_idToUrl.getRecid() );
            }

            // get record id of the object named "parentURL"
            recid = recman.getNamedObject("parentURL");
            if (recid != 0){
                convtable_idToUrl = HTree.load(recman, recid);
            } else {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject( "parentURL", convtable_idToUrl.getRecid() );
            }

            // get record id of the object named "childURL"
            recid = recman.getNamedObject("childURL");
            if (recid != 0){
                convtable_idToUrl = HTree.load(recman, recid);
            } else {
                convtable_idToUrl = HTree.createInstance(recman);
                recman.setNamedObject( "childURL", convtable_idToUrl.getRecid() );
            }
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
        try {
            for (int i = 0; i < links.length; i++) {
                vec_links.add(links[i].toString());

                if (convtable_urlToId.get(links[i].toString()) == null) { // crawled an entirely new link
                    // add it to the conversion tables url <=> id
                    String newId = UUID.randomUUID().toString();
                    convtable_urlToId.put(links[i].toString(), newId);
                    convtable_idToUrl.put(newId, links[i].toString());
                    // add the last modification date to hash table
                    URL url = new URL(links[i].toString());
                    URLConnection connection = url.openConnection();
                    long lastModified = connection.getLastModified();
                    hashtable_modificationDate.put(newId, Long.toString(lastModified));
                } else {

                }
            }
            recman.commit();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return vec_links;
    }

    public Vector<String> extractLinks() throws ParseException{
        Vector<String> vec_links = new Vector<>(); // use this as the storage of all links
        Queue<String> link_queue = new LinkedList<String>(); // queue of the links to be extracted
        // initialize vec_link and link_queue
        vec_links.add(startUrl);
        link_queue.offer(startUrl);

        // start fetching recursively using BFS
        try {
            String curLink = null;
            while (vec_links.size() < num_pages && ((curLink = link_queue.poll()) != null)) {
                if (convtable_urlToId.get(curLink) == null) { // curLink is a new URL
                    // fetch all links in the url
                    Vector<String> crawled_links = extractSinglePageLinks(curLink);

                    // add new URLs to vec_links and link_queue
                    for(String url:crawled_links){
                        if (convtable_urlToId.get(url) == null){ // it is a new URL
                            vec_links.add(url);
                            link_queue.offer(url);
                        }
                    }
                    vec_links.addAll(crawled_links);

                    // add the result to link_queue
                    for(String link: crawled_links){
                        link_queue.offer(link);
                    }
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Vector<String> returned_links = new Vector<>(vec_links.subList(0, num_pages));
        return returned_links;
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Vector<String> links = spider.extractLinks();
//            System.out.println("Number of links: " + links.size());
//            for(int i = 0; i < links.size(); i++){
//                System.out.println(links.get(i));
//            }
            FastIterator iter1 = spider.convtable_urlToId.keys();
            FastIterator iter2 = spider.convtable_idToUrl.keys();
            String key;

            // clear the hash table
//            spider.convtable_urlToId.remove("key3");
//            spider.recman.commit();


//            // print out the hash table
//            System.out.println("URL -> ID:");
//            int i = 0;
//            while( (key = (String)iter1.next())!=null)
//            {
//                System.out.println(key + " : " + spider.convtable_urlToId.get(key));
//            }
//
//            System.out.println("ID -> URL:");
//            while( (key = (String)iter2.next())!=null)
//            {
//                System.out.println(key + " : " + spider.convtable_idToUrl.get(key));
//            }


        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
