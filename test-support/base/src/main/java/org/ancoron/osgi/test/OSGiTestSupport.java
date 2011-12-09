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

import org.testng.annotations.AfterSuite;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dynamicjava.api_bridge.ApiBridge;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.util.tracker.ServiceTracker;
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
    protected final List<BundleStateTracker> trackers = new ArrayList<BundleStateTracker>();
    protected final Map<String, String> services = new HashMap<String, String>();

    public T getFramework() {
        return framework;
    }
    
    protected void setFramework(T framework) {
        this.framework = framework;
    }

    protected void testBundleInstalled(final Bundle bundle) {
        if(bundle.getState() < Bundle.INSTALLED) {
            fail("Bundle " + toString(bundle) + " is not installed: state="
                    + toString(bundle.getState()));
        }

        log.log(Level.INFO, "... installed bundle {0} ({1})",
                new Object[]{toString(bundle), bundle.getLocation()});
    }
    
    protected String toString(Bundle bundle) {
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";
    }

    protected Bundle installBundle(File bundlePath, BundleContext bundleContext) {
        if (!bundlePath.exists()) {
            log.log(Level.WARNING, "File does not exist: {0}", bundlePath.getAbsolutePath());
        } else {
            log.log(Level.INFO, "Installing artifact {0} ...", bundlePath.getAbsolutePath());
            try {
                return bundleContext.installBundle("file://" + bundlePath.getCanonicalPath());
            } catch (Exception ex) {
                 fail("Installing bundle at " + bundlePath.getAbsolutePath() + " failed", ex);
            }
        }

        return null;
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
    
    /**
     * A helper method to easily deploy bundles inside tests based on their
     * maven artifact string.
     * 
     * <p>
     * e.g.:
     * <pre>
     * Bundle test = installAndStart("org.ancoron.osgi.test:movie-service-test");
     * </pre>
     * </p>
     * 
     * @param artifact 
     * 
     * @return 
     */
    protected Bundle installAndStart(String artifact) {
        File f = MavenHelper.getArtifact(artifact);
        
        BundleContext ctx = getFramework().getBundleContext();
        Bundle b = installBundle(f, ctx);
        
        startBundle(b);
        
        return b;
    }

    public void waitForServices() {
        if(services != null && !services.isEmpty()) {
            waitForServices(services);
        }
    }
    
    public void waitForServices(Map<String, String> services) {
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
    
    public <T> T waitForService(final Class<T> clazz) {
        return waitForService(clazz, SERVICE_LOOKUP_TRACKER);
    }
    
    protected static final int SERVICE_LOOKUP_REFERENCE = 0;
    protected static final int SERVICE_LOOKUP_TRACKER = 1;
    
    public <T> T waitForService(final Class<T> clazz, final int type) {
        final BundleContext ctx = getFramework().getBundleContext();
        T inst = null;

        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            inst = exec.submit(new Callable<T>() {

                @Override
                public T call() throws Exception {
                    Object svc = null;
                    while(svc == null) {
                        switch(type) {
                            case SERVICE_LOOKUP_REFERENCE:
                                svc = getWithServiceReference(ctx, clazz.getName());
                                break;
                            case SERVICE_LOOKUP_TRACKER:
                                svc = getWithServiceTracker(ctx, clazz.getName());
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown service lookup type " + type);
                        }
                        if(svc != null) {
                            ApiBridge apiBridge = ApiBridge.getApiBridge(
                                    Thread.currentThread().getContextClassLoader(), clazz.getPackage().getName());

                            return (T) apiBridge.bridge(svc);
                        }
                        Thread.sleep(500);
                    }
                    
                    return null;
                }
            }).get();
        } catch (Exception ex) {
            fail("Unable to get all services", ex);
        }
        
        return inst;
    }
    
    private Object getWithServiceTracker(BundleContext ctx, String className) throws InterruptedException {
        Object svc = null;
        ServiceTracker st = new ServiceTracker(ctx, className, null);
        st.open();
        try {
            // svc = st.getService();
            svc = st.waitForService(10000L);
        } finally {
            st.close();
        }
        return svc;
    }
    
    private Object getWithServiceReference(BundleContext ctx, String className) {
        if(ctx == null) {
            return null;
        }
        ServiceReference ref = ctx.getServiceReference(className);
        return ctx.getService(ref);
    }
    
    public <T> T getService(Class<T> clazz, String filter, String... pkgs) {
        BundleContext ctx = getFramework().getBundleContext();
        T inst = null;

        try {
            ServiceReference[] refs = ctx.getServiceReferences(clazz.getName(), filter);
            if(refs != null && refs.length > 0) {
                Object svc = ctx.getService(refs[0]);
                
                if(svc != null) {
                    ApiBridge apiBridge = ApiBridge.getApiBridge(
                            Thread.currentThread().getContextClassLoader(), pkgs);
                    
                    inst = (T) apiBridge.bridge(svc);
                }
            }
        } catch (Exception ex) {
            fail("Unable to get service", ex);
        }
        
        return inst;
    }
    
    public abstract void configureFramework();
    
    public void startFramework() {
        try {
            log.info("Initializing OSGi framework ...");
            getFramework().init();

            log.info("Starting OSGi framework ...");
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
            FrameworkEvent event = getFramework().waitForStop(0);
            
            while(event.getType() != FrameworkEvent.STOPPED) {
                event = getFramework().waitForStop(0);
            }
            
            framework = null;
        } catch (Exception ex) {
            fail("Unable to stop framework", ex);
        }
    }
    
    @BeforeSuite(alwaysRun=true, timeOut=120000, groups={"generic-osgi-start"})
    public void init() {
        log.info("Initializing test class environment...");
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
        log.info("Stopping OSGi framework...");
        stopFramework();

        log.info("Clearing test class caches...");
        files.clear();
        bundles.clear();
        trackers.clear();
        services.clear();
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

        for(final Bundle bundle : bundles) {
            testBundleInstalled(bundle);
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
        final BundleContext ctx = getFramework().getBundleContext();
        for(final Bundle bundle : bundles) {
            if(isFragment(bundle)) {
                log.log(Level.INFO, "Skipping fragment bundle {0} (will be resolved by its host)...",
                        toString(bundle));
                continue;
            }

            log.log(Level.INFO, "Starting bundle {0} ...", toString(bundle));

            try {
                final BundleStateTracker tracker;

                if(isFragment(bundle)) {
                    tracker = new BundleStateTracker(
                            bundle.getBundleId(), Bundle.RESOLVED, ctx);
                } else {
                    tracker = new BundleStateTracker(
                            bundle.getBundleId(), Bundle.ACTIVE, ctx);
                }

                trackers.add(tracker);
                ctx.addBundleListener(tracker);
                bundle.start();
            } catch(Exception x) {
                log.log(Level.WARNING, "Unable to start bundle in the first place: "
                        + bundle.getSymbolicName(), x);
            }
        }
    }

    @Test(timeOut=30000, dependsOnMethods={"testStartBundles"},
            groups={"generic-osgi", "generic-osgi-startup"})
    public void testBundlesStarted()
            throws Exception
    {
        logTest();
        
        ExecutorService exec = Executors.newFixedThreadPool(bundles.size());
        for(Future<Boolean> fute : exec.invokeAll(trackers)) {
            fute.get();
        }
        
        for(final Bundle bundle : bundles) {
            int state = bundle.getState();
            if(isFragment(bundle)) {
                if(state != Bundle.RESOLVED) {
                    fail("Fragment bundle " + toString(bundle) + " has not been resolved: "
                        + unresolvableReason(bundle));
                } else {
                    log.log(Level.INFO, "... resolved fragment {0}", toString(bundle));
                }
            } else {
                if(state != Bundle.ACTIVE) {
                    fail("Bundle " + toString(bundle) + " has not been started (state="
                            + toString(state) + ")");
                } else {
                    log.log(Level.INFO, "... started bundle {0}", toString(bundle));
                }
            }
        }
    }
    
    protected String toString(int bundleState) {
        switch(bundleState) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }
    
    protected boolean isExtension(Bundle b) {
        String host = (String) b.getHeaders().get(Constants.FRAGMENT_HOST);
        
        return host.matches("^.*;\\s*" + Constants.EXCLUDE_DIRECTIVE
                + "\\s*:=[\\s\"']*(" + Constants.EXTENSION_FRAMEWORK + "|"
                + Constants.EXTENSION_BOOTCLASSPATH + ")[\\s\"']*$");
    }

    protected String unresolvableReason(Bundle b) {
        String msg = "Unknown reason";

        final long id = b.getBundleId();
        final String name = b.getSymbolicName();
        final Version version = b.getVersion();
        
        Dictionary headers = b.getHeaders();
        if(isFragment(b)) {
            String host = (String) headers.get(Constants.FRAGMENT_HOST);
            host = host.trim();
            
            if(isExtension(b)) {
                Matcher m = Pattern.compile("^\\s*([^\\s].*[^\\s])\\s*;\\s*"
                        + Constants.EXCLUDE_DIRECTIVE + "\\s*:=[\\s\"']*("
                        + Constants.EXTENSION_FRAMEWORK + "|"
                        + Constants.EXTENSION_BOOTCLASSPATH + ")[\\s\"']*$").matcher(host);

                if(m.find()) {
                    String bundleName = m.group(1);
                    String extType = m.group(2);
                    
                    if(Constants.EXTENSION_BOOTCLASSPATH.equals(extType)) {
                        // system extension...
                        if(bundleName.equals(Constants.SYSTEM_BUNDLE_SYMBOLICNAME)) {
                            return "The extension bundle didn't attach to the system bundle";
                        } else {
                            return "The boot classpath extension bundle should specify the host '"
                                    + Constants.SYSTEM_BUNDLE_SYMBOLICNAME
                                    + "' instead of '" + bundleName + "'";
                        }
                    } else {
                        // framework extension...
                        Set<Bundle> frm = getBundlesBySymbolicName(bundleName);
                        if(frm.isEmpty()) {
                            return "The current framework is '"
                                    + getFramework().getSymbolicName()
                                    + "' and not '" + bundleName + "'";
                        } else {
                            Bundle f = frm.iterator().next();
                            if(f.getBundleId() != 0) {
                                return "The specified host bundle " + toString(f)
                                        + " is not the framework bundle, which is "
                                        + toString(getFramework());
                            }
                        }
                    }
                }
                
                return "The extension bundle declaration '" + host + "' is invalid";
            }
            
            Set<Bundle> found = getBundlesBySymbolicName(host);
            
            if(found.isEmpty()) {
                return "Bundle header '" + Constants.FRAGMENT_HOST + "' with value '"
                        + host + "' didn't resolve to any installed bundle";
            } else if(found.iterator().next().getBundleId() == id) {
                return "Cannot attach fragment to itself";
            } else {
                // check imports...
                List<PackageImport> imports = getBundleImports(b);
                for(Bundle tmp : getFramework().getBundleContext().getBundles()) {
                    for(PackageExport exp : getBundleExports(tmp)) {
                        for(Iterator<PackageImport> it = imports.iterator(); it.hasNext(); ) {
                            PackageImport imp = it.next();
                            if(imp.canImport(exp)) {
                                log.log(Level.INFO, "Import {0} specified by {1} is satisfied by bundle {2}",
                                        new Object[]{imp.toString(), toString(b), toString(tmp)});
                                it.remove();
                            }
                        }
                    }
                }
                
                if(!imports.isEmpty()) {
                    // all remaining imports have not been satisfied...
                    msg = "The following package imports are unsatisfied:";
                    for(PackageImport imp : imports) {
                        msg += "\n" + imp.toString();
                    }
                    
                    return msg;
                }
            }
        }
        
        return msg;
    }
    
    protected List<PackageImport> getBundleImports(Bundle b) {
        List<PackageImport> imports = new ArrayList<PackageImport>();
        
        String header = (String) b.getHeaders().get(Constants.IMPORT_PACKAGE);
        if(header != null) {
            for(String imp : header.split(",")) {
                imports.add(new PackageImport(imp.trim()));
            }
        }
        
        return imports;
    }
    
    protected List<PackageExport> getBundleExports(Bundle b) {
        List<PackageExport> exports = new ArrayList<PackageExport>();
        
        String header = (String) b.getHeaders().get(Constants.EXPORT_PACKAGE);
        if(header != null) {
            for(String exp : header.split(",")) {
                exports.add(new PackageExport(exp.trim()));
            }
        }
        
        return exports;
    }
    
    protected Set<Bundle> getBundlesBySymbolicName(String name) {
        Set<Bundle> found = new HashSet<Bundle>();
        for(Bundle tmp : getFramework().getBundleContext().getBundles()) {
            if(name.startsWith(tmp.getSymbolicName())) {
                found.add(tmp);
            }
        }
        return found;
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
