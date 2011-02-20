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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author ancoron
 */
public class ServiceCheck implements Callable<Boolean> {
    
    private final String clazz;
    private final String filter;
    private final BundleContext ctx;
    private boolean state = false;
    
    public ServiceCheck(BundleContext ctx, String clazz, String filter) {
        this.ctx = ctx;
        this.clazz = clazz;
        this.filter = filter;
    }
    
    @Override
    public Boolean call() throws Exception {
        ServiceReference[] svcs = null;
        try {
            svcs = ctx.getServiceReferences(clazz, filter.toString());
        } catch (Exception ex) {
            System.err.println("Unable to check service availability for class '"
                    + clazz + "' (filter=" + filter + ")");
            ex.printStackTrace(System.err);
        }

        if(svcs != null && svcs.length < 1) {
            for(ServiceReference ref : svcs) {
                if(ctx.getService(ref) != null) {
                    // if at least one service is available...
                    state = true;
                }
            }
        }
        
        return Boolean.valueOf(state);
    }
}
