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
package com.compomics.compomicscrowd.view.screen;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Scene;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public abstract class ScreenSaverVisuals {

    protected final Scene scene;
    protected final double width;
    protected final double height;
    protected final Group root;
    protected StringProperty message;

    public ScreenSaverVisuals(Group root, Scene scene, double width, double height) {
        this.scene = scene;
        this.width = width;
        this.height = height;
        this.root = root;
        this.message = new SimpleStringProperty();
    }

    public abstract void runVisuals();

    public void updateMessage(String message) {
        this.message.set(message);
    }
}
