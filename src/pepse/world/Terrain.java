package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.*;
import java.util.*;

public class Terrain {

    private static final int TERRAIN_DEPTH = 20;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    public static final String GROUND_TAG = "ground";
    private final float groundHeightAtX0;
    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private final Vector2 windowDimensions;
    private final int hillWidthFactor;
    private final float hillHeightFactor;


    public Terrain(GameObjectCollection gameObjects,
                   int groundLayer,
                   Vector2 windowDimensions,
                   int seed) {
        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        this.groundHeightAtX0 = (windowDimensions.y() * ((float) 9 / 10));// the lowest point of th ground
        Random random = new Random(Objects.hash(60, seed));
        hillWidthFactor = random.nextInt(20) + 80; // the randomly calculated width of hill
        hillHeightFactor = random.nextFloat() % 0.1f + 0.2f; // the randomly calculated height of hill


    }


    /**
     * the mathematical function that calculates terrain Y coordinates at every X given
     *
     * @param x given x coordinate
     * @return the  result of the sin mathematical function (float)
     */
    private float terrainFunc(float x) {

        float ans = (float) Math.sin(x / hillWidthFactor);
        if (ans > 0)
            ans = 0;
        return groundHeightAtX0 + (ans * windowDimensions.y() * hillHeightFactor);
    }

    /**
     * returns the height at every given x coordinate (y coordinate)
     *
     * @param x x coordinate
     * @return the y of the current function for given x
     */
    public float groundHeightAt(float x) {
        return terrainFunc(x);
    }

    /**
     * Creates a column of blocks at the specified x coordinate.
     *
     * @param curXCord the x coordinate at which to create the column of blocks
     */
    private void createBlockColumn(float curXCord) {
        float height = ((int) terrainFunc(curXCord) / Block.SIZE) * Block.SIZE;
        int i = 0;
        while (i < TERRAIN_DEPTH) {
            Renderable blockImg = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
            GameObject block = new Block(new Vector2(curXCord, height), blockImg);
            block.setTag(GROUND_TAG);
            gameObjects.addGameObject(block, groundLayer);
            height += Block.SIZE;
            i++;
        }
    }

    /**
     * This function rounds up a given range to a block coordinate range, using the size of BLOCK.SIZE().
     * The function always starts calculations from 0 to ensure a uniform result every time it is called.
     *
     * @param minX int of given range minimum
     * @param maxX int of given range maximum
     * @return Vector2 object which contains final building range (x - minimum bound, y - maximum bound)
     */
    private Vector2 calcBuildRange(int minX, int maxX) {
        float min = 0f, max = Block.SIZE;
        if (minX > min) {
            while (minX > min)
                min += Block.SIZE;
            min -= Block.SIZE;
        } else if (minX < min)
            while (minX < min)
                min -= Block.SIZE;
        if (max > maxX) {
            while (max > maxX)
                max -= Block.SIZE;
            max += Block.SIZE;
        } else if (max < maxX)
            while (max < maxX)
                max += Block.SIZE;

        return new Vector2(min, max);
    }

    /**
     * generates ground blocks terrain and adds them to game
     * starts from x = 0 and keeps filling until all the area between
     * given minX - maxX are filled (including and sometimes overpassing)
     *
     * @param minX minimum x coordinates to fill from
     * @param maxX maximum x coordinates to reach
     */
    public void createInRange(int minX, int maxX) {
        Vector2 range = calcBuildRange(minX, maxX);
        for (float runner = range.x(); runner < range.y(); runner += Block.SIZE) {
            createBlockColumn(runner);
        }
    }
}
