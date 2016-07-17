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
package com.compomics.compomicscrowd.core.model.denovo;


import com.compomics.compomicscrowd.core.control.connect.CompomicsResultConsumer;
import com.compomics.compomicscrowd.core.control.connect.actors.DeNovoTaskPusher;
import com.compomics.compomicscrowd.core.control.util.CrowdProperties;
import com.compomics.compomicscrowd.model.denovo.DeNovoResult;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class DeNovoTask {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsResultConsumer.class);
    /**
     * A list of spectra
     */
    private final List<MSnSpectrum> spectra;
    /**
     * A identificationsparameters object
     */
    private final IdentificationParameters idParam;
    /**
     * a temp file
     */
    private File tempFile;
    /**
     * The local output folder
     */
    private File output;
    /**
     * an identifier for the task
     */
    private int identifier = 0;
    /**
     * The name of original inputfile
     */
    private final String fileName;

    /**
     * Generates a denovo task
     *
     * @param fileName the name of the original input file
     * @param spectra list of spectra
     */
    public DeNovoTask(String fileName, List<MSnSpectrum> spectra) {
        this.fileName = fileName;
        this.spectra = spectra;
        this.idParam = new IdentificationParameters();
    }

    /**
     * Generates a denovo task
     *
     * @param fileName the name of the original input file
     * @param spectra list of spectra
     * @param param the identification parameters
     */
    public DeNovoTask(String fileName, List<MSnSpectrum> spectra, IdentificationParameters param) {
        this.fileName = fileName;
        this.spectra = spectra;
        this.idParam = param;
    }

    /**
     * Sets the identifier
     *
     * @param identifier the identifier
     */
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    private File writeToTempFile() throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        try (FileWriter out = new FileWriter(tempFile)) {
            for (MSnSpectrum aSpectrum : spectra) {
                out.append(aSpectrum.asMgf()).append(System.lineSeparator()).flush();
            }
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    private File writeParametersToTempFile() throws IOException {
        File tempFile = File.createTempFile("denovo_scr", ".par");
        IdentificationParameters.saveIdentificationParameters(idParam, tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private File getTempFolder() throws IOException {
        tempFile = File.createTempFile("denovo_scr", "");
        tempFile.deleteOnExit();
        tempFile = new File(tempFile.getParentFile(), "denovo_scr/output");
        tempFile.mkdirs();
        for (File aFile : tempFile.listFiles()) {
            aFile.deleteOnExit();
        }
        return tempFile;
    }

    /**
     * Generates the command line arguments for the DeNovoGUI processing
     *
     * @return the list of denovoGUI arguments
     * @throws IOException if the files can not be created
     * @throws UnspecifiedPladipusException if the jar can not be found
     */
    public List<String> constructArguments() throws IOException, UnspecifiedPladipusException {
        File deNovoGUIJar = CrowdProperties.getInstance().getDeNovoGUIJar();
        File mgf = writeToTempFile();
        File param = writeParametersToTempFile();
        output = getTempFolder();

        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(deNovoGUIJar.getAbsolutePath());
        cmdArgs.add("com.compomics.denovogui.cmd.DeNovoCLI");
        cmdArgs.add("-spectrum_files");
        cmdArgs.add(mgf.getAbsolutePath());
        cmdArgs.add("-id_params");
        cmdArgs.add(param.getAbsolutePath());
        cmdArgs.add("-output_folder");
        cmdArgs.add(output.getAbsolutePath());
        cmdArgs.add("-pepnovo");
        cmdArgs.add("0");
        cmdArgs.add("-directag");
        cmdArgs.add("1");
        cmdArgs.add("-threads");
        cmdArgs.add(String.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() - 1)));
        StringBuilder builder = new StringBuilder();
        cmdArgs.stream().forEach((aString) -> {
            builder.append(aString).append(" ");
        });
        LOGGER.info(builder.toString());
        LOGGER.info("Executed at " + output.getAbsolutePath());
        return cmdArgs;
    }

    /**
     * Runs the task
     *
     * @param visuals the chosen visualisation
     * @return the system exit value
     * @throws InterruptedException if the filesystem fails
     * @throws UnspecifiedPladipusException in case the executable can not be
     * found
     */
    public int run() throws InterruptedException, UnspecifiedPladipusException {
        try {
            File file = new File(System.getProperty("user.home") + "/pladipus/log/ScreenSaverProcess.log");
            file.getParentFile().mkdirs();
            Process process;
            process = new ProcessBuilder(constructArguments()).inheritIO().start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())); FileWriter logFileWriter = new FileWriter(file)) {
                String line;
                while ((line = in.readLine()) != null) {
                    logFileWriter.append(line + System.lineSeparator()).flush();
                }
            }
            int exitValue = process.waitFor();

            //ToDo SEND RESULT FILE CONTENT BACK TO SERVER (Queue)
            for (File aFile : tempFile.listFiles()) {
                if (aFile.getName().endsWith(".tags")) {
                    JsonMarshaller marshaller = new JsonMarshaller();
                    DeNovoResult result = new DeNovoResult(fileName, getResultFileContent(aFile));
                    DeNovoTaskPusher.push_result(marshaller.toJson(result), identifier);
                }
                aFile.delete();
            }

            return exitValue;
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return -1;
    }

    /**
     * Reads the contents of a file into a String
     *
     * @param file the target file
     * @return the String content of the file
     * @throws IOException if the filesystem is not ready
     */
    public String getResultFileContent(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(encoded, "UTF-8");
    }

}
