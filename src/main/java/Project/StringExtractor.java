package Project;

import org.htmlparser.NodeFilter;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.ParserException;

import java.util.Vector;

public class StringExtractor {
    private String resource;
    public StringExtractor(String url)
    {
        this.resource = url;
    };

    public Vector<String> splitWord(String longStr)
    {
        Vector<String> v = new Vector<>();
        String[] string_array = longStr.split("[ ,“    ?+.~()/!…:\\r?\\n|\\r]");
        for(String str: string_array)
        {
            if(str.length()>0)
                v.add(str);
        }
        return v;
    }

    /**
     * This function is used to get all raw string from the URL link
     * @param link set this value to be true
     * @return a Vector collecting all string in the link
     */
    public Vector<String> getAllString(Boolean link)
    {
//        Vector<String> v = new Vector<>();
        StringBean sb = new StringBean();
        sb.setLinks(link);
        sb.setURL(this.resource);
        String allString = sb.getStrings();


//        String[] string_array = allString.split("[ ,“    ?+.~()/!…:\\r?\\n|\\r]");
//        for(String str: string_array)
//        {
//            if(str.length()>0)
//                v.add(str);
//        }
        return splitWord(allString);
    }

    public String getBodyText() throws ParserException
    {
        FilterBean fb = new FilterBean();
        fb.setURL(this.resource);
        fb.setFilters(new NodeFilter[]{new TagNameFilter("body")});
        return fb.getText();

    }

    public String getTitle() throws ParserException
    {
        FilterBean fb = new FilterBean();
        fb.setURL(this.resource);
        fb.setFilters(new NodeFilter[]{new TagNameFilter("title")});
        return fb.getText();

    }

    public Vector<String> getTitleArray() throws ParserException
    {
        return splitWord(getTitle());
    }

    public Vector<String> getBodyTextArray() throws ParserException
    {
        return splitWord(getBodyText());
    }
    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
//        StringExtractor se = new StringExtractor("https://www.bilibili.com/");

//        Vector<String> v = se.getAllString(true);
//        for(String str:v)
//        {
//            System.out.println(str);
//        }

        try
        {
            String title = se.getTitle();
            System.out.println(title);
//            Vector<String> v = se.getTitleArray();
            Vector<String> v = se.getBodyTextArray();

            for(String str: v)
            {
                System.out.println(str);
            }
        }
        catch(ParserException e)
        {
            e.printStackTrace();
        }
    }
}
