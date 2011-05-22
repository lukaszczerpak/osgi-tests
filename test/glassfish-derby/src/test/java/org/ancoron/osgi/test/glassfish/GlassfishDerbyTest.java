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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.ancoron.movie.MovieService;
import org.ancoron.movie.MovieServiceException;
import org.ancoron.movie.jpa.MovieJPAService;
import org.ancoron.movie.persistence.MoviePersistenceException;
import org.ancoron.movie.persistence.MoviePersistenceService;
import org.ancoron.movie.test.MovieServiceTest;
import org.ancoron.osgi.test.ejb.slsb.SLSBInterface;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
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

        SLSBInterface svc = waitForService(SLSBInterface.class, SERVICE_LOOKUP_REFERENCE);
        if(svc == null) {
            fail("Unable to lookup service via ServiceReference: " + SLSBInterface.class.getName());
        }

        svc = waitForService(SLSBInterface.class, SERVICE_LOOKUP_TRACKER);
        if(svc == null) {
            fail("Unable to lookup service via ServiceTracker: " + SLSBInterface.class.getName());
        }

        if(svc.getName() == null) {
            fail("Service " + SLSBInterface.class.getName() + " returned null on method getName()");
        }
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
        
        installAndStart("org.ancoron.osgi.test:movie-service-test");

        MovieServiceTest svc = waitForService(MovieServiceTest.class);
        try {
            svc.test();
        } catch (Exception ex) {
            fail("Error during service test", ex);
        }
    }
    
    @Test(timeOut=30000, dependsOnMethods={"testMovieService"})
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
    
    protected DefaultHttpClient getHTTPClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] { new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers =============");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                System.out.println("checkClientTrusted =============");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                System.out.println("checkServerTrusted =============");
            }
        } }, new SecureRandom());
        
        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme httpsScheme = new Scheme("https", 8181, sf);

        PlainSocketFactory plain = new PlainSocketFactory();
        Scheme httpScheme = new Scheme("http", 8080, plain);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);
        schemeRegistry.register(httpScheme);

        HttpParams params = new BasicHttpParams();
        
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
        // Increase max total connection to 200
        cm.setMaxTotal(200);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);

        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
                "UTF-8");
        return httpClient;
    }
    
    @Test(dependsOnGroups={"glassfish-osgi-startup"})
    public void testWebAvailability() throws NoSuchAlgorithmException, KeyManagementException, IOException {
        logTest();
        
        DefaultHttpClient http = getHTTPClient();
        try {
            HttpGet httpget = new HttpGet("http://[::1]/movie/dummy/");
            httpget.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, 
                HttpVersion.HTTP_1_1); // Use HTTP 1.1 for this request only
            httpget.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, 
                Boolean.FALSE);

            HttpResponse res = http.execute(httpget);
            
            if(res.getStatusLine().getStatusCode() == 200) {
                fail("Gained access to secured page at '" + httpget.getURI().toASCIIString()
                        + "' [status=" + res.getStatusLine().getStatusCode() + "]");
            }
            
            log.log(Level.INFO, "[HTTP] Got response for ''{0}'' [status={1}]",
                    new Object[] {httpget.getURI().toASCIIString(), res.getStatusLine().getStatusCode()});

            HttpGet httpsget = new HttpGet("https://[::1]/movie/dummy/");
            httpsget.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, 
                HttpVersion.HTTP_1_1); // Use HTTP 1.1 for this request only
            httpsget.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, 
                Boolean.FALSE);

            res = http.execute(httpsget);
            
            if(res.getStatusLine().getStatusCode() == 200) {
                fail("Gained access to secured page at '" + httpsget.getURI().toASCIIString()
                        + "' [status=" + res.getStatusLine().getStatusCode() + "] "
                        + res.getStatusLine().getReasonPhrase());
            }
            
            log.log(Level.INFO, "[HTTP] Got response for ''{0}'' [status={1}]",
                    new Object[] {httpget.getURI().toASCIIString(), res.getStatusLine().getStatusCode()});
            
            http.getCredentialsProvider().setCredentials(
                    new AuthScope("[::1]", 8181),
                    new UsernamePasswordCredentials("fail", "fail"));
            
            res = http.execute(httpsget);
            
            if(res.getStatusLine().getStatusCode() == 200) {
                fail("Gained access with invalid user role to secured page at '"
                        + httpsget.getURI().toASCIIString()
                        + "' [status=" + res.getStatusLine().getStatusCode() + "] "
                        + res.getStatusLine().getReasonPhrase());
            }
            
            log.log(Level.INFO, "[HTTP] Got response for ''{0}'' [status={1}]",
                    new Object[] {httpget.getURI().toASCIIString(), res.getStatusLine().getStatusCode()});
            
            http.getCredentialsProvider().setCredentials(
                    new AuthScope("[::1]", 8181),
                    new UsernamePasswordCredentials("test", "test"));
            
            res = http.execute(httpsget);
            
            if(res.getStatusLine().getStatusCode() != 200) {
                fail("Failed to access " + httpsget.getURI().toASCIIString()
                        + ": [status=" + res.getStatusLine().getStatusCode() + "] "
                        + res.getStatusLine().getReasonPhrase());
            }
            
            log.log(Level.INFO, "[HTTP] Got response for ''{0}'' [status={1}]",
                    new Object[] {httpget.getURI().toASCIIString(), res.getStatusLine().getStatusCode()});
        } finally {
            http.getConnectionManager().shutdown();
        }
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
                    logLines.add(String.format("    |%11s| id=%4d, interfaces: %s",
                            "-->", id, Arrays.toString(clazz)));
                }
            }

            refs = tmp.getServicesInUse();
            if(refs != null) {
                for(ServiceReference ref : refs) {
                    String[] clazz = (String[]) ref.getProperty(Constants.OBJECTCLASS);
                    Long id = (Long) ref.getProperty(Constants.SERVICE_ID);
                    logLines.add(String.format("    |%11s| id=%4d, bundle=%4d, interfaces: %s",
                            "<--", id, ref.getBundle().getBundleId(), Arrays.toString(clazz)));
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
