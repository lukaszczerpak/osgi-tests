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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author ancoron
 */
public class ServiceAvailabilityChecker implements Callable<Boolean> {

    private static final Logger log = Logger.getLogger("GenericOSGiServices");

    private final BundleContext ctx;
    private final Map<String, String> services;
    
    public ServiceAvailabilityChecker(Map<String, String> services, BundleContext ctx) {
        this.services = services;
        this.ctx = ctx;
    }

    @Override
    public Boolean call() throws Exception {
        boolean ready = checkServices();
        
        while(!ready) {
            Thread.sleep(250);
            ready = checkServices();
        }
        
        return ready;
    }

    private Boolean checkServices() throws Exception {
        boolean ready = true;
        for(String clazz : services.keySet()) {
            String filter = services.get(clazz);
            ServiceReference[] svcs = null;
            try {
                svcs = ctx.getServiceReferences(clazz, filter);
            } catch (Exception ex) {
                log.log(Level.WARNING,
                        "Unable to check service availability for class ''{0}'' (filter={1})",
                        new Object[]{clazz, filter});
            }
            
            ready = false;

            if(svcs != null && svcs.length > 0) {
                for(ServiceReference ref : svcs) {
                    if(ctx.getService(ref) != null) {
                        // if at least one service is available...
                        ready = true;
                    }
                }
            }

            if(!ready) {
                break;
            }
        }
        
        return ready;
    }
}
