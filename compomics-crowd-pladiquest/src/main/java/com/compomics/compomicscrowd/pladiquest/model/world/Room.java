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

import com.compomics.compomicscrowd.pladiquest.model.GameObject;
import java.util.HashMap;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class Room extends GameObject {

    private String name;
    private String type = "regular";
    private String description;
    private HashMap<String, String> border = new HashMap<String, String>();
    private HashMap<String, String> container = new HashMap<String, String>();
    private HashMap<String, String> item = new HashMap<String, String>();
    private HashMap<String, String> creature = new HashMap<String, String>();

    public Room() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, String> getBorder() {
        return border;
    }

    public void setBorder(HashMap<String, String> border) {
        this.border = border;
    }

    public HashMap<String, String> getContainer() {
        return container;
    }

    public void setContainer(HashMap<String, String> container) {
        this.container = container;
    }

    public HashMap<String, String> getItem() {
        return item;
    }

    public void setItem(HashMap<String, String> item) {
        this.item = item;
    }

    public HashMap<String, String> getCreature() {
        return creature;
    }

    public void setCreature(HashMap<String, String> creature) {
        this.creature = creature;
    }

}
