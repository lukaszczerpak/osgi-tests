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
import org.ancoron.movie.model.Director;
import org.ancoron.movie.model.Video;

/**
 *
 * @author ancoron
 */
public class DirectorImpl extends PersonImpl implements Director, Serializable {

    public DirectorImpl() {}
    
    public DirectorImpl(String forename, String surname, Gender gender) {
        setForename(forename);
        setSurname(surname);
        setGender(gender);
    }

    private List<Video> videos = new ArrayList<Video>();

    /**
     * Get the value of videos
     *
     * @return the value of videos
     */
    public List<Video> getVideos() {
        return videos;
    }

    /**
     * Set the value of videos
     *
     * @param videos new value of videos
     */
    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
}
