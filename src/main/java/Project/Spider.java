package Project;

import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.text.ParseException;

public class Spider {
    private String url;
    private int num_pages;

    Spider(String _url, int _num_pages){
        url = _url;
        num_pages = _num_pages;
    }

    public Vector<String> extractLinks() throws ParseException{
        LinkBean lb;
        lb = new LinkBean();
        lb.setURL(url);
        URL[] links = lb.getLinks();

        Vector<String> vec_links = new Vector<>();
        for (int i = 0; i < links.length; i++){
            vec_links.add(links[i].toString());
        }
        return vec_links;
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Vector<String> links = spider.extractLinks();
            for(int i = 0; i < links.size(); i++){
                System.out.println(links.get(i));
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
