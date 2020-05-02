package org.searsia.suggest;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class CallSpellcorrectTest {

    private static String proxy = null;
    private static SuggestIndex index;
    
    @BeforeClass
    public static void setUp() throws Exception {
    	index = new SuggestIndex("src/test/resources/exampleSuggestions.txt");
    }
    
    @Test
    public void testOk() throws IOException {
        String queryString = "collection";
        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        Response response = spellcorrect.query(queryString);
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("application/searsia+json", contentType);
        Assert.assertTrue(result.length() == 0);
    }
   
    @Test
    public void testInsertion() throws IOException {
        String queryString = "colection";
        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        Response response = spellcorrect.query(queryString);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        JSONObject resource = json.getJSONObject("resource");
        Assert.assertEquals("collection", result.getJSONObject(0).get("title"));
        Assert.assertEquals("spellcorrect", resource.get("id"));
    }
    
    @Test
    public void testDeletion() throws IOException {
        String queryString = "colllection";
        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        Response response = spellcorrect.query(queryString);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("collection", result.getJSONObject(0).get("title"));
    }
    
    @Test
    public void testSubstitution() throws IOException {
        String queryString = "downloed";
        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        Response response = spellcorrect.query(queryString);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("download", result.getJSONObject(0).get("title"));
    }
    
    @Test
    public void testPhrase() throws IOException {
        String queryString = "MASA result";
        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        Response response = spellcorrect.query(queryString);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("mesa results", result.getJSONObject(0).get("title"));
    }
    
    /**
     * Check each query in the train data for spell corrections
     * For testing purposes only
     * @param args
     * @throws IOException 
     * @throws JSONException 
     * @throws NumberFormatException 
     */
    public static void main(String[] args) throws NumberFormatException, JSONException, IOException {
    	final String fileString = "/home/databases/Data/anchors_count.txt";
    	SuggestIndex index = new SuggestIndex(fileString);

        CallSpellcorrect spellcorrect = new CallSpellcorrect(index, proxy);
        BufferedReader reader = new BufferedReader(new FileReader(fileString)); 
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#") && line.length() > 0) {
                String[] fields = line.split("\t");
                String queryString = fields[1];
                Response response = spellcorrect.query(queryString);
                String entity = (String) response.getEntity();
                JSONObject json = new JSONObject(entity);
                JSONArray result = json.getJSONArray("hits");
                if (result.length() > 0) {
                 	System.out.println(queryString + "\t:\t" + result.getJSONObject(0).get("title"));
                }
            }
        }
        reader.close();
    }    	


    
}
