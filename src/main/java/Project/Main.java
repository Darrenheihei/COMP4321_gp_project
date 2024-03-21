package Project;

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import java.net.URL;
import java.util.Scanner;
import java.io.InputStream;


public class Main {
    public static String getTitle(String url){
        InputStream response = null;
        try {
            response = new URL(url).openStream();

            Scanner scanner = new Scanner(response);
            String responseBody = scanner.useDelimiter("\\A").next();
            return responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args){
        try {
            Spider spider = new Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm", 30);
            Vector<String> links = spider.extractLinks();
            // get title of the url
            for (String url:links){
                String title = getTitle(url);
                System.out.println(title);
            }

        }
        catch (ParseException e){
            e.printStackTrace();
        }


    }
}
