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

package org.ancoron.movie.persistence;

import java.util.List;
import org.ancoron.movie.persistence.model.ActorEntity;
import org.ancoron.movie.persistence.model.CharacterEntity;
import org.ancoron.movie.persistence.model.DirectorEntity;
import org.ancoron.movie.persistence.model.VideoEntity;

/**
 *
 * @author ancoron
 */
public interface MoviePersistenceService {

    public Long saveVideo(VideoEntity video) throws MoviePersistenceException;

    public VideoEntity getVideo(Long id) throws MoviePersistenceException;

    public CharacterEntity getCharacter(Long id) throws MoviePersistenceException;

    public ActorEntity getActor(Long id) throws MoviePersistenceException;

    public DirectorEntity getDirector(Long id) throws MoviePersistenceException;

    public List<VideoEntity> getAllVideos() throws MoviePersistenceException;
}
