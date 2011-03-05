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

package org.ancoron.osgi.test.glassfish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.ancoron.movie.MovieService;
import org.ancoron.movie.MovieServiceException;
import org.ancoron.movie.jpa.MovieJPAService;
import org.ancoron.movie.persistence.MoviePersistenceException;
import org.ancoron.movie.persistence.MoviePersistenceService;
import org.ancoron.osgi.test.ejb.slsb.SLSBInterface;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class GlassfishDerbyTest extends GlassfishDerbyTestSupport {

    private static final Logger log = Logger.getLogger(GlassfishDerbyTest.class.getName());
    private final List<String> logLines = new ArrayList<String>();

    @Test(timeOut=10000, dependsOnGroups={"glassfish-osgi-startup"})
    public void testSLSBService() {
        logTest();

        Map<String, String> svcs = new HashMap<String, String>();
        svcs.put(SLSBInterface.class.getName(), null);
        
        waitForServices(svcs);
        
        svcs.clear();
    }
    
    @Test(timeOut=10000, dependsOnGroups={"glassfish-osgi-startup"})
    public void testMovieServicesAvailability() {
        logTest();
        
        Map<String, String> svcs = new HashMap<String, String>();
        svcs.put(MoviePersistenceService.class.getName(), null);
        svcs.put(MovieJPAService.class.getName(), null);
        svcs.put(MovieService.class.getName(), null);

        waitForServices(svcs);

        svcs.clear();
    }
    
    @Test(timeOut=10000, dependsOnMethods={"testMovieServicesAvailability"})
    public void testMoviePersistenceService() {
        logTest();
        
        MoviePersistenceService movieService = getService(
                MoviePersistenceService.class, null,
                "org.ancoron.movie",
                "org.ancoron.movie.model",
                "org.ancoron.movie.persistence",
                "org.ancoron.movie.persistence.model",
                "org.ancoron.movie.jpa",
                "org.ancoron.movie.jpa.entity");
        try {
            movieService.getAllVideos();
        } catch (MoviePersistenceException ex) {
            fail("Unexpected error during persistence service call", ex);
        }
    }
    
    @Test(timeOut=10000, dependsOnMethods={"testMovieServicesAvailability"})
    public void testMovieService() {
        logTest();

        MovieService movieService = getService(
                MovieService.class, null,
                "org.ancoron.common.model",
                "org.ancoron.movie",
                "org.ancoron.movie.model",
                "org.ancoron.movie.persistence",
                "org.ancoron.movie.persistence.model",
                "org.ancoron.movie.jpa",
                "org.ancoron.movie.jpa.entity");
        
        try {
            movieService.getAllVideos();

        } catch (MovieServiceException ex) {
            fail("Service error during service call", ex);
        }
    }
    
    @Test(timeOut=10000, dependsOnMethods={"testMovieService"})
    public void testUpdatePersistenceAPI() {
        logTest();
        
        try {
            Bundle b = null;
            
            for(Bundle tmp : bundles) {
                if("org.ancoron.movie.persistence".equals(tmp.getSymbolicName())) {
                    b = tmp;
                    break;
                }
            }
            
            updateBundle(b, null);

            Thread.sleep(1000);
        } catch (Exception ex) {
            fail("Unexpected error during bundle update", ex);
        }
        
        
        testMovieServicesAvailability();
        
        testMovieService();
    }
    
    @Override
    public void stopFramework() {
        BundleContext ctx = getFramework().getBundleContext();
        List<Bundle> b = new ArrayList<Bundle>();
        for(final Bundle bundle : ctx.getBundles()) {
            b.add(bundle);
        }
        
        Collections.sort(b, new BundleComparator());

        for(Bundle tmp : b) {
            logLines.add(String.format("%4d|%11s| %s (%s)",
                    tmp.getBundleId(),
                    toString(tmp.getState()),
                    tmp.getSymbolicName(),
                    tmp.getVersion().toString()));
            ServiceReference[] refs = tmp.getRegisteredServices();
            if(refs != null) {
                for(ServiceReference ref : refs) {
                    String[] clazz = (String[]) ref.getProperty(Constants.OBJECTCLASS);
                    Long id = (Long) ref.getProperty(Constants.SERVICE_ID);
                    logLines.add(String.format("    |%11s| id=%d, interfaces: %s",
                            "providing", id, Arrays.toString(clazz)));
                }
            }

            refs = tmp.getServicesInUse();
            if(refs != null) {
                for(ServiceReference ref : refs) {
                    String[] clazz = (String[]) ref.getProperty(Constants.OBJECTCLASS);
                    Long id = (Long) ref.getProperty(Constants.SERVICE_ID);
                    logLines.add(String.format("    |%11s| <-- id=%d, bundle=%d, interfaces: %s",
                            "consuming", id, ref.getBundle().getBundleId(), Arrays.toString(clazz)));
                }
            }
        }

        super.stopFramework();
    }

    public class BundleComparator implements Comparator<Bundle> {

        @Override
        public int compare(Bundle o1, Bundle o2) {
            if(o1.getBundleId() == o2.getBundleId()) {
                return 0;
            } else if(o1.getBundleId() < o2.getBundleId()) {
                return -1;
            }
            return 1;
        }
    }
    
    @AfterSuite(alwaysRun=true)
    @Override
    public void shutdown() {
        super.shutdown();
        
        StringBuilder sb = new StringBuilder();
        for(String tmp : logLines) {
            sb.append("\n").append(tmp);
        }
        
        log.info(sb.toString() + "\n");
    }
}
