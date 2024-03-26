package Project;

import java.util.Vector;

public class StopStem {

    private StopWordsTable swTable = StopWordsTable.getInstance();
    public StopStem()
    {

    }

    /**
     * @param wordList a raw word list before processing
     * @return a Vector of words after removing stop words
     */
    public Vector<String> removeStopWords(Vector<String> wordList)
    {
        Vector<String> newWordList = new Vector<>();

        for(String str: wordList)
        {
            if(!swTable.isStopWords(str))
            {
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
        for(String str:wordList)
        {
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
    public Vector<String> stopAndStem(Vector<String> v)
    {
        return stem(removeStopWords(v));
    }

    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
        Vector<String> v = se.getAllString(true);
        StopStem stop_stem = new StopStem();
//        System.out.println("The words before processing: ");
//        for(int i=0;i<10;i++)
//        {
//            System.out.println(v.get(i));
//        }
//
//        v = stop_stem.removeStopWords(v);
//        System.out.println("The words after removing stopwords: ");
//        for(int i=0;i<10;i++)
//        {
//            System.out.println(v.get(i));
//        }
//        v = stop_stem.stem(v);
//        System.out.println("The words after stemming: ");
//        for(int i=0;i<10;i++)
//        {
//            System.out.println(v.get(i));
//        }

//        v = stop_stem.removeStopWords(v);
        v = stop_stem.stopAndStem(v);
        for(String str:v)
        {
            System.out.println(str);
        }

    }
}
