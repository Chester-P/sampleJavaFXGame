import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.LinkedList;


class Arrow {
    private ImageView arrow;
    private boolean facing;

    public Arrow(boolean facing, double X, double Y){
        arrow = new ImageView(sampleGame.arrowImage);
        arrow.setX(50);
        arrow.setY(25);
        arrow.setScaleX(facing ? -1 : 1);
        arrow.setTranslateX(X);
        arrow.setTranslateY(Y);
        arrow.setPreserveRatio(true);
        arrow.setFitWidth(100);
        this.facing = facing;
    }

    public ImageView getNode(){
        return this.arrow;
    }

    public boolean getFacing(){
        return this.facing;
    }

}


public class sampleGame extends Application {
    public static Image image;
    public static Image arrowImage;

    @Override
    public void start(Stage stage) throws Exception{

        ImageView player = new ImageView(image);
        player.setX(50);
        player.setY(25);

        BooleanProperty facing = new SimpleBooleanProperty(); // true for right, false for left

        player.setPreserveRatio(true);
        player.setFitWidth(100);

        // define movement speed
        double vSpeed = 200.0;
        double hSpeed = 200.0;

        double arrowSpeed = 100.0;
        LinkedList<Arrow> arrowList = new LinkedList<>();

        //Creating a Group
        Group root = new Group(player);

        //Creating a Scene
        Scene scene = new Scene(root, 800, 800);

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

        final LongProperty lastUpdateTime = new SimpleLongProperty();

        final AnimationTimer rectangleAnimation = new AnimationTimer() {
            @Override
            public void handle(long timestamp) {
                final double elapsedSeconds = (timestamp - lastUpdateTime.get()) / 1000000000.0 ;
                if (! keyStack.isEmpty() && lastUpdateTime.get() > 0) {
                    double deltaX = 0 ;
                    double deltaY = 0 ;
                    if(keyStack.contains(KeyCode.UP)){
                        deltaY = -vSpeed * elapsedSeconds;
                    }
                    if(keyStack.contains(KeyCode.DOWN)){
                        deltaY = vSpeed * elapsedSeconds;
                    }
                    if(keyStack.contains(KeyCode.LEFT)){
                        deltaX = -hSpeed * elapsedSeconds;
                        facing.set(false);
                        player.setScaleX(-1);
                    }
                    if(keyStack.contains(KeyCode.RIGHT)){
                        deltaX = hSpeed * elapsedSeconds;
                        facing.set(true);
                        player.setScaleX(1);
                    }
                    if(keyStack.contains(KeyCode.A)){
                        Arrow arrow = new Arrow(facing.get(), 
                                player.getTranslateX() + (facing.get() ? 100 : -100),
                                player.getTranslateY() + 30);
                        root.getChildren().add(arrow.getNode());
                        arrowList.add(arrow);
                        // delete the keycode in keystack as it's been handled
                        keyStack.remove(KeyCode.A);
                    }
                    double oldX = player.getTranslateX();
                    double oldY = player.getTranslateY();
                    // System.out.format("before: x: %f, y: %f\n", oldX, oldY);
                    player.setTranslateX(oldX + deltaX);
                    player.setTranslateY(oldY + deltaY);
                    // System.out.format("after: x: %f, y: %f\n",
                    //         player.getTranslateX(), player.getTranslateY());
                }
                LinkedList<Arrow> toBeDeleted = new LinkedList<>();
                for (Arrow arrow : arrowList){
                    ImageView arrowNode = arrow.getNode();
                    double oldArrowX = arrowNode.getTranslateX();
                    arrowNode.setTranslateX(oldArrowX + 
                            (arrow.getFacing() ? 1 : -1) * elapsedSeconds * arrowSpeed);
                    if(arrowNode.getBoundsInParent().intersects(player.getBoundsInParent())){
                        root.getChildren().remove(arrowNode);
                        toBeDeleted.add(arrow);
                    }
                }
                arrowList.removeAll(toBeDeleted);
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
        sampleGame.image = new Image(new FileInputStream("player.png"));
        sampleGame.arrowImage = new Image(new FileInputStream("arrow.png"));
        launch(args);
    }
}
