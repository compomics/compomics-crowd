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
package com.compomics.compomicscrowd.core.control.connect;

import com.compomics.compomicscrowd.core.control.connect.actors.DeNovoTaskPusher;
import com.compomics.compomicscrowd.core.control.util.JMSPropertyEditor;
import com.compomics.compomicscrowd.core.model.denovo.DeNovoTask;
import com.compomics.pladipus.core.control.distribution.service.queue.impl.CompomicsSessionConsumer;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class CompomicsTaskConsumer extends CompomicsSessionConsumer {

    /**
     * The logging instance
     */
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(CompomicsTaskConsumer.class);
    /**
     * The json unmarshaller
     */
    private static final JsonMarshaller MARSHALLER = new IdentificationParametersMarshaller();


    public CompomicsTaskConsumer() throws IOException, JMSException {
        super(CompomicsQueue.SCREENSAVER_JOB);
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
            DeNovoTask fromJson = (DeNovoTask) MARSHALLER.fromJson(DeNovoTask.class, text);
            try {
                fromJson.run();
                commit(message);
            } catch (InterruptedException | UnspecifiedPladipusException | IOException | SQLException ex) {
                System.err.println(ex);
                try {
                    rollbackWithError(message);
                } catch (IOException | SQLException ex1) {
                    Logger.getLogger(CompomicsTaskConsumer.class.getName()).log(Level.SEVERE, null, ex1);
                }
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
            LOGGER.error("Resetting fail counts : REASON : " + e);
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
