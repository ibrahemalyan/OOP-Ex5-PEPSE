package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.ColorSupplier;

import java.awt.*;

public class Sky {


    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");
    private static final String SKY_TAG = "sky lock";

    /**
     * Creates a new GameObject representing the sky and adds it to the game.
     *
     * @param gameObjects      the collection of game objects to which the sky object will be added
     * @param windowDimensions the dimensions of the game window
     * @param skyLayer         the layer at which the sky object will be rendered in the game
     * @return the created sky GameObject
     */
    public static GameObject create(GameObjectCollection gameObjects, Vector2 windowDimensions, int skyLayer) {
        // Create a new GameObject with the specified dimensions and a renderable for the sky color
        GameObject sky = new GameObject(Vector2.ZERO, windowDimensions,
                new RectangleRenderable(ColorSupplier.approximateColor(BASIC_SKY_COLOR)));

        // Set the coordinate space for the sky object to camera coordinates
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // Add the sky object to the game and set its tag for debugging purposes
        gameObjects.addGameObject(sky, skyLayer);
        sky.setTag(SKY_TAG);

        // Return the created sky object
        return sky;
    }


}
