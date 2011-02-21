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

package org.ancoron.osgi.test;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public abstract class OSGiTestSupport<T extends Framework> {
    
    private T framework;

    protected List<File> files = new ArrayList<File>();
    protected List<Bundle> bundles = new ArrayList<Bundle>();
    
    public T getFramework() {
        return framework;
    }
    
    protected void setFramework(T framework) {
        this.framework = framework;
    }
    
    protected String toString(Bundle bundle) {
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";
    }

    protected Bundle installBundle(File bundlePath, BundleContext bundleContext) {
        Bundle bundle = null;

        if (!bundlePath.exists()) {
            System.out.println("WARNING: File does not exist: " + bundlePath.getAbsolutePath());
        } else {
            System.out.println("Installing artifact " + bundlePath.getAbsolutePath() + " ...");
            try {
                bundle = bundleContext.installBundle("file://" + bundlePath.getCanonicalPath());
            } catch (Exception ex) {
                 fail("Installing bundle " + toString(bundle) + " failed", ex);
            }
        }

        return bundle;
    }
    
    protected void uninstallBundle(Bundle bundle) {
        System.out.println("Uninstalling bundle " + toString(bundle) + " ...");
        try {
            bundle.uninstall();
        } catch (BundleException ex) {
            fail("Uninstalling bundle " + toString(bundle) + " failed", ex);
        }
    }
    
    protected boolean isFragment(Bundle b) {
        return b.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }
    
    protected void startBundle(Bundle bundle) {
        System.out.println("Starting bundle " + toString(bundle) + " ...");
        try {
            bundle.start();
        } catch (BundleException ex) {
            fail("Starting bundle " + toString(bundle) + " failed", ex);
        }
    }
    
    protected void stopBundle(Bundle bundle) {
        System.out.println("Stopping bundle " + toString(bundle) + " ...");
        try {
            bundle.stop();
        } catch (BundleException ex) {
            fail("Stopping bundle " + toString(bundle) + " failed", ex);
        }
    }

    protected void updateBundle(Bundle bundle, InputStream is) {
        System.out.println("Updating bundle " + toString(bundle) + " ...");
        try {
            bundle.update(is);
        } catch (BundleException ex) {
            fail("Updating bundle " + toString(bundle) + " failed", ex);
        }
    }

    public Map<String, String> getTrackedServices() {
        return new HashMap<String, String>();
    }
    
    public void waitForServices(int timeout) {
        Map<String, String> services = getTrackedServices();

        if(services != null && !services.isEmpty()) {
            boolean ready = false;
            BundleContext ctx = getFramework().getBundleContext();
            ServiceAvailabilityChecker checker = new ServiceAvailabilityChecker(services, ctx);

            ExecutorService exec = Executors.newSingleThreadExecutor();
            try {
                ready = exec.submit(checker).get(timeout, TimeUnit.MILLISECONDS).booleanValue();
            } catch (Exception ex) {
                fail("Unable to get all services", ex);
            }

            if(!ready) {
                fail("Unable to get all services");
            }
        }
    }
    
    public abstract void configureFramework();
    
    public void startFramework() {
        try {
            System.out.println("Starting framework ...");
            getFramework().start();
            
            getFramework().getBundleContext().addServiceListener(new LoggingServiceListener());
        } catch (BundleException ex) {
            fail("Unable to start framework", ex);
        }
    }

    public void stopFramework() {
        try {
            System.out.println("Stopping framework ...");
            getFramework().stop();
            getFramework().waitForStop(0);
        } catch (Exception ex) {
            fail("Unable to stop framework", ex);
        }
    }
    
    @BeforeSuite
    public void init() {
        try {
            files = MavenHelper.getProvidedArtifacts();
        } catch (Exception ex) {
            fail("Unable to get deployment artifacts", ex);
        }
        
        configureFramework();
        
        startFramework();
    }
    
    @AfterSuite
    public void shutdown() {
        stopFramework();
        files.clear();
        bundles.clear();
    }

    protected void logTest()
    {
        StackTraceElement test = Thread.currentThread().getStackTrace()[2];
        System.out.println("Executing test " + test.getClassName() + "::" + test.getMethodName() + " ...");
    }

    /**
     * Install all bundles.
     * 
     * The bundle that are to be installed must be specified with scope
     * <code>provided</code> inside the test project
     *
     * @throws BundleException
     * @throws IOException
     */
    @Test
    public void testInstallBundles() throws Exception {
        logTest();
        BundleContext bundleContext = getFramework().getBundleContext();

        for(File modulePath : files) {
            bundles.add(installBundle(modulePath, bundleContext));
        }

        for(Bundle bundle : bundles) {
            if(bundle.getState() != Bundle.INSTALLED) {
                fail("Bundle " + toString(bundle) + " is not installed.");
            }

            System.out.println("... installed bundle " + toString(bundle)
                    + " (" + bundle.getLocation() + ")");
        }
    }

    /**
     * Start all bundles.
     *
     * @throws BundleException
     * @throws IOException
     */
    @Test(dependsOnMethods = {"testInstallBundles"})
    public void testStartBundles() throws Exception {
        logTest();
        Thread.sleep(500);
        for(final Bundle bundle : bundles) {
            if(isFragment(bundle)) {
                System.out.println("Skipping fragment bundle " + toString(bundle) + " (will be resolved by its host)...");
                continue;
            }
            System.out.println("Starting bundle " + toString(bundle) + " ...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bundle.start();
                    } catch(Exception x) {
                        System.err.println("Unable to start bundle in the first place: "
                                + bundle.getSymbolicName());
                    }
                }
            }).start();
        }
    }

    @Test(dependsOnMethods = {"testStartBundles"})
    public void testBundlesStarted()
            throws Exception
    {
        logTest();
        
        BundleContext ctx = getFramework().getBundleContext();
        ExecutorService exec = Executors.newFixedThreadPool(bundles.size());
        Set<BundleStateTracker> trackers = new HashSet<BundleStateTracker>();
        for(Bundle bundle : bundles) {
            BundleStateTracker tracker = null;
            if(isFragment(bundle)) {
                tracker = new BundleStateTracker(bundle.getBundleId(), Bundle.RESOLVED);
            } else {
                tracker = new BundleStateTracker(bundle.getBundleId(), Bundle.ACTIVE);
            }
            ctx.addBundleListener(tracker);
        }
        
        exec.invokeAll(trackers, 10, TimeUnit.SECONDS);
        
        for(Bundle bundle : bundles) {
            if(isFragment(bundle)) {
                assert bundle.getState() == Bundle.RESOLVED : "Fragment bundle " + toString(bundle)
                        + " has not been resolved.";
                System.out.println("... resolved fragment " + toString(bundle));
            } else {
                assert bundle.getState() == Bundle.ACTIVE : "Bundle " + toString(bundle)
                        + " has not been started.";
                System.out.println("... started bundle " + toString(bundle));
            }
        }
    }

    @Test(dependsOnMethods = {"testBundlesStarted"})
    public void testServicesAvailable()
            throws Exception
    {
        logTest();
        
        waitForServices(30000);
    }
}
