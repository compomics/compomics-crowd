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

import com.compomics.compomicscrowd.pladiquest.control.PladiQuest;
import com.compomics.compomicscrowd.pladiquest.model.GameObject;
import com.compomics.compomicscrowd.pladiquest.model.condition.Condition;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class Creature extends GameObject {

    private String name;
    private String description;
    private HashMap<String, String> vulnerability = new HashMap<String, String>();
    private ArrayList<Condition> conditions = new ArrayList<Condition>();
    private ArrayList<String> print = new ArrayList<String>();
    private ArrayList<String> action = new ArrayList<String>();

    public Creature() {
    }

    /* Evaluate the success of an attack*/
    public boolean attack(PladiQuest zork, String weapon) {
        if (vulnerability.get(weapon) == null) {
            return false;
        }
        for (int i = 0; i < conditions.size(); i++) {
            if (!conditions.get(i).evaluate(zork)) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, String> getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(HashMap<String, String> vulnerability) {
        this.vulnerability = vulnerability;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    public ArrayList<String> getPrint() {
        return print;
    }

    public void setPrint(ArrayList<String> print) {
        this.print = print;
    }

    public ArrayList<String> getAction() {
        return action;
    }

    public void setAction(ArrayList<String> action) {
        this.action = action;
    }

}
