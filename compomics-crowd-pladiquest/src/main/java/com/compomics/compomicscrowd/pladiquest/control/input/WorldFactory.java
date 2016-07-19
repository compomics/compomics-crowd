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
package com.compomics.compomicscrowd.pladiquest.control.input;

import static com.compomics.compomicscrowd.pladiquest.control.PladiQuest.getString;
import com.compomics.compomicscrowd.pladiquest.model.GameObject;
import com.compomics.compomicscrowd.pladiquest.model.condition.impl.Command;
import com.compomics.compomicscrowd.pladiquest.model.condition.impl.ConditionLink;
import com.compomics.compomicscrowd.pladiquest.model.condition.impl.ConditionStatus;
import com.compomics.compomicscrowd.pladiquest.model.trigger.Trigger;
import com.compomics.compomicscrowd.pladiquest.model.world.Container;
import com.compomics.compomicscrowd.pladiquest.model.world.Creature;
import com.compomics.compomicscrowd.pladiquest.model.world.Item;
import com.compomics.compomicscrowd.pladiquest.model.world.Room;
import com.compomics.compomicscrowd.pladiquest.model.world.Wall;
import com.compomics.compomicscrowd.pladiquest.model.world.World;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class WorldFactory {

    private static WorldFactory INSTANCE = new WorldFactory();
    private HashMap<String, String> fileNames = new HashMap<>();
    private static final Random rand = new Random();

    private WorldFactory() {
        fileNames.put("Secret Treasure Room", "awesome_treasure_room.xml");
        fileNames.put("Gate_Room", "trap_room_1.xml");
    }

    public static WorldFactory getInstance() {
        return INSTANCE;
    }

    public void loadRandomRoom(World world, String direction) {
        Room currentRoom = world.getRooms().get(world.getCurrentRoom());
        if (rand.nextDouble() > 0.3) {
            System.out.println("Added new room");
            int nextInt = rand.nextInt(fileNames.size());
            try {
                Map.Entry<String, String> randomEntry = null;
                boolean mapFound = false;
                int attempts = world.getRooms().size();
                while (!mapFound) {
                    int i = -1;
                    Iterator<Map.Entry<String, String>> iterator = fileNames.entrySet().iterator();
                    while (iterator.hasNext() && !mapFound) {
                        randomEntry = iterator.next();
                        i++;
                        if (i == nextInt) {
                            if (randomEntry != null) {
                                if (world.getRooms().containsKey(randomEntry.getKey())) {
                                    if (attempts == 0) {
                                        addWall(world, direction);
                                        mapFound = true;
                                    } else {
                                        attempts--;
                                        nextInt = rand.nextInt(fileNames.size());
                                    }
                                } else {
                                    loadRoom(randomEntry.getValue(), world);
                                    mapFound = true;
                                }
                            } else {
                                addWall(world, direction);
                                mapFound = true;
                            }
                        }
                    }

                }
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                //something went wrong
                ex.printStackTrace();
            }
        } else {
            addWall(world, direction);
        }
        //get the last room and add a connection to the current room?
        Room latestRoom = world.getLatestRoom();
        latestRoom.getBorder().put(getOppositeDirection(direction), world.getCurrentRoom());
        currentRoom.getBorder().put(direction, latestRoom.getName());
    }

    private void addWall(World world, String direction) {
        Room room = new Wall();
        room.setName(System.currentTimeMillis() + "_" + direction + "_wall");
        room.getBorder().put(getOppositeDirection(direction), world.getCurrentRoom());
        world.getRooms().put(room.getName(), room);
    }

    public void loadRoom(String fileName, World world) throws ParserConfigurationException, SAXException, IOException {
        try (InputStream xmlStream = new ClassPathResource("rooms/"+fileName).getInputStream()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            loadFromXML(world, doc);
        }
    }

    public void loadFromXML(World world, Document doc) {
        int i, j, k, l, x, y, z;
        try {
            Element rootElement = doc.getDocumentElement();

            /* Every single first generation child is a room, container, creature, or item.  So load them in*/
            NodeList nodes = rootElement.getChildNodes();
            for (k = 0; k < nodes.getLength(); k++) {
                Node node = nodes.item(k);
                Element element;
                if (node instanceof Element) {
                    element = (Element) node;
                    String tagType = element.getTagName();

                    /* If it's a room ... */
                    switch (tagType) {
                        case "room": {
                            Room tempRoom = new Room();
                            /*Get all possible Room attributes*/
                            NodeList name = element.getElementsByTagName("name");
                            tempRoom.setName(getString((Element) name.item(0)));
                            NodeList type = element.getElementsByTagName("type");
                            if (type.getLength() > 0) {
                                tempRoom.setType(getString((Element) type.item(0)));
                            } else {
                                tempRoom.setType("regular");
                            }
                            NodeList status = element.getElementsByTagName("status");
                            if (status.getLength() > 0) {
                                tempRoom.setStatus(getString((Element) type.item(0)));
                            } else {
                                tempRoom.setStatus("");
                            }
                            NodeList description = element.getElementsByTagName("description");
                            tempRoom.setDescription(getString((Element) description.item(0)));
                            NodeList items = element.getElementsByTagName("item");
                            for (j = 0; j < items.getLength(); j++) {
                                Element item = (Element) items.item(j);
                                String itemName = getString(item);
                                tempRoom.getItem().put(itemName, itemName);
                            }
                            NodeList creatures = element.getElementsByTagName("creature");
                            for (j = 0; j < creatures.getLength(); j++) {
                                Element creature = (Element) creatures.item(j);
                                String creatureName = getString(creature);
                                tempRoom.getCreature().put(creatureName, creatureName);
                            }
                            NodeList triggers = element.getElementsByTagName("trigger");
                            for (j = 0; j < triggers.getLength(); j++) {
                                Trigger tempTrigger = new Trigger();
                                Element trigger = (Element) triggers.item(j);
                                NodeList commands = trigger.getElementsByTagName("command");
                                for (l = 0; l < commands.getLength(); l++) {
                                    Element command = (Element) commands.item(l);
                                    Command tempCommand = new Command();
                                    tempCommand.setCommand(getString(command));
                                    tempTrigger.getConditions().add(tempCommand);
                                    tempTrigger.setHasCommand(true);
                                }
                                NodeList conditions = trigger.getElementsByTagName("condition");
                                for (l = 0; l < conditions.getLength(); l++) {
                                    Element condition = (Element) conditions.item(l);
                                    NodeList object = condition.getElementsByTagName("object");
                                    NodeList has = condition.getElementsByTagName("has");
                                    if (has.getLength() > 0) {
                                        ConditionLink tempConditionHas = new ConditionLink();
                                        tempConditionHas.setHas(getString((Element) has.item(0)));
                                        tempConditionHas.setObject(getString((Element) object.item(0)));
                                        NodeList owner = condition.getElementsByTagName("owner");
                                        tempConditionHas.setOwner(getString((Element) owner.item(0)));
                                        tempTrigger.getConditions().add(tempConditionHas);

                                    } else {
                                        ConditionStatus tempConditionStatus = new ConditionStatus();
                                        tempConditionStatus.setObject(getString((Element) object.item(0)));
                                        NodeList sstatus = condition.getElementsByTagName("status");
                                        tempConditionStatus.setStatus(getString((Element) sstatus.item(0)));
                                        tempTrigger.getConditions().add(tempConditionStatus);
                                    }

                                }
                                NodeList ttype = element.getElementsByTagName("type");
                                if (ttype.getLength() > 0) {
                                    tempTrigger.setType(getString((Element) ttype.item(0)));
                                } else {
                                    tempTrigger.setType("single");
                                }
                                NodeList prints = trigger.getElementsByTagName("print");
                                for (l = 0; l < prints.getLength(); l++) {
                                    Element print = (Element) prints.item(l);
                                    tempTrigger.getPrint().add(getString(print));
                                }
                                NodeList actions = trigger.getElementsByTagName("action");
                                for (l = 0; l < actions.getLength(); l++) {
                                    Element action = (Element) actions.item(l);
                                    tempTrigger.getAction().add(getString(action));
                                }

                                tempRoom.getTrigger().add(tempTrigger);
                            }
                            NodeList containers = element.getElementsByTagName("container");
                            for (j = 0; j < containers.getLength(); j++) {
                                Element container = (Element) containers.item(j);
                                String containerName = getString(container);
                                tempRoom.getContainer().put(containerName, containerName);
                            }
                            NodeList borders = element.getElementsByTagName("border");
                            for (j = 0; j < borders.getLength(); j++) {
                                Element border = (Element) borders.item(j);
                                String borderdirection = getString((Element) border.getElementsByTagName("direction").item(0));
                                String bordername = getString((Element) border.getElementsByTagName("name").item(0));
                                tempRoom.getBorder().put(borderdirection, bordername);
                            }
                            /*Add this room to the rooms hashmap, put it in the generic objects hashmap, and store it's type in the objectlookup hashmap*/
                            world.getRooms().put(tempRoom.getName(), tempRoom);
                            world.getObjects().put(tempRoom.getName(), (GameObject) tempRoom);
                            world.getObjectLookup().put(tempRoom.getName(), "room");
                            break;
                        }
                        /* If it's an item... */
                        case "item": {
                            Item tempItem = new Item();
                            /* Get all possible item attributes*/
                            NodeList name = element.getElementsByTagName("name");
                            if (name.getLength() > 0) {
                                tempItem.setName(getString((Element) name.item(0)));
                            }
                            NodeList status = element.getElementsByTagName("status");
                            if (status.getLength() > 0) {
                                tempItem.setStatus(getString((Element) status.item(0)));
                            } else {
                                tempItem.setStatus("");
                            }
                            NodeList description = element.getElementsByTagName("description");
                            if (description.getLength() > 0) {
                                tempItem.setDescription(getString((Element) description.item(0)));
                            }
                            NodeList writing = element.getElementsByTagName("writing");
                            if (writing.getLength() > 0) {
                                tempItem.setWriting(getString((Element) writing.item(0)));
                            }
                            NodeList turnon = element.getElementsByTagName("turnon");
                            if (turnon.getLength() > 0) {
                                NodeList prints = element.getElementsByTagName("print");
                                for (j = 0; j < prints.getLength(); j++) {
                                    tempItem.getTurnOnPrint().add(getString((Element) prints.item(j)));
                                }
                                NodeList actions = element.getElementsByTagName("action");
                                for (j = 0; j < actions.getLength(); j++) {
                                    tempItem.getTurnOnAction().add(getString((Element) actions.item(j)));
                                }

                            }
                            NodeList triggers = element.getElementsByTagName("trigger");
                            for (j = 0; j < triggers.getLength(); j++) {
                                Trigger tempTrigger = new Trigger();
                                Element trigger = (Element) triggers.item(j);
                                NodeList commands = trigger.getElementsByTagName("command");
                                for (l = 0; l < commands.getLength(); l++) {
                                    Element command = (Element) commands.item(l);
                                    Command tempCommand = new Command();
                                    tempCommand.setCommand(getString(command));
                                    tempTrigger.getConditions().add(tempCommand);
                                    tempTrigger.setHasCommand(true);
                                }
                                NodeList conditions = trigger.getElementsByTagName("condition");
                                for (l = 0; l < conditions.getLength(); l++) {
                                    Element condition = (Element) conditions.item(l);
                                    NodeList object = condition.getElementsByTagName("object");
                                    NodeList has = condition.getElementsByTagName("has");
                                    if (has.getLength() > 0) {
                                        ConditionLink tempConditionHas = new ConditionLink();
                                        tempConditionHas.setHas(getString((Element) has.item(0)));
                                        tempConditionHas.setObject(getString((Element) object.item(0)));
                                        NodeList owner = condition.getElementsByTagName("owner");
                                        tempConditionHas.setOwner(getString((Element) owner.item(0)));
                                        tempTrigger.getConditions().add(tempConditionHas);

                                    } else {
                                        ConditionStatus tempConditionStatus = new ConditionStatus();
                                        tempConditionStatus.setObject(getString((Element) object.item(0)));
                                        NodeList sstatus = condition.getElementsByTagName("status");
                                        tempConditionStatus.setStatus(getString((Element) sstatus.item(0)));
                                        tempTrigger.getConditions().add(tempConditionStatus);
                                    }

                                }
                                NodeList ttype = element.getElementsByTagName("type");
                                if (ttype.getLength() > 0) {
                                    tempTrigger.setType(getString((Element) ttype.item(0)));
                                } else {
                                    tempTrigger.setType("single");
                                }
                                NodeList prints = trigger.getElementsByTagName("print");
                                for (l = 0; l < prints.getLength(); l++) {
                                    Element print = (Element) prints.item(l);
                                    tempTrigger.getPrint().add(getString(print));
                                }
                                NodeList actions = trigger.getElementsByTagName("action");
                                for (l = 0; l < actions.getLength(); l++) {
                                    Element action = (Element) actions.item(l);
                                    tempTrigger.getAction().add(getString(action));
                                }

                                tempItem.getTrigger().add(tempTrigger);
                            }
                            /* Put each item in the items hashmap, the generic objects hashmap, and store its type in objectlookup*/
                            world.getItems().put(tempItem.getName(), tempItem);
                            world.getObjects().put(tempItem.getName(), (GameObject) tempItem);
                            world.getObjectLookup().put(tempItem.getName(), "item");
                            break;
                        }
                        /* If it's a container... */
                        case "container": {
                            Container tempCont = new Container();
                            /*Get all possible container attributes*/
                            NodeList name = element.getElementsByTagName("name");
                            if (name.getLength() > 0) {
                                tempCont.setName(getString((Element) name.item(0)));
                            }
                            NodeList status = element.getElementsByTagName("status");
                            if (status.getLength() > 0) {
                                tempCont.setStatus(getString((Element) status.item(0)));
                            }
                            /*Initially assume a closed container*/
                            tempCont.setIsOpen(false);
                            NodeList description = element.getElementsByTagName("description");
                            if (description.getLength() > 0) {
                                tempCont.setDescription(getString((Element) description.item(0)));
                            }
                            NodeList accepts = element.getElementsByTagName("accept");
                            for (j = 0; j < accepts.getLength(); j++) {
                                /* If container has an accepts attribute, then it is always open*/
                                tempCont.setIsOpen(true);
                                tempCont.getAccept().add(getString((Element) accepts.item(j)));
                            }
                            NodeList citems = element.getElementsByTagName("item");
                            for (j = 0; j < citems.getLength(); j++) {
                                Element item = (Element) citems.item(j);
                                String itemName = getString(item);
                                tempCont.getItem().put(itemName, itemName);
                            }
                            NodeList triggers = element.getElementsByTagName("trigger");
                            for (j = 0; j < triggers.getLength(); j++) {
                                Trigger tempTrigger = new Trigger();
                                Element trigger = (Element) triggers.item(j);
                                NodeList commands = trigger.getElementsByTagName("command");
                                for (l = 0; l < commands.getLength(); l++) {
                                    Element command = (Element) commands.item(l);
                                    Command tempCommand = new Command();
                                    tempCommand.setCommand(getString(command));
                                    tempTrigger.getConditions().add(tempCommand);
                                    tempTrigger.setHasCommand(true);
                                }
                                NodeList conditions = trigger.getElementsByTagName("condition");
                                for (l = 0; l < conditions.getLength(); l++) {
                                    Element condition = (Element) conditions.item(l);
                                    NodeList object = condition.getElementsByTagName("object");
                                    NodeList has = condition.getElementsByTagName("has");
                                    if (has.getLength() > 0) {
                                        ConditionLink tempConditionHas = new ConditionLink();
                                        tempConditionHas.setHas(getString((Element) has.item(0)));
                                        tempConditionHas.setObject(getString((Element) object.item(0)));
                                        NodeList owner = condition.getElementsByTagName("owner");
                                        tempConditionHas.setOwner(getString((Element) owner.item(0)));
                                        tempTrigger.getConditions().add(tempConditionHas);

                                    } else {
                                        ConditionStatus tempConditionStatus = new ConditionStatus();
                                        tempConditionStatus.setObject(getString((Element) object.item(0)));
                                        NodeList sstatus = condition.getElementsByTagName("status");
                                        tempConditionStatus.setStatus(getString((Element) sstatus.item(0)));
                                        tempTrigger.getConditions().add(tempConditionStatus);
                                    }

                                }
                                NodeList ttype = element.getElementsByTagName("type");
                                if (ttype.getLength() > 0) {
                                    tempTrigger.setType(getString((Element) ttype.item(0)));
                                } else {
                                    tempTrigger.setType("single");
                                }
                                NodeList prints = trigger.getElementsByTagName("print");
                                for (l = 0; l < prints.getLength(); l++) {
                                    Element print = (Element) prints.item(l);
                                    tempTrigger.getPrint().add(getString(print));
                                }
                                NodeList actions = trigger.getElementsByTagName("action");
                                for (l = 0; l < actions.getLength(); l++) {
                                    Element action = (Element) actions.item(l);
                                    tempTrigger.getAction().add(getString(action));
                                }

                                tempCont.getTrigger().add(tempTrigger);
                            }
                            /* Put each container in the containers hashmap, the generic object hashmap, and the objectlookup hashmap*/
                            world.getContainers().put(tempCont.getName(), tempCont);
                            world.getObjects().put(tempCont.getName(), (GameObject) tempCont);
                            world.getObjectLookup().put(tempCont.getName(), "container");
                            break;
                        }
                        /* And finally, if it's a creature...*/
                        case "creature": {
                            Creature tempCreature = new Creature();
                            /* Get all possible creature attributes*/
                            NodeList name = element.getElementsByTagName("name");
                            if (name.getLength() > 0) {
                                tempCreature.setName(getString((Element) name.item(0)));
                            }
                            NodeList status = element.getElementsByTagName("status");
                            if (status.getLength() > 0) {
                                tempCreature.setStatus(getString((Element) status.item(0)));
                            }
                            NodeList description = element.getElementsByTagName("description");
                            if (description.getLength() > 0) {
                                tempCreature.setDescription(getString((Element) description.item(0)));
                            }
                            NodeList vulns = element.getElementsByTagName("vulnerability");
                            for (j = 0; j < vulns.getLength(); j++) {
                                String vulnString = getString((Element) vulns.item(j));
                                tempCreature.getVulnerability().put(vulnString, vulnString);
                            }
                            NodeList attacks = element.getElementsByTagName("attack");
                            for (j = 0; j < attacks.getLength(); j++) {
                                Element attack = (Element) attacks.item(j);
                                NodeList conditions = attack.getElementsByTagName("condition");
                                for (l = 0; l < conditions.getLength(); l++) {
                                    Element condition = (Element) conditions.item(l);
                                    NodeList object = condition.getElementsByTagName("object");
                                    NodeList has = condition.getElementsByTagName("has");
                                    if (has.getLength() > 0) {
                                        ConditionLink tempConditionHas = new ConditionLink();
                                        tempConditionHas.setHas(getString((Element) has.item(0)));
                                        tempConditionHas.setObject(getString((Element) object.item(0)));
                                        NodeList owner = condition.getElementsByTagName("owner");
                                        tempConditionHas.setOwner(getString((Element) owner.item(0)));
                                        tempCreature.getConditions().add(tempConditionHas);

                                    } else {
                                        ConditionStatus tempConditionStatus = new ConditionStatus();
                                        tempConditionStatus.setObject(getString((Element) object.item(0)));
                                        NodeList sstatus = condition.getElementsByTagName("status");
                                        tempConditionStatus.setStatus(getString((Element) sstatus.item(0)));
                                        tempCreature.getConditions().add(tempConditionStatus);
                                    }

                                }
                                NodeList prints = attack.getElementsByTagName("print");
                                for (l = 0; l < prints.getLength(); l++) {
                                    Element print = (Element) prints.item(l);
                                    tempCreature.getPrint().add(getString(print));
                                }
                                NodeList actions = attack.getElementsByTagName("action");
                                for (l = 0; l < actions.getLength(); l++) {
                                    Element action = (Element) actions.item(l);
                                    tempCreature.getAction().add(getString(action));
                                }

                            }
                            NodeList triggers = element.getElementsByTagName("trigger");
                            for (j = 0; j < triggers.getLength(); j++) {
                                Trigger tempTrigger = new Trigger();
                                Element trigger = (Element) triggers.item(j);
                                NodeList commands = trigger.getElementsByTagName("command");
                                for (l = 0; l < commands.getLength(); l++) {
                                    Element command = (Element) commands.item(l);
                                    Command tempCommand = new Command();
                                    tempCommand.setCommand(getString(command));
                                    tempTrigger.getConditions().add(tempCommand);
                                    tempTrigger.setHasCommand(true);
                                }
                                NodeList conditions = trigger.getElementsByTagName("condition");
                                for (l = 0; l < conditions.getLength(); l++) {
                                    Element condition = (Element) conditions.item(l);
                                    NodeList object = condition.getElementsByTagName("object");
                                    NodeList has = condition.getElementsByTagName("has");
                                    if (has.getLength() > 0) {
                                        ConditionLink tempConditionHas = new ConditionLink();
                                        tempConditionHas.setHas(getString((Element) has.item(0)));
                                        tempConditionHas.setObject(getString((Element) object.item(0)));
                                        NodeList owner = condition.getElementsByTagName("owner");
                                        tempConditionHas.setOwner(getString((Element) owner.item(0)));
                                        tempTrigger.getConditions().add(tempConditionHas);

                                    } else {
                                        ConditionStatus tempConditionStatus = new ConditionStatus();
                                        tempConditionStatus.setObject(getString((Element) object.item(0)));
                                        NodeList sstatus = condition.getElementsByTagName("status");
                                        tempConditionStatus.setStatus(getString((Element) sstatus.item(0)));
                                        tempTrigger.getConditions().add(tempConditionStatus);
                                    }

                                }
                                NodeList ttype = element.getElementsByTagName("type");
                                if (ttype.getLength() > 0) {
                                    tempTrigger.setType(getString((Element) ttype.item(0)));
                                } else {
                                    tempTrigger.setType("single");
                                }
                                NodeList prints = trigger.getElementsByTagName("print");
                                for (l = 0; l < prints.getLength(); l++) {
                                    Element print = (Element) prints.item(l);
                                    tempTrigger.getPrint().add(getString(print));
                                }
                                NodeList actions = trigger.getElementsByTagName("action");
                                for (l = 0; l < actions.getLength(); l++) {
                                    Element action = (Element) actions.item(l);
                                    tempTrigger.getAction().add(getString(action));
                                }

                                tempCreature.getTrigger().add(tempTrigger);
                            }
                            /* Put each creature in the creatures hashmap, the generic object hashmap, and the objectlookup hashmap*/
                            world.getCreatures().put(tempCreature.getName(), tempCreature);
                            world.getObjects().put(tempCreature.getName(), (GameObject) tempCreature);
                            world.getObjectLookup().put(tempCreature.getName(), "creature");
                            break;
                        }
                        default:
                            break;
                    }

                }
            }
        } catch (Exception e) {
            System.out.println("Invalid XML file, exiting");
            System.exit(-1);
            //e.printStackTrace();
        }
    }

    private String getOppositeDirection(String direction) {
        String newDirection = "";
        switch (direction) {
            case "north":
                newDirection = "south";
                break;
            case "south":
                newDirection = "north";
                break;
            case "west":
                newDirection = "east";
                break;
            case "east":
                newDirection = "west";
                break;
        }
        return newDirection;
    }
}
