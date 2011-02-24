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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import org.ancoron.osgi.test.ejb.slsb.SLSBInterface;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

/**
 *
 * @author ancoron
 */
public class GlassfishDerbyTest extends GlassfishDerbyTestSupport {

    private static final Logger log = Logger.getLogger(GlassfishDerbyTest.class.getName());
    private final List<String> logLines = new ArrayList<String>();

    @Test(timeOut=10000, dependsOnGroups={"glassfish-osgi-startup"})
    public void testSLSBService() {
        logTest();

        services.put(SLSBInterface.class.getName(), null);
        
        waitForServices();
        
        services.clear();
    }
    
    
    @Override
    public void stopFramework() {
        BundleContext ctx = getFramework().getBundleContext();
        List<Bundle> b = new ArrayList<Bundle>();
        for(final Bundle bundle : ctx.getBundles()) {
            b.add(bundle);
        }
        
        Collections.sort(b, new BundleComparator());

        for(Bundle tmp : b) {
            logLines.add(String.format("[%4d][%11s] %s", tmp.getBundleId(), toString(tmp.getState()), tmp.getSymbolicName()));
        }

        super.stopFramework();
    }

    public class BundleComparator implements Comparator<Bundle> {

        @Override
        public int compare(Bundle o1, Bundle o2) {
            if(o1.getBundleId() == o2.getBundleId()) {
                return 0;
            } else if(o1.getBundleId() < o2.getBundleId()) {
                return -1;
            }
            return 1;
        }
    }
    
    @AfterSuite(alwaysRun=true)
    @Override
    public void shutdown() {
        super.shutdown();
        
        StringBuilder sb = new StringBuilder();
        for(String tmp : logLines) {
            sb.append("\n").append(tmp);
        }
        
        log.info(sb.toString());
    }
}
