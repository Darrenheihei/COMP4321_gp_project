package Project;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.UUID;

/**
 * This class is used to convert keyword to id and convert id to keyword.
 * Use after creating an object.
 */
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
        String id = convtable_keywordToId.get(keyword).toString();
        if(id!=null)
        {
            convtable_keywordToId.remove(keyword);
            convtable_idToKeyword.remove(id);
        }
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
            String id = k2i.getId("hello");
            System.out.println(id);
            String keyword = k2i.getKeyword(id);
            System.out.println(keyword);
            k2i.deleteKeyword(keyword);
            k2i.finalize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

}
