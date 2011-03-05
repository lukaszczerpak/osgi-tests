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
import java.io.FileNotFoundException;
import java.util.List;
import javax.xml.stream.XMLStreamException;

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
    
    private static POMReader pom = null;
    
    private static POMReader read() throws XMLStreamException, FileNotFoundException {
        if(pom == null) {
            pom = new POMReader("pom.xml", mavenRepo);
            pom.read();
        }
        
        return pom;
    }

    public static List<File> getProvidedArtifacts() throws Exception
    {
        return read().getProvided();
    }
    
    public static File getArtifact(String id) {
        String[] ids = id.split(":");
        
        String[] props = new String[4];
        
        // groupId...
        props[0] = ids[0];

        // artifactId...
        props[1] = ids[1];
        
        // version...
        if(ids.length > 2) {
            props[2] = ids[2];
        } else {
            props[2] = pom.getVersion();
        }
        
        // type...
        if(ids.length > 3) {
            props[3] = ids[3];
        } else {
            props[3] = "jar";
        }
        
        return getArtifact(props[0], props[1], props[2], props[3]);
    }

    public static File getArtifact(String groupId, String artifactId, String version, String type) {
        if(type == null) {
            type = "jar";
        }
        
        String path = groupId.replaceAll("\\.", "/") + "/" + artifactId + "/"
                + version + "/" + artifactId + "-" + version + "." + type;
        
        return new File(mavenRepo, path);
    }
}
