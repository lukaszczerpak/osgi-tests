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
import javax.persistence.Column;
import org.ancoron.movie.model.Actor;
import org.ancoron.movie.model.Figure;
import org.ancoron.movie.model.Video;

/**
 *
 * @author ancoron
 */
public class CharacterImpl extends AbstractIdentifiable implements Figure, Serializable {

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

    private Actor actor;

    /**
     * Get the value of actor
     *
     * @return the value of actor
     */
    @Override
    public Actor getActor() {
        return actor;
    }

    /**
     * Set the value of actor
     *
     * @param actor new value of actor
     */
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    private Video video;

    /**
     * Get the value of video
     *
     * @return the value of video
     */
    @Override
    public Video getVideo() {
        return video;
    }

    /**
     * Set the value of video
     *
     * @param video new value of video
     */
    public void setVideo(Video video) {
        this.video = video;
    }
}
