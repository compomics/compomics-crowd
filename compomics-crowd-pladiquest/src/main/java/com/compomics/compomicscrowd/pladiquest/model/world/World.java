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
package com.compomics.compomicscrowd.pladiquest.model.world;

import com.compomics.compomicscrowd.pladiquest.control.input.WorldFactory;
import com.compomics.compomicscrowd.pladiquest.model.GameObject;
import com.compomics.compomicscrowd.pladiquest.model.player.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class World {

    /*Hashmaps to store the instance of the game*/
    private LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();
    private LinkedHashMap<String, Item> items = new LinkedHashMap<>();
    private LinkedHashMap<String, Container> containers = new LinkedHashMap<>();
    private LinkedHashMap<String, GameObject> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, Creature> creatures = new LinkedHashMap<>();
    private Player player = new Player();
    private LinkedHashMap<String, String> objectLookup = new LinkedHashMap<>();
    private String currentRoom;
    private File file;

    public World(File file) {
        if (!file.canRead()) {
            System.out.println("Error opening file.  Exiting...");
            return;
        }
        /* Open the xml file*/
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            WorldFactory.getInstance().loadFromXML(this, doc);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public World(String xml) {
        /* Open the xml file*/
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xml);
            WorldFactory.getInstance().loadFromXML(this, doc);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Room getLatestRoom() {
        Room latestRoom = null;
        Iterator<Map.Entry<String, Room>> iterator = getRooms().entrySet().iterator();
        while (iterator.hasNext()) {
            latestRoom = iterator.next().getValue();
        }
        return latestRoom;
    }

    public HashMap<String, Room> getRooms() {
        return rooms;
    }

    public void setRooms(LinkedHashMap<String, Room> Rooms) {
        this.rooms = Rooms;
    }

    public HashMap<String, Item> getItems() {
        return items;
    }

    public void setItems(LinkedHashMap<String, Item> Items) {
        this.items = Items;
    }

    public HashMap<String, Container> getContainers() {
        return containers;
    }

    public void setContainers(LinkedHashMap<String, Container> Containers) {
        this.containers = Containers;
    }

    public HashMap<String, GameObject> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, GameObject> Objects) {
        this.objects = Objects;
    }

    public HashMap<String, Creature> getCreatures() {
        return creatures;
    }

    public void setCreatures(LinkedHashMap<String, Creature> Creatures) {
        this.creatures = Creatures;
    }

    public HashMap<String, String> getInventory() {
        return player.getInventory();
    }

    public void setInventory(HashMap<String, String> Inventory) {
        this.player.setInventory(Inventory);
    }

    public HashMap<String, String> getObjectLookup() {
        return objectLookup;
    }

    public void setObjectLookup(LinkedHashMap<String, String> ObjectLookup) {
        this.objectLookup = ObjectLookup;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
