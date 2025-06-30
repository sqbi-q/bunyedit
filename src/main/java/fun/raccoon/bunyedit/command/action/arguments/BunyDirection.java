package fun.raccoon.bunyedit.command.action.arguments;

import javax.annotation.Nonnull;

import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.util.DirectionHelper;
import net.minecraft.core.util.helper.Direction;

public class BunyDirection {
    private final Direction direction;
    private final boolean isLocal;

    private BunyDirection(Direction direction, boolean isLocal) {
        this.direction = direction;
        this.isLocal = isLocal;
    }

    public static BunyDirection fromDirection(@Nonnull Direction dir) {
        return new BunyDirection(dir, false);
    }

    public static BunyDirection playerDirection() {
        return new BunyDirection(null, true);
    }

    public Direction getDirection(LookDirection playerDir) {
        if (isLocal) {
            return DirectionHelper.from(playerDir);
        }

        return direction;
    }
}
