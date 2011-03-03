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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.ancoron.movie.persistence.model.DirectorEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
@Entity
@Table(name="mov_director")
@DiscriminatorValue("D")
public class DirectorEntityImpl extends PersonEntityImpl implements DirectorEntity, Serializable {

    @ManyToMany(targetEntity=VideoEntityImpl.class)
    @JoinTable(name="mov_video_directors",
        joinColumns={@JoinColumn(name="fk_director", referencedColumnName="c_id")},
        inverseJoinColumns={@JoinColumn(name="fk_video", referencedColumnName="c_id")})
    private List<VideoEntity> videos = new ArrayList<VideoEntity>();

    /**
     * Get the value of videos
     *
     * @return the value of videos
     */
    public List<VideoEntity> getVideos() {
        return videos;
    }

    /**
     * Set the value of videos
     *
     * @param videos new value of videos
     */
    public void setVideos(List<VideoEntity> videos) {
        this.videos = videos;
    }
}
