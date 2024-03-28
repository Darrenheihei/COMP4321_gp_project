package Project;

import org.htmlparser.util.ParserException;

import java.util.SortedMap;
import java.util.Vector;

public class StopStem {

    private StopWordsTable swTable = StopWordsTable.getInstance();
    public StopStem() {}

    /**
     * @param wordList a raw word list before processing
     * @return a Vector of words after removing stop words
     */
    public Vector<String> removeStopWords(String[] wordList)
    {
        Vector<String> newWordList = new Vector<>();

        for(String str: wordList) {
            if(!swTable.isStopWords(str)) {
                newWordList.add(str);
            }
        }
        return newWordList;
    }

    /**
     * @param wordList a word list after removing stop words but before stemming
     * @return stemmed word list
     */
    public Vector<String> stem(Vector<String> wordList)
    {
        Vector<String> newWordList = new Vector<>();
        Porter porter = new Porter();
        for(String str:wordList) {
            String newStr = porter.stripAffixes(str);
            if(newStr.length() > 0)
                newWordList.add(newStr);
        }
        return newWordList;
    }

    /**
     * This is the combination of removeStopWords() and stem()
     * @param v raw word list before processing
     * @return word list after removing stop words and stemming
     */
    public Vector<String> stopAndStem(String[] v)
    {
        return stem(removeStopWords(v));
    }

    public static void main(String[] args)
    {
        try {
            StringExtractor se = new StringExtractor("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
            String[] v = se.splitWord(se.getBodyText());

            StopStem stop_stem = new StopStem();
            Vector<String> result = stop_stem.stopAndStem(v);
            for (String str : result) {
                System.out.println(str);
            }
        }
        catch (ParserException e){
            e.printStackTrace();
        }

    }
}
