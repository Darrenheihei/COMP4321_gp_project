package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

public class Keyword2Id {
    private RecordManager recman;
    private HTree convtable_keywordToId; // conversion table: Keyword to ID
    private HTree convtable_idToKeyword; // conversion table: ID to Keyword
    public Keyword2Id() throws IOException
    {

        recman = RecordManagerFactory.createRecordManager("projectRM");

        //handle keyword to id
        long recid_key2id = recman.getNamedObject("keywordToId");
        if(recid_key2id != 0)
        {
            convtable_keywordToId = HTree.load(recman,recid_key2id);
        }
        else
        {
            convtable_keywordToId = HTree.createInstance(recman);
            recman.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
        }

        //handle id to keyword
        long recid_id2key = recman.getNamedObject("idToKeyword");
        if(recid_id2key != 0)
        {
            convtable_idToKeyword = HTree.load(recman,recid_id2key);
        }
        else
        {
            convtable_idToKeyword = HTree.createInstance(recman);
            recman.setNamedObject("idToKeyword",convtable_idToKeyword.getRecid());
        }



    }

    public void addKeyword(String keyword) throws IOException
    {
        if(convtable_keywordToId.get(keyword)==null)
        {
            String newId = UUID.randomUUID().toString();
            convtable_keywordToId.put(keyword,newId);
            convtable_idToKeyword.put(newId,keyword);
            recman.commit();
        }

    }
    public String getId(String keyword) throws IOException
    {

        if(convtable_keywordToId.get(keyword) == null)
        {
            addKeyword(keyword);
        }
        String id = convtable_keywordToId.get(keyword).toString();
        return id;
    }

    public String getKeyword(String id) throws IOException
    {
        String keyword = "";
        if(convtable_idToKeyword.get(id)!= null)
        {
            keyword =  convtable_idToKeyword.get(id).toString();
        }

        return keyword;
    }

    public void deleteKeyword(String keyword) throws IOException
    {

        if(convtable_keywordToId.get(keyword)!=null)
        {
            String id = convtable_keywordToId.get(keyword).toString();
            convtable_keywordToId.remove(keyword);
            convtable_idToKeyword.remove(id);
            recman.commit();
        }
    }



    public void addKeywordFromUrl(String url) throws IOException
    {
        StringExtractor se = new StringExtractor(url);
        Vector<String> v = se.getAllString(true);
        StopStem stop_stem = new StopStem();
        v = stop_stem.stopAndStem(v);

        for(String str:v)
        {
            if(convtable_keywordToId.get(str)==null) {
                String newId = UUID.randomUUID().toString();
                convtable_keywordToId.put(str, newId);
                convtable_idToKeyword.put(newId, str);
            }
        }
        recman.commit();
    }



    public void finalize() throws IOException
    {
        recman.commit();
        recman.close();
    }

    static public void main(String[] args)
    {
        try
        {
            Keyword2Id k2i = new Keyword2Id();
            k2i.addKeyword("hello");
//            String id = k2i.convtable_keywordToId.get("hello").toString();
//            System.out.println(id);
//            String keyword = k2i.convtable_idToKeyword.get(id).toString();
//            System.out.println(keyword);
//            k2i.deleteKeyword(keyword);
//            k2i.finalize();
            RecordManager recman = RecordManagerFactory.createRecordManager("projectRM");
            HTree convtable_keywordToId;
            HTree convtable_idToKeyword;
            long recid_keywordToId = recman.getNamedObject("keywordToId");
            if(recid_keywordToId != 0)
            {
                convtable_keywordToId = HTree.load(recman,recid_keywordToId);
            }
            else
            {
                convtable_keywordToId = HTree.createInstance(recman);
                recman.setNamedObject("keywordToId",convtable_keywordToId.getRecid());
            }
            long recid_idToKeyword = recman.getNamedObject("idToKeyword");
            if(recid_idToKeyword != 0)
            {
                convtable_idToKeyword = HTree.load(recman,recid_idToKeyword);
            }
            else
            {
                convtable_idToKeyword = HTree.createInstance(recman);
                recman.setNamedObject("idToKeyword",convtable_idToKeyword.getRecid());
            }
            FastIterator it3 = convtable_keywordToId.keys();
            String key3;
            while((key3 = (String)it3.next())!=null)
            {
                System.out.println(key3 + " = " + convtable_keywordToId.get(key3));
            }
            FastIterator it4 = convtable_idToKeyword.keys();
            String key4;
            while((key4 = (String)it4.next())!=null)
            {
                System.out.println(key4 + " = " + convtable_idToKeyword.get(key4));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

}