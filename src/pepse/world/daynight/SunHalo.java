package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import pepse.util.ColorSupplier;

import java.awt.*;

public class SunHalo {

    private static final float HALO_OPACITY = 0.1f;
    private static final float RADIUS_FACTOR = 4f;

    /**
     * creates the halo of the sun
     *
     * @param gameObjects gameObjects of the game
     * @param sun         GameObject of the current sun to follow
     * @param color
     * @param layer
     * @return
     */
    public static GameObject create(GameObjectCollection gameObjects,
                                    int layer, GameObject sun,
                                    Color color) {
        Renderable haloImg = new OvalRenderable(ColorSupplier.approximateColor(color));
        GameObject halo = new GameObject(sun.getTopLeftCorner(),
                Sun.SUN_DIMENSIONS.mult(RADIUS_FACTOR),
                haloImg);
        halo.renderer().setOpaqueness(HALO_OPACITY);
        halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        halo.addComponent(deltaTime -> {
            halo.setCenter(sun.getCenter());
        });
        gameObjects.addGameObject(halo, layer);
        return halo;
    }
}
