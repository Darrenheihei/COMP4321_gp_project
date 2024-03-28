package Project;

import org.htmlparser.NodeFilter;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

public class StringExtractor {
    private String resource;
    public StringExtractor(String url)
    {
        this.resource = url;
    };

    public String[] splitWord(String str) {
        return str.split("[^a-zA-Z0-9']+");
    }

    public String getBodyText() throws ParserException {
        FilterBean fb = new FilterBean();
        fb.setURL(resource);
        fb.setFilters(new NodeFilter[]{new TagNameFilter("body")});
        return fb.getText();
    }

    public String getTitle() throws ParserException {
        FilterBean fb = new FilterBean();
        fb.setURL(resource);
        fb.setFilters(new NodeFilter[]{new TagNameFilter("title")});
        return fb.getText();
    }

    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");

        try {
            String[] arr = se.splitWord(se.getBodyText());
            System.out.println(Arrays.toString(arr));
        }
        catch (ParserException e){
            e.printStackTrace();
        }
    }
}
