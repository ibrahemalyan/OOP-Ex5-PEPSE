package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.*;

public class Night {

    private static final Float INIT_OPAQUENESS = 0f;
    private static final Float MIDNIGHT_OPACITY = 0.5f;

    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            Vector2 windowDimensions,
            float cycleLength) {
        Renderable nightImg = new RectangleRenderable(ColorSupplier.approximateColor(Color.BLACK));
        GameObject night = new GameObject(Vector2.ZERO,
                windowDimensions,
                nightImg);
        gameObjects.addGameObject(night, layer);
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        new Transition<Float>(night,
                night.renderer()::setOpaqueness,
                INIT_OPAQUENESS,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength / 2,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
        return night;
    }
}
