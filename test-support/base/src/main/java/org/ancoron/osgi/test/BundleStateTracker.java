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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 *
 * @author ancoron
 */
public class BundleStateTracker implements Callable<Boolean>, BundleListener {

    private final int state;
    private final long bundleId;
    private int currentState;
    
    public BundleStateTracker(long bundleId, int state) {
        this.bundleId = bundleId;
        this.state = state;
    }
    
    @Override
    public Boolean call() throws Exception {
        while(currentState != state) {
            Thread.sleep(100);
        }
        return Boolean.TRUE;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if(event.getBundle().getBundleId() == bundleId) {
            switch(event.getType()) {
                case BundleEvent.INSTALLED:
                    currentState = Bundle.INSTALLED;
                    break;
                case BundleEvent.RESOLVED:
                    currentState = Bundle.RESOLVED;
                    break;
                case BundleEvent.STARTING:
                    currentState = Bundle.STARTING;
                    break;
                case BundleEvent.STARTED:
                    currentState = Bundle.ACTIVE;
                    break;
                case BundleEvent.STOPPING:
                    currentState = Bundle.STOPPING;
                    break;
                case BundleEvent.STOPPED:
                    currentState = event.getBundle().getState();
                    break;
                case BundleEvent.UNINSTALLED:
                    currentState = Bundle.UNINSTALLED;
                    break;
                default:
                    break;
            }
        }
    }
}
