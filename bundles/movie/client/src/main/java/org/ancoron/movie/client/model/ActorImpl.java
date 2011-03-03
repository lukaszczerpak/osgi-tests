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
import java.util.ArrayList;
import java.util.List;
import org.ancoron.common.model.Gender;
import org.ancoron.movie.model.Actor;
import org.ancoron.movie.model.Figure;

/**
 *
 * @author ancoron
 */
public class ActorImpl extends PersonImpl implements Actor, Serializable {
    
    public ActorImpl() {}
    
    public ActorImpl(String forename, String surname, Gender gender) {
        setForename(forename);
        setSurname(surname);
        setGender(gender);
    }

    private List<Figure> characters = new ArrayList<Figure>();

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
}
