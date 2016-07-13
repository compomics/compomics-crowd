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
package com.compomics.compomicscrowd.control.connect;

import com.compomics.compomicscrowd.control.connect.actors.DeNovoTaskPusher;
import com.compomics.compomicscrowd.control.util.JMSPropertyEditor;
import com.compomics.compomicscrowd.model.denovo.DeNovoResult;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.control.distribution.service.queue.impl.CompomicsSessionConsumer;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.util.io.json.JsonMarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;



/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class CompomicsScreenSaverResultConsumer extends CompomicsSessionConsumer {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsScreenSaverResultConsumer.class);
    /**
     * The outputfolder
     */
    private final File outputFolder;

    /**
     * A consumer for the resultqueue
     *
     * @param outputFolder the folder where result message will be stored
     * @throws IOException
     * @throws JMSException
     */
    public CompomicsScreenSaverResultConsumer(File outputFolder) throws IOException, JMSException {
        super(CompomicsQueue.SCREENSAVER_RESULT);
        this.outputFolder = outputFolder;
    }

    /**
     * commits the message if it was successfull. Resets it on the queue in case
     * of failure/prerequisite mismatch
     *
     * @param message the recieved message
     * @throws JMSException
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public void commit(Message message) throws JMSException, IOException, SQLException {
        CompomicsQueueConnectionFactory.getInstance().getSession().commit();
    }

    @Override
    public void processMessage(Message message) throws JMSException {
        if (message == null) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                //ignore for now;
            }
        } else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            JsonMarshaller marshaller = new JsonMarshaller();
            DeNovoResult result = (DeNovoResult) marshaller.fromJson(DeNovoResult.class, text);
            outputFolder.mkdirs();
            int i = 1;
            File outputFile = new File(outputFolder, result.getFileName() + "_tags.tsv");
            while (outputFile.exists()) {
                i++;
                outputFile = new File(outputFolder, result.getFileName() + "_" + i + "_tags.tsv");
            }
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.append(result.getResultAsString()).flush();
                commit(message);
            } catch (IOException | SQLException ex) {
                rollback();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //ignore for now
            }
        } else {
            LOGGER.error("Could not convert message..." + message);
        }
    }

    /**
     * Rolls back the message while storing the error in the database
     *
     * @param message
     * @throws JMSException
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public void rollbackWithError(Message message) throws JMSException, IOException, SQLException {
        //add a fail to the counter and check the failcount
        //  int processID = Integer.parseInt(message.getJMSCorrelationID());
        int maxFailCount = NetworkProperties.getInstance().getMaxFailCount();
        int failcount = 0;
        try {
            Enumeration<String> propertyNames = message.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                if (propertyName.equalsIgnoreCase("fails")) {
                    failcount = message.getIntProperty("fails");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Resetting fail counts : REASON : " + e);
        }
        if (failcount < maxFailCount) {
            rollback();
            //repush the message
            //update the properties where needed
            HashMap<String, Object> messageProperties = JMSPropertyEditor.getMessageProperties(message);
            messageProperties.put("fails", (int) messageProperties.getOrDefault("fails", 0) + 1);
            JMSPropertyEditor.setMessageProperties(message, messageProperties);
            //send back to queue
            DeNovoTaskPusher.push((TextMessage) message);
        } else {
            //don't send this task back
        }
    }

    
}
