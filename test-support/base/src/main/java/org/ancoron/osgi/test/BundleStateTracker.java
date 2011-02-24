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

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 *
 * @author ancoron
 */
public class BundleStateTracker implements Callable<Boolean>, BundleListener {

    private static final Logger log = Logger.getLogger("BundleStateTracker");
    
    private final int state;
    private final long bundleId;
    private boolean ready = false;
    
    public BundleStateTracker(long bundleId, int state) {
        this.bundleId = bundleId;
        this.state = state;
    }
    
    @Override
    public Boolean call() throws Exception {
        while(!ready) {
            Thread.sleep(100);
        }
        
        log.log(Level.INFO, "Bundle [{0}] reached target state {1}",
                new Object[]{bundleId, toString(state)});

        return Boolean.TRUE;
    }

    public boolean isReady() {
        return ready;
    }
    
    protected String toString(int bundleState) {
        switch(bundleState) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if(event.getBundle().getBundleId() == bundleId) {
            if(event.getType() == state) {
                log.log(Level.INFO, "Bundle [{0}] reached target state {1}",
                        new Object[]{bundleId, toString(state)});
                ready = true;
            }
        }
    }
}
