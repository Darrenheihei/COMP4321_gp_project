package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.Vector;

/**
 * THe major part of this class is the hashmap which stores <keyword, frequency> pair
 */
public class KeywordFreqPair {

    private RecordManager recman;
    private HTree hashtable;
    public KeywordFreqPair(String recordmanager, String objectname) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);
        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else
        {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject( objectname, hashtable.getRecid() );
        }

    }

    /**
     * This function map keyword to its frequency
     * @param v processed word list
     */
    void map(Vector<String> v)
    {
        try
        {
            for(String s: v)
            {
                Object result = hashtable.get(s);
                if(result == null){
                    hashtable.put(s,"1");
                }
                else {
                    hashtable.put(s,Integer.toString((Integer.parseInt(hashtable.get(s).toString())+1)));
                }
            }
            recman.commit();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printAll() throws IOException
    {
        // Print all the data in the hashtable
        FastIterator it = hashtable.keys();
        String key;
        while((key = (String)it.next())!=null)
        {
            System.out.println(key  + " " + hashtable.get(key));
        }

    }
    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
        Vector<String> v = se.getAllString(true);
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        KeywordFreqPair p;
        try
        {
            p = new KeywordFreqPair("projectRM","keyword-frequency");
            p.map(v);
            p.printAll();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
