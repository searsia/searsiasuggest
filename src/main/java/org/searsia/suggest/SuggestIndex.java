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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make a suggestion index, and provide two search functions: autocomplete, and related.
 * @author Djoerd Hiemstra
 */
public class SuggestIndex {

    private static boolean EVAL = false;  // set this to true for autocomplete evaluation (-Dmaven.test.skip=true)

    private List<String> suggestionList;
    private List<String> normalizedList;
    private List<String> tagList;
    private List<Double> scoreList;
    private Map<String, Double> unigrams;
    private long sumOfUnigramF;
    private static Map<Character, Character> MAP_NORM;
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz";
    
    static {
        MAP_NORM = new HashMap<Character, Character>();
        MAP_NORM.put('ª', 'a');
        MAP_NORM.put('º', 'o');
        MAP_NORM.put('§', 's');
        MAP_NORM.put('³', '3');
        MAP_NORM.put('²', '2');
        MAP_NORM.put('¹', '1');
        MAP_NORM.put('à', 'a');
        MAP_NORM.put('á', 'a');
        MAP_NORM.put('â', 'a');
        MAP_NORM.put('ã', 'a');
        MAP_NORM.put('ä', 'a');
        MAP_NORM.put('è', 'e');
        MAP_NORM.put('é', 'e');
        MAP_NORM.put('ê', 'e');
        MAP_NORM.put('ë', 'e');
        MAP_NORM.put('í', 'i');
        MAP_NORM.put('ì', 'i');
        MAP_NORM.put('î', 'i');
        MAP_NORM.put('ï', 'i');
        MAP_NORM.put('ù', 'u');
        MAP_NORM.put('ú', 'u');
        MAP_NORM.put('û', 'u');
        MAP_NORM.put('ü', 'u');
        MAP_NORM.put('ò', 'o');
        MAP_NORM.put('ó', 'o');
        MAP_NORM.put('ô', 'o');
        MAP_NORM.put('õ', 'o');
        MAP_NORM.put('ö', 'o');
        MAP_NORM.put('ñ', 'n');
        MAP_NORM.put('ç', 'c');
    }

    public SuggestIndex(String fileName) throws IOException {
        this.suggestionList = new ArrayList<String>();
        this.normalizedList = new ArrayList<String>();
        this.tagList        = new ArrayList<String>();
        this.scoreList      = new ArrayList<Double>();
        this.unigrams       = new HashMap<String, Double>();
        this.sumOfUnigramF  = 1l;
        readSuggestionFile(fileName);
    }

    private String normalizeQuery(String query) {
        if (query == null) { return null; }
        StringBuilder builder = new StringBuilder();
        char c, prevc = ' ';
        Character newc;
        for(int i = 0; i < query.length(); i++) {
            c = Character.toLowerCase(query.charAt(i));
            newc = MAP_NORM.get(c);
            if (newc != null) { 
                c = newc;
            }
            if (EVAL || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || ( c == ' ' && prevc != ' ')) {
                builder.append(c);     
                prevc = c;
            }
        }
        return builder.toString();
    }
    
    // returns false if 'query' is not a valid unicode string
    private boolean isValidUnicode(String query) {
        char current; 
        if (query == null) return false;
        for (int i = 0; i < query.length(); i++) {
            current = query.charAt(i);
            if (current == '\uFFFD' ) { 
                return false;   // '\uFFFD' is the standard unicode replacement code 
            }
        }
        return true;
    }

    private void addToUniGrams(String query, double frequency) {
        String queryString = normalizeQuery(query);
        for (String term: queryString.split(" ")) {
            Double oldf = this.unigrams.get(term);
            if (oldf == null) {
                this.unigrams.put(term, frequency);
            } else {
                this.unigrams.put(term, oldf + frequency);
            }
        }
    }

    private void prepareData(String query, String tag, Double score) {
    	this.normalizedList.add(normalizeQuery(query));
        if (!EVAL) { // minimal data usage for evaluation
            this.suggestionList.add(query);
            this.tagList.add(tag);
            this.scoreList.add(score);
            addToUniGrams(query, score); // constant 1.0d, gives quite different results!
        }
    }
    
    private void readSuggestionFile(String fileString) throws IOException {
    	if (EVAL) {
    		System.err.println("Warning: Compiled in eval-only mode.");
    	}
    	Path path = Paths.get(fileString);
    	BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        long invalid = 0;
        try {
            String line;
            Double prevScore = null;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && line.length() > 0) {
                    String[] fields = line.split("\t");
                    Double thisScore = Double.parseDouble(fields[0]);
                    if (prevScore != null && prevScore < thisScore) {
                        throw new IOException("Scores must be ordered from high to low.");
                    }
                    prevScore = thisScore;
                    String query = fields[1];
                    String tag = "";
                    if (fields.length > 2) {
                    	tag = fields[2];
                    }
                    if (isValidUnicode(query)) {
                    	prepareData(query, tag, thisScore);
                    } else {
                        invalid++;
                    }
                }
            }
        }
        finally {
            reader.close();
        }
        if (invalid > 0) {
            System.err.println("Warning: ignored " + invalid + " malformed suggestions.");    
        }
        setUnigramSize();
    }

    private void setUnigramSize() {
        long unigramSize = 0;
        for (String term: this.unigrams.keySet()) {
            unigramSize += this.unigrams.get(term);
        }
        long suggestionSize = size();
        if (unigramSize > suggestionSize) {
            this.sumOfUnigramF = unigramSize;
        } else {
            this.sumOfUnigramF = suggestionSize;
        }
    }


    /**
     * Searches the index for suggestions that start with 'query'
     * @param query
     * @return list of query suggestions
     */
    public List<String> autocomplete(String query, String tag) {
        List<String> result = new ArrayList<String>();
        if (query == null) { return result; }
        if (tag != null && tag.length() > 1) { 
        	tag = tag.substring(0, 1);
        }
        String queryString = normalizeQuery(query);
        int nrSearched = 0;
        int nrFound = 0;
        for (String completion: this.normalizedList) {
            if (completion.startsWith(queryString)) {
            	String foundTag = this.tagList.get(nrSearched);
            	if (tag == null || foundTag.contains(tag)) {
                	if (EVAL) {
                        result.add(completion);
                	} else {
                        result.add(this.suggestionList.get(nrSearched));
                	}
                    if (++nrFound >= 10) { break; }            		
            	}
            }
            nrSearched++;
        }
        return result;
    }


    /**
     * Searches the index for suggestions that match with 'query'
     * @param query
     * @return list of suggestions
     */
    public List<String> related(String query) {
        List<String> result = new ArrayList<String>();
        if (query == null || query.equals("")) { return result; }
        TopSuggestions top = new TopSuggestions();
        Map<String, Double> queryTerms = new HashMap<String, Double>();
        String queryString = normalizeQuery(query);
        for (String term: queryString.split(" ")) { // put query terms in 'queryTerms'
            Double unigramFreq = this.unigrams.get(term);
            if (unigramFreq == null) { unigramFreq = 0.01; }
            queryTerms.put(term, unigramFreq);
        }
        int nrSearched = 0;
        int nrFound = 0;
        for (String suggestion: this.normalizedList) { // run through suggestions
            String[] suggestionTerms = suggestion.split(" ");
            int suggestionLength = suggestionTerms.length; 
            double score = 0;
            double indexSize = this.sumOfUnigramF * 0.2; // lambda=0.2 in language model
            for(String term: suggestionTerms) { // for each term
                Double unigramFreq = queryTerms.get(term);
                if (unigramFreq != null) {  // we got a match
                    score += Math.log(1 + (indexSize / (unigramFreq * suggestionLength)));
                }
            }
            if (score > 0 && !suggestion.equals(queryString)) {
                try {
                    Double prior = this.scoreList.get(nrSearched);
                    score += Math.log(prior);
                } catch (IndexOutOfBoundsException e) {
                    if (nrFound == 0) { System.err.println("Warning: Prior index unavailabe."); }
                }
                top.add(this.suggestionList.get(nrSearched), score);                
                if (nrFound++ > 9999) { break; } // TODO: smart early stop
            }
            nrSearched++;
        }
        return top.toList();
    }

    
    private TopSuggestions editDistanceOne(String term) {
        final double penalty = 0.01; // 0.053 =  0.05/0.95:  one in twenty is error
        double penalty2 = 1.0;
        TopSuggestions result = new TopSuggestions();
        Double score = this.unigrams.get(term);
        if (score == null) { score = 0.1; }
        result.add(term, score / this.sumOfUnigramF);
        int termLength = term.length();
        for (int i = 0; i < termLength; i++) {
            penalty2 = 1.0;
            if (i == 0) { penalty2 = 0.2; } // extra penalty for 1st letter
            char current = term.charAt(i);
            String prefix  = term.substring(0, i);
            String prefixC = prefix + current; 
            String postfix = term.substring(i + 1, termLength);
            String corrected = prefix + postfix;  // deletion
            score = this.unigrams.get(corrected);
            if (score != null) { result.add(corrected, score * penalty * penalty2 / this.sumOfUnigramF); }
            for (int j = 0; j < CHARS.length(); j++) {
                char c = CHARS.charAt(j);
                corrected = prefixC + CHARS.charAt(j) + postfix; // insertion (there is no insertion at the front)
                score = this.unigrams.get(corrected);
                if (score != null) { result.add(corrected, score * penalty / this.sumOfUnigramF); }
                if (current != c) {
                    corrected = prefix + CHARS.charAt(j) + postfix; // substitution
                    score = this.unigrams.get(corrected);
                    if (score != null) {
                    	result.add(corrected, score * penalty * penalty2 / this.sumOfUnigramF); 
                    }
                }
            }
        }
        return result;
    }

    /**
     * Spelling correction. TODO: bigrams / edit distance > 1
     * @param query
     * @return
     */
    public String spellcorrect(String query) {
        if (query == null) return null;
        query = query.replaceAll(" +$", "");
        String queryString = normalizeQuery(query.replaceAll("[\\.\\-_\"\']", " "));
        StringBuilder newQuery = new StringBuilder(""); 
        for (String term: queryString.split(" ")) {
            if (newQuery.length() > 0) { newQuery.append(" "); }        	
        	if (term.length() > 2) {
            	TopSuggestions top = editDistanceOne(term);
                newQuery.append(top.getBest());
        	} else {
        		newQuery.append(term);
        	}
        }
        String newQueryString = newQuery.toString();
        if (newQueryString.equals(queryString)) {
        	return null;
        } else {
            return newQueryString;
        }
    }
    
    /**
     * Returns number of suggestions in the index.
     * @return size
     */
    public long size() {
        return this.normalizedList.size();
    }
    
    
}
