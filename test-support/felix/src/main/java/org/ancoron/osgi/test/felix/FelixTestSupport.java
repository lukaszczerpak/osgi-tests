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

package org.ancoron.osgi.test.felix;

import java.io.File;
import java.util.Map;
import org.ancoron.osgi.test.OSGiTestSupport;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.StringMap;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class FelixTestSupport extends OSGiTestSupport<Felix> {

    /**
     * A simple  method to customize the configuration for Felix to meet your
     * requirements.
     * 
     * Just override this method to customize the startup configuration.
     * 
     * @param configMap A StringMap with very basic configuration
     */
    public void customizeFrameworkConfig(final Map configMap) {
    }

    @Override
    public void configureFramework() {
        Map configMap = new StringMap(false);
        configMap.put(BundleCache.CACHE_ROOTDIR_PROP, "test");

        File file = new File("target/osgi-cache");
        file.delete();
        file.mkdirs();
        file.deleteOnExit();

        try {
            configMap.put("org.osgi.framework.storage", file.getCanonicalPath());
            
            customizeFrameworkConfig(configMap);
        } catch(Exception x) {
            fail("Configuring Apache Felix failed", x);
        }

        setFramework(new Felix(configMap));
    }
}
