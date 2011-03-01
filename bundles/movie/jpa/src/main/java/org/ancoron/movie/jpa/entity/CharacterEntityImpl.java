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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.ancoron.common.persistence.AbstractIdentifiable;
import org.ancoron.movie.persistence.model.ActorEntity;
import org.ancoron.movie.persistence.model.CharacterEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="mov_char")
public class CharacterEntityImpl extends AbstractIdentifiable implements CharacterEntity, Serializable {

    @Column(name="c_name")
    private String name;

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(targetEntity=ActorEntityImpl.class)
    @JoinColumn(name="fk_actor")
    private ActorEntity actor;

    /**
     * Get the value of actor
     *
     * @return the value of actor
     */
    @Override
    public ActorEntity getActor() {
        return actor;
    }

    /**
     * Set the value of actor
     *
     * @param actor new value of actor
     */
    public void setActor(ActorEntity actor) {
        this.actor = actor;
    }

    @ManyToOne(targetEntity=VideoEntityImpl.class)
    @JoinColumn(name="fk_video")
    private VideoEntity video;

    /**
     * Get the value of video
     *
     * @return the value of video
     */
    @Override
    public VideoEntity getVideo() {
        return video;
    }

    /**
     * Set the value of video
     *
     * @param video new value of video
     */
    public void setVideo(VideoEntity video) {
        this.video = video;
    }
}
