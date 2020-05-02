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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Searsia Autocomplete application.
 * 
 * @author Dolf Trieschnigg and Djoerd Hiemstra
 */
public class SuggestApp extends ResourceConfig {

    public SuggestApp(SuggestIndex index, String myProxyUrl) throws IOException {
        super();
        Logger.getLogger("org.glassfish.grizzly").setLevel(Level.WARNING);
        register(new CallAutocomplete(index));
        register(new CallRelated(index, myProxyUrl));
        register(new CallSpellcorrect(index, myProxyUrl));
    }
	
}
