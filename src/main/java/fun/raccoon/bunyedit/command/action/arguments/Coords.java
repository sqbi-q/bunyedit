package fun.raccoon.bunyedit.command.action.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.data.look.LookAxis;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.net.command.helpers.IntegerCoordinate;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Coords {
    private final IntegerCoordinate x;
    private final IntegerCoordinate y;
    private final IntegerCoordinate z;
    private final boolean isLocal;

    public Coords(
        boolean isLocal,
        IntegerCoordinate x, IntegerCoordinate y, IntegerCoordinate z
    ) {
        this.isLocal = isLocal;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ChunkPosition asAbsolute(ChunkPosition source, LookDirection lookDir)
    throws CommandSyntaxException {
        if (isLocal) {
            LookAxis[] lookAxes = LookAxis.values();
            int[] absCoords = new int[3];
            int[] sourceCoords = PosMath.toArray(source);
            IntegerCoordinate[] triple = {x, y, z};
            
            for (int i = 0; i < 3; i++) {
                int axis = lookDir.globalAxis(lookAxes[i]).ordinal();
                boolean inv = lookDir.globalInv(lookAxes[i]);

                absCoords[axis] = sourceCoords[axis] + (inv ? -1 : 1) * triple[i].get(null);
            }

            return PosMath.fromArray(absCoords);
        }

        return new ChunkPosition(
            x.get(source.x),
            y.get(source.y),
            z.get(source.z)
        );
    }

    public boolean isLocal() {
        return this.isLocal;
    }
}
