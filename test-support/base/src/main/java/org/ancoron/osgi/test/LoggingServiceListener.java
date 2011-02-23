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

import java.util.Arrays;
import java.util.logging.Logger;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author ancoron
 */
public class LoggingServiceListener implements ServiceListener {

    private static final Logger log = Logger.getLogger("GenericOSGiListener");

    @Override
    public void serviceChanged(ServiceEvent event) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("[ServiceEvent][");
        sb.append(toString(event.getType()));
        sb.append("] ");
        append(sb, event.getServiceReference());
        
        log.info(sb.toString());
    }
    
    protected String toString(int eventType) {
        final String event;
        
        switch(eventType) {
            case ServiceEvent.REGISTERED:
                event = "REGISTERED";
                break;
            case ServiceEvent.UNREGISTERING:
                event = "UNREGISTERING";
                break;
            case ServiceEvent.MODIFIED:
                event = "MODIFIED";
                break;
            case ServiceEvent.MODIFIED_ENDMATCH:
                event = "MODIFIED_ENDMATCH";
                break;
            default:
                event = "UNKNOWN";
                break;
        }
        
        return event;
    }

    protected void append(final StringBuilder sb, ServiceReference ref) {
        property(sb, Constants.SERVICE_ID, ref, false);
        property(sb, Constants.OBJECTCLASS, ref, true);
        property(sb, Constants.SERVICE_PID, ref, true);
    }
    
    protected void property(final StringBuilder sb, String property, ServiceReference ref, boolean add) {
        Object prop = ref.getProperty(property);

        if(prop != null) {
            if(add) {
                sb.append(", ");
            }

            if(prop.getClass().isArray()) {
                prop = Arrays.toString((Object[]) prop);
            }

            sb.append(property).append("=").append(prop);
        }
    }
}
