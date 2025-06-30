package fun.raccoon.bunyedit.command.action.arguments;

import javax.annotation.Nonnull;

import fun.raccoon.bunyedit.data.look.LookAxis;
import fun.raccoon.bunyedit.data.look.LookDirection;
import net.minecraft.core.util.helper.Axis;

public class BunyAxis {
    private final Axis axis;
    private final boolean isLocal;

    private BunyAxis(Axis axis, boolean isLocal) {
        this.axis = axis;
        this.isLocal = isLocal;
    }

    public static BunyAxis fromAxis(@Nonnull Axis axis) {
        return new BunyAxis(axis, false);
    }

    public static BunyAxis playerDirection() {
        return new BunyAxis(Axis.NONE, true);
    }

    public Axis getDirection(LookDirection playerDir) {
        if (isLocal) {
            return playerDir.globalAxis(LookAxis.SURGE);
        }

        return axis;
    }
}
