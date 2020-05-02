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

import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;


/**
 * Searsia Main class
 * 
 * Start as:  java -jar target/searsiasuggestions.jar -f <completions-file>
 * All interesting code is in SuggestionIndex.java
 * 
 * @author Djoerd Hiemstra and Dolf Trieschnigg
 * 
 */
public class Main {
    
	private static final String defaultUrl = "http://localhost:8088/suggestions";
	
	private static void exitWithHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Suggestions", options);
        System.exit(1);
	}
	
    private static CommandLine getOptions(String[] args) {
    	Options options = new Options();
        options.addOption("f", "file", true,  "File with suggestions.");
        options.addOption("u", "url",  true,  "Set url of web service endpoint. (default: '" + defaultUrl + "')");
        options.addOption("p", "proxy", true, "Set reverse proxy url of web service endpoint.");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
        	exitWithHelp(options);
        }
        if (!cmd.hasOption("f")) { // mandatory option
        	exitWithHelp(options);
        }
        return cmd;
    }

    /**
     * Start the suggestions application
     * @param args
     */
    public static void main(String[] args) {
        String myFile  = null;
        String myUrl   = defaultUrl; // local url
        String myProxy = null;       // optionally: reverse proxy url

        String encoding = System.getProperties().getProperty("file.encoding");
        if (encoding == null || !encoding.equals("UTF-8")) {
            System.err.println("Warning: Unknown encoding. Set JVM encoding with '-Dfile.encoding=UTF-8'");
        }
        
        CommandLine cmd = getOptions(args);
        myFile  = cmd.getOptionValue("f");
        if (cmd.hasOption("u")) {
            myUrl  = cmd.getOptionValue("u");
        }
        if (cmd.hasOption("p")) {
        	myProxy = cmd.getOptionValue("p");
        }
        
        SuggestIndex index = null;
        HttpServer server = null;
    	try {
            index = new SuggestIndex(myFile);
            System.err.println("Searsia Suggest Version 0.1.3");
            System.err.println("Created index of " + index.size() + " suggestions");
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(myUrl), new SuggestApp(index, myProxy));
    	} catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
    	}
    	System.err.println("URL Templates: " + myUrl + "/autocomplete?q={searchTerms}&t={tag}&f={format}");
    	System.err.println("               " + myUrl + CallRelated.getRequest);
    	System.err.println("               " + myUrl + CallSpellcorrect.getRequest);

        try {
            while(true) {
                Thread.sleep(8000);
            }
        } catch (InterruptedException e) {  }

        server.shutdownNow();
    }

    
    
}
