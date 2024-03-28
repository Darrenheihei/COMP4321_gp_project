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
        Vector<String> a = new Vector<>();
        Vector<String> b = new Vector<>();
        Vector<String> c = new Vector<>();
        String d = "Hello World\n";
        a.add("a");
        b.add("b");
        c.addAll(a);
        c.addAll(b);
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d.length());



//        }
//        catch (ParseException e) {
//            e.printStackTrace();
//        }
//        catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }
}
