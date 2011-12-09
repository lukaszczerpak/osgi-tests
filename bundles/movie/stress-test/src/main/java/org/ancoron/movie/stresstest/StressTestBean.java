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

package org.ancoron.movie.stresstest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import org.ancoron.movie.MovieService;
import org.ancoron.movie.MovieServiceException;
import org.glassfish.osgicdi.OSGiService;

/**
 *
 * @author ancoron
 */
@Singleton
@Startup
public class StressTestBean {
    
    private static final ExecutorService exec = Executors.newCachedThreadPool();
    
    @Inject
    @OSGiService(dynamic=true)
    private MovieService svc;
    
    @Resource
    private TimerService timers;

    @PostConstruct
    protected void init() {
        timers.createSingleActionTimer(3000, new TimerConfig());
    }
    
    @Timeout
    protected void timeout() {
        final int num = 10000;
        
        for(int i=0; i<num; i++) {
            final int tmp = i;
            exec.execute(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Running task #" + tmp);
                    try {
                        svc.getVideo(Long.valueOf(Math.round(Math.random() * Long.MAX_VALUE)));
                    } catch(MovieServiceException msx) {
                        msx.printStackTrace(System.err);
                    }
                }
            });
        }
        
    }
}
