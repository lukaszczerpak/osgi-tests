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

import static org.testng.Assert.*;

import java.util.Map;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.testng.annotations.Test;

/**
 *
 * @author ancoron
 */
public abstract class OSGiTestSupport<T extends Framework> {
    
    private T framework;
    
    public T getFramework() {
        return framework;
    }
    
    protected void setFramework(T framework) {
        this.framework = framework;
    }

    public abstract void configureFramework(final Map<String, String> properties);
    
    @Test(alwaysRun=true)
    public void startFramework() {
        try {
            getFramework().start();
        } catch (BundleException ex) {
            fail("Unable to start framework", ex);
        }
    }

    @Test(alwaysRun=true)
    public void stopFramework() {
        try {
            getFramework().stop();
        } catch (BundleException ex) {
            fail("Unable to stop framework", ex);
        }
    }
}
