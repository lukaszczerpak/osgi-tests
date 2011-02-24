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
public class PackageImport {
    
    private static final Pattern p_version = Pattern.compile("version\\s*=\\s*[\"']?(([^\"']+)?)[\"']?");
    private static final Pattern p_version_part = Pattern.compile("[0-9](\\.[0-9])*(\\.[a-zA-Z]+)?");
    private static final Pattern p_resolution = Pattern.compile("resolution\\s*:=\\s*[\"']?([a-zA-Z0-9\\-_\\.]+)[\"']?");
    
    public PackageImport(String statement) {
        String[] parts = statement.split(";");
        pkg = parts[0];
        
        if(parts.length > 1) {
            for(int i=1; i<parts.length; i++) {
                Matcher v = p_version.matcher(parts[i]);
                if(v.find()) {
                    version = v.group(1);
                    
                    if(version.startsWith("(")) {
                        minExclusive = true;
                    }
                    if(version.endsWith(")")) {
                        maxExclusive = true;
                    }
                    
                    v = p_version_part.matcher(version);
                    if(v.find()) {
                        min = new Version(v.group(0));
                    }
                    if(v.find()) {
                        max = new Version(v.group(0));
                    }
                }
                Matcher o = p_resolution.matcher(parts[i]);
                if(o.find()) {
                    optional = o.group(1).equalsIgnoreCase("optional");
                }
            }
        }
    }
    
    public boolean canImport(PackageExport export) {
        boolean imp = false;

        if(export.getPkg().equals(getPkg())) {
            imp = true;
            Version exp = export.getVersion();
            if(exp == null) {
                exp = new Version(0, 0, 0);
            }

            if(getMin() != null) {
                imp = exp.compareTo(getMin()) > (isMinExclusive() ? 0 : -1);
            }

            if(imp && getMax() != null) {
                imp = exp.compareTo(getMax()) < (isMaxExclusive() ? 0 : -1);
            }
        }
        
        return imp;
    }

    private final String pkg;

    /**
     * Get the value of pkg
     *
     * @return the value of pkg
     */
    public String getPkg() {
        return pkg;
    }

    private boolean minExclusive = false;

    /**
     * Get the value of minExclusive
     *
     * @return the value of minExclusive
     */
    public boolean isMinExclusive() {
        return minExclusive;
    }

    private boolean maxExclusive = false;

    /**
     * Get the value of maxExclusive
     *
     * @return the value of maxExclusive
     */
    public boolean isMaxExclusive() {
        return maxExclusive;
    }

    private Version max;

    /**
     * Get the value of max
     *
     * @return the value of max
     */
    public Version getMax() {
        return max;
    }

    private Version min;

    /**
     * Get the value of min
     *
     * @return the value of min
     */
    public Version getMin() {
        return min;
    }

    private String version;

    /**
     * Get the value of version
     *
     * @return the value of version
     */
    public String getVersion() {
        return version;
    }

    private boolean optional = false;

    /**
     * Get the value of optional
     *
     * @return the value of optional
     */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(150);
        sb.append(pkg);
        if(version != null) {
            sb.append(";version=\"").append(version).append("\"");
        }
        if(isOptional()) {
            sb.append(";resolution:=optional");
        }
        return sb.toString();
    }
}
