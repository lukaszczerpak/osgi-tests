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
import java.util.List;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.ancoron.common.persistence.AbstractIdentifiable;
import org.ancoron.movie.persistence.model.CharacterEntity;
import org.ancoron.movie.persistence.model.DirectorEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="mov_video")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="c_type", discriminatorType=DiscriminatorType.CHAR)
@NamedQueries({
    @NamedQuery(
        name=VideoEntityImpl.FIND_ALL,
        query="SELECT v FROM VideoEntityImpl v ORDER BY v.id")
})
public abstract class VideoEntityImpl extends AbstractIdentifiable implements VideoEntity, Serializable {
    
    public final static String FIND_ALL = "video.findAll";

    @Column(name="c_default_title_lang")
    private String defaultTitleLanguage;

    /**
     * Get the value of defaultTitleLanguage
     *
     * @return the value of defaultTitleLanguage
     */
    @Override
    public String getDefaultTitleLanguage() {
        return defaultTitleLanguage;
    }

    /**
     * Set the value of defaultTitleLanguage
     *
     * @param defaultTitleLanguage new value of defaultTitleLanguage
     */
    public void setDefaultTitleLanguage(String defaultTitleLanguage) {
        this.defaultTitleLanguage = defaultTitleLanguage;
    }

    @ElementCollection
    @MapKeyColumn(name="c_lang")
    @Column(name="c_title")
    @CollectionTable(name="mov_title_map", joinColumns=@JoinColumn(name="fk_video"))
    private Map<String, String> titleMap;

    /**
     * Get the value of titleMap
     *
     * @return the value of titleMap
     */
    @Override
    public Map<String, String> getTitleMap() {
        return titleMap;
    }

    /**
     * Set the value of titleMap
     *
     * @param titleMap new value of titleMap
     */
    public void setTitleMap(Map<String, String> titleMap) {
        this.titleMap = titleMap;
    }

    @OneToMany(mappedBy="video", targetEntity=CharacterEntityImpl.class)
    @OrderColumn(name="c_index")
    private List<CharacterEntity> characters;

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

    @ManyToMany(mappedBy="videos", targetEntity=DirectorEntityImpl.class)
    @OrderColumn(name="c_index")
    private List<DirectorEntity> directors;

    /**
     * Get the value of directors
     *
     * @return the value of directors
     */
    @Override
    public List<DirectorEntity> getDirectors() {
        return directors;
    }

    /**
     * Set the value of directors
     *
     * @param directors new value of directors
     */
    public void setDirectors(List<DirectorEntity> directors) {
        this.directors = directors;
    }
}
