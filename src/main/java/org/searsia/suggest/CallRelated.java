/*
 * Copyright 2016 Searsia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.searsia.suggest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Generates json response for HTTP GET search.
 * 
 * @author Dolf Trieschnigg and Djoerd Hiemstra
 */
@Path("related")
public class CallRelated {

	public static final String getRequest = "/related?q={searchTerms}";
    private SuggestIndex index;
    private JSONObject resource;
    
    public CallRelated(SuggestIndex index, String myProxyUrl) throws IOException {
        this.index = index;
        this.resource = new JSONObject();
        this.resource.put("id", "related");
        this.resource.put("name", "Related Searches: ");
        this.resource.put("mimetype", "application/searsia+json");
        if (myProxyUrl != null) {
            this.resource.put("apitemplate", myProxyUrl + getRequest);
            this.resource.put("directaccess", "yes");
        }
    }

    private String jsonSearsia(List<String> result) {
        JSONObject json = new JSONObject();
        JSONArray hits = new JSONArray();
        for (String suggestion: result) {
            hits.put(new JSONObject().put("title", suggestion).put("tags", "#small #suggestion"));
        }
        json.put("hits", hits);
        json.put("resource",  this.resource);
        return json.toString();
    }


    @OPTIONS
    public Response options() {
        return Response.status(Response.Status.NO_CONTENT)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .build();
    }

    @GET
    public Response query(@QueryParam("q") String queryString) {
        List<String> result = index.related(queryString);
        return Response.ok(jsonSearsia(result))
            .header("Access-Control-Allow-Origin", "*")
            .header("Content-Type", "application/searsia+json")
            .build();
    }
    
}
