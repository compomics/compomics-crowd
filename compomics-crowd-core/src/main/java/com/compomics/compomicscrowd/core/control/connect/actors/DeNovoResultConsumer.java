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

import com.compomics.compomicscrowd.core.control.connect.CompomicsResultConsumer;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import java.io.File;
import java.io.IOException;
import javax.jms.JMSException;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class DeNovoResultConsumer {

    /**
     * The json marshaller for the incoming denovo tasks
     */
    private static final JsonMarshaller denovoTaskMarshaller = new IdentificationParametersMarshaller();
    /**
     * The outputfolder to store the result files
     */
    private static final File outputFolder = new File(System.getProperty("user.home") + "/pladipus/screensaver/denovo");

    public static void gatherResultFiles(File outputFolder) {
        while (true) {
            CompomicsResultConsumer consumer;
            try {
                consumer = new CompomicsResultConsumer(outputFolder);
                consumer.run();
            } catch (IOException | JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void gatherResultFiles() {
        gatherResultFiles(outputFolder);
    }

}
