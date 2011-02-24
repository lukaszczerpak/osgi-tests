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

import java.io.PrintWriter;
import org.apache.derby.drda.NetworkServerControl;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 *
 * @author ancoron
 */
public class GlassfishDerbyTestSupport extends GlassfishTestSupport {

    private NetworkServerControl derby;
    
    @Override
    public void startFramework() {
        try {
            System.setProperty("derby.stream.error.file", "target/derby.log");
            derby = new NetworkServerControl();
            derby.start(new PrintWriter(System.out));
            // derby.setTraceDirectory("target/derby");
            // derby.trace(true);
            derby.logConnections(true);
        } catch(Exception x) {
            fail("Unable to start derby network server", x);
        }
        
        super.startFramework();
    }

    @Test(groups={"glassfish-osgi-startup"}, dependsOnGroups={"felix-osgi-startup"})
    public void testDerbyAvailable() {
        try {
            derby.ping();
        } catch(Exception x) {
            fail("Unable to connect to derby network server", x);
        }
    }

    @Override
    public void stopFramework() {
        super.stopFramework();
        
        if(derby != null) {
            try {
                derby.shutdown();
            } catch (Exception ex) {
                fail("Unable to stop derby network server", ex);
            }
        }
    }
}
