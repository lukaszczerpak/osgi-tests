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
import org.ancoron.osgi.test.felix.FelixTestSupport;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class GlassfishTestSupport extends FelixTestSupport {
    
    private static final String GF_DIR_PROP = "glassfish.install.dir";
    private static final String GF_VER_PROP = "glassfish.version";

    private Bundle glassfish = null;
    
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
        boolean install = !gfDir.exists();
        
        if(install) {
            GlassfishHelper.installGlassfish(gfDir.getCanonicalPath(), version);
        }
    }
    
    protected File getGlassfishBundle() {
        return new File(getGlassfishDir(), "glassfish3/glassfish/modules/glassfish.jar");
    }

    @Override
    public void startFramework() {
        super.startFramework();
        try {
            prepare();
        } catch (IOException ex) {
            fail("Unable to prepare GlassFish", ex);
        }
        
        File gf = getGlassfishBundle();
        
        System.out.println("Starting GlassFish using " + gf.getAbsolutePath() + " ...");
        
        glassfish = installBundle(gf, getFramework().getBundleContext());

        try {
            glassfish.start(Bundle.START_TRANSIENT);
        } catch (BundleException ex) {
            fail("Unable to start GlassFish", ex);
        }
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
