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

package org.ancoron.osgi.test.felix;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ancoron.osgi.test.helloservice.HelloService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author ancoron
 */
public class FelixTest extends FelixTestSupport {
    
    @Test(timeOut=10000L)
    public void uninstallSimpleBundle() {
        logTest();

        Bundle serviceBundle = null;
        for(Bundle b : bundles) {
            if("org.ancoron.osgi.test.bundles-simple".equals(b.getSymbolicName())) {
                serviceBundle = b;
                break;
            }
        }
        
        uninstallBundle(serviceBundle);
    }
    
    @Test(timeOut=10000L)
    public void installHelloService() {
        logTest();

        installAndStart("org.ancoron.osgi.test:hello-service");
        
        HelloService svc = waitForService(HelloService.class);
        
        if(svc == null) {
            Assert.fail("Unable to get HelloService");
        }
    }
    
    @Test(timeOut=10000L, dependsOnMethods="installHelloService")
    public void uninstallHelloService() {
        logTest();

        Bundle helloBundle = getBundlesBySymbolicName("org.ancoron.osgi.test.hello-service").iterator().next();
        uninstallBundle(helloBundle);
    }
    
    @Test(timeOut=10000L, dependsOnMethods="uninstallHelloService")
    public void testWaitForService() throws InterruptedException, ExecutionException {
        logTest();

        Bundle hostBundle = null;
        for(Bundle b : bundles) {
            if("org.ancoron.osgi.test.fragments-simple-host".equals(b.getSymbolicName())) {
                hostBundle = b;
                break;
            }
        }
        
        final Bundle client = hostBundle;

        Future<Object> svc = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                ServiceTracker st = new ServiceTracker(client.getBundleContext(),
                        HelloService.class.getName(), null);
                st.open();
                try {
                    return st.waitForService(10000L);
                } finally {
                    st.close();
                }
            }
        });
        
        // installBundle(MavenHelper.getArtifact("org.ancoron.osgi.test", "hello-service", "1.0.1-SNAPSHOT", "jar"),
        //         getFramework().getBundleContext());
        
        installAndStart("org.ancoron.osgi.test:hello-service");
        
        svc.get();
    }

    @Test(timeOut=10000L, dependsOnMethods="testWaitForService")
    public void testWaitForService2() throws InterruptedException, ExecutionException {
        logTest();

        Bundle hostBundle = null;
        Bundle helloBundle = getBundlesBySymbolicName("org.ancoron.osgi.test.hello-service").iterator().next();
        for(Bundle b : bundles) {
            if("org.ancoron.osgi.test.fragments-simple-host".equals(b.getSymbolicName())) {
                hostBundle = b;
                break;
            }
        }
        
        final Bundle client = hostBundle;

        uninstallBundle(helloBundle);
        installAndStart("org.ancoron.osgi.test:hello-service");

        Future<Object> svc = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                ServiceTracker st = new ServiceTracker(client.getBundleContext(),
                        HelloService.class.getName(), null);
                st.open();
                try {
                    return st.waitForService(5000L);
                } finally {
                    st.close();
                }
            }
        });
        
        Object o = svc.get();
        
        if(o != null) {
            Assert.fail("I was NOT expecting to get a valid service instance!");
        }
    }
    
    @Test(timeOut=10000L, dependsOnMethods="testWaitForService")
    public void testWaitForService3() throws InterruptedException, ExecutionException {
        logTest();

        Bundle helloBundle = getBundlesBySymbolicName("org.ancoron.osgi.test.hello-service").iterator().next();
        uninstallBundle(helloBundle);
        installAndStart("org.ancoron.osgi.test:hello-service");

        Future<Object> svc = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                ServiceTracker st = new ServiceTracker(getFramework().getBundleContext(),
                        HelloService.class.getName(), null);
                st.open();
                try {
                    return st.waitForService(5000L);
                } finally {
                    st.close();
                }
            }
        });
        
        Object o = svc.get();
        
        if(o == null) {
            Assert.fail("I didn't get a valid service instance!");
        }
    }
    
    @Test(timeOut=10000L, alwaysRun=true, dependsOnMethods="testWaitForService3")
    public void testWaitForService4() throws InterruptedException, ExecutionException {
        logTest();

        Bundle helloBundle = getBundlesBySymbolicName("org.ancoron.osgi.test.hello-service").iterator().next();
        uninstallBundle(helloBundle);
        final BundleContext client = installAndStart("org.ancoron.osgi.test:hello-client").getBundleContext();
        installAndStart("org.ancoron.osgi.test:hello-service");

        Future<Object> svc = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                ServiceTracker st = new ServiceTracker(client,
                        HelloService.class.getName(), null);
                st.open();
                try {
                    return st.waitForService(5000L);
                } finally {
                    st.close();
                }
            }
        });
        
        Object o = svc.get();
        
        if(o == null) {
            Assert.fail("I didn't get a valid service instance!");
        }
    }
}
