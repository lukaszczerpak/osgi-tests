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

package org.ancoron.movie.jpa.impl;

import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.ancoron.common.model.Identifiable;
import org.ancoron.movie.jpa.MovieJPAService;
import org.ancoron.movie.jpa.entity.ActorEntityImpl;
import org.ancoron.movie.jpa.entity.CharacterEntityImpl;
import org.ancoron.movie.jpa.entity.DirectorEntityImpl;
import org.ancoron.movie.jpa.entity.MovieEntityImpl;
import org.ancoron.movie.jpa.entity.VideoEntityImpl;
import org.ancoron.movie.persistence.MoviePersistenceException;
import org.ancoron.movie.persistence.MoviePersistenceService;
import org.ancoron.movie.persistence.model.ActorEntity;
import org.ancoron.movie.persistence.model.CharacterEntity;
import org.ancoron.movie.persistence.model.DirectorEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
@Stateless(name="MovieJPAServiceBean")
@Local
public class MovieJPAServiceImpl implements MoviePersistenceService, MovieJPAService {
    
    @PersistenceContext(name="MovieUnit")
    private EntityManager em;
    
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    protected <T extends Identifiable> T save(T entity) throws MoviePersistenceException {
        try {
            return em.merge(entity);
        } catch(Exception x) {
            throw new MoviePersistenceException("Unable to store "
                    + entity.getClass().getSimpleName() + " " + entity, x);
        }
    }

    protected <T extends Identifiable> T get(Long id, Class<T> clazz) throws MoviePersistenceException {
        try {
            return em.find(clazz, id);
        } catch(Exception x) {
            throw new MoviePersistenceException("Unable to find "
                    + clazz.getSimpleName() + " for ID " + id, x);
        }
    }
    
    @Override
    public Long saveVideo(VideoEntity video) throws MoviePersistenceException {
        return save(video).getId();
    }

    @Override
    public VideoEntity getVideo(Long id) throws MoviePersistenceException {
        VideoEntity entity = get(id, MovieEntityImpl.class);
        
        if(entity == null) {
            entity = get(id, VideoEntityImpl.class);
        }
        
        return entity;
    }

    @Override
    public List<VideoEntity> getAllVideos() throws MoviePersistenceException {
        List<VideoEntity> videos = null;
        try {
            videos = em.createNamedQuery(VideoEntityImpl.FIND_ALL).getResultList();
        } catch(Exception x) {
            throw new MoviePersistenceException("Unable to execute named query", x);
        }
        return videos;
    }

    @Override
    public CharacterEntity getCharacter(Long id) throws MoviePersistenceException {
        return get(id, CharacterEntityImpl.class);
    }

    @Override
    public ActorEntity getActor(Long id) throws MoviePersistenceException {
        return get(id, ActorEntityImpl.class);
    }

    @Override
    public DirectorEntity getDirector(Long id) throws MoviePersistenceException {
        return get(id, DirectorEntityImpl.class);
    }
}
