package com.compomics.compomicscrowd.pladiquest.control;

import com.compomics.compomicscrowd.pladiquest.control.input.ActionTerm;
import com.compomics.compomicscrowd.pladiquest.control.input.UserInput;
import com.compomics.compomicscrowd.pladiquest.control.input.impl.CommandLineInput;
import com.compomics.compomicscrowd.pladiquest.control.output.OutputChannel;
import com.compomics.compomicscrowd.pladiquest.control.output.impl.ConsoleOutputChannel;
import com.compomics.compomicscrowd.pladiquest.model.conversation.ConversationLibrary;
import com.compomics.compomicscrowd.pladiquest.model.trigger.Trigger;
import com.compomics.compomicscrowd.pladiquest.model.world.Container;
import com.compomics.compomicscrowd.pladiquest.model.world.Creature;
import com.compomics.compomicscrowd.pladiquest.model.world.Item;
import com.compomics.compomicscrowd.pladiquest.model.world.Room;
import com.compomics.compomicscrowd.pladiquest.model.world.World;
import com.compomics.compomicscrowd.pladiquest.view.PladiQuestHUD;
import java.io.File;
import org.w3c.dom.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* And away we go*/
public class PladiQuest {

    PladiQuestHUD hud = new PladiQuestHUD();
    /**
     * The user input
     */
    // private UserInput input = new CommandLineInput();
    private UserInput input = hud;
    /**
     * The Current user input
     */
    private String currentUserInput = "";
    /**
     * The current pladipus world
     */
    private World world;
    /**
     * The output channel
     */
    //private OutputChannel outputChannel = new ConsoleOutputChannel();

    private OutputChannel outputChannel = hud;

    public PladiQuest(String filename) {
        hud.setVisible(true);
        File file = new File(filename);
        if (!file.canRead()) {
            outputChannel.show("Error opening file.  Exiting...");
            return;
        }
        world = new World(file);
        world.setCurrentRoom("Entrance");
        boolean skip;
        ConversationLibrary.getInstance();
        /* Print out the first entrance description, starting the game!*/
        outputChannel.show(world.getRooms().get(world.getCurrentRoom()).getDescription());
        /*Start the entire game loop*/
        while (true) {
            if (currentUserInput.isEmpty()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PladiQuest.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentUserInput = input.getNextUserInput();
            } else {
                /*Now that we have the user command, check the input*/
                skip = checkAllTriggers();
                /*We only skip if a matched trigger overwrites the execution of a command*/
                if (skip) {
                    input.resetUserInput();
                    currentUserInput = "";
                    continue;
                }
                /* If we haven't skipped, perform the user action*/
                executeAction(currentUserInput);
                /* Clear the user input, and check the triggers again (various states have changed, gnomes need to be found!*/
                input.resetUserInput();
                currentUserInput = "";
                checkAllTriggers();
            }
        }
    }

    /* Execute a user action or an action command from some <action> element that is not one of the "Special Commands"*/
    private void executeAction(String input) {
        String tempString;
        String printTempString;
        Container tempContainer;
        String prefix;
        /* Movement */
        if (input.equals("n") || input.equals("s") || input.equals("e") || input.equals("w")) {
            move(input);
        } /* Inventory */ else if (input.equals("i") || input.equals("inventory")) {
            inventory();
        } /* Take */ else if (!(prefix = ActionTerm.LOOT.getPrefix(input)).isEmpty() && input.split(" ").length >= 1) {
            boolean found = false;
            printTempString = input.replace(prefix, "").replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ").trim();
            tempString = printTempString.toLowerCase();
            if ((world.getRooms().get(world.getCurrentRoom())).getItem().get(tempString) != null) {
                world.getInventory().put(tempString, tempString);
                Room tempRoom = (world.getRooms().get(world.getCurrentRoom()));
                tempRoom.getItem().remove(tempString);
                world.getRooms().put(tempRoom.getName(), tempRoom);
                found = true;
            } else {
                /*Search all containers in the current room for the item!*/
                for (String key : world.getRooms().get(world.getCurrentRoom()).getContainer().keySet()) {
                    tempContainer = world.getContainers().get(key);
                    if (tempContainer != null && tempContainer.isIsOpen() && tempContainer.getItem().get(tempString) != null) {
                        world.getInventory().put(tempString, tempString);
                        tempContainer.getItem().remove(tempString);
                        world.getContainers().put(tempContainer.getName(), tempContainer);
                        found = true;
                        break;
                    }
                }
            }
            String randomReply = ConversationLibrary.getInstance().getRandomReply(ActionTerm.LOOT, found);
            outputChannel.show(randomReply.replace("[item]", "<b>" + printTempString + "</b>"));
        } /* Open Exit (you should be so lucky)*/ else if (!(prefix = ActionTerm.VICTORY.getPrefix(input)).isEmpty()) {
            if (world.getRooms().get(world.getCurrentRoom()).getType().equals("exit")) {
                outputChannel.show("Game Over");
                System.exit(0);
            } else {
                outputChannel.show("Error");
            }
        } /* Open a container */ else if (!(prefix = ActionTerm.OPEN.getPrefix(input)).isEmpty() && input.split(" ").length >= 1) {
            printTempString = input.replace(prefix, "").replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ").trim();
            tempString = printTempString.toLowerCase();
            String found = world.getRooms().get(world.getCurrentRoom()).getContainer().get(tempString);
            if (found != null) {
                tempContainer = world.getContainers().get(tempString);
                tempContainer.setIsOpen(true);
                outputChannel.show(ConversationLibrary.getInstance().getRandomReply(ActionTerm.OPEN, true).replace("[container]", "<b>" + printTempString + "</b>"));
                containerInventory(tempContainer.getItem(), tempString);
            } else {
                outputChannel.show(ConversationLibrary.getInstance().getRandomReply(ActionTerm.LOOT, false).replace("[container]", "<b>" + printTempString + "</b>"));
            }
        } /* Read an object */ else if (!(prefix = ActionTerm.READ.getPrefix(input)).isEmpty() && input.split(" ").length > 1) {
            tempString = input.toLowerCase().replace(prefix, "").trim();
            Item tempItem;
            if (world.getInventory().get(tempString) != null) {
                tempItem = world.getItems().get(tempString);
                if (tempItem.getWriting() != null && tempItem.getWriting() != "") {
                    outputChannel.show(tempItem.getWriting());
                } else {
                    outputChannel.show("Nothing written.");
                }
            } else {
                outputChannel.show("Error");
            }
        } /* Drop an item*/ else if (!(prefix = ActionTerm.DROP.getPrefix(input)).isEmpty() && input.split(" ").length >= 1) {
            tempString = input.toLowerCase().replace(prefix, "").trim();
            if (world.getInventory().get(tempString) != null) {
                Room tempRoom = world.getRooms().get(world.getCurrentRoom());
                tempRoom.getItem().put(tempString, tempString);
                world.getRooms().put(tempRoom.getName(), tempRoom);
                world.getInventory().remove(tempString);
                outputChannel.show(tempString + " dropped.");
            } else {
                outputChannel.show("Error");
            }
        } /* Put an item somewhere */ else if (!(prefix = ActionTerm.PUT.getPrefix(input)).isEmpty()) {
            tempString = input.split(" ")[1];
            String destination = input.split(" ")[3];
            if (world.getRooms().get(world.getCurrentRoom()).getContainer().get(destination) != null && world.getContainers().get(destination).isIsOpen() && world.getInventory().get(tempString) != null) {
                tempContainer = world.getContainers().get(destination);
                tempContainer.getItem().put(tempString, tempString);
                world.getInventory().remove(tempString);
                outputChannel.show("Item " + tempString + " added to " + destination + ".");
            } else {
                outputChannel.show("Error");
            }
        } /* Turn on an item*/ else if (!(prefix = ActionTerm.ACTIVATE.getPrefix(input)).isEmpty()) {
            tempString = input.replace(prefix, "").trim();
            Item tempItem;
            if (world.getInventory().get(tempString) != null) {
                tempItem = world.getItems().get(tempString);
                outputChannel.show("You activate the " + tempString + ".");
                if (tempItem != null) {
                    for (int y = 0; y < tempItem.getTurnOnPrint().size(); y++) {
                        outputChannel.show(tempItem.getTurnOnPrint().get(y));
                    }
                    for (int y = 0; y < tempItem.getTurnOnAction().size(); y++) {
                        performAction(tempItem.getTurnOnAction().get(y));
                    }
                } else {
                    outputChannel.show("Error");
                }
            } else {
                outputChannel.show("Error");
            }

        } /* Attempt an attack, you feeling lucky?*/ else if (!(prefix = ActionTerm.ATTACK.getPrefix(input)).isEmpty() && input.split(" ").length >= 4) {
            tempString = input.split(" ")[1];
            Creature tempCreature;
            String weapon = input.split(" ")[3];
            if (world.getRooms().get(world.getCurrentRoom()).getCreature().get(tempString) != null) {
                tempCreature = world.getCreatures().get(tempString);
                if (tempCreature != null && world.getInventory().get(weapon) != null) {
                    if (tempCreature.attack(this, weapon)) {
                        outputChannel.show("You assault the " + tempString + " with the " + weapon + ".");
                        for (int y = 0; y < tempCreature.getPrint().size(); y++) {
                            outputChannel.show(tempCreature.getPrint().get(y));
                        }
                        for (int y = 0; y < tempCreature.getAction().size(); y++) {
                            performAction(tempCreature.getAction().get(y));
                        }
                    } else {
                        outputChannel.show("Error");
                    }
                } else {
                    outputChannel.show("Error");
                }
            } else {
                outputChannel.show("Error");
            }
        } /* Invalid command*/ else {
            outputChannel.show("Error");
        }
    }


    /* Get a string from an element (XML parsing stuff)*/
    public static String getString(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    /* Check triggers */
    private boolean checkAllTriggers() {

        /*Variable initialization*/
        boolean skip = false;
        int i, j, k, l, x, y, z;
        Trigger tempTrigger;
        Container tempContainer;

        /*Check Room triggers*/
        for (x = 0; x < world.getRooms().get(world.getCurrentRoom()).getTrigger().size(); x++) {
            tempTrigger = world.getRooms().get(world.getCurrentRoom()).getTrigger().get(x);
            if (tempTrigger.evaluate(this)) {
                for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                    outputChannel.show(tempTrigger.getPrint().get(y));
                }
                for (y = 0; y < tempTrigger.getAction().size(); y++) {
                    performAction(tempTrigger.getAction().get(y));
                }
                if (tempTrigger.isHasCommand()) {
                    skip = true;
                }
                if (tempTrigger.getType().equals("single")) {
                    world.getRooms().get(world.getCurrentRoom()).getTrigger().remove(x);
                }
            }
        }

        /* Check items in the containers in the room */
        for (String key : world.getRooms().get(world.getCurrentRoom()).getContainer().keySet()) {
            tempContainer = world.getContainers().get(key);
            for (String key2 : tempContainer.getItem().keySet()) {
                Item tempItem = world.getItems().get(key2);
                for (x = 0; x < tempItem.getTrigger().size(); x++) {
                    tempTrigger = tempItem.getTrigger().get(x);
                    if (tempTrigger.evaluate(this)) {
                        for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                            outputChannel.show(tempTrigger.getPrint().get(y));
                        }
                        for (y = 0; y < tempTrigger.getAction().size(); y++) {
                            performAction(tempTrigger.getAction().get(y));
                        }
                        if (tempTrigger.isHasCommand()) {
                            skip = true;
                        }
                        if (tempTrigger.getType().equals("single")) {
                            tempItem.getTrigger().remove(x);
                        }
                    }
                }
            }

            /* Check all containers in the room*/
            for (x = 0; x < tempContainer.getTrigger().size(); x++) {
                tempTrigger = tempContainer.getTrigger().get(x);
                if (tempTrigger.evaluate(this)) {
                    for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                        outputChannel.show(tempTrigger.getPrint().get(y));
                    }
                    for (y = 0; y < tempTrigger.getAction().size(); y++) {
                        performAction(tempTrigger.getAction().get(y));
                    }
                    if (tempTrigger.isHasCommand()) {
                        skip = true;
                    }
                    if (tempTrigger.getType().equals("single")) {
                        tempContainer.getTrigger().remove(x);
                    }
                }
            }
        }

        /* Check all creatures in the room */
        for (String key : world.getRooms().get(world.getCurrentRoom()).getCreature().keySet()) {
            Creature tempCreature = world.getCreatures().get(key);
            for (x = 0; x < tempCreature.getTrigger().size(); x++) {
                tempTrigger = tempCreature.getTrigger().get(x);
                if (tempTrigger.evaluate(this)) {
                    for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                        outputChannel.show(tempTrigger.getPrint().get(y));
                    }
                    for (y = 0; y < tempTrigger.getAction().size(); y++) {
                        performAction(tempTrigger.getAction().get(y));
                    }
                    if (tempTrigger.isHasCommand()) {
                        skip = true;
                    }
                    if (tempTrigger.getType().equals("single")) {
                        tempCreature.getTrigger().remove(x);
                    }
                }
            }
        }

        /* Check items in inventory */
        for (String key : world.getInventory().keySet()) {
            Item tempItem = world.getItems().get(key);
            for (x = 0; x < tempItem.getTrigger().size(); x++) {
                tempTrigger = tempItem.getTrigger().get(x);
                if (tempTrigger.evaluate(this)) {
                    for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                        outputChannel.show(tempTrigger.getPrint().get(y));
                    }
                    for (y = 0; y < tempTrigger.getAction().size(); y++) {
                        performAction(tempTrigger.getAction().get(y));
                    }
                    if (tempTrigger.isHasCommand()) {
                        skip = true;
                    }
                    if (tempTrigger.getType().equals("single")) {
                        tempItem.getTrigger().remove(x);
                    }
                }
            }
        }

        /* Check items in room */
        for (String key : world.getRooms().get(world.getCurrentRoom()).getItem().keySet()) {
            Item tempItem = world.getItems().get(key);
            for (x = 0; x < tempItem.getTrigger().size(); x++) {
                tempTrigger = tempItem.getTrigger().get(x);
                if (tempTrigger.evaluate(this)) {
                    for (y = 0; y < tempTrigger.getPrint().size(); y++) {
                        outputChannel.show(tempTrigger.getPrint().get(y));
                    }
                    for (y = 0; y < tempTrigger.getAction().size(); y++) {
                        performAction(tempTrigger.getAction().get(y));
                    }
                    if (tempTrigger.isHasCommand()) {
                        skip = true;
                    }
                    if (tempTrigger.getType().equals("single")) {
                        tempItem.getTrigger().remove(x);
                    }
                }
            }
        }
        return skip;
    }

    /*Basic movement function */
    public void move(String direction) {
        String fullDirection = "";
        String destination = "";
        if (direction.equals("n")) {
            fullDirection = "north";
        } else if (direction.equals("s")) {
            fullDirection = "south";
        } else if (direction.equals("e")) {
            fullDirection = "east";
        } else if (direction.equals("w")) {
            fullDirection = "west";
        }

        destination = (world.getRooms().get(world.getCurrentRoom())).getBorder().get(fullDirection);
        if (destination != null) {
            world.setCurrentRoom(destination);
            outputChannel.show(world.getRooms().get(world.getCurrentRoom()).getDescription());
        } else  {
            //generate and add a new room
            outputChannel.show("Can't go that way.");
        }
    }

    /* This is used to perform the "Special Actions"*/
    public void performAction(String action) {
        String object;
        String objectType;
        /* Update: figure out what type of item it is, and then change it's status*/
        if (action.startsWith("Update")) {
            object = action.split(" ")[1];
            String newStatus = action.split(" ")[3];

            objectType = world.getObjectLookup().get(object);
            if (objectType.equals("room")) {
                Room tempRoom = world.getRooms().get(object);
                tempRoom.setStatus(newStatus);
                world.getRooms().put(tempRoom.getName(), tempRoom);
            } else if (objectType.equals("container")) {
                Container tempContainer = world.getContainers().get(object);
                tempContainer.setStatus(newStatus);
                world.getContainers().put(tempContainer.getName(), tempContainer);

            } else if (objectType.equals("creature")) {
                Creature tempCreature = world.getCreatures().get(object);
                tempCreature.setStatus(newStatus);
                world.getCreatures().put(tempCreature.getName(), tempCreature);

            } else if (objectType.equals("item")) {
                Item tempItem = world.getItems().get(object);
                tempItem.setStatus(newStatus);
                world.getItems().put(tempItem.getName(), tempItem);

            }

        } else if (action.equals("Game Over")) {
            outputChannel.show("Victory!");
            System.exit(0);
        } else if (action.startsWith("Add")) {
            String destination = action.split(" ")[3];
            object = action.split(" ")[1];
            objectType = world.getObjectLookup().get(object);
            String destinationType = world.getObjectLookup().get(destination);
            if (destinationType.equals("room")) {
                Room tempRoom = world.getRooms().get(destination);
                if (objectType.equals("item")) {
                    tempRoom.getItem().put(object, object);
                } else if (objectType.equals("creature")) {
                    tempRoom.getCreature().put(object, object);
                } else if (objectType.equals("container")) {
                    tempRoom.getContainer().put(object, object);
                } else {
                    outputChannel.show("Error");
                }
                world.getRooms().put(tempRoom.getName(), tempRoom);
            } else if (destinationType.equals("container")) {
                Container tempContainer = world.getContainers().get(destination);
                if (objectType.equals("item")) {
                    tempContainer.getItem().put(object, object);
                } else {
                    outputChannel.show("Error");
                }
                world.getContainers().put(tempContainer.getName(), tempContainer);
            } else {
                outputChannel.show("Error");
            }
        } /* Delete: figure out what object it is and delete it accordingly.  Rooms are especially tricky */ else if (action.startsWith("Delete")) {
            object = action.split(" ")[1];
            objectType = world.getObjectLookup().get(object);
            world.getObjects().remove(object);
            if (objectType.equals("room")) {
                Room tempRoom;
                for (String key : world.getRooms().keySet()) {
                    tempRoom = world.getRooms().get(key);
                    for (String key2 : tempRoom.getBorder().keySet()) {
                        if (tempRoom.getBorder().get(key2).equals(object)) {
                            tempRoom.getBorder().remove(key2);
                        }
                    }
                    world.getRooms().put(tempRoom.getName(), tempRoom);
                }
            } else if (objectType.equals("item")) {
                Room tempRoom;
                for (String key : world.getRooms().keySet()) {
                    tempRoom = world.getRooms().get(key);
                    if (tempRoom.getItem().get(object) != null) {
                        tempRoom.getItem().remove(object);
                        world.getRooms().put(tempRoom.getName(), tempRoom);
                    }
                }
                Container tempContainer;
                for (String key : world.getContainers().keySet()) {
                    tempContainer = world.getContainers().get(key);
                    if (tempContainer.getItem().get(object) != null) {
                        tempContainer.getItem().remove(object);
                        world.getContainers().put(tempContainer.getName(), tempContainer);
                    }
                }
            } else if (objectType.equals("container")) {
                Room tempRoom;
                for (String key : world.getRooms().keySet()) {
                    tempRoom = world.getRooms().get(key);
                    if (tempRoom.getContainer().get(object) != null) {
                        tempRoom.getContainer().remove(object);
                        world.getRooms().put(tempRoom.getName(), tempRoom);
                    }
                }
            } else if (objectType.equals("creature")) {
                Room tempRoom;
                for (String key : world.getRooms().keySet()) {
                    tempRoom = world.getRooms().get(key);
                    if (tempRoom.getCreature().get(object) != null) {
                        tempRoom.getCreature().remove(object);
                        world.getRooms().put(tempRoom.getName(), tempRoom);
                    }
                }
            }
        } else {
            /*If it's not a "Special Action", just treat it normally */
            currentUserInput = action;
            executeAction(action);
        }
    }

    /* Print out the what's in a container when it's been opened*/
    public void containerInventory(HashMap<String, String> Container, String Name) {
        String output = "";
        if (Container.isEmpty()) {
            outputChannel.show("The " + Name + " is empty");
        } else {
            outputChannel.show("The " + Name + " contains ");
            for (String key : Container.keySet()) {
                output += key + ", ";
            }
            output = output.substring(0, output.length() - 2);
            outputChannel.show(output + ".");
        }
    }

    /* Print out the inventory when user types i */
    public void inventory() {
        String output = "Inventory: ";
        if (world.getInventory().isEmpty()) {
            outputChannel.show("Inventory: empty");
        } else {
            for (String key : world.getInventory().keySet()) {
                output += key + ", ";
            }
            output = output.substring(0, output.length() - 2);
            outputChannel.show(output);
        }
    }

    public World getWorld() {
        return world;
    }

    public String getCurrentUserInput() {
        return currentUserInput;
    }

    /* I love how basic java main functions are sometimes.*/
    public static void main(String[] args) {
        PladiQuest zork = new PladiQuest("C:\\Users\\compomics\\Documents\\NetBeansProjects\\compomics-crowd\\compomics-crowd-pladiquest\\src\\main\\resources\\sampleGame.xml");
    }
}
