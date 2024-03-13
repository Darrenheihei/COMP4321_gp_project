package Project;

import org.htmlparser.beans.StringBean;

import java.util.Vector;

public class StringExtractor {
    private String resource;
    public StringExtractor(String url)
    {
        this.resource = url;
    }

    /**
     * This function is used to get all raw string from the URL link
     * @param link set this value to be true
     * @return a Vector collecting all string in the link
     */
    public Vector<String> getString(Boolean link)
    {
        Vector<String> v = new Vector<>();
        StringBean sb = new StringBean();
        sb.setLinks(link);
        sb.setURL(this.resource);
        String allString = sb.getStrings();
        String[] string_array = allString.split("[ ,    ?+.()/:]");
        for(String str: string_array)
        {
            if(str.length()>0)
                v.add(str);
        }
        return v;
    }
    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
        Vector<String> v = se.getString(true);
        for(String str:v)
        {
            System.out.println(str);
        }
    }
}
