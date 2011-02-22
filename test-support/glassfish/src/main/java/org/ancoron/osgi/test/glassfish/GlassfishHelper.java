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
package org.ancoron.osgi.test.glassfish;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ancoron
 */
public class GlassfishHelper {
    
    private final static int BUFFER = 4 * 1024;
    private final static String DOWNLOAD_BASE;
    
    private static final Logger log = Logger.getLogger("glassfishtest");
    
    static {
        String dlProp = System.getProperty(GlassfishConfig.DOWNLOAD_URL);
        if(dlProp != null) {
            DOWNLOAD_BASE = dlProp;
        } else {
            DOWNLOAD_BASE = "http://dlc.sun.com.edgesuite.net/glassfish/v3.1/promoted";
        }
    }
    
    public static void installGlassfish(String finalPath, String version) throws IOException {
        File f = File.createTempFile("glassfish", null);
        f.delete();
        String tmpPath = f.getCanonicalPath();
        
        String url = DOWNLOAD_BASE + "/glassfish-" + version + ".zip";
        download(url, tmpPath);
        
        unzip(tmpPath, finalPath);

        f.delete();
        f = null;
    }
    
    protected static String getSysProp(String name) {
        return System.getProperty(name);
    }
    
    protected static File getDomainDir() throws IOException {
        String dir = getSysProp(GlassfishConfig.DOMAIN_DIR);
        
        if(dir == null) {
            dir = "target/test-domain";
        }
        
        return new File(dir);
    }

    protected static File getConfiguredFile(String sysProp,
            String defaultLocation, boolean checkExists)
    {
        File xmlFile = null;
        String xml = getSysProp(sysProp);
        
        if(xml == null) {
            xml = defaultLocation;
        }

        xmlFile = new File(xml);
        
        if(checkExists && !xmlFile.exists()) {
            xmlFile = null;
        }
        
        return xmlFile;
    }
    
    protected static File getDomainDirOverlay() throws IOException {
        String xml = "src/test/resources/glassfish-domain";
        File xmlFile = new File(xml);
        
        if(!xmlFile.exists()) {
            xmlFile = null;
        } else if(!xmlFile.isDirectory()) {
            throw new IOException("Must be a directory: " + xmlFile.getAbsolutePath());
        }
        
        return xmlFile;
    }
    
    public static void configureGlassfish(final String installPath)
            throws IOException
    {
        final File domainDir = getDomainDir();
        
        if(!domainDir.exists()) {
            log.log(Level.INFO, "Preparing GlassFish domain: {0}", domainDir.getCanonicalPath());
            domainDir.mkdirs();
            File orig = new File(installPath, "glassfish3/glassfish/domains/domain1");
            FileUtils.copyDirectory(orig, domainDir, false);
            
            File tmp = new File(domainDir, "applications");
            if(tmp.exists()) {
                FileUtils.cleanDirectory(tmp);
            }

            tmp = new File(domainDir, "generated");
            if(tmp.exists()) {
                FileUtils.cleanDirectory(tmp);
            }

            tmp = new File(domainDir, "osgi-cache");
            if(tmp.exists()) {
                FileUtils.cleanDirectory(tmp);
            }

            tmp = new File(domainDir, "logs");
            if(tmp.exists()) {
                FileUtils.cleanDirectory(tmp);
            }
        }
        
        File config = getDomainDirOverlay();
        if(config != null) {
            FileUtils.copyDirectory(config, domainDir, false);
        }
        
        System.setProperty("com.sun.aas.instanceRoot", domainDir.getCanonicalPath());
    }

    public static void unzip(String zipFile, String targetPath) throws IOException {
        log.log(Level.INFO, "Extracting {0} ...", zipFile);
        log.log(Level.INFO, "...target directory: {0}", targetPath);
        File path = new File(targetPath);
        path.delete();
        path.mkdirs();
        
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
            if(entry.isDirectory()) {
                File f = new File(targetPath + "/" + entry.getName());
                f.mkdirs();
                continue;
            }
            int count;
            byte data[] = new byte[BUFFER];
            
            // write the files to the disk
            FileOutputStream fos = new FileOutputStream(targetPath + "/" + entry.getName());
            dest = new BufferedOutputStream(fos, BUFFER);
            while((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }

    public static void download(String url, String toFile) throws MalformedURLException, IOException {
        log.log(Level.INFO, "Downloading {0} ...", url);
        
        long startTime = System.currentTimeMillis();

        URL intUrl = new URL(url);
        intUrl.openConnection();
        InputStream reader = intUrl.openStream();

        /*
         * Setup a buffered file writer to write
         * out what we read from the website.
         */
        FileOutputStream writer = new FileOutputStream(toFile);

        try {
            byte[] buffer = new byte[BUFFER];
            int totalBytesRead = 0;
            int bytesRead = 0;

            // log.info("Reading ZIP file 150KB blocks at a time.\n");

            while ((bytesRead = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            long endTime = System.currentTimeMillis();

            log.log(Level.INFO, "Done. {0} bytes read ({1} milliseconds).",
                    new Object[]{new Integer(totalBytesRead).toString(),
                        new Long(endTime - startTime).toString()});
        } finally {
            writer.close();
            reader.close();
        }
    }
}
