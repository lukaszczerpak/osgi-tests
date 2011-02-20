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

import java.io.File;
import java.util.List;

/**
 *
 * @author ancoron
 */
public class MavenHelper
{
    private static final String mavenRepo;
    
    static {
        mavenRepo = System.getProperty("maven.repo");
    }

    public static List<File> getProvidedArtifacts() throws Exception
    {
        POMReader reader = new POMReader("pom.xml", mavenRepo);
        reader.read();
        
        return reader.getProvided();
    }
}
