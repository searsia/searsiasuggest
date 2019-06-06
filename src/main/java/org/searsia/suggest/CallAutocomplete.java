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
@Path("autocomplete")
public class CallAutocomplete {

    private SuggestIndex index;
	
	public CallAutocomplete(SuggestIndex index) throws IOException {
		this.index = index;
	}

	private String jsonOpensearch(String queryString, List<String> result) {
	    if (queryString == null) { queryString = ""; }
	    JSONArray json = new JSONArray();
	    json.put(queryString);
        json.put(result);
        return json.toString();
	}


    private String jsonSearsia(List<String> result) {
        JSONObject json = new JSONObject();
        JSONArray hits = new JSONArray();
        for (String suggestion: result) {
            hits.put(new JSONObject().put("title", suggestion).put("tags", "#small #suggestion"));
        }
        json.put("hits", hits);
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
    public Response query(@QueryParam("q") String queryString, 
    		              @QueryParam("f") String format,
    		              @QueryParam("t") String tag) {
        List<String> result = index.autocomplete(queryString, tag);
        if (format != null && format.equals("opensearch")) {
            return Response.ok(jsonOpensearch(queryString, result))
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Content-Type", "application/x-suggestions+json")
                    .build();
        } else {
            return Response.ok(jsonSearsia(result))
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Content-Type", "application/searsia+json")
                    .build();
        }
    }
	
}
