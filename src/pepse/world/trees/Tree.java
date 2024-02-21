package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.MutableVector2;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;
import pepse.world.LeafBlock;
import pepse.world.Terrain;

import java.awt.*;
import java.util.*;

public class Tree {

    private static final int TREE_PLANT_PERCENTAGE = 10;
    private static final int TREE_MAX_BLOCK_HEIGHT = 15;
    private static final int TREE_MIN_BLOCK_HEIGHT = 8;
    private static final Color TREE_BLOCK_COLOR = new Color(100, 50, 20);
    private static final Color LEAF_BLOCK_COLOR = new Color(50, 200, 30);
    public static final int LEAF_BLOCK_RANGE = 2;
    public static final int LEAF_PLANT_PERCENTAGE = 8;
    public static final float LEAF_OPACITY = 0.8f;
    public static final float DELAY_OF_FIVE = 5f;
    public static final String LEAF_TAG = "leaf";
    public static final String TREE_TAG = "tree";


    private final GameObjectCollection gameObjects;
    private final Terrain terrain;
    private final int treeLayer;
    private final int leafLayer;
    private final Random random;
    private final MutableVector2 windowBorder;


    public Tree(GameObjectCollection gameObjects, Terrain terrain, int treeLayer, MutableVector2 windowBorder,
                int leafLayer, long seed) {

        this.gameObjects = gameObjects;
        this.terrain = terrain;
        this.treeLayer = treeLayer;
        this.leafLayer = leafLayer;
        this.windowBorder = windowBorder; // for use in transition control
        random = new Random(Objects.hash(60, seed));
    }

    /**
     * Generates a set of x coordinates for tree placement within a given range.
     *
     * @param minX the minimum x coordinate
     * @param maxX the maximum x coordinate
     * @return a set of x coordinates for tree placement
     */
    private Set<Float> getTreeCoordinates(int minX, int maxX) {
        Set<Float> coordinateSet = new HashSet<>();
        int relativeRange = (int) (((maxX - minX) - ((maxX - minX) % Block.SIZE)) / Block.SIZE);
        int i = 0;
        while (i < relativeRange) {
            if (random.nextInt(100) < TREE_PLANT_PERCENTAGE) {
                coordinateSet.add((float) i * Block.SIZE + minX + ((maxX - minX) % Block.SIZE));
                i += 2;
            } else {
                i++;
            }
        }
        return coordinateSet;
    }


    /**
     * Generates a random tree height within a given range.
     *
     * @return a random tree height
     */
    private int getRandomHeight() {
        return random.nextInt((TREE_MAX_BLOCK_HEIGHT - TREE_MIN_BLOCK_HEIGHT) + 1) + TREE_MIN_BLOCK_HEIGHT;
    }

    /**
     * planting a single tree (sorting blocks) in the
     * given coordinates
     *
     * @param xCoordinate float of x coordinate to plant tree in
     * @param yCoordinate float of y coordinate to plant tree in
     * @return GameObject of the last tree Block (top)
     * @throws IndexOutOfBoundsException
     */
    private GameObject createTree(float xCoordinate, float yCoordinate) throws IndexOutOfBoundsException {
        int height = getRandomHeight();
        Renderable treeBlockImg = new RectangleRenderable(ColorSupplier.approximateColor(TREE_BLOCK_COLOR));
        GameObject treeBlock = null;
        for (int i = 0; i < height; ++i) {
            Vector2 coordinates = new Vector2(xCoordinate - (xCoordinate % Block.SIZE),
                    yCoordinate - (i * Block.SIZE));
            treeBlock = new Block(coordinates, treeBlockImg);
            gameObjects.addGameObject(treeBlock, treeLayer);
            treeBlock.setTag(TREE_TAG);
        }
        if (treeBlock == null) {
            throw new IndexOutOfBoundsException();
        }
        return treeBlock;
    }


    /**
     * function generates coordinates of leaves to build around a tree of given
     * topLeftCorner given
     *
     * @param treeTopLeftCorner Vector2 of the last tree block to build leaves around
     * @return set of Vector2 object of the coordinates (TopLeft) to build leaves in
     */
    Set<Vector2> getLeafCoordinates(Vector2 treeTopLeftCorner) {
        Set<Vector2> coordinates = new HashSet<>();
        for (int x = -LEAF_BLOCK_RANGE; x <= LEAF_BLOCK_RANGE; ++x) {
            for (int y = -LEAF_BLOCK_RANGE; y <= LEAF_BLOCK_RANGE; ++y) {
                if (random.nextInt(10) < LEAF_PLANT_PERCENTAGE) {
                    Vector2 coordinate = new Vector2(
                            treeTopLeftCorner.x() + x * Block.SIZE,
                            treeTopLeftCorner.y() + y * Block.SIZE
                    );
                    coordinates.add(coordinate);
                }
            }
        }
        return coordinates;
    }


    /**
     * provides random angles to replace the delay
     *
     * @return float of randomly chosen angle
     */
    private float randomizeAngle() {
        int rand = random.nextInt(4);
        if (rand == 0) {
            return -DELAY_OF_FIVE;
        } else if (rand == 1) {
            return DELAY_OF_FIVE;
        } else if (rand == 2) {
            return -2 * DELAY_OF_FIVE;
        } else if (rand == 3) {
            return 2 * DELAY_OF_FIVE;
        }
        return 0 * DELAY_OF_FIVE;
    }


    /**
     * Runs the necessary tasks for a leaf block.
     *
     * @param leaf the leaf block
     */
    private void initializeLeafBlock(LeafBlock leaf) {
        Vector2 initialTopLeftCorner = new Vector2(leaf.getTopLeftCorner().x(), leaf.getTopLeftCorner().y());
        float init = randomizeAngle();

        createAngleTransition(leaf, init);
        createDimensionTransition(leaf);
        createLeafLife(leaf, initialTopLeftCorner);
        createOffScreenCheck(leaf);
    }

    /**
     * Creates an angle transition for a leaf block.
     *
     * @param leaf the leaf block
     * @param init the initial angle value
     */
    private void createAngleTransition(LeafBlock leaf, float init) {
        Transition<Float> angleTransition = new Transition<>(
                leaf,
                leaf.renderer()::setRenderableAngle,
                init,
                -init,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                0.6f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        leaf.addComponent(angleTransition);
    }

    /**
     * Creates a dimension transition for a leaf block.
     *
     * @param leaf the leaf block
     */
    private void createDimensionTransition(LeafBlock leaf) {
        Transition<Vector2> dimensionTransition = new Transition<>(
                leaf,
                leaf::setDimensions,
                leaf.getDimensions().mult(0.985f),
                leaf.getDimensions().mult(1.005f),
                Transition.CUBIC_INTERPOLATOR_VECTOR,
                0.8f,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        leaf.addComponent(dimensionTransition);
    }

    /**
     * Creates a scheduled task for a leaf block's life cycle.
     *
     * @param leaf                 the leaf block
     * @param initialTopLeftCorner the initial top-left corner position of the leaf block
     */
    private void createLeafLife(LeafBlock leaf, Vector2 initialTopLeftCorner) {
        ScheduledTask leafLife = new ScheduledTask(leaf, random.nextInt(60) + 20,
                true, () -> {
            Transition<Float> fallTransition = new Transition<>(
                    leaf,
                    leaf.transform()::setVelocityY,
                    0f,
                    200f,
                    Transition.LINEAR_INTERPOLATOR_FLOAT,
                    0.1f,
                    Transition.TransitionType.TRANSITION_ONCE,
                    null
            );

            leaf.renderer().fadeOut(10f, () -> {
                leaf.renderer().setOpaqueness(LEAF_OPACITY);
                leaf.transform().setTopLeftCorner(initialTopLeftCorner);
            });
        });

        leaf.addComponent(leafLife);
    }

    /**
     * Creates a component that deactivates transitions when a leaf block goes off screen.
     *
     * @param leaf the leaf block
     */
    private void createOffScreenCheck(LeafBlock leaf) {
        leaf.setTransitionsActive(true);
        leaf.addComponent(deltaTime -> {
            if (leaf.getTopLeftCorner().x() >= windowBorder.x() && leaf.getTopLeftCorner().x()
                    + Block.SIZE <= windowBorder.y()) {
                if (!leaf.isTransitionsActive()) {
                    leaf.setTransitionsActive(true);
                }
            } else {
                if (leaf.isTransitionsActive()) {
                    leaf.setTransitionsActive(false);
                }
            }
        });
    }


    /**
     * Creates LeafBlock objects and adds them to the gameObjects collection.
     *
     * @param lastTreeBlock the last tree block in the tree
     */
    private void createLeaves(GameObject lastTreeBlock) {
        Vector2 treeCoordinates = lastTreeBlock.getTopLeftCorner();
        Set<Vector2> coordinateSet = getLeafCoordinates(treeCoordinates);
        Renderable leafBlockLmg = new RectangleRenderable(ColorSupplier.approximateColor(LEAF_BLOCK_COLOR));
        Iterator<Vector2> it = coordinateSet.iterator();
        while (it.hasNext()) {
            Vector2 coordinate = it.next();
            LeafBlock leaf = new LeafBlock(coordinate, leafBlockLmg);
            leaf.renderer().setOpaqueness(LEAF_OPACITY);
            leaf.setTag(LEAF_TAG);

            initializeLeafBlock(leaf);

            gameObjects.addGameObject(leaf, leafLayer);
        }
    }

    /**
     * Creates trees within a given range of x coordinates.
     *
     * @param minX the minimum x coordinate
     * @param maxX the maximum x coordinate
     */
    public void createInRange(int minX, int maxX) {
        Set<Float> coordinateSet = getTreeCoordinates(minX, maxX);
        Iterator<Float> it = coordinateSet.iterator();
        while (it.hasNext()) {
            float x = it.next();
            float y = terrain.groundHeightAt(x);
            GameObject lastTreeBlock = createTree(x, y);
            createLeaves(lastTreeBlock);
        }
    }


}
