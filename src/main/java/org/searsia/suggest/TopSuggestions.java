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

import java.util.ArrayList;
import java.util.List;


/**
 * Ranked list class
 * Acts like a fixed size priority queue: keeps the top max
 */
public class TopSuggestions {
    int size;
    int max;
    String[] query;
    Double[] score;
    
    public TopSuggestions(int max) {
    	this.size = 0;
    	this.max = max;
    	this.query = new String[max];
    	this.score = new Double[max];
    }
    
    public TopSuggestions() {
    	this(10);
    }
    
    public void add(String query, Double score) {
        int i = this.size;
        while (i > 0 && this.score[i-1] < score ) { 
            if (i < max) {
                this.query[i] = this.query[i-1];
                this.score[i] = this.score[i-1];
            }
            i--;
        }
        if (i < max) {
            this.query[i] = query;
            this.score[i] = score;
            if (this.size < max) { this.size++; }
        }
    }
    
    public List<String> toList() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < this.size; i++) {
            result.add(this.query[i]);
        }
        return result;
    }
    
    public String getBest() {
    	if (this.size > 0) {
    		return this.query[0];
    	} else {
    		return null;
    	}
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.size; i++) {
            if (i > 0) { builder.append(", "); }
            builder.append(this.query[i]);
            builder.append("=");
            builder.append(this.score[i]);
        }
        return builder.toString();
    }
}
