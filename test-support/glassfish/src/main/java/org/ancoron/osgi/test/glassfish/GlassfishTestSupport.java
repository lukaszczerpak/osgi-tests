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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.ancoron.osgi.test.felix.FelixTestSupport;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class GlassfishTestSupport extends FelixTestSupport {
    
    private static final String GF_DIR_PROP = "glassfish.install.dir";
    private static final String GF_VER_PROP = "glassfish.version";

    private Bundle glassfish = null;
    private Object glassfishService = null;
    
    private Map<String, String> services = new HashMap<String, String>();
    
    protected File getGlassfishDir() {
        String path = System.getProperty(GF_DIR_PROP);
        if(path == null) {
            fail("Please configure a system property named '" + GF_DIR_PROP + "'");
        }
        
        return new File(path);
    }
    
    protected void prepare() throws IOException {

        String version = System.getProperty(GF_VER_PROP);
        if(version == null) {
            fail("Please configure a system property named '" + GF_VER_PROP + "'");
        }

        File gfDir = getGlassfishDir();
        String dir = gfDir.getCanonicalPath();
        boolean install = !gfDir.exists();
        
        if(install) {
            GlassfishHelper.installGlassfish(dir, version);
        }
        
        System.setProperty("com.sun.aas.installRoot", dir + "/glassfish3/glassfish/");
        System.setProperty("com.sun.aas.installRootURI", "file://" + dir + "/glassfish3/glassfish/");
        
        GlassfishHelper.configureGlassfish(gfDir.getCanonicalPath());
    }
    
    protected File getGlassfishBundle() {
        return new File(getGlassfishDir(), "glassfish3/glassfish/modules/glassfish.jar");
    }

    @Override
    public void customizeFrameworkConfig(Map configMap) {
        try {
            prepare();
        } catch (IOException ex) {
            fail("Unable to prepare GlassFish", ex);
        }
        
        configMap.put("com.sun.aas.installRoot", System.getProperty("com.sun.aas.installRoot"));
        configMap.put("com.sun.aas.instanceRoot", System.getProperty("com.sun.aas.instanceRoot"));
        configMap.put("com.sun.aas.installRootURI", System.getProperty("com.sun.aas.installRootURI"));

        super.customizeFrameworkConfig(configMap);
    }

    @Override
    public void startFramework() {
        super.startFramework();
        BundleContext ctx = getFramework().getBundleContext();

        File gf = getGlassfishBundle();
        
        System.out.println("Starting GlassFish using " + gf.getAbsolutePath() + " ...");
        glassfish = installBundle(gf, getFramework().getBundleContext());

        try {
            // glassfish = getFramework().getBundleContext().installBundle(gf.getCanonicalPath());
            glassfish.start(Bundle.START_TRANSIENT);
        } catch (Exception ex) {
            fail("Unable to start GlassFish", ex);
        }

        String gfService = "org.glassfish.embeddable.GlassFish";
        services.put(gfService, null);
        waitForServices(10000);
        
        ServiceReference ref = ctx.getServiceReference(gfService);
        
        glassfishService = ctx.getService(ref);
        // glassfishService.getCommandRunner().run("", null);
        
        try {
            Object status = invokeGlassfishService("getStatus", Object.class);
            System.out.println("GlassFish status: " + status);
        } catch (Exception ex) {
            fail("Unable to get status of GlassFish", ex);
        }
        
        services.clear();
    }
    
    protected <T> T invokeGlassfishService(String method, Class<T> returnType) {
        return invoke(glassfishService, method, returnType);
    }

    protected <T> T invoke(final Object inst, final String method, Class<T> returnType) {
        T ret = null;

        try {
            Method m = inst.getClass().getMethod(method, new Class<?>[0]);
            ret = (T) m.invoke(inst, new Object[0]);
        } catch(Exception x) {
            fail("Invoking method " + method + " failed", x);
        }
        
        return ret;
    }

    @Override
    public void stopFramework() {
        try {
            glassfish.stop(Bundle.STOP_TRANSIENT);
        } catch (BundleException ex) {
            fail("Unable to stop GlassFish", ex);
        }
        
        super.stopFramework();
    }
}
