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
package com.compomics.compomicscrowd.core;

import com.compomics.compomicscrowd.core.control.connect.actors.DeNovoResultConsumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class DeNovoResultProcessor {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(DeNovoResultProcessor.class);
    /**
     * The command line options
     */
    private static Options options = new Options();

    public static void main(String[] args) throws IOException, ClassNotFoundException, FileNotFoundException, MzMLUnmarshallerException {
        options.addOption("h", "help", false, "show help.");
        options.addOption("output_folder", false, "The folder where the resultfiles will be downloaded to");
        parse(args);
    }

    private static void parse(String[] args) throws IOException, ClassNotFoundException, FileNotFoundException, MzMLUnmarshallerException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                help();
            }
            if (cmd.hasOption("output_folder")) {
                File outputFolder = new File(cmd.getOptionValue("output_folder"));
                DeNovoResultConsumer.gatherResultFiles(outputFolder);
            } else {
                DeNovoResultConsumer.gatherResultFiles();
            }
        } catch (ParseException e) {
            LOGGER.error(e);
            help();
        }
    }

    private static void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("Command Line Interface", options);
        //     System.exit(0);
    }
}
