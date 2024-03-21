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

import java.util.UUID;

public class Spider {
    private String url;
    private int num_pages;

    private RecordManager recman;
    private HTree convtable_urlToId; // conversion table: URL to ID
    private HTree convtable_idToUrl; // conversion table: ID to URL

    public Spider(String _url, int _num_pages){
        url = _url;
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
                // if crawled an entirely new link, add it to the conversion tables url <=> id
                if (convtable_urlToId.get(links[i].toString()) == null) {
                    String newId = UUID.randomUUID().toString();
                    convtable_urlToId.put(links[i].toString(), newId);
                    convtable_idToUrl.put(newId, links[i].toString());
                }
            }
            recman.commit();
            System.out.println(count);
        }
        catch (IOException e){
            e.printStackTrace();
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
