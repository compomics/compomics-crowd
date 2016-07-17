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
package com.compomics.compomicscrowd.core.control.provider.impl;


import com.compomics.compomicscrowd.core.control.provider.DeNovoTaskProvider;
import com.compomics.compomicscrowd.core.model.denovo.DeNovoTask;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class ActiveMQTaskProvider implements DeNovoTaskProvider, Callable<DeNovoTask>, ExceptionListener {

    /**
     * The marshaller instance for json objects
     */
    private final IdentificationParametersMarshaller marshaller;
    /**
     * The executor service to run this task in
     */
    private final ExecutorService executorPool;
    /**
     * The logging instance
     */
    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(ActiveMQTaskProvider.class);

    /**
     * Creates a new task provider for the ActiveMQ based system
     */
    public ActiveMQTaskProvider() {
        marshaller = new IdentificationParametersMarshaller();
        executorPool = Executors.newSingleThreadExecutor();
    }

    @Override
    public DeNovoTask call() {
        DeNovoTask task = null;
        try {

            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue("TEST.FOO");

            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);

            // Wait for a message
            Message message = consumer.receive(1000);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                task = (DeNovoTask) marshaller.fromJson(DeNovoTask.class, text);
            }

            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            LOGGER.error("Caught: " + e);
        }
        return task;
    }

    @Override
    public synchronized void onException(JMSException ex) {
        LOGGER.error("JMS Exception occured.  Shutting down client.");
    }

    @Override
    public DeNovoTask getNextTask() {
        DeNovoTask task = null;
        Future<DeNovoTask> submit = executorPool.submit(this);
        try {
            task = submit.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ActiveMQTaskProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return task;
    }
}
