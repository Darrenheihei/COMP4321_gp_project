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
import org.htmlparser.util.ParserException;

import java.net.URLConnection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;

public class Spider {
    private String startUrl;
    private int num_pages;

    private static RecordManager recman;
    private static HTree convtable_urlToId; // conversion table: URL to ID, name: "urlToId"
    private static HTree convtable_idToUrl; // conversion table: ID to URL, name: "idToUrl"
    private static HTree hashtable_modDate; // key is urlID, value is time in millisecond, name: "modDate"
    private static HTree hashtable_parentURL; // key is child urlID, value is the parent urlID, name: "parentURL", value is null means no parent page
    private static HTree hashtable_childURL; // key is parent urlID, value is all child urlID, name: "childURL", value is null means no fetched child page
    private static HTree hashtable_fetchedUrl; // key is urlId, value is Url, only contains fetched pages
    private Indexer indexer;

    public Spider(String url, int _num_pages){
        startUrl = url;
        num_pages = _num_pages;

        updateHtrees();
        indexer = new Indexer();
    }

    private void updateHtrees(){
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
                hashtable_modDate = HTree.load(recman, recid);
            } else {
                hashtable_modDate = HTree.createInstance(recman);
                recman.setNamedObject( "modDate", hashtable_modDate.getRecid() );
            }

            // get record id of the object named "parentURL"
            recid = recman.getNamedObject("parentURL");
            if (recid != 0){
                hashtable_parentURL = HTree.load(recman, recid);
            } else {
                hashtable_parentURL = HTree.createInstance(recman);
                recman.setNamedObject( "parentURL", hashtable_parentURL.getRecid() );
            }

            // get record id of the object named "childURL"
            recid = recman.getNamedObject("childURL");
            if (recid != 0){
                hashtable_childURL = HTree.load(recman, recid);
            } else {
                hashtable_childURL = HTree.createInstance(recman);
                recman.setNamedObject( "childURL", hashtable_childURL.getRecid() );
            }

            // get record id of the object named "fetchedUrl"
            recid = recman.getNamedObject("fetchedUrl");
            if (recid != 0){
                hashtable_fetchedUrl = HTree.load(recman, recid);
            } else {
                hashtable_fetchedUrl = HTree.createInstance(recman);
                recman.setNamedObject( "fetchedUrl", hashtable_fetchedUrl.getRecid() );
            }
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
    }

    public Vector<String> extractSinglePageLinks(String link){
        LinkBean lb;
        lb = new LinkBean();
        lb.setURL(link);
        URL[] links = lb.getLinks();

        Vector<String> vec_links = new Vector<>();

        for (URL url:links) {
            vec_links.add(url.toString());
        }

        return vec_links;
    }

    public Vector<String> extractLinks() throws ParseException{
        Vector<String> vec_links = new Vector<>(); // use this as the storage of all links
        Vector<String> vec_links_id = new Vector<>(); // store the IDs of all the pages in vec_links
        Queue<String> link_queue = new LinkedList<String>(); // queue of the links to be extracted
        // initialize link_queue
        link_queue.offer(startUrl);

        // start fetching recursively using BFS
        try {
            String curLink = null;
            while (vec_links.size() < num_pages && ((curLink = link_queue.poll()) != null)) {
                // add the link to vec_links
                vec_links.add(curLink);

                // get the last modification time
                URL curUrl = new URL(curLink);
                URLConnection connection = curUrl.openConnection();
                long lastModified = connection.getLastModified();


                // get the id of the current url
                updateHtrees();
                Object id = convtable_urlToId.get(curLink); // added toString() here because the IDE doesn't know the object being stored is string so it will display an error, which looks bad

                // start to check different conditions
                if (id == null) { // situation 1: curLink is a new URL, not yet stored in database
                    // add current url to conversion tables url <=> id
                    id = UUID.randomUUID().toString();
                    convtable_urlToId.put(curLink, id);
                    convtable_idToUrl.put(id, curLink);

                    // add the last modification date to hash table
                    hashtable_modDate.put(id, Long.toString(lastModified));

                    // save all changes
                    recman.commit();

                    // perform indexing and record other necessary info
                    updateHtrees();
                    System.out.println("\nlink " + vec_links.size() + " " + convtable_idToUrl.get(id));
                    indexer.newPageIndex(curLink, id.toString());

                    // fetch all links in the current url
                    Vector<String> crawled_links = extractSinglePageLinks(curLink);

                    // add new URLs to link_queue
                    for(String url:crawled_links){
                        if (!vec_links.contains(url)){ // it is not in vec_link
                            link_queue.offer(url);
                        }
                    }
                }
                // TODO: this is for phase 2
//                else if (lastModified > Long.parseLong(hashtable_modDate.get(id).toString())) { // situation 2: already in database, but modification time is later than the recorded time
//                    // update the last modification date
//                    hashtable_modDate.put(id, Long.toString(lastModified));
//
//                    // save all changes
//                    recman.commit();
//
//                    // TODO: update inverted index and forward index
//
//                    // fetch all links in the current url
//                    Vector<String> crawled_links = extractSinglePageLinks(curLink);
//
//                    // add new URLs to link_queue
//                    for (String url:crawled_links){
//                        if (!vec_links.contains(url)){ // it is not in vec_link
//                            link_queue.offer(url);
//                        }
//                    }
//                }
            }

            // for child pages that are not included in vec_link,
            // i.e. links in link_queue but not in vec_link,
            // still provide them an url ID and add to the conversion tables url <=> id
            while ((curLink = link_queue.poll()) != null){
                // add current url to conversion tables url <=> id
                Object id = UUID.randomUUID().toString();
                convtable_urlToId.put(curLink, id);
                convtable_idToUrl.put(id, curLink);
            }

            recman.commit();

            // update the childID and parentID hash tables
            updateHtrees();
            for (String parentUrl: vec_links){
                Object parentId = convtable_urlToId.get(parentUrl);
                String childIds = "";
                Vector<String> childURLs = extractSinglePageLinks(parentUrl);

                // get all the child IDs + update parentURL
                for (String childUrl: childURLs){
                    Object childId = convtable_urlToId.get(childUrl);
                    childIds += childId + " ";

                    // update parentURL
                    hashtable_parentURL.put(childId, parentId);
                }
                // update childURL
                hashtable_childURL.put(parentId, childIds);
            }

            // update fetchUrl
            for(String url:vec_links){
                hashtable_fetchedUrl.put(convtable_urlToId.get(url).toString(), url);
            }

            // save all changes
            recman.commit();

            // get a vector of ID of fetched pages
            updateHtrees();
            for (String link:vec_links){
                vec_links_id.add(convtable_urlToId.get(link).toString());
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (ParserException e) {
            e.printStackTrace();
        }

        return vec_links_id;
    }

    public static void main(String[] args){
        try {
            System.out.println("Start Fetching...");
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Vector<String> links = spider.extractLinks();
            System.out.println("Finish Fetching");
            System.out.println("Number of links: " + links.size());
            for(int i = 0; i < links.size(); i++){
                System.out.println(links.get(i));
            }

//            System.out.println();
//            String parent = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
//            String parentId = spider.convtable_urlToId.get(parent).toString();
//            String[] childIds = spider.hashtable_childURL.get(parentId).toString().split(" ");
//            System.out.println(spider.convtable_idToUrl.get(spider.hashtable_parentURL.get(parentId)));
//            System.out.println();
//            for (String childId:childIds){
//                System.out.println(spider.convtable_idToUrl.get(childId));
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(Long.parseLong(spider.hashtable_modDate.get(childId).toString()));
//                String formattedDate = formatter.format(date);
//                System.out.println(formattedDate);
//                System.out.println(spider.convtable_idToUrl.get(spider.hashtable_parentURL.get(childId)));
//
//            }
//            FastIterator iter1 = spider.convtable_urlToId.keys();
//            FastIterator iter2 = spider.convtable_idToUrl.keys();
//            String key;

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

    }
}