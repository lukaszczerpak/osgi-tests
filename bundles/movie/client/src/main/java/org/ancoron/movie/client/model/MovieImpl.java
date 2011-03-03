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
import java.util.Locale;
import org.ancoron.movie.model.Movie;

/**
 *
 * @author ancoron
 */
public class MovieImpl extends VideoImpl implements Movie, Serializable {
    
    public MovieImpl() {}
    
    public MovieImpl(String defaultTitle, Locale defaultLocale) {
        setDefaultTitleLanguage(defaultLocale);
        getTitleMap().put(defaultLocale, defaultTitle);
    }

    private String iMDBId;

    /**
     * Get the value of iMDBId
     *
     * @return the value of iMDBId
     */
    @Override
    public String getIMDBId() {
        return iMDBId;
    }

    /**
     * Set the value of iMDBId
     *
     * @param iMDBId new value of iMDBId
     */
    public void setIMDBId(String iMDBId) {
        this.iMDBId = iMDBId;
    }
}
