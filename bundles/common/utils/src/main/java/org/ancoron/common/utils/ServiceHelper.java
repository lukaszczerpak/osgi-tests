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

package org.ancoron.common.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancoron.common.service.ServiceAccessException;
import org.ancoron.common.service.ServiceUnavailableException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author ancoron
 */
public class ServiceHelper {
    
    private static final Logger log = Logger.getLogger(ServiceHelper.class.getPackage().getName());
    
    public static BundleContext getBundleContext(final Class clazz) {
        return BundleReference.class.cast(clazz.getClassLoader()).getBundle().getBundleContext();
    }

    public static <T> T lookup(final Class<T> clazz, final String filter) throws ServiceAccessException {
        T inst = null;
        BundleContext ctx = getBundleContext(clazz);

        try {
            ServiceReference[] refs = ctx.getServiceReferences(clazz.getName(), filter);
            if(refs == null || refs.length == 0) {
                throw new ServiceUnavailableException("No service available for class '"
                        + clazz.getName() + "' and filter '" + filter + "'");
            } else if(refs.length > 1) {
                log.log(Level.WARNING, "Found more than one matching services for class "
                        + "''{0}'' and filter ''{1}''",
                        new Object[]{clazz.getName(), filter});
            }
            
            inst = (T) ctx.getService(refs[0]);
        } catch(InvalidSyntaxException x) {
            throw new ServiceAccessException(x);
        }
        
        if(inst == null) {
            throw new ServiceUnavailableException(
                    "Unable to get an instance for service '" + clazz.getName() + "'");
        }
        
        return inst;
    }
}
