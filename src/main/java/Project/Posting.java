package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/**
 * THe major part of this class is the hashmap which stores <keyword, frequency> pair
 */
public class Posting {

    private RecordManager recman;
    private HTree hashtable;
    public Posting(String recordmanager, String objectname) throws IOException
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

        HashMap<String,Integer> hm = new HashMap<>();
        for(String str:v)
        {
            if(hm.get(str)==null)
            {
                hm.put(str,1);
            }
            else
            {
                hm.put(str,hm.get(str)+1);
            }
        }
        try
        {
            for(String s: hm.keySet())
            {
                hashtable.put(s,String.valueOf(hm.get(s)));
            }
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
            System.out.println(key  + hashtable.get(key));
        }

    }
    public static void main(String[] args)
    {
        StringExtractor se = new StringExtractor("http://www.cs.ust.hk/~dlee/4321/");
        Vector<String> v = se.getString(true);
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        Posting p;
        try
        {
            p = new Posting("projectRM","keyword-frequency");
            p.map(v);
            p.printAll();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
