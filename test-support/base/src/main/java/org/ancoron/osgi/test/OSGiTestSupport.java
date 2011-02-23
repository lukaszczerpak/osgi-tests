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
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final Logger log = Logger.getLogger("GenericOSGiTest");
    
    private T framework;

    protected final List<File> files = new ArrayList<File>();
    protected final List<Bundle> bundles = new ArrayList<Bundle>();
    protected final Map<String, String> services = new HashMap<String, String>();

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
            log.log(Level.WARNING, "File does not exist: {0}", bundlePath.getAbsolutePath());
        } else {
            log.log(Level.INFO, "Installing artifact {0} ...", bundlePath.getAbsolutePath());
            try {
                bundle = bundleContext.installBundle("file://" + bundlePath.getCanonicalPath());
            } catch (Exception ex) {
                 fail("Installing bundle " + toString(bundle) + " failed", ex);
            }
        }

        return bundle;
    }
    
    protected void uninstallBundle(Bundle bundle) {
        log.log(Level.INFO, "Uninstalling bundle {0} ...", toString(bundle));
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
        log.log(Level.INFO, "Starting bundle {0} ...", toString(bundle));
        try {
            bundle.start();
        } catch (BundleException ex) {
            fail("Starting bundle " + toString(bundle) + " failed", ex);
        }
    }
    
    protected void stopBundle(Bundle bundle) {
        log.log(Level.INFO, "Stopping bundle {0} ...", toString(bundle));
        try {
            bundle.stop();
        } catch (BundleException ex) {
            fail("Stopping bundle " + toString(bundle) + " failed", ex);
        }
    }

    protected void updateBundle(Bundle bundle, InputStream is) {
        log.log(Level.INFO, "Updating bundle {0} ...", toString(bundle));
        try {
            bundle.update(is);
        } catch (BundleException ex) {
            fail("Updating bundle " + toString(bundle) + " failed", ex);
        }
    }

    public void waitForServices() {
        if(services != null && !services.isEmpty()) {
            boolean ready = false;
            BundleContext ctx = getFramework().getBundleContext();
            ServiceAvailabilityChecker checker = new ServiceAvailabilityChecker(services, ctx);

            ExecutorService exec = Executors.newSingleThreadExecutor();
            try {
                ready = exec.submit(checker).get().booleanValue();
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
            log.info("Starting framework ...");
            getFramework().start();
            
            getFramework().getBundleContext().addServiceListener(new LoggingServiceListener());
        } catch (BundleException ex) {
            fail("Unable to start framework", ex);
        }
    }

    public void stopFramework() {
        try {
            log.info("Stopping framework ...");
            getFramework().stop();
            getFramework().waitForStop(0);
        } catch (Exception ex) {
            fail("Unable to stop framework", ex);
        }
    }
    
    @BeforeSuite(alwaysRun=true, timeOut=30000, groups={"generic-osgi-start"})
    public void init() {
        try {
            files.addAll(MavenHelper.getProvidedArtifacts());
        } catch (Exception ex) {
            fail("Unable to get deployment artifacts", ex);
        }
        
        configureFramework();
        
        startFramework();
    }
    
    @AfterSuite(alwaysRun=true, timeOut=30000, groups={"generic-osgi-stop"})
    public void shutdown() {
        stopFramework();
        files.clear();
        bundles.clear();
    }

    protected void logTest()
    {
        StackTraceElement test = Thread.currentThread().getStackTrace()[2];
        log.log(Level.INFO, "Executing test {0}::{1} ...",
                new Object[]{test.getClassName(), test.getMethodName()});
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
    @Test(groups={"generic-osgi", "generic-osgi-startup"})
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

            log.log(Level.INFO, "... installed bundle {0} ({1})",
                    new Object[]{toString(bundle), bundle.getLocation()});
        }
    }

    /**
     * Start all bundles.
     *
     * @throws BundleException
     * @throws IOException
     */
    @Test(dependsOnMethods={"testInstallBundles"},
            groups={"generic-osgi", "generic-osgi-startup"})
    public void testStartBundles() throws Exception {
        logTest();
        Thread.sleep(500);
        for(final Bundle bundle : bundles) {
            if(isFragment(bundle)) {
                log.log(Level.INFO, "Skipping fragment bundle {0} (will be resolved by its host)...",
                        toString(bundle));
                continue;
            }
            log.log(Level.INFO, "Starting bundle {0} ...", toString(bundle));
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

    @Test(timeOut=30000, dependsOnMethods={"testStartBundles"},
            groups={"generic-osgi", "generic-osgi-startup"})
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
        
        exec.invokeAll(trackers);
        
        for(Bundle bundle : bundles) {
            if(isFragment(bundle)) {
                assert bundle.getState() == Bundle.RESOLVED : "Fragment bundle " + toString(bundle)
                        + " has not been resolved.";
                log.log(Level.INFO, "... resolved fragment {0}", toString(bundle));
            } else {
                assert bundle.getState() == Bundle.ACTIVE : "Bundle " + toString(bundle)
                        + " has not been started.";
                log.log(Level.INFO, "... started bundle {0}", toString(bundle));
            }
        }
    }

    @Test(timeOut=10000, dependsOnMethods={"testBundlesStarted"},
            groups={"generic-osgi", "generic-osgi-startup"})
    public void testServicesAvailable()
            throws Exception
    {
        logTest();
        
        waitForServices();
    }
}
