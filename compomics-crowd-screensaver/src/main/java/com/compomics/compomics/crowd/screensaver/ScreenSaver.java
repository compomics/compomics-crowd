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
package com.compomics.compomics.crowd.screensaver;

import com.compomics.compomicscrowd.core.control.connect.CompomicsTaskConsumer;
import com.compomics.compomicscrowd.core.control.util.CrowdProperties;
import com.compomics.compomicscrowd.view.impl.Bubbles;
import com.compomics.compomicscrowd.view.screen.ScreenSaverVisuals;
import com.compomics.pladipus.view.dialogs.management.ConfigurationDialog;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class ScreenSaver extends Application {

    /**
     * The current mode the screensaver is running in
     */
    private Mode mode;
    /**
     * The width of the screen
     */
    private double width;
    /**
     * The height of the screen
     */
    private double height;
    /**
     * The root for javaFX
     */
    private Group root;
    /**
     * The scene for javaFX
     */
    private Scene scene;
    /**
     * The visuals to be used
     */
    private static ScreenSaverVisuals visuals;
    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(ScreenSaver.class);
    private static boolean shutdownRequested = false;
    private Thread denovoThread;

    public enum Mode {
        SETTINGS, SCREEN_SAVER, NA;
    }

    public static void main(String[] args) {
                ScreenSaver.launch(args);
    }

    @Override
    public void init() throws Exception {
        //init GUI
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = screenSize.getWidth();
        height = screenSize.getHeight();
        root = new Group();
        scene = new Scene(root, width, height, Color.BLACK);
        //init visuals 
        visuals = new Bubbles(root, scene, width, height);
        //command line
        List<String> rawParameters = getParameters().getRaw();
        if (rawParameters == null || rawParameters.isEmpty()) {
            mode = Mode.SCREEN_SAVER;
        } else if (!rawParameters.isEmpty()) {
            switch (rawParameters.get(0).trim().substring(0, 2)) {
                case "/c":
                    mode = Mode.SETTINGS;
                    break;
                case "/s":
                    mode = Mode.SCREEN_SAVER;
                    break;
                default:
                    mode = Mode.NA;
                    break;
            }
        }
        LOGGER.info("Starting in " + mode.toString() + "-mode.");
    }

    private void initSceneListeners(Scene scene) {
        scene.setOnKeyPressed(new EventHandlerImpl());
        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {

            private long firstMouseMove = -1;

            @Override
            public void handle(MouseEvent event) {
                if (firstMouseMove != -1
                        && firstMouseMove + 1000 < System.currentTimeMillis()) {
                    event.consume();
                    handleExit();
                } else {
                    firstMouseMove = System.currentTimeMillis();
                }
            }
        });
    }

    /**
     * Handles the exit (should be made clean)
     */
    public void handleExit() {
        //ToDo make this clean
        shutdownRequested = true;
        denovoThread.interrupt();
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        switch (mode) {
            case SCREEN_SAVER:
                primaryStage.initStyle(StageStyle.UNDECORATED);
                // Close the screen saver when any key is being pressed.
                initSceneListeners(scene);
                //start processing attempts
                denovoThread = new Thread(new TaskExecutor());
                denovoThread.setName("DeNovoGUI-thread");
                denovoThread.start();
                // start the visuals
                primaryStage.setScene(scene);
                visuals = new Bubbles(root, scene, width, height);
                visuals.runVisuals();
                primaryStage.setFullScreen(true);
                primaryStage.show();
                break;
            case SETTINGS:
                CrowdProperties instance = CrowdProperties.getInstance();
                File networkPropertiesFile = new File(instance.getFileLocation());
                LOGGER.info("Loading from " + networkPropertiesFile.getAbsolutePath());
                ConfigurationDialog dialog = new ConfigurationDialog(null, true);
                dialog.setProperties(instance);
                dialog.setVisible(true);
                break;
            default:
                LOGGER.error("Invalid command");
                Platform.exit();
                break;
        }
    }

    private static class TaskExecutor implements Runnable {

        private final ScreenSaverVisuals visuals;
        private CompomicsTaskConsumer consumer = null;
        private boolean initialised = false;

        public TaskExecutor() {
            this.visuals = ScreenSaver.visuals;
        }

        @Override
        public void run() {
            initConnections(false);
            while (!shutdownRequested) {
                if (!initialised) {
                    initConnections(true);
                } else {
                    consumer.run();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
        }

        private void initConnections(boolean reset) {
            try {
                LOGGER.info("Attempting to initialize...");
                if (reset) {
                    CrowdProperties.getInstance().reload();
                    CompomicsQueueConnectionFactory.reset(CrowdProperties.getInstance());
                }
                CompomicsQueueConnectionFactory.getInstance(CrowdProperties.getInstance());
                consumer = new CompomicsTaskConsumer();
                initialised = true;
                LOGGER.info("All is initialized,starting processing attempts");
            } catch (javax.jms.JMSException | IOException e) {
                LOGGER.error("Server could not be reached : " + e);
                //means the server is not reachable...that's fine
            }
        }
    }

    private class EventHandlerImpl implements EventHandler<KeyEvent> {

        public EventHandlerImpl() {
        }

        @Override
        public void handle(KeyEvent event) {
            handleExit();
        }
    }

}
