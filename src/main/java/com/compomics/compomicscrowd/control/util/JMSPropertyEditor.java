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
package com.compomics.compomicscrowd.control.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class JMSPropertyEditor {
/**
 * Retrieves the properties for a jms message 
 * @param msg the jms message
 * @return a HashMap of the properties
 * @throws JMSException 
 */
    public static HashMap<String, Object> getMessageProperties(Message msg) throws JMSException {
        HashMap<String, Object> properties = new HashMap<>();
        Enumeration srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            String propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, msg.getObjectProperty(propertyName));
        }
        return properties;
    }
/**
 * resets the properties for a jms message
 * @param msg the jms message
 * @param properties map with properties
 * @throws JMSException 
 */
    public static void setMessageProperties(Message msg, HashMap<String, Object> properties) throws JMSException {
        if (properties == null) {
            return;
        }
        HashMap<String, Object> tempProps = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();
            tempProps.put(propertyName, value);
        }
        msg.clearProperties();
        for (Map.Entry<String, Object> tempProp : tempProps.entrySet()) {
            msg.setObjectProperty(tempProp.getKey(), tempProp.getValue());
        }
    }

}
