package fun.raccoon.bunyedit.command.action.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.util.helper.Axis;

public class ArgumentTypeBunyAxis implements ArgumentType<BunyAxis> {

    @Override
    public BunyAxis parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw CommandExceptions.incomplete().createWithContext(reader);
        }

        char ch = reader.read();
        switch (Character.toUpperCase(ch)) {
            case 'X': return BunyAxis.fromAxis(Axis.X);
            case 'Y': return BunyAxis.fromAxis(Axis.Y);
            case 'Z': return BunyAxis.fromAxis(Axis.Z);
            case '^': return BunyAxis.playerDirection();
            default:
                // TODO rename to avoid name collision
                throw fun.raccoon.bunyedit.command.CommandExceptions.INVALID_AXIS.create();
        }
    }
}
