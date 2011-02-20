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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;

/**
 *
 * @author ancoron
 */
public class ServiceAvailabilityChecker implements Callable<Boolean> {

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
            Thread.sleep(1000);
            ready = checkServices();
        }
        
        return ready;
    }

    private Boolean checkServices() throws Exception {
        boolean ready = true;
        ExecutorService svc = Executors.newFixedThreadPool(services.size());
        Set<ServiceCheck> tasks = new HashSet<ServiceCheck>();
        for(String clazz : services.keySet()) {
            String filter = services.get(clazz);
            tasks.add(new ServiceCheck(ctx, clazz, filter));
        }

        List<Future<Boolean>> futes = svc.invokeAll(tasks, 1000, TimeUnit.MILLISECONDS);

        for(Future<Boolean> task : futes) {
            Boolean b = Boolean.FALSE;
            try {
                b = task.get();
            } catch (Exception ex) {
                System.err.println("Unable to get service: " + ex);
            }
            if(!b.booleanValue()) {
                ready = false;
            }
        }
        
        return ready;
    }
}
