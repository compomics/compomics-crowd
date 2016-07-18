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
import com.compomics.compomicscrowd.pladiquest.model.GameObject;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class ConditionStatus extends Condition {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


@Override
    public boolean evaluate(PladiQuest zork) {
        GameObject tested = zork.getWorld().getObjects().get(object);
        return tested != null && tested.getStatus().equals(status);
    }
}
