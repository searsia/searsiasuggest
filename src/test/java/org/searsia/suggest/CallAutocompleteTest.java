package org.searsia.suggest;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CallAutocompleteTest {
   
    private static SuggestIndex index1, index2;
    
    @BeforeClass
    public static void setUp() throws Exception {
    	index1 = new SuggestIndex("src/test/resources/exampleSuggestions.txt");
    	index2 = new SuggestIndex("src/test/resources/exampleSuggestionsAdvanced.txt");
    }

    @Test
    public void test1() throws IOException {
    	testOpensearch1(index1);
    	testOpensearch1(index2);
    }
    
    @Test
    public void test2() throws IOException {
    	testOpensearch2(index1);
    	testOpensearch2(index2);
    }
    
    @Test
    public void test3() throws IOException {
    	testSearsia(index1);
    	testSearsia(index2);
    }

    @Test
    public void test4() throws IOException {
    	testEmpty1(index1);
    	testEmpty1(index2);
    }

    @Test
    public void test5() throws IOException {
    	testEmpty2(index1);
    	testEmpty2(index2);
    }

    @Test
    public void testAdvancedCompletions1() throws IOException {
        String queryString = "test";
        CallAutocomplete autocomplete = new CallAutocomplete(index2);
        Response response = autocomplete.query(queryString, "opensearch", "links");
        String entity = (String) response.getEntity();
        JSONArray json = new JSONArray(entity);
        Assert.assertEquals(json.get(0), queryString);
        JSONArray suggestions = json.getJSONArray(1);
        Assert.assertEquals("Test Collection Download", suggestions.get(0));
        Assert.assertEquals("test again", suggestions.get(1));
    }
    
    @Test
    public void testAdvancedCompletions2() throws IOException {
        String queryString = "mesa+";
        CallAutocomplete search = new CallAutocomplete(index2);
        Response response = search.query(queryString, "opensearch", "images");
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONArray json = new JSONArray(entity);
        JSONArray result = json.getJSONArray(1);
        Assert.assertEquals("application/x-suggestions+json", contentType);
        Assert.assertEquals("MESA+ Events", result.get(0));
        Assert.assertEquals("MESA+ Nanolab", result.get(1));
    }
    
	private void testOpensearch1(SuggestIndex index) throws IOException {
        String queryString = "test";
        CallAutocomplete autocomplete = new CallAutocomplete(index);
        Response response = autocomplete.query(queryString, "opensearch", null);
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
    
    private void testOpensearch2(SuggestIndex index) throws IOException {
        String queryString = "mesa+ news";
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(queryString, "opensearch", null);
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONArray json = new JSONArray(entity);
        JSONArray result = json.getJSONArray(1);
        Assert.assertEquals("application/x-suggestions+json", contentType);
        Assert.assertEquals("MESA+ News", result.get(0));
    }
    
    private void testSearsia(SuggestIndex index) throws IOException {
        String queryString = "nog een";
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(queryString, "searsia", null);
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("application/searsia+json", contentType);
        Assert.assertEquals("NOG ÉÉNTJE?", result.getJSONObject(0).get("title"));
    }

    private void testEmpty1(SuggestIndex index) throws IOException {
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query("", "", "");
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertTrue(result.length() > 5);
    }

    private void testEmpty2(SuggestIndex index) throws IOException {
        CallAutocomplete search = new CallAutocomplete(index);
        Response response = search.query(null, null, null);
        String entity = (String) response.getEntity();
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals(0, result.length());
    }

}
