package fun.raccoon.bunyedit.command.action.arguments.bound;

import java.util.EnumMap;
import java.util.Map.Entry;

import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.chunk.ChunkPosition;

/**
 * Bound parser for usage with Brigadier as ArgumentType<T>.
 * Alternative to orignal Bound based on Strings.
 * Stores map between each direction and magnitude used for offsets. 
 */
public class Bound {
    private EnumMap<BoundDirection, Integer> directionMagnitudes;

    public Bound() {
        directionMagnitudes = new EnumMap<>(BoundDirection.class);
    }

    public Bound(EnumMap<BoundDirection, Integer> directionMagnitudes) {
        this.directionMagnitudes = directionMagnitudes;
    }

    /**
     * Set magnitude in one direction.
     */
    public Bound set(BoundDirection direction, int magnitude) {
        this.directionMagnitudes.put(direction, magnitude);
        return this;
    }

    /**
     * Set magnitude in specified directions.
     */
    public Bound set(BoundComponent component) {
        for (BoundDirection dir : component.directions) {
            set(dir, component.magnitude);
        }
        return this;
    }

    /**
     * Set magnitude in all six directions.
     */
    public Bound all(int magnitude) {

        for (BoundDirection dir : BoundDirection.allDirections()) {
            int currentValue = directionMagnitudes.getOrDefault(dir, 0);
            directionMagnitudes.put(dir, currentValue + magnitude);
        }

        return this;
    }


    /**
     * Offsets pair used for bound selection growth.
     */
    public Pair<ChunkPosition, ChunkPosition> getOffsets(
        ValidSelection selection, LookDirection playerLook
    ) {
        ChunkPosition primary = selection.getPrimary();
        ChunkPosition secondary = selection.getSecondary();

        boolean isPrimaryFarthest[] = {
            (primary.x > secondary.x),
            (primary.y > secondary.y),
            (primary.z > secondary.z)
        };

        ChunkPosition offsetPrimary = PosMath.all(0);
        ChunkPosition offsetSecondary = PosMath.all(0);

        // Look-Axes and normal Axes can cumulate
        // TODO Notify on collision between look-axis and normal-axis (e.g. N and F are same direction)

        for (Entry<BoundDirection, Integer> entry : directionMagnitudes.entrySet()) {
            BoundDirection direction = entry.getKey();
            Integer magnitude = entry.getValue();

            // Turn eventual look-axes to global ones
            int axis = direction.asGlobalAxisOrdinal(playerLook);
            /*     S
             *     ^
             * W <-+-> E
             *     v
             *     N
             * S and E are positive, N and W are negative
             */

            boolean shouldPrimaryBeOffset = direction.isPositive ^ !isPrimaryFarthest[axis]; 

            ChunkPosition offset = PosMath.mul( 
                direction.directionOffset(playerLook),
                PosMath.all(magnitude)
            );

            if (shouldPrimaryBeOffset) {
                offsetPrimary = PosMath.add(offsetPrimary, offset);
            } else {
                offsetSecondary = PosMath.add(offsetSecondary, offset);
            }
        }

        return Pair.of(offsetPrimary, offsetSecondary);
    }

    public EnumMap<BoundDirection, Integer> getDirectionMagnitudes() {
        return directionMagnitudes;
    }
}
