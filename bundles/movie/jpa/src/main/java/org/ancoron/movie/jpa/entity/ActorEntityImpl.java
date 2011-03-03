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

package org.ancoron.movie.jpa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.ancoron.movie.persistence.model.ActorEntity;
import org.ancoron.movie.persistence.model.CharacterEntity;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="mov_actor")
@DiscriminatorValue("A")
public class ActorEntityImpl extends PersonEntityImpl implements ActorEntity, Serializable {

    @OneToMany(targetEntity=CharacterEntityImpl.class, mappedBy="actor")
    private List<CharacterEntity> characters = new ArrayList<CharacterEntity>();

    /**
     * Get the value of characters
     *
     * @return the value of characters
     */
    @Override
    public List<CharacterEntity> getCharacters() {
        return characters;
    }

    /**
     * Set the value of characters
     *
     * @param characters new value of characters
     */
    public void setCharacters(List<CharacterEntity> characters) {
        this.characters = characters;
    }
}
