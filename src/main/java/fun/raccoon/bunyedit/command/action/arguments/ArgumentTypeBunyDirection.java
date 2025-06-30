package fun.raccoon.bunyedit.command.action.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.util.DirectionHelper;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.util.helper.Direction;

public class ArgumentTypeBunyDirection implements ArgumentType<BunyDirection> {
    
    @Override
    public BunyDirection parse(StringReader reader) throws CommandSyntaxException {
        // expecting N|E|S|W|U|D|^
        if (!reader.canRead()) {
            throw CommandExceptions.incomplete().createWithContext(reader);
        }

        char input = reader.read();

        if (input == '^') {
            return BunyDirection.playerDirection();
        }

        String abbrev = Character.toString(input).toUpperCase();
        Direction dir = DirectionHelper.fromAbbrev(abbrev);
        
        if (dir == null) {
            // TODO rename to avoid name collision
            throw fun.raccoon.bunyedit.command.CommandExceptions.INVALID_DIRECTION.create();
        }

        return BunyDirection.fromDirection(dir);
    }
}
