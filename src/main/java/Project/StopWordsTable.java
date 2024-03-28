package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.io.FileNotFoundException;


/**
 * This class is used to store a table which collects all stop words.
 * In line 21, please use the absolute path of stopwords.txt
 * Singleton pattern is used, please call StopWordsTable.getInstance().hashset to call the table
 */
public class StopWordsTable {
    private static StopWordsTable instance = new StopWordsTable();
    private HashSet<String> hashset = new HashSet<>();

    public StopWordsTable() {
        BufferedReader br = null;
        try {
            FileReader fd = new FileReader("Project/stopwords.txt");
            br = new BufferedReader(fd);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            while((line= br.readLine())!=null) {
                hashset.add(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param str a String
     * @return true if the String is a stop word, false otherwise
     */
    public boolean isStopWords(String str)
    {
        return hashset.contains(str);
    }
    public static StopWordsTable getInstance()
    {
        return instance;
    }

    public static void main(String[] args)
    {
        StopWordsTable swTable = StopWordsTable.getInstance();
        for(String str: swTable.hashset)
        {
            System.out.println(str);
        }
    }
}
