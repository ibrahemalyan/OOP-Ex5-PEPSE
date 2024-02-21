package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.*;

public class Sun {


    public static final Vector2 SUN_DIMENSIONS = new Vector2(200, 220);
    public static final String SUN_TAG = "sun";
    private static final float ANGLE_TO_CENTER = 270;
    private static final double RAD_ANGLE = Math.PI / 180;
    private static final float ELLIPSE_PERCENTAGE = 0.75f;
    public static final float ZER0_ANGLE_DEGREE = 0f;
    public static final float FULL_CYCLE_DEGREE = 360f;

    /**
     * create the sun object and activates the transition responsible
     * for its elliptic movement
     *
     * @param windowDimensions Vector2 of current window dimensions
     * @param cycleLength      int of the sun cycle
     * @param gameObjects      gameObjects
     * @param layer            int of the layer to add the sun into
     * @return result GameObject of the sun
     */
    public static GameObject create(GameObjectCollection gameObjects,
                                    int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Renderable sunImg = new OvalRenderable(ColorSupplier.approximateColor(Color.YELLOW));

        GameObject sun = new GameObject(Vector2.ZERO, SUN_DIMENSIONS, sunImg);
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(sun, layer);
        sun.setTag(SUN_TAG);
        new Transition<>(sun, degree -> sun.setCenter(new Vector2(((windowDimensions.x() / 2) +
                ((windowDimensions.x() / 2) * ELLIPSE_PERCENTAGE * ((float) Math.cos((degree +
                        ANGLE_TO_CENTER) * RAD_ANGLE)))),
                (windowDimensions.x() / 2) + ((windowDimensions.x() / 2) *
                        ((float) Math.sin((degree + ANGLE_TO_CENTER) * RAD_ANGLE))))),

                ZER0_ANGLE_DEGREE,
                FULL_CYCLE_DEGREE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );

        return sun;
    }


}
