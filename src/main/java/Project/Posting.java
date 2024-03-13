package Project;

import java.util.HashMap;
import java.util.Vector;

/**
 * THe major part of this class is the hashmap which stores <keyword, frequency> pair
 */
public class Posting {

    private HashMap<String,Integer> hashMap;
    public Posting()
    {
        hashMap = new HashMap<>();
    }

    /**
     * This function map keyword to its frequency
     * @param v processed word list
     */
    void map(Vector<String> v)
    {
        for(String str:v)
        {
            if(hashMap.get(str)==null)
            {
                hashMap.put(str,1);
            }
            else
            {
                hashMap.replace(str,hashMap.get(str)+1);
            }
        }
    }
    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
        Vector<String> v = se.getString(true);
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        Posting p = new Posting();
        p.map(v);
        for(String str:p.hashMap.keySet())
        {
            System.out.println(str + ": "+ p.hashMap.get(str));
        }
    }
}
