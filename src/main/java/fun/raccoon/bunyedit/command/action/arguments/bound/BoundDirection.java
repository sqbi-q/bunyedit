package fun.raccoon.bunyedit.command.action.arguments.bound;

import java.util.EnumSet;

import javax.annotation.Nullable;

import fun.raccoon.bunyedit.data.look.LookAxis;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.world.chunk.ChunkPosition;

public enum BoundDirection {
    NORTH   (2, false /* 0,  0, -1*/),
    EAST    (0, true  /* 1,  0,  0*/),
    SOUTH   (2, true  /* 0,  0,  1*/),
    WEST    (0, false /*-1,  0,  0*/),
    UP      (1, true  /* 0,  1,  0*/),
    DOWN    (1, false /* 0, -1,  0*/),

    FORWARD (BoundDirection.SURGE_AXIS, true),
    BACKWARD(BoundDirection.SURGE_AXIS, false),
    LEFT    (BoundDirection.SWAY_AXIS,  false),
    RIGHT   (BoundDirection.SWAY_AXIS,  true);

    public static final int SURGE_AXIS = 3;
    public static final int SWAY_AXIS = 4;

    /**
     * X = 0, Y = 1, Z = 2;
     * SURGE = 3, SWAY = 4. 
     */
    public final int axisAsOrdinal;
    public final boolean isPositive;

    private BoundDirection(int axisAsOrdinal, boolean isPositive) {
        this.axisAsOrdinal = axisAsOrdinal;
        this.isPositive = isPositive;
    }

    /**
     * All six directions: North, East, South, West, Up and Down.
     */
    public static EnumSet<BoundDirection> allDirections() {
        return EnumSet.range(NORTH, DOWN);
    }

    public static @Nullable BoundDirection fromAbbrev(char abbrev) {
        switch(Character.toUpperCase(abbrev)) {
            case 'N': return NORTH;
            case 'E': return EAST;
            case 'S': return SOUTH;
            case 'W': return WEST;
            case 'U': return UP;
            case 'D': return DOWN;

            case 'F': return FORWARD;
            case 'B': return BACKWARD;
            case 'L': return LEFT;
            case 'R': return RIGHT;

            default: 
                return null;
        }
    }

    public int asGlobalAxisOrdinal(LookDirection lookDir) {
        switch (axisAsOrdinal) {
            case 0:
            case 1:
            case 2:
                return axisAsOrdinal;

            case SURGE_AXIS: return lookDir.globalAxis(LookAxis.SURGE).ordinal();
            case SWAY_AXIS:  return lookDir.globalAxis(LookAxis.SWAY).ordinal();

            default:
                throw new RuntimeException("Invalid axis of enum BoundDirection");
        }
    }

    private int getAxisSign(LookDirection lookDir) {
        int originSign = isPositive ? 1 : -1;

        switch (axisAsOrdinal) {
            case 0:
            case 1:
            case 2:
                return originSign;
            
            case SURGE_AXIS: 
                return (lookDir.globalInv(LookAxis.SURGE) ? -1 : 1) * originSign;
            case SWAY_AXIS:
                return (lookDir.globalInv(LookAxis.SWAY) ? -1 : 1) * originSign;

            default:
                throw new RuntimeException("Invalid axis of enum BoundDirection");
        }
    }

    public ChunkPosition directionOffset(LookDirection lookDir) {
        int[] offset = new int[3];
        offset[asGlobalAxisOrdinal(lookDir)] = getAxisSign(lookDir);
        return PosMath.fromArray(offset);
    }
}