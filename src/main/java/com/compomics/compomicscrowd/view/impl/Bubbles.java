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
package com.compomics.compomicscrowd.view.impl;

import com.compomics.compomicscrowd.view.screen.ScreenSaverVisuals;
import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class Bubbles
        extends ScreenSaverVisuals {

    private HashMap<Node, Double[]> positions;
    private long speed = 20000L;

    public Bubbles(Group root, Scene scene, double width, double height) {
        super(root, scene, width, height);
    }

    public void initVisuals() {
        Group circles = new Group();
        for (int i = 0; i < 30; i++) {
            Circle circle = new Circle(150.0D, Color.web("white", 0.05D));
            circle.setStrokeType(StrokeType.OUTSIDE);
            circle.setStroke(Color.web("white", 0.16D));
            circle.setStrokeWidth(4.0D);
            circles.getChildren().add(circle);
        }
        Rectangle colors = new Rectangle(this.scene.getWidth(), this.scene.getHeight(), new LinearGradient(0.0D, 1.0D, 1.0D, 0.0D, true, CycleMethod.NO_CYCLE, new Stop[]{new Stop(0.0D, Color.web("#f8bd55")), new Stop(0.14D, Color.web("#c0fe56")), new Stop(0.28D, Color.web("#5dfbc1")), new Stop(0.43D, Color.web("#64c2f8")), new Stop(0.57D, Color.web("#be4af7")), new Stop(0.71D, Color.web("#ed5fc2")), new Stop(0.85D, Color.web("#ef504c")), new Stop(1.0D, Color.web("#f2660f"))}));
        colors.widthProperty().bind(this.scene.widthProperty());
        colors.heightProperty().bind(this.scene.heightProperty());

        Group blendModeGroup = new Group(new Node[]{new Group(new Node[]{new Rectangle(this.scene.getWidth(), this.scene.getHeight(), Color.BLACK), circles}), colors});

        colors.setBlendMode(BlendMode.OVERLAY);
        this.root.getChildren().add(blendModeGroup);

        circles.setEffect(new BoxBlur(10.0D, 10.0D, 3));
        initializeCircles(circles);
        getAnimationTimeLine(circles);
    }

    private void initializeCircles(Group circles) {
        this.positions = new HashMap();
        for (Node aNode : circles.getChildren()) {
            this.positions.put(aNode, new Double[]{Double.valueOf(Math.random() * this.width), Double.valueOf(Math.random() * this.height)});
        }
    }

    private Timeline getAnimationTimeLine(final Group circles) {
        Timeline timeline = new Timeline();
        for (Node circle : circles.getChildren()) {
            Double[] coordinates = (Double[]) this.positions.get(circle);
            Double[] newCoordinates = {Double.valueOf(Math.random() * this.width), Double.valueOf(Math.random() * this.height)};
            timeline.getKeyFrames().addAll(new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(circle
                .translateXProperty(), coordinates[0]), new KeyValue(circle
                .translateYProperty(), coordinates[1])}), new KeyFrame(new Duration(this.speed), new KeyValue[]{new KeyValue(circle
                .translateXProperty(), newCoordinates[0]), new KeyValue(circle
                .translateYProperty(), newCoordinates[1])})});
            this.positions.put(circle, newCoordinates);
        }
        timeline.setOnFinished(new EventHandler() {
            @Override
            public void handle(Event t) {
                Bubbles.this.getAnimationTimeLine(circles).play();
            }
        });
        timeline.play();
        return timeline;
    }
}
