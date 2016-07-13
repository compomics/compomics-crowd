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
package com.compomics.compomicscrowd.control.util.mock;

import com.compomics.compomicscrowd.control.provider.DeNovoTaskProvider;
import com.compomics.compomicscrowd.model.denovo.DeNovoTask;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class DeNovoTaskMockup implements DeNovoTaskProvider {

    @Override
    public DeNovoTask getNextTask() {
        String jsonTask;
        try {
            jsonTask = getJsonTask();
            return (DeNovoTask) new IdentificationParametersMarshaller().fromJson(DeNovoTask.class, jsonTask);
        } catch (IOException | ClassNotFoundException | MzMLUnmarshallerException ex) {
            Logger.getLogger(DeNovoTaskMockup.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private DeNovoTask mockTask() throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        ClassLoader classLoader = getClass().getClassLoader();
        File testMGF = new File(classLoader.getResource("test.mgf").getFile());
        File testPar = new File(classLoader.getResource("test.par").getFile());
        SpectrumFactory factory = SpectrumFactory.getInstance();
        factory.clearFactory();
        SpectrumFactory.getInstance();
        factory.addSpectra(testMGF);
        List<MSnSpectrum> spectra = new ArrayList<>();
        for (String spectrum : factory.getSpectrumTitles(testMGF.getName())) {
            spectra.add((MSnSpectrum) factory.getSpectrum(testMGF.getName(), spectrum));
        }

        IdentificationParameters param = IdentificationParameters.getIdentificationParameters(testPar);

        return new DeNovoTask(testMGF.getName(), spectra, param);
    }

    public String getJsonTask() throws IOException, FileNotFoundException, ClassNotFoundException, MzMLUnmarshallerException {
        DeNovoTask task = mockTask();
        IdentificationParametersMarshaller marshaller = new IdentificationParametersMarshaller();
        return marshaller.toJson(task);
    }

}
