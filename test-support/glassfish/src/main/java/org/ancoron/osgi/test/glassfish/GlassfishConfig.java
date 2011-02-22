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

/**
 * An interface just for defining configuration constants.
 *
 * @author ancoron
 */
public interface GlassfishConfig {

    /**
     * System property for the download location ({@value }).
     */
    public static final String DOWNLOAD_URL = "glassfish.download.url";
    
    /**
     * System property for the target domain directory ({@value }).
     */
    public static final String DOMAIN_DIR = "glassfish.domain.dir";
    
    /**
     * System property for the domain's HTTP listener address ({@value }).
     */
    public static final String HTTP_ADDRESS = "glassfish.http.address";
    
    /**
     * System property for the domain's HTTP port ({@value }).
     */
    public static final String HTTP_PORT = "glassfish.http.port";
    
    /**
     * System property for the domain's HTTPS listener address ({@value }).
     */
    public static final String HTTPS_ADDRESS = "glassfish.https.address";
    
    /**
     * System property for the domain's HTTPS port ({@value }).
     */
    public static final String HTTPS_PORT = "glassfish.https.port";
    
}
