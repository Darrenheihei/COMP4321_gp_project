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
import java.util.Queue;
import java.util.LinkedList;

public class Testing {
    private int a;
    private Testing(int _a){
        a = _a;
    }
    private Testing changing(){
        return new Testing(100);
    }

    public static void main(String[] args){
//        try {
//            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30);
            Long a = 10l;
            String b = a.toString();
            System.out.println(b + 1);
            Long c = Long.parseLong(b);
            System.out.println(c + 1);



//        }
//        catch (ParseException e) {
//            e.printStackTrace();
//        }
//        catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }
}
