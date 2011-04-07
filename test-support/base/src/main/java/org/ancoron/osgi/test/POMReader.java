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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 * A Maven project XML file reader.
 *
 * @author ancoron
 */
public class POMReader
{
    private static final Logger log = Logger.getLogger("MavenPOMReader");

    private static final String ELEMENT_PROJECT = "project";
    private static final String ELEMENT_PARENT = "parent";
    private static final String ELEMENT_GROUPID = "groupId";
    private static final String ELEMENT_ARTIFACTID = "artifactId";
    private static final String ELEMENT_VERSION = "version";
    private static final String ELEMENT_SCOPE = "scope";
    private static final String ELEMENT_TYPE = "type";
    private static final String ELEMENT_PACKAGING = "packaging";
    private static final String ELEMENT_DEPENDENCIES = "dependencies";
    private static final String ELEMENT_DEPENDENCY = "dependency";
    private static final String ELEMENT_DEPENDENCYMGMT = "dependencyManagement";
    private static final String ELEMENT_PROPERTIES = "properties";

    private final XMLInputFactory inputFactory;
    private final String xmlFile;
    private final String mavenRepo;
    private XMLStreamReader xml = null;
    private Map<String, String> dependencies = new LinkedHashMap<String, String>();
    private Properties properties = new Properties();
    private final Deque<String> stack = new ArrayDeque<String>();
    private String[] dependency = new String[5];
    private String[] parent = new String[4];
    private String[] project = new String[4];
    private boolean inProperties = false;
    private boolean inDependencyMgmt = false;
    private String currentProperty = null;
    private List<String[]> managedDeps = new ArrayList<String[]>();

    public POMReader(String xmlFile, String mavenRepo)
    {
        this.inputFactory = XMLInputFactory.newInstance();
        this.xmlFile = xmlFile;
        this.mavenRepo = mavenRepo;
    }

    public Properties getProperties()
    {
        return properties;
    }
    
    public String getVersion() {
        String version = project[2];
        
        if(version == null) {
            version = parent[2];
        }
        
        return version;
    }

    public List<File> getProvided()
    {
        String[] cp = System.getProperty("java.class.path").split(":");
        List<File> files = new ArrayList<File>();

        for (String path : dependencies.keySet()) {
            if (dependencies.get(path).equals("provided")) {
                File file = null;
                for (String entry : cp) {
                    if (entry.endsWith(path)) {
                        file = new File(entry);
                        break;
                    }
                }

                if(file == null) {
                    // not in class-path, add manually...
                    file = new File(mavenRepo, path);
                }

                log.log(Level.INFO, "Analyzing dependency ''{0}'' ...", file.getAbsolutePath());
                if (!file.exists()) {
                    throw new IllegalArgumentException("Unable to resolve dependency: " + path);
                }
                files.add(file);
            }
        }

        return files;
    }

    public void read() throws XMLStreamException, FileNotFoundException
    {
        read(new FileInputStream(xmlFile));
    }

    protected void read(final InputStream is) throws XMLStreamException
    {
        xml = inputFactory.createXMLStreamReader(is);

        String name = null;

        while (xml.hasNext()) {
            switch (xml.next()) {
                case XMLEvent.START_ELEMENT:
                    name = xml.getLocalName();
                    handleElement(name);
                    stack.push(name);
                    break;
                case XMLEvent.CHARACTERS:
                    name = xml.getText();
                    handleValue(name);
                    break;
                case XMLEvent.END_ELEMENT:
                    name = stack.pop();
                    finishElement(name);
                    break;
                default:
                    // nothing to do here...
                    break;
            }
        }
        xml.close();
    }

    protected void handleElement(final String name) throws XMLStreamException
    {
        if (ELEMENT_PARENT.equals(name)) {
            dependency = new String[5];
        } else if (ELEMENT_DEPENDENCYMGMT.equals(name)) {
            inDependencyMgmt = true;
        } else if (ELEMENT_DEPENDENCY.equals(name)) {
            dependency = new String[5];
        } else if (ELEMENT_PROPERTIES.equals(name) && stack.size() == 1) {
            inProperties = true;
        } else if (inProperties) {
            currentProperty = name;
        }
    }

    protected void finishElement(final String name) throws XMLStreamException
    {
        if (ELEMENT_PARENT.equals(name)) {
            parent = dependency;

            String path = parent[0].replaceAll("\\.", "/") + "/"
                    + parent[1] + "/" + parent[2] + "/"
                    + parent[1] + "-" + parent[2] + ".pom";

            File parentPom = new File(mavenRepo, path);
            try {
                POMReader reader = new POMReader(parentPom.getCanonicalPath(), mavenRepo);
                reader.read();
                Properties props = reader.getProperties();
                for (String key : props.stringPropertyNames()) {
                    if (!properties.containsKey(key)) {
                        properties.put(key, props.get(key));
                    }
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Unable to load parent pom from '"
                        + parentPom.getAbsolutePath() + "'", ex);
            }

            project = parent;
            dependency = new String[5];
        } else if (ELEMENT_PROPERTIES.equals(name)) {
            inProperties = false;
        } else if (inProperties) {
            currentProperty = null;
        } else if (ELEMENT_PROJECT.equals(stack.peek())) {
            if (ELEMENT_GROUPID.equals(name)) {
                project[0] = dependency[0];
            } else if (ELEMENT_ARTIFACTID.equals(name)) {
                project[1] = dependency[1];
            } else if (ELEMENT_VERSION.equals(name)) {
                project[2] = dependency[2];
            }
        } else if (ELEMENT_DEPENDENCY.equals(name)) {
            if (ELEMENT_DEPENDENCIES.equals(stack.peek()) && stack.size() == 2) {
                if (dependency[0].equals("${project.groupId}") || dependency[0].equals("${groupId}")) {
                    // project groupId...
                    dependency[0] = project[0];
                }
                if (dependency[1].equals("${project.artifactId}") || dependency[1].equals("${artifactId}")) {
                    // project artifactId...
                    dependency[1] = project[1];
                }

                if(dependency[2] == null) {
                    throw new XMLStreamException("Please define the <version> for dependency "
                            + dependency[0] + ":" + dependency[1]);
                }

                if (dependency[2].equals("${project.version}") || dependency[2].equals("${version}")) {
                    // project version...
                    dependency[2] = project[2];
                } else if (dependency[2].startsWith("${") && dependency[2].endsWith("}")) {
                    // resolve properties...
                    dependency[2] = properties.getProperty(dependency[2].substring(2, dependency[2].length() - 1));
                }

                dependency[3] = dependency[3] == null ? "compile" : dependency[3];
                dependency[4] = dependency[4] == null ? "jar" : dependency[4];

                log.log(Level.INFO, "Parsed dependency {0}:{1}:{2}:{3}:{4} ...",
                        new Object[]{dependency[0], dependency[1], dependency[2], dependency[4], dependency[3]});
                String path = dependency[0].replaceAll("\\.", "/") + "/"
                        + dependency[1] + "/" + dependency[2] + "/"
                        + dependency[1] + "-" + dependency[2] + "."
                        + dependency[4];


                dependencies.put(path, dependency[3]);
            } else if (ELEMENT_DEPENDENCIES.equals(stack.peek()) && inDependencyMgmt) {
                if (dependency[0].equals("${project.groupId}") || dependency[0].equals("${groupId}")) {
                    // project groupId...
                    dependency[0] = project[0];
                }
                if (dependency[1].equals("${project.artifactId}") || dependency[1].equals("${artifactId}")) {
                    // project artifactId...
                    dependency[1] = project[1];
                }

                if(dependency[2] == null) {
                    throw new XMLStreamException("Please define the <version> for dependency "
                            + dependency[0] + ":" + dependency[1]);
                }

                if (dependency[2].equals("${project.version}") || dependency[2].equals("${version}")) {
                    // project version...
                    dependency[2] = project[2];
                } else if (dependency[2].startsWith("${") && dependency[2].endsWith("}")) {
                    // resolve properties...
                    dependency[2] = properties.getProperty(dependency[2].substring(2, dependency[2].length() - 1));
                }
                
                dependency[3] = dependency[3] == null ? "compile" : dependency[3];
                dependency[4] = dependency[4] == null ? "jar" : dependency[4];

                log.log(Level.INFO, "Parsed managed dependency {0}:{1}:{2}:{3}:{4} ...",
                        new Object[]{dependency[0], dependency[1], dependency[2], dependency[4], dependency[3]});

                managedDeps.add(dependency);
            }
            dependency = new String[5];
        } else if(ELEMENT_DEPENDENCYMGMT.equals(name)) {
            inDependencyMgmt = false;
        }
    }

    protected void handleValue(String value)
    {
        if (inProperties && currentProperty != null) {
            properties.put(currentProperty, xml.getText());
        } else if (!stack.contains("plugin") && !stack.contains("extension")) {
            if (stack.peek().equals(ELEMENT_GROUPID)) {
                dependency[0] = xml.getText();
            } else if (stack.peek().equals(ELEMENT_ARTIFACTID)) {
                dependency[1] = xml.getText();
            } else if (stack.peek().equals(ELEMENT_VERSION)) {
                dependency[2] = xml.getText();
            } else if (stack.peek().equals(ELEMENT_SCOPE)) {
                dependency[3] = xml.getText();
            } else if (stack.peek().equals(ELEMENT_TYPE)) {
                dependency[4] = xml.getText();
            }
        }
    }
}
