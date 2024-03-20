package Project;
import java.io.IOException;
import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.text.ParseException;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.UUID;

public class Testing {
    private int a;
    private Testing(int _a){
        a = _a;
    }
    private Testing changing(){
        return new Testing(100);
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Vector<String> links = spider.extractLinks();
//            System.out.println("Number of links: " + links.size());
//            for(int i = 0; i < links.size(); i++){
//                System.out.println(links.get(i));
//            }


        }
        catch (ParseException e) {
            e.printStackTrace();
        }
//        catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }
}
