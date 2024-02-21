package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Counter;
import danogl.util.MutableVector2;
import danogl.util.Vector2;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.*;
import pepse.world.trees.Tree;

import java.awt.*;

public class PepseGameManager extends GameManager {

    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int SUN_LAYER = Layer.BACKGROUND + 1;
    private static final Color HALO_BG_COLOR = new Color(255, 255, 0, 20);
    private static final int HALO_LAYER = Layer.BACKGROUND + 10;
    private static final int TREE_LAYER = Layer.STATIC_OBJECTS;
    private static final int TERRAIN_LAYER = Layer.STATIC_OBJECTS;
    private static final int LEAF_LAYER = Layer.STATIC_OBJECTS + 1;
    private static final int CYCLE_LENGTH = 50;
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    private static final float OBJECT_REMOVAL_RANGE = 2f;
    private static final float CAMERA_OFFSET = 0.3f;
    private static final float BUILD_FACTOR = 0.8f;
    private static final float OBJECT_REMOVAL_FACTOR = 2.8f;
    private static final int SEED = 6;


    private Tree tree;
    private Float updateFactor;
    private MutableVector2 windowBorder;
    private Vector2 windowDimensions;
    private MutableVector2 currentBuiltRange;
    private Terrain terrain;


    PepseGameManager(String title) {
        super(title);
        currentBuiltRange = new MutableVector2(Vector2.ZERO);
    }


    /**
     * Initializes the game, including setting up the window, background, avatar, and static objects
     *
     * @param imageReader
     * @param soundReader
     * @param inputListener
     * @param windowController
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        windowDimensions = windowController.getWindowDimensions();
        updateFactor = windowDimensions.x();

        currentBuiltRange = new MutableVector2((int) (-updateFactor), (int) (windowDimensions.x() + updateFactor));
        windowController.setTargetFramerate(60);

        createBackgroundObjects();
        createAvatar(inputListener, imageReader);
        windowBorder = new MutableVector2(camera().getTopLeftCorner().x(),
                camera().getTopLeftCorner().x() + this.windowDimensions.x());
        createTreesAndTerrains();

        gameObjects().layers().shouldLayersCollide(LEAF_LAYER, TERRAIN_LAYER, true);
        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT, LEAF_LAYER, false);
    }

    /**
     * generate the background layers including
     * sun - Night - sunHalo - sky
     */
    private void createBackgroundObjects() {
        Sky.create(gameObjects(), windowDimensions, SKY_LAYER);
        GameObject sun = Sun.create(gameObjects(), SUN_LAYER, windowDimensions, CYCLE_LENGTH);
        Night.create(gameObjects(), NIGHT_LAYER, windowDimensions, CYCLE_LENGTH);
        SunHalo.create(gameObjects(), HALO_LAYER, sun, HALO_BG_COLOR);

    }

    /**
     * Initializes the static background objects in the game world, including the terrain and trees.
     * The range for these objects extends beyond the current window dimensions by a factor of 3 screen sizes.
     **/
    private void createTreesAndTerrains() {
        // Create terrain and tree objects
        terrain = new Terrain(gameObjects(), TERRAIN_LAYER, windowDimensions, SEED);
        tree = new Tree(gameObjects(), terrain, TREE_LAYER, windowBorder, LEAF_LAYER, SEED);

        // Create terrain and trees within a range that extends beyond the current window dimensions
        int minRange = (int) (-updateFactor);
        int maxRange = (int) (windowDimensions.x() + updateFactor);
        terrain.createInRange(minRange, maxRange);
        tree.createInRange(minRange, maxRange);

        // Set the current built range to the extended range
        currentBuiltRange.setXY(minRange, maxRange);
    }


    /**
     * Initializes the avatar for the game, sets the starting coordinates and conditions
     *
     * @param inputListener user input listener for avatar
     * @param imageReader   image reader for avatar
     */
    private void createAvatar(UserInputListener inputListener, ImageReader imageReader) {
        Avatar avatar = Avatar.create(gameObjects(), Layer.DEFAULT, Vector2.ZERO, inputListener, imageReader);
        avatar.setCenter(windowDimensions.mult(0.5f).multY(0.8f));
        setCamera(new Camera(avatar,
                Vector2.UP.mult(windowDimensions.y() * CAMERA_OFFSET),
                windowDimensions,
                windowDimensions));
        createAvatarEnergyCounter(windowDimensions, avatar.getEnergyCounter());
    }


    /**
     * Removes the given game object from the appropriate layer
     *
     * @param object: game object to remove
     */
    private void removeGameObject(GameObject object) {
        int layer;
        if (object instanceof LeafBlock) {
            layer = LEAF_LAYER;
        } else {
            layer = TERRAIN_LAYER;
        }
        gameObjects().removeGameObject(object, layer);
    }

    /**
     * Removes game objects that are more than two screens away from the camera, for performance maintenance
     */
    private void removeExcessObjects() {
        // Upper bound (right)
        if (currentBuiltRange.y() > windowBorder.y() + updateFactor * OBJECT_REMOVAL_FACTOR) {
            gameObjects().forEach(object -> {
                if (object instanceof Block || object instanceof LeafBlock) {
                    if (object.getTopLeftCorner().x() > currentBuiltRange.y()
                            + updateFactor * 2 - object.getDimensions().x()) {
                        removeGameObject(object);
                    }
                }
            });
            currentBuiltRange.setY(currentBuiltRange.y() - updateFactor * OBJECT_REMOVAL_RANGE);
        }
        // Lower bound (right)
        if (currentBuiltRange.x() < windowBorder.x() - updateFactor * OBJECT_REMOVAL_FACTOR) {
            gameObjects().forEach(object -> {
                if (object instanceof Block || object instanceof LeafBlock) {
                    if (object.getTopLeftCorner().x() < currentBuiltRange.x()
                            + updateFactor * OBJECT_REMOVAL_RANGE) {
                        removeGameObject(object);
                    }
                }
            });
            currentBuiltRange.setX(currentBuiltRange.x() + updateFactor * 3f);
        }
    }

    /**
     * Builds a new screen worth of world objects when the camera is close to the edge of the current screen
     */
    private void buildMissingObjects() {
        if (currentBuiltRange.x() > windowBorder.x() - updateFactor * BUILD_FACTOR) {
            terrain.createInRange((int) (currentBuiltRange.x() - updateFactor), (int) currentBuiltRange.x());
            tree.createInRange((int) (currentBuiltRange.x() - updateFactor), (int) currentBuiltRange.x());
            currentBuiltRange.setX(currentBuiltRange.x() - updateFactor);
        }
        if (currentBuiltRange.y() < windowBorder.y() + updateFactor * BUILD_FACTOR) {
            terrain.createInRange((int) currentBuiltRange.y(), (int) (currentBuiltRange.y() + updateFactor));
            tree.createInRange((int) currentBuiltRange.y(), (int) (currentBuiltRange.y() + updateFactor));
            currentBuiltRange.setY(currentBuiltRange.y() + updateFactor);
        }
    }


    // Updates the game world, including building missing objects and removing excess ones
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        windowBorder.setXY(camera().getTopLeftCorner().x(), camera().getTopLeftCorner().x()
                + this.windowDimensions.x());

        buildMissingObjects();
        removeExcessObjects();
    }


    /**
     * Builds the static energy counter for the avatar
     *
     * @param windowDimensions: dimensions of the game window
     * @param energyCounter:    counter for the avatar's energy
     */
    private void createAvatarEnergyCounter(Vector2 windowDimensions, Counter energyCounter) {
        Renderable img = new TextRenderable("100");
        GameObject counter = new GameObject(windowDimensions.mult(0.01f), new Vector2(100, 30), img);
        counter.addComponent(deltaTime -> {
            counter.renderer().setRenderable(new TextRenderable("Energy: " + energyCounter.value()));
        });
        counter.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects().addGameObject(counter, Layer.BACKGROUND);
    }


    public static void main(String[] args) {
        new PepseGameManager("PEPSE").run();
    }
}
