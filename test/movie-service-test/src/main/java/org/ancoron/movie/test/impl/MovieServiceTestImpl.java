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

package org.ancoron.movie.test.impl;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancoron.common.model.Gender;
import org.ancoron.common.utils.ServiceHelper;
import org.ancoron.movie.MovieService;
import org.ancoron.movie.client.model.ActorImpl;
import org.ancoron.movie.client.model.CharacterImpl;
import org.ancoron.movie.client.model.DirectorImpl;
import org.ancoron.movie.client.model.MovieImpl;
import org.ancoron.movie.model.Video;
import org.ancoron.movie.test.MovieServiceTest;

/**
 *
 * @author ancoron
 */
public class MovieServiceTestImpl implements MovieServiceTest {
    
    private static final Logger log = Logger.getLogger(MovieServiceTestImpl.class.getPackage().getName());

    @Override
    public void test() throws Exception {
        MovieService svc = ServiceHelper.lookup(MovieService.class, null);

        MovieImpl m = new MovieImpl("Black Swan", Locale.ENGLISH);
        m.setOriginalLanguage(Locale.ENGLISH);

        DirectorImpl dir = new DirectorImpl("Darren", "Aronofsky", Gender.MALE);
        dir.getVideos().add(m);
        m.getDirectors().add(dir);

        ActorImpl n = new ActorImpl("Natalie", "Portman", Gender.FEMALE);
        CharacterImpl nc = new CharacterImpl("Nina Sayers");
        nc.setActor(n);
        n.getCharacters().add(nc);
        nc.setVideo(m);
        m.getCharacters().add(nc);

        ActorImpl ma = new ActorImpl("Mila", "Kunis", Gender.FEMALE);
        CharacterImpl mc = new CharacterImpl("Lily");
        mc.setActor(ma);
        ma.getCharacters().add(mc);
        mc.setVideo(m);
        m.getCharacters().add(mc);

        ActorImpl va = new ActorImpl("Vincent", "Cassel", Gender.MALE);
        CharacterImpl vc = new CharacterImpl("Thomas Leroy");
        vc.setActor(va);
        va.getCharacters().add(vc);
        vc.setVideo(m);
        m.getCharacters().add(vc);

        Long id = svc.saveVideo(m);

        if(id == null) {
            throw new IllegalStateException("Saved movie didn't get an ID assigned");
        }

        Video v = svc.getVideo(id);
        
        log.log(Level.INFO, "Movie {0} has been saved with ID {1}",
                new Object[]{v.getTitle(), id.toString()});
        
        if(v.getCharacters().size() != 3) {
            throw new IllegalStateException("Not all characters have been assigned");
        }
    }
}
