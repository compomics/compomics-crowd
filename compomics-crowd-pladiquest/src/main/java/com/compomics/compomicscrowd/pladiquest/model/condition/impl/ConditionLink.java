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
package com.compomics.compomicscrowd.pladiquest.model.condition.impl;

import com.compomics.compomicscrowd.pladiquest.control.PladiQuest;
import com.compomics.compomicscrowd.pladiquest.model.condition.Condition;
import com.compomics.compomicscrowd.pladiquest.model.world.Container;
import com.compomics.compomicscrowd.pladiquest.model.world.Room;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class ConditionLink extends Condition {

    /**
     * The object 
     */
    private String ownedObject;
      /**
     * The object's owner 
     */
    private String owner;

    public String getHas() {
        return ownedObject;
    }

    public void setHas(String has) {
        this.ownedObject = has;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    @Override
    public boolean evaluate(PladiQuest zork) {
        /*Inventory is a special case as it isn't the name of any object in the game, check for it specifically*/
        if (owner.equals("inventory")) {
            if (zork.getWorld().getInventory().get(object) != null && ownedObject.equals("yes") || zork.getWorld().getInventory().get(object) == null && ownedObject.equals("no")) {
                return true;
            } else {
                return false;
            }
        } else {
            /* is it a room?*/
            Room roomObject = zork.getWorld().getRooms().get(owner);
            if (roomObject != null) {
                if ((roomObject).getItem().get(object) != null && ownedObject.equals("yes") || (roomObject).getItem().get(object) == null && ownedObject.equals("no")) {
                    return true;
                } else {
                    return false;
                }
            } /* is it a container?*/ else {
                Container containerObject = zork.getWorld().getContainers().get(owner);
                if (containerObject != null) {
                    if ((containerObject).getItem().get(object) != null && ownedObject.equals("yes") || (containerObject).getItem().get(object) == null && ownedObject.equals("no")) {
                        return true;
                    } else {
                        return false;
                    }

                }
            }
        }

        return false;
    }


}
