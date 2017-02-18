package org.searsia.suggest;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CallAutocompleteTest {
   
    private static SuggestIndex index;
    
    @BeforeClass
    public static void setUp() throws Exception {
    	index = new SuggestIndex("src/test/resources/exampleSuggestions.txt");
    }

    @Test
	public void testOpensearch1() throws IOException {
        String queryString = "test";
        CallAutocomplete autocomplete = new CallAutocomplete(index);
        Response response = autocomplete.query(queryString, "opensearch");
        int status = response.getStatus();
        String entity = (String) response.getEntity();
        JSONArray json = new JSONArray(entity);
        Assert.assertEquals(200, status);
        Assert.assertEquals(json.get(0), queryString);
        JSONArray result = json.getJSONArray(1);
        Assert.assertEquals("test results", result.get(0));
        Assert.assertEquals("Test Collection Download", result.get(1));
        Assert.assertEquals("test, train and validation set", result.get(2));
	}
    
    @Test
    public void testOpensearch2() throws IOException {
        String queryString = "mesa+ news";
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(queryString, "opensearch");
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONArray json = new JSONArray(entity);
        JSONArray result = json.getJSONArray(1);
        Assert.assertEquals("application/x-suggestions+json", contentType);
        Assert.assertEquals("MESA+ News", result.get(0));
    }
    
    @Test
    public void testSearsia() throws IOException {
        String queryString = "nog een";
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(queryString, "searsia");
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("application/searsia+json", contentType);
        Assert.assertEquals("NOG ÉÉNTJE?", result.getJSONObject(0).get("title"));
    }

    @Test
    public void testEmpty1() throws IOException {
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query("", "");
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertTrue(result.length() > 5);
    }

    @Test
    public void testEmpty2() throws IOException {
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(null, null);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals(0, result.length());
    }


}
