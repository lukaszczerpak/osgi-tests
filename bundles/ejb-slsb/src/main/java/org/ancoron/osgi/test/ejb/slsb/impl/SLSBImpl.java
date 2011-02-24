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

package org.ancoron.osgi.test.ejb.slsb.impl;

import javax.ejb.Local;
import javax.ejb.Stateless;
import org.ancoron.osgi.test.ejb.slsb.SLSBInterface;

/**
 *
 * @author ancoron
 */
@Stateless(name="SimpleSLSB", mappedName="java:global/osgi-test/slsb/SimpleSLSB")
@Local(SLSBInterface.class)
public class SLSBImpl implements SLSBInterface {

    @Override
    public String getName() {
        return "Jack";
    }

    @Override
    public void setname(String name) {
        System.err.println("Setting a name is not yet supported");
    }
}
