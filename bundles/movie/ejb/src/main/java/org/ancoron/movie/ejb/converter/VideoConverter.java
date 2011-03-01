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

package org.ancoron.movie.ejb.converter;

import java.util.HashMap;
import java.util.Locale;
import javax.inject.Named;
import org.ancoron.common.model.Person;
import org.ancoron.common.service.ServiceAccessException;
import org.ancoron.common.utils.ServiceHelper;
import org.ancoron.movie.client.model.ActorImpl;
import org.ancoron.movie.client.model.CharacterImpl;
import org.ancoron.movie.client.model.DirectorImpl;
import org.ancoron.movie.client.model.MovieImpl;
import org.ancoron.movie.client.model.PersonImpl;
import org.ancoron.movie.client.model.VideoImpl;
import org.ancoron.movie.jpa.entity.ActorEntityImpl;
import org.ancoron.movie.jpa.entity.CharacterEntityImpl;
import org.ancoron.movie.jpa.entity.DirectorEntityImpl;
import org.ancoron.movie.jpa.entity.MovieEntityImpl;
import org.ancoron.movie.jpa.entity.PersonEntityImpl;
import org.ancoron.movie.jpa.entity.VideoEntityImpl;
import org.ancoron.movie.model.Actor;
import org.ancoron.movie.model.Director;
import org.ancoron.movie.model.Figure;
import org.ancoron.movie.model.Movie;
import org.ancoron.movie.model.Video;
import org.ancoron.movie.persistence.MoviePersistenceException;
import org.ancoron.movie.persistence.MoviePersistenceService;
import org.ancoron.movie.persistence.model.ActorEntity;
import org.ancoron.movie.persistence.model.CharacterEntity;
import org.ancoron.movie.persistence.model.DirectorEntity;
import org.ancoron.movie.persistence.model.MovieEntity;
import org.ancoron.movie.persistence.model.PersonEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
@Named("VideoConverter")
public class VideoConverter {
    
    private static final Locale defaultLang = Locale.ENGLISH;
    
    public MoviePersistenceService getService() throws ServiceAccessException {
        return ServiceHelper.lookup(MoviePersistenceService.class, null);
    }
    
    public VideoEntity toEntity(Video video) throws MoviePersistenceException, ServiceAccessException {
        VideoEntityImpl entity = null;
        
        if(video != null) {
            if(video.getId() != null) {
                entity = (VideoEntityImpl) getService().getVideo(video.getId());
            }

            if(entity == null) {
                if(video instanceof Movie) {
                    entity = new MovieEntityImpl();
                } else {
                    throw new IllegalArgumentException("Cannot handle video of type " + video.getClass());
                }
            }
            
            entity.setDefaultTitleLanguage(video.getDefaultTitleLanguage().getLanguage());
            
            if(!video.getTitleMap().isEmpty()) {
                entity.setTitleMap(new HashMap<String, String>());
                for(Locale loc : video.getTitleMap().keySet()) {
                    entity.getTitleMap().put(loc.getLanguage(),
                            video.getTitle(loc));
                }
            } else if(!entity.getTitleMap().isEmpty()) {
                // clear the persistent state...
                entity.getTitleMap().clear();
            }

            if(!video.getCharacters().isEmpty()) {
                entity.getCharacters().clear();
                
                for(Figure f : video.getCharacters()) {
                    CharacterEntityImpl ch = (CharacterEntityImpl) toEntity(f);
                    entity.getCharacters().add(ch);
                    ch.setVideo(entity);
                }
            }

            if(!video.getDirectors().isEmpty()) {
                // rebuild the list...
                entity.getDirectors().clear();
                
                for(Director d : video.getDirectors()) {
                    DirectorEntityImpl dir = (DirectorEntityImpl) toEntity(d);
                    entity.getDirectors().add(dir);
                    dir.getVideos().add(entity);
                }
            } else if(!entity.getDirectors().isEmpty()) {
                // if some relationship existed we must remove it...
                for(DirectorEntity d : entity.getDirectors()) {
                    DirectorEntityImpl dir = (DirectorEntityImpl) d;
                    dir.getVideos().remove(entity);
                    entity.getDirectors().clear();
                }
            }
        }
        
        return entity;
    }
    
    public CharacterEntity toEntity(Figure character) throws MoviePersistenceException, ServiceAccessException {
        CharacterEntityImpl entity = null;
        
        if(character != null) {
            if(character.getId() != null) {
                entity = (CharacterEntityImpl) getService().getCharacter(character.getId());
            }
            
            if(entity == null) {
                entity = new CharacterEntityImpl();
            }
            
            entity.setName(character.getName());
            
            if(character.getActor() != null) {
                final ActorEntityImpl act = (ActorEntityImpl) toEntity(character.getActor());
                if(!act.getCharacters().contains(entity)) {
                    act.getCharacters().add(entity);
                }
                entity.setActor(act);
            } else if(entity.getActor() != null) {
                // if some relationship existed we must remove it...
                final ActorEntityImpl act = (ActorEntityImpl) entity.getActor();
                if(act.getCharacters() != null) {
                    act.getCharacters().remove(entity);
                }
                
                // dereference...
                entity.setActor(null);
            }
        }
        
        return entity;
    }
    
    public ActorEntity toEntity(Actor actor) throws ServiceAccessException, MoviePersistenceException {
        ActorEntityImpl entity = null;
        
        if(actor != null) {
            if(actor.getId() != null) {
                entity = (ActorEntityImpl) getService().getActor(actor.getId());
            }
            
            if(entity == null) {
                entity = new ActorEntityImpl();
            }

            fillPersonEntity(entity, actor);
        }
        
        return entity;
    }

    private void fillPersonEntity(final PersonEntityImpl entity, final Person person) {
        entity.setForename(person.getForename());
        entity.setSurname(person.getSurname());
        entity.setGender(person.getGender());
    }
    
    public DirectorEntity toEntity(Director dir) throws ServiceAccessException, MoviePersistenceException {
        DirectorEntityImpl entity = null;
        
        if(dir != null) {
            if(dir.getId() != null) {
                entity = (DirectorEntityImpl) getService().getDirector(dir.getId());
            }
            
            if(entity == null) {
                entity = new DirectorEntityImpl();
            }

            fillPersonEntity(entity, dir);
        }
        
        return entity;
    }

    public Video fromEntity(VideoEntity entity) {
        VideoImpl video = null;
        
        if(entity != null) {
            if(entity instanceof MovieEntity) {
                video = new MovieImpl();
            } else {
                throw new UnsupportedOperationException("Unsupported entity of type "
                        + entity.getClass().getName());
            }

            video.setId(entity.getId());
            
            if(entity.getDefaultTitleLanguage() != null) {
                video.setDefaultTitleLanguage(new Locale(entity.getDefaultTitleLanguage()));
            }
            
            if(!entity.getTitleMap().isEmpty()) {
                for(String loc : entity.getTitleMap().keySet()) {
                    video.getTitleMap().put(new Locale(loc), entity.getTitleMap().get(loc));
                }
            }
            
            if(!entity.getDirectors().isEmpty()) {
                for(DirectorEntity dir : entity.getDirectors()) {
                    video.getDirectors().add(fromEntity(dir));
                }
            }
            
            if(!entity.getCharacters().isEmpty()) {
                for(CharacterEntity ch : entity.getCharacters()) {
                    video.getCharacters().add(fromEntity(ch));
                }
            }
        }
        
        return video;
    }

    public Director fromEntity(DirectorEntity entity) {
        DirectorImpl dir = null;
        
        if(entity != null) {
            dir = new DirectorImpl();
            fillPersonClient(dir, entity);
        }
        
        return dir;
    }

    private void fillPersonClient(final PersonImpl person, final PersonEntity entity) {
        person.setId(entity.getId());
        person.setForename(entity.getForename());
        person.setSurname(entity.getSurname());
        person.setGender(entity.getGender());
    }

    private Figure fromEntity(CharacterEntity entity) {
        CharacterImpl ch = null;
        
        if(entity != null) {
            ch = new CharacterImpl();
            ch.setId(entity.getId());
            ch.setActor(fromEntity(entity.getActor()));
            ch.setName(entity.getName());
        }
        
        return ch;
    }

    private Actor fromEntity(ActorEntity entity) {
        ActorImpl actor = null;
        
        if(entity != null) {
            actor = new ActorImpl();
            fillPersonClient(actor, entity);
        }
        
        return actor;
    }
}
