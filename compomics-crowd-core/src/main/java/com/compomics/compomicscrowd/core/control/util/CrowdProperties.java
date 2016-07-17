/* 
 * Copyright 2016 Kenneth Verheggen <kenneth.verheggen@gmail.com>.
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
package com.compomics.compomicscrowd.core.control.util;

import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
/**
 *
 * @author Kenneth Verheggen
 */
public class CrowdProperties extends NetworkProperties {

    /**
     * The Logger Instance
     */
    private static final Logger LOGGER = Logger.getLogger(CrowdProperties.class);
    /**
     * The URL to a workable DeNovoGUI version
     */
    private String deNovoGUIAddress = "http://genesis.ugent.be/maven2/com/compomics/denovogui/DeNovoGUI/1.12.1/DeNovoGUI-1.12.1-windows.zip";

    private static File getConfigFolder() throws URISyntaxException {
        return new File(System.getProperty("user.home") + "/pladipus/screensaver/config");
    }

    /**
     *
     * @return an initiated instance of the network properties
     */
    public static CrowdProperties getInstance() {
        if (instance == null) {
            instance = new CrowdProperties();
            try {
                configFolder = getConfigFolder();
            } catch (URISyntaxException ex) {
                LOGGER.warn("Could not find property file, changing to defaults");
                configFolder = new File(System.getProperty("user.home"));
            }
            defaultPropFile = new File(configFolder, "crowd.properties");
            if (!defaultPropFile.exists()) {
                copyFromResources();
            }
            try {
                instance.load(defaultPropFile);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
        return (CrowdProperties) instance;
    }

    private static void copyFromResources() {
        //loads the codontable from within the jar...
        if (defaultPropFile != null && !defaultPropFile.exists()) {
            defaultPropFile.getParentFile().mkdirs();
            try {
                defaultPropFile.createNewFile();
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
            try (OutputStream outputStream = new FileOutputStream(defaultPropFile); InputStream inputStream = new ClassPathResource("crowd.properties").getInputStream()) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
    }

    public File getDeNovoGUIJar() throws IOException, UnspecifiedPladipusException {
        return new File(instance.getProperty("DeNovoGUI.jar", downloadJar().getAbsolutePath()));
    }

    /**
     * Gets the jar for DeNovoGUI and downloads if absent...
     *
     * @return the DeNovoGUI jar
     * @throws IOException if the filesystem can not run the jar
     * @throws UnspecifiedPladipusException if the jar cannot be found or
     * downloaded
     */
    public File downloadJar() throws IOException, UnspecifiedPladipusException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "DeNovoGUI");
        if (!temp.exists()) {
            File searchGUIFile = PladipusFileDownloadingService.downloadFile(deNovoGUIAddress, toolFolder);
            if (searchGUIFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(searchGUIFile, temp);
            }
        }
        return JarLookupService.lookupFile("DeNovoGUI-.*.jar", temp);
    }

    /**
     *
     * @return the host the pladipus MYSQL is running on
     */
    @Override
    public String getStateDatabaseLocation() {
        return "NOT SUPPORTED IN SCREENSAVER MODE";
    }

    /**
     *
     * @return the login for the state database
     */
    @Override
    public String getStateDatabaseLogin() {
        return "NOT SUPPORTED IN SCREENSAVER MODE";
    }

    /**
     *
     * @return the login for the state database
     */
    @Override
    public String getStateDatabasePassWord() {
        return "NOT SUPPORTED IN SCREENSAVER MODE";
    }

}
