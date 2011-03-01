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

package org.ancoron.movie.client.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.ancoron.movie.model.Director;
import org.ancoron.movie.model.Figure;
import org.ancoron.movie.model.Video;

/**
 *
 * @author ancoron
 */
public abstract class VideoImpl extends AbstractIdentifiable implements Video, Serializable {
    
    private Locale defaultTitleLanguage;

    /**
     * Get the value of defaultTitleLanguage
     *
     * @return the value of defaultTitleLanguage
     */
    @Override
    public Locale getDefaultTitleLanguage() {
        return defaultTitleLanguage;
    }

    /**
     * Set the value of defaultTitleLanguage
     *
     * @param defaultTitleLanguage new value of defaultTitleLanguage
     */
    public void setDefaultTitleLanguage(Locale defaultTitleLanguage) {
        this.defaultTitleLanguage = defaultTitleLanguage;
    }
    
    private Locale originalLanguage;

    /**
     * Get the value of originalLanguage
     *
     * @return the value of originalLanguage
     */
    @Override
    public Locale getOriginalLanguage() {
        return originalLanguage;
    }

    /**
     * Set the value of originalLanguage
     *
     * @param originalLanguage new value of originalLanguage
     */
    public void setOriginalLanguage(Locale originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    @Override
    public String getTitle() {
        return getTitle(getDefaultTitleLanguage());
    }

    @Override
    public String getTitle(Locale locale) {
        return getTitleMap().get(locale);
    }
    
    private Map<Locale, String> titleMap = new HashMap<Locale, String>(3);

    /**
     * Get the value of titleMap
     *
     * @return the value of titleMap
     */
    @Override
    public Map<Locale, String> getTitleMap() {
        return titleMap;
    }

    /**
     * Set the value of titleMap
     *
     * @param titleMap new value of titleMap
     */
    public void setTitleMap(Map<Locale, String> titleMap) {
        this.titleMap = titleMap;
    }

    private List<Figure> characters;

    /**
     * Get the value of characters
     *
     * @return the value of characters
     */
    @Override
    public List<Figure> getCharacters() {
        return characters;
    }

    /**
     * Set the value of characters
     *
     * @param characters new value of characters
     */
    public void setCharacters(List<Figure> characters) {
        this.characters = characters;
    }

    private List<Director> directors;

    /**
     * Get the value of directors
     *
     * @return the value of directors
     */
    @Override
    public List<Director> getDirectors() {
        return directors;
    }

    /**
     * Set the value of directors
     *
     * @param directors new value of directors
     */
    public void setDirectors(List<Director> directors) {
        this.directors = directors;
    }
}
