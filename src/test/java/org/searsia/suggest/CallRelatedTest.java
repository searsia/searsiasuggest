package org.searsia.suggest;


import java.io.IOException;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class CallRelatedTest {

    private static SuggestIndex index;
    
    @BeforeClass
    public static void setUp() throws Exception {
    	index = new SuggestIndex("src/test/resources/exampleSuggestions.txt");
    }
    
    @Test
    public void test() throws IOException {
        String queryString = "results help";
        CallRelated related = new CallRelated(index);
        Response response = related.query(queryString);
        String entity = (String) response.getEntity();
        String contentType = response.getHeaderString("Content-Type");
        JSONObject json = new JSONObject(entity);
        JSONArray result = json.getJSONArray("hits");
        Assert.assertEquals("application/searsia+json", contentType);
        Assert.assertEquals("test results", result.getJSONObject(0).get("title"));
        Assert.assertEquals("collection help",  result.getJSONObject(1).get("title"));
    }
    


    
}
