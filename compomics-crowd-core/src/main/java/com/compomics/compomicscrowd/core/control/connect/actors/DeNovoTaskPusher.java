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
package com.compomics.compomicscrowd.core.control.connect.actors;

import com.compomics.compomicscrowd.core.model.denovo.DeNovoTask;
import com.compomics.compomicscrowd.core.control.util.mock.DeNovoTaskMockup;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;



/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class DeNovoTaskPusher {

    /**
     * The marshaller for json denovo tasks
     */
    private static final JsonMarshaller denovoTaskMarshaller = new IdentificationParametersMarshaller();

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(DeNovoTaskPusher.class);

    public static void main(String[] args) {
        pushMockTask();
    }

    /**
     * Sends a mock task to the queue
     */
    public static void pushMockTask() {
        push(new DeNovoTaskMockup().getNextTask());
    }

    /**
     * Pushes an mgf to the job queue
     *
     * @param mgfFile the task that needs to be send
     * @param parameterFile a file for parameters
     */
    public static void push(File mgfFile, File parameterFile) throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        IdentificationParameters param = IdentificationParameters.getIdentificationParameters(parameterFile);
        pushToQueue(mgfFile, param);
    }

    /**
     * Pushes an mgf to the job queue
     *
     * @param mgfFile the task that needs to be send
    */
    public static void push(File mgfFile) throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        IdentificationParameters param = new IdentificationParameters();
        pushToQueue(mgfFile, param);
    }

    private static void pushToQueue(File mgfFile, IdentificationParameters param) throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        for (List<MSnSpectrum> batch : getBatches(mgfFile)) {
            DeNovoTask task = new DeNovoTask(mgfFile.getName(), batch, param);
            push(task);
        }
    }

    private static List<List<MSnSpectrum>> getBatches(File mgfFile) throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        SpectrumFactory factory = SpectrumFactory.getInstance();
        factory.clearFactory();
        SpectrumFactory.getInstance();
        factory.addSpectra(mgfFile);
        IdentificationParameters param = new IdentificationParameters();

        List<MSnSpectrum> spectra = new ArrayList<>();
        for (String spectrum : factory.getSpectrumTitles(mgfFile.getName())) {
            spectra.add((MSnSpectrum) factory.getSpectrum(mgfFile.getName(), spectrum));
        }
        return makeBatches(spectra, 50);
    }

    /**
     * Splits a given input list into batches of batch size
     *
     * @param list the complete list of spectra
     * @param batchSize the size of the batches
     * @return a list of batched spectra
     */
    private static List<List<MSnSpectrum>> makeBatches(List<MSnSpectrum> list, final int batchSize) {
        List<List<MSnSpectrum>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += batchSize) {
            parts.add(new ArrayList<MSnSpectrum>(
                    list.subList(i, Math.min(N, i + batchSize)))
            );
        }
        return parts;
    }

    /**
     * Pushes a denovotask to the job queue
     *
     * @param task the task that needs to be send
     * @param identifier an optional identifier for the task
     */
    public static void push(DeNovoTask task, int identifier) {
        push(denovoTaskMarshaller.toJson(task), identifier);
    }

    /**
     * Pushes a denovotask to the job queue
     *
     * @param task the task that needs to be send
     */
    public static void push(DeNovoTask task) {
        push(denovoTaskMarshaller.toJson(task), (int) System.currentTimeMillis());
    }

    /**
     * Pushes a textmessage to the queue
     *
     * @param message the string that needs to be send
     * @param identifier an optional identifier for the task
     */
    public static void push(String message, int identifier) {
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.SCREENSAVER_JOB, 5)) {
            String xmlForProcess = new DeNovoTaskMockup().getJsonTask();
            producer.addMessage(xmlForProcess, identifier);
            producer.run();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Pushes a result message to the queue
     *
     * @param resultMessage the string that needs to be send
     * @param identifier an optional identifier for the task
     */
    public static void push_result(String resultMessage, int identifier) {
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.SCREENSAVER_RESULT, 5)) {
            producer.addMessage(resultMessage, identifier);
            producer.run();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    /**
     * Pushes a JMS message object to the queue
     *
     * @param message the JMS object that needs to be send
     */
    public static void push(TextMessage message) {
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, 5)) {
            producer.sendMessage(message);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

}
