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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.Version;

/**
 *
 * @author ancoron
 */
public class PackageExport {
    
    private static final Pattern p_version = Pattern.compile("version\\s*=\\s*[\"']?([\\[\\(]?[0-9\\.]+([^\"'\\]\\)]*)?)[\"']?");
    
    public PackageExport(String statement) {
        String[] parts = statement.split(";");
        pkg = parts[0];
        
        if(parts.length > 1) {
            for(int i=1; i<parts.length; i++) {
                Matcher v = p_version.matcher(parts[i]);
                if(v.find()) {
                    version = new Version(v.group(1));
                }
            }
        }
    }

    private String pkg;

    /**
     * Get the value of pkg
     *
     * @return the value of pkg
     */
    public String getPkg() {
        return pkg;
    }

    /**
     * Set the value of pkg
     *
     * @param pkg new value of pkg
     */
    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    protected Version version;

    /**
     * Get the value of version
     *
     * @return the value of version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Set the value of version
     *
     * @param version new value of version
     */
    public void setVersion(Version version) {
        this.version = version;
    }
}
