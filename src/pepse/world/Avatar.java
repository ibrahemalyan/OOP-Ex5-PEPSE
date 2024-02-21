package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.*;
import danogl.util.Counter;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

public class Avatar extends GameObject {

    private static final float RUN_VELOCITY = 300f;
    private static final int JUMP_VELOCITY = 350;
    private static final float AVATAR_CAMERA_OFFSET = 60f;
    private static final float GRAVITY = 200f;
    public static final String AVATAR_PATH = "assets/character.png";
    public static final String AVATAR_TAG = "avatar";
    public static final int INIT_COUNTER_VALUE = 100;
    public static final String GROUND_TAG = "ground";
    public static final int STOP_VELOCITY_Y = 0;
    public static final int MAX_HEALTH = 100;
    private final UserInputListener inputListener;
    private final Counter energyCounter;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable,
                  UserInputListener inputListener, Counter energyCounter) {
        super(topLeftCorner, dimensions, renderable);
        this.inputListener = inputListener;
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        this.energyCounter = energyCounter;
    }

    /**
     * function returns the energy counter of the avatar
     * for use in main game manager
     *
     * @return Counter object
     */
    public Counter getEnergyCounter() {
        return this.energyCounter;
    }

    /**
     * Creates a new Avatar object and adds it to the game.
     *
     * @param gameObjects   the collection of game objects to which the avatar will be added
     * @param layer         the layer at which the avatar will be rendered in the game
     * @param topLeftCorner the top-left corner of the avatar's bounding box
     * @param inputListener the user input listener for the avatar
     * @param imageReader   the image reader used to load the avatar's image
     * @return the created Avatar object
     */
    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader) {
        Renderable image = imageReader.readImage(AVATAR_PATH, false);
        Avatar avatar = new Avatar(topLeftCorner, Vector2.ONES.mult(AVATAR_CAMERA_OFFSET), image, inputListener,
                new Counter(INIT_COUNTER_VALUE));
        avatar.transform().setAccelerationY(GRAVITY);
        avatar.setTag(AVATAR_TAG);
        gameObjects.addGameObject(avatar, layer);
        return avatar;
    }


    /**
     * Handles key press actions for the avatar.
     * If the space key and shift key are both pressed and the energy counter's value is greater than 0,
     * sets the avatar's acceleration to -GRAVITY and decrements the energy counter.
     * If only the space key is pressed and the avatar's velocity in the y direction is STOP_VELOCITY_Y,
     * sets the avatar's velocity to JUMP_VELOCITY in the up direction.
     * If the left arrow key is pressed, sets the avatar's velocity to -RUN_VELOCITY in the x direction
     * and flips the avatar's image horizontally.
     * If the right arrow key is pressed, sets the avatar's velocity to RUN_VELOCITY in the x direction
     * and unflips the avatar's image horizontally.
     * If neither arrow key is pressed, sets the avatar's velocity to STOP_VELOCITY_Y in the x direction.
     */
    private void keyPressActions() {

        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                inputListener.isKeyPressed(KeyEvent.VK_SHIFT) &&
                energyCounter.value() > 0) {
            transform().setAccelerationY(-GRAVITY);
            energyCounter.decrement();
        } else {
            transform().setAccelerationY(GRAVITY);
        }
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                this.getVelocity().y() == STOP_VELOCITY_Y) {
            transform().setVelocity(Vector2.UP.mult(JUMP_VELOCITY));
        }

        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            renderer().setIsFlippedHorizontally(true);
            transform().setVelocityX(-RUN_VELOCITY);
        } else {
            if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
                renderer().setIsFlippedHorizontally(false);
                transform().setVelocityX(RUN_VELOCITY);
            } else
                transform().setVelocityX(0);
        }

    }


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (getVelocity().y() == STOP_VELOCITY_Y && energyCounter.value() < MAX_HEALTH) {
            energyCounter.increment();
        }
        keyPressActions();
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(GROUND_TAG)) {
            transform().setVelocityY(STOP_VELOCITY_Y);
        }
    }
}
