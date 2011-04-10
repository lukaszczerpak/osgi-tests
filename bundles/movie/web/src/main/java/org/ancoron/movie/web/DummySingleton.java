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
package org.ancoron.movie.web;

import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;

/**
 *
 * @author ancoron
 */
@Singleton
@EJB(name = "java:global/dummy/DummySingleton", beanInterface = DummySingletonLocal.class)
@DenyAll
public class DummySingleton implements DummySingletonLocal {
    
    @Resource
    private EJBContext ctx;

    @RolesAllowed({"Test Group"})
    public String getName()
    {
        System.out.println("Called DummySingleton with '" + ctx.getCallerPrincipal() + "'...");

        return "Dummy Web";
    }
 
}
