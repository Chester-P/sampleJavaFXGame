import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.FileInputStream;
import javafx.animation.AnimationTimer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

enum Direction {
    UP(0),
    DOWN(180),
    LEFT(270),
    RIGHT(90);

    private int numVal;

    Direction(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}

interface Movable {
    // handleCollision return val bit fields:
    int DELETE_CALLER = 0x01; // 0001
    int DELETE_ARG = 0x02; // 0010
    int DELETE_BOTH = 0x03; // 0011
    int HANDLED = 0x04; // 0100
    int REJECT = 0x08; // 1000


    double getSpeed();
    Direction getFacing();
    int handleCollision(GameObject obj);
}

class GameObject{
    private static int objCount = 0;
    protected int objID;
    protected Point position;
    public static HashMap<Integer, GameObject> allObjs = new HashMap<>();
    protected ImageView node;

    public GameObject(ImageView node){
        this.objID = objCount++;
        this.node = node;
        node.setId(Integer.toString(this.objID));
        node.setPreserveRatio(true);
        node.setFitWidth(50);
        node.setFitHeight(50);
        allObjs.put(this.objID, this);
    }

    public int getObjID(){return this.objID;}

    public ImageView getNode(){return node;}

    public static GameObject getObjByID(int id){
        return allObjs.get(id);
    }

    @Override
    public String toString(){
        return String.format("<%d|%s at (%d, %d)>", objID, getClass().getName(), position.x, position.y);
    }

    public void updatePosition(int x, int y){
        this.position.setLocation(x, y);
        System.out.format("%s is now at %s", this, this.position);
    }
}

class Player extends GameObject implements Movable {
    private Direction facing;
    private double speed;

    public Player(){
        super(new ImageView(sampleGame.playerImage));
        this.position = sampleGame.convertPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()));
        this.speed = 0;
    }

    public void setDirection(Direction d){
        if(d == Direction.LEFT)
            node.setScaleX(-1);
        else if (d == Direction.RIGHT)
            node.setScaleX(1);
        facing = d;
    }

    @Override
    public double getSpeed(){return speed;}

    public void setSpeed(double speed){this.speed = speed;}

    @Override
    public Direction getFacing(){return facing;}

    @Override
    public int handleCollision(GameObject obj){
        // TODO: Change this instanceof to some kinda pattern
        if (obj instanceof Boulder){
            ((Boulder) obj).setSpeed(50);
            ((Boulder) obj).setFacing(this.facing);
        }
        System.out.println("Collision detected: " + this + " | " + obj);
        System.out.println("Rejected, player will not move");
        return REJECT;
    }

}

class Arrow extends GameObject implements Movable{
    private Direction facing;

    public Arrow(Direction facing, double X, double Y){
        super(new ImageView(sampleGame.arrowImage));
        node.setRotate(facing.getNumVal());
        node.setTranslateX(X);
        node.setTranslateY(Y);
        this.position = sampleGame.convertPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()));
        this.facing = facing;
    }


    @Override
    public Direction getFacing(){
        return this.facing;
    }

    @Override
    public double getSpeed(){return 100;}

    @Override
    public int handleCollision(GameObject obj){
        System.out.println("Collision detected: " + this + " | " + obj);
        System.out.println("Both Arrow and " + obj.getClass().getName() + " is destroyed");
        return DELETE_BOTH;
    }

}

class Wall extends GameObject {
    private static double currX = 100;
    private static double currY = 200;

    public Wall(){
        super(new ImageView(sampleGame.wallImage));
        node.setTranslateX(currX);
        node.setTranslateY(currY);
        currX += 50;
        this.position = sampleGame.convertPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()));
    }

}

class Boulder extends GameObject implements Movable{
    private static double currX = 100;
    private static double currY = 300;
    private Direction facing;
    private double speed;

    public Boulder(){
        super(new ImageView(sampleGame.boulderImage));
        node.setTranslateX(currX);
        node.setTranslateY(currY);
        currX += 60;
        this.position = sampleGame.convertPoint(new Point2D.Double(node.getTranslateX(), node.getTranslateY()));
    }


    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    @Override
    public Direction getFacing(){
        return this.facing;
    }

    @Override
    public double getSpeed(){return speed;}

    public void setSpeed(double speed){this.speed = speed;}

    @Override
    public int handleCollision(GameObject obj){
        System.out.println("Collision detected: " + this + " | " + obj);
        System.out.println("Rejected, Boulder will not move");
        return REJECT;
    }

}


public class sampleGame extends Application {
    public static Image playerImage;
    public static Image arrowImage;
    public static Image boulderImage;
    public static Image wallImage;
    public static final double GRID_SIZE = 50;

    // NOTE: front-end point are referencing upper-left corner of the node
    public static Point convertPoint(Point2D.Double point){
        return new Point((int)(point.getX() / GRID_SIZE), (int)(point.getY() / GRID_SIZE));
    }

    public static Point2D.Double convertPoint(Point point){
        return new Point2D.Double(point.getX() * GRID_SIZE, point.getY() * GRID_SIZE);
    }

    @Override
    public void start(Stage stage) throws Exception{

        sampleGame.playerImage = new Image(getClass().getClassLoader().getResourceAsStream("player.png"));
        sampleGame.arrowImage = new Image(getClass().getClassLoader().getResourceAsStream("arrow.png"));
        sampleGame.boulderImage = new Image(getClass().getClassLoader().getResourceAsStream("boulder.png"));
        sampleGame.wallImage = new Image(getClass().getClassLoader().getResourceAsStream("wall.png"));

        Player player = new Player();

        List<Boulder> boulders = new LinkedList<>();
        List<Wall> walls = new LinkedList<>();
        for(int i = 0; i < 10; i++){
            walls.add(new Wall());
            boulders.add(new Boulder());
        }

        //Creating a Group
        Group root = new Group(player.node);

        //Creating a Scene
        Scene scene = new Scene(root, 800, 800);

        root.getChildren().addAll(boulders.stream().map(obj -> obj.getNode()).collect(Collectors.toList()));
        root.getChildren().addAll(walls.stream().map(obj -> obj.getNode()).collect(Collectors.toList()));
        List<Movable> movingObjects = new LinkedList<>();
        movingObjects.add(player);
        movingObjects.addAll(boulders);

        //Setting title to the scene
        stage.setTitle("Sample Game");

        final LinkedList<KeyCode> keyStack = new LinkedList<>();
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (! keyStack.contains(code)) {
                keyStack.push(code);
            }
        });

        scene.setOnKeyReleased(event ->
            keyStack.remove(event.getCode()));

        final LongProperty lastUpdateTime = new SimpleLongProperty(-1);

        final AnimationTimer rectangleAnimation = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                final double elapsedSeconds = (timestamp - lastUpdateTime.get()) / 1000000000.0 ;
//                System.out.println("Elapsed sec: " + elapsedSeconds);
                player.setSpeed(0);
                for(Boulder b: boulders)
                    b.setSpeed(0);
                if (! keyStack.isEmpty() && lastUpdateTime.get() > 0) {
                    double deltaX = 0 ;
                    double deltaY = 0 ;
                    switch(keyStack.peek()){
                        case UP:
                            player.setDirection(Direction.UP);
                            player.setSpeed(200);
                            break;
                        case DOWN:
                            player.setDirection(Direction.DOWN);
                            player.setSpeed(200);
                            break;
                        case LEFT:
                            player.setDirection(Direction.LEFT);
                            player.setSpeed(200);
                            break;
                        case RIGHT:
                            player.setDirection(Direction.RIGHT);
                            player.setSpeed(200);
                            break;
                    }
                    if(keyStack.contains(KeyCode.A)){
                        double offsetX = 0, offsetY = 0;
                        switch (player.getFacing()){
                            case UP:
                                offsetY = -60;
                                offsetX = 10;
                                break;
                            case DOWN:
                                offsetY = 50;
                                offsetX = 10;
                                break;
                            case LEFT:
                                offsetX = -50;
                                break;
                            case RIGHT:
                                offsetX = 60;
                                break;
                        }
                        Arrow arrow = new Arrow(player.getFacing(),
                                player.getNode().getTranslateX() + offsetX,
                                player.getNode().getTranslateY() + offsetY);
                        root.getChildren().add(arrow.getNode());
                        movingObjects.add(arrow);
                        // delete the keycode in keystack as it's been handled
                        keyStack.remove(KeyCode.A);
                    }
                    double oldX = player.getNode().getTranslateX();
                    double oldY = player.getNode().getTranslateY();
                    player.getNode().setTranslateX(oldX + deltaX);
                    player.getNode().setTranslateY(oldY + deltaY);
                }
                LinkedList<Movable> movingObjsToBeDeleted = new LinkedList<>();
                List<Integer> allGameObjsToBeDeleted = new LinkedList<>();
                for (Movable obj : movingObjects){
                    ImageView objNode = ((GameObject)obj).getNode();
                    double oldX = objNode.getTranslateX();
                    double oldY = objNode.getTranslateY();
                    double deltaX = (obj.getFacing() == Direction.RIGHT ? 1 : 0) * obj.getSpeed() -
                            (obj.getFacing() == Direction.LEFT ? 1 : 0) * obj.getSpeed();
                    double deltaY = (obj.getFacing() == Direction.DOWN ? 1 : 0) * obj.getSpeed() -
                            (obj.getFacing() == Direction.UP ? 1 : 0) * obj.getSpeed();

                    objNode.setTranslateX(oldX + deltaX * elapsedSeconds);
                    objNode.setTranslateY(oldY + deltaY * elapsedSeconds);

                    for(GameObject anotherObj: GameObject.allObjs.values()){
                        if(obj.equals(anotherObj)) continue;
                        if(objNode.getBoundsInParent().intersects(anotherObj.getNode().getBoundsInParent())){
                            int flag = obj.handleCollision(anotherObj);
                            if ((flag & Movable.HANDLED) > 0) continue;
                            if ((flag & Movable.DELETE_CALLER) > 0){
                                root.getChildren().remove(objNode);
                                allGameObjsToBeDeleted.add(Integer.parseInt(objNode.getId()));
                                movingObjsToBeDeleted.add(obj);
                            }
                            if ((flag & Movable.DELETE_ARG) > 0){
                                allGameObjsToBeDeleted.add(anotherObj.objID);
                                root.getChildren().remove(anotherObj.getNode());
                            }
                            if ((flag & Movable.REJECT) > 0) {
                                objNode.setTranslateX(oldX);
                                objNode.setTranslateY(oldY);
                            }
                            break;
                        }
                    }
                }
                movingObjects.removeAll(movingObjsToBeDeleted);
                for(Integer key: allGameObjsToBeDeleted)
                    GameObject.allObjs.remove(key);

                lastUpdateTime.set(timestamp);
            }
        };
        rectangleAnimation.start();

        //Adding the scene to the stage
        stage.setScene(scene);

        //Displaying the contents of a scene
        stage.show();


    }

    public static void main(String[] args) throws Exception{
        launch(args);
    }
}
