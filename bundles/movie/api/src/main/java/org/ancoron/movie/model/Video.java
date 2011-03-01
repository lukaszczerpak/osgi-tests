/*
 * Copyright 2011 ancoron.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ancoron.movie.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.ancoron.common.model.Identifiable;

/**
 *
 * @author ancoron
 */
public interface Video extends Identifiable {
    
    public Locale getOriginalLanguage();

    public String getTitle();
    
    public Locale getDefaultTitleLanguage();

    public String getTitle(Locale language);

    public Map<Locale, String> getTitleMap();
    
    public List<Figure> getCharacters();
    
    public List<Director> getDirectors();
}
