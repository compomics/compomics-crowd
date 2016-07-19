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
package com.compomics.compomicscrowd.pladiquest.control.input.impl;

import com.compomics.compomicscrowd.pladiquest.control.input.UserInput;
import java.util.Scanner;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class CommandLineInput implements UserInput {

    /**
     * A scanner instance
     */
    private final Scanner source;
    /**
     * The latest user input
     */
    private String currentUserInput;

    public CommandLineInput() {
        source = new Scanner(System.in);
    }

    @Override
    public String getNextUserInput() {
        //scanner automatically waits for input
        currentUserInput = source.nextLine();
        return getCurrentUserInput();
    }

    @Override
    public String getCurrentUserInput() {
        return currentUserInput;
    }

    @Override
    public void resetUserInput() {
     currentUserInput="";
    }
}
