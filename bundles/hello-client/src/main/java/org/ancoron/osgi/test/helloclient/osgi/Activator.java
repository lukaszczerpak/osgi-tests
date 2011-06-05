package org.ancoron.osgi.test.helloclient.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;

public class Activator implements BundleActivator {

    private ServiceListener listener = null;
    
    @Override
    public void start(BundleContext context) throws Exception {
        listener = new HelloServiceListener();
        context.addServiceListener(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.removeServiceListener(listener);
    }

}
