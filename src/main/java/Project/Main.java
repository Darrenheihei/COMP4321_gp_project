package Project;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.htmlparser.NodeFilter;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.filters.TagNameFilter;


public class Main {
    public static String getTitle(String url){
//        try {
            FilterBean fb = new FilterBean();
            fb.setURL(url);
            fb.setFilters(new NodeFilter[]{new TagNameFilter("title")});
            System.out.println(fb.getNodes());
            return fb.getText();
//        }
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm", 30);
            Vector<String> links = spider.extractLinks();
            // get title of the url
            for (String url:links){
                String title = getTitle(url);
                System.out.println(url + ": " + title);
            }
            System.out.println(links.size());

            // get formatted last modification date
            long lastModified = -1; // TODO: change this to read from the modDate HTree
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(lastModified);
            String formattedDate = formatter.format(date);
            System.out.println(formattedDate);

        }
        catch (ParseException e){
            e.printStackTrace();
        }


    }
}
