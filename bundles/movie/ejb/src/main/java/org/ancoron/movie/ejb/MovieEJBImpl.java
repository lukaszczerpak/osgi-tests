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

package org.ancoron.movie.ejb;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.ancoron.common.service.ServiceAccessException;
import org.ancoron.movie.MovieService;
import org.ancoron.movie.MovieServiceException;
import org.ancoron.movie.ejb.converter.VideoConverter;
import org.ancoron.movie.model.Video;
import org.ancoron.movie.persistence.MoviePersistenceException;
import org.ancoron.movie.persistence.MoviePersistenceService;
import org.ancoron.movie.persistence.model.VideoEntity;
import org.glassfish.osgicdi.OSGiService;

/**
 *
 * @author ancoron
 */
@Stateless(name="MovieEJBServiceBean")
@Local
public class MovieEJBImpl implements MovieService {
    
    @Inject
    @OSGiService(dynamic=true)
    private MoviePersistenceService persistence;
    
    @Inject
    private VideoConverter converter;

    @Override
    public Long saveVideo(Video video) throws MovieServiceException {
        Long id = null;
        try {
            VideoEntity entity = converter.toEntity(video);
            
            id = persistence.saveVideo(entity);
        } catch (MoviePersistenceException ex) {
            throw new MovieServiceException("Unable to convert video into an entity", ex);
        } catch (ServiceAccessException ex) {
            throw new MovieServiceException("Unable to convert into an entity", ex);
        }
        
        return id;
    }

    @Override
    public Video getVideo(Long id) throws MovieServiceException {
        Video video = null;
        VideoEntity entity = null;
        try {
            entity = persistence.getVideo(id);
        } catch (MoviePersistenceException ex) {
            throw new MovieServiceException("Unable to load video entity", ex);
        }
        
        if(entity != null) {
            video = converter.fromEntity(entity);
        }
        
        return video;
    }

    @Override
    public List<Video> getAllVideos() throws MovieServiceException {
        List<Video> videos = new ArrayList<Video>();
        List<VideoEntity> entities = null;

        try {
            entities = persistence.getAllVideos();
        } catch (MoviePersistenceException ex) {
            throw new MovieServiceException("Unable to load video entity", ex);
        }
        
        if(entities != null) {
            for(VideoEntity tmp : entities) {
                videos.add(converter.fromEntity(tmp));
            }
        }
        
        return videos;
    }
}
