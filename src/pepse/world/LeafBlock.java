package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class LeafBlock extends GameObject {

    private boolean transitionsActive;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public LeafBlock(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(Block.SIZE), renderable);
        transitionsActive = false;
    }

    public void fall(float yCoordinate) {
        this.transform().setTopLeftCorner(new Vector2(
                this.getTopLeftCorner().x(),
                yCoordinate
        ));
    }


    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        this.transform().setVelocity(Vector2.ZERO);
        this.transform().setAccelerationY(0);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    /**
     * Returns whether transitions between levels are currently active.
     *
     * @return true if transitions are active, false otherwise
     */
    public boolean isTransitionsActive() {
        return transitionsActive;
    }

    /**
     * Sets the value of the transitionsActive field.
     *
     * @param transitionsActive the new value for the transitionsActive field
     */
    public void setTransitionsActive(boolean transitionsActive) {
        this.transitionsActive = transitionsActive;
    }

}
