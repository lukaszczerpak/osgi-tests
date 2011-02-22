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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ancoron.osgi.test.OSGiTestSupport;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.framework.util.Util;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class FelixTestSupport extends OSGiTestSupport<Felix> {
    
    private final Pattern UNRESOLVED = Pattern.compile(".*$\\{(.+)\\}.*");

    /**
     * A simple  method to customize the configuration for Felix to meet your
     * requirements.
     * 
     * Just override this method to customize the startup configuration.
     * 
     * @param configMap A StringMap with very basic configuration
     */
    public void customizeFrameworkConfig(final Map configMap) {
        File config = new File("src/main/resources/felix/conf/config.properties");
        
        if(config.exists()) {
            try {
                System.out.println("Loading Felix configuration from " + config.getCanonicalPath());
                URL configUrl = config.toURI().toURL();

                Properties props = new Properties();
                InputStream is = null;
                try {
                    // Try to load config.properties.
                    is = configUrl.openConnection().getInputStream();
                    props.load(is);
                    is.close();
                } catch(Exception ex) {
                    // Try to close input stream if we have one.
                    try {
                        if(is != null) is.close();
                    } catch (Exception ex2) {}
                }

                // Perform variable substitution for system properties.
                for(Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
                    String name = (String) e.nextElement();
                    props.setProperty(name, Util.substVars(props.getProperty(name), name, null, props));
                }
                
                for(Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
                    String name = (String) e.nextElement();
                    String value = (String) props.getProperty(name);
                    Matcher m = UNRESOLVED.matcher(value);
                    if(m.find()) {
                        fail("Felix configuration property '" + m.group(1) + "' not resolved");
                    }
                    configMap.put(name, value);
                }
                
                System.out.println("Configured " + configMap.size() + " properties");
            } catch(Exception x) {
                fail("Unable to load Felix configuration", x);
            }
        } else {
            System.out.println("No Felix configuration available at " + config.getAbsolutePath());
        }
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
