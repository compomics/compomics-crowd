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


import com.compomics.compomicscrowd.core.control.connect.actors.DeNovoTaskPusher;
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
public class DeNovoTaskLauncher {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(DeNovoTaskLauncher.class);
    /**
     * The command line options
     */
    private static Options options = new Options();

    public static void main(String[] args) throws IOException, ClassNotFoundException, FileNotFoundException, MzMLUnmarshallerException {
        options.addOption("h", "help", false, "show help.");
        options.addOption("spectrum_file", true, "The input MGF file to push to the queue");
        options.addOption("id_param", false, "The input parameters file to push to the queue");
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

            if (cmd.hasOption("spectrum_file")) {
                File mgfFile = new File(cmd.getOptionValue("spectrum_file"));
                if (cmd.hasOption("id_param")) {
                    File param = new File(cmd.getOptionValue("id_param"));
                    DeNovoTaskPusher.push(mgfFile, param);
                } else {
                    DeNovoTaskPusher.push(mgfFile);
                }
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
