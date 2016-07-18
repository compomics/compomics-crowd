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
package com.compomics.compomicscrowd.pladiquest.model.trigger;

import com.compomics.compomicscrowd.pladiquest.control.PladiQuest;
import com.compomics.compomicscrowd.pladiquest.model.condition.Condition;
import java.util.ArrayList;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class Trigger {

    private ArrayList<Condition> conditions = new ArrayList<Condition>();
    private String type = "single";
    /*By default, single*/
    private boolean hasCommand = false;
    private ArrayList<String> print = new ArrayList<String>();
    private ArrayList<String> action = new ArrayList<String>();

    public boolean evaluate(PladiQuest zork) {
        for (int i = 0; i < conditions.size(); i++) {
            if (!conditions.get(i).evaluate(zork)) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHasCommand() {
        return hasCommand;
    }

    public void setHasCommand(boolean hasCommand) {
        this.hasCommand = hasCommand;
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
