package fun.raccoon.bunyedit.command.action.arguments.bound;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class ArgumentTypeBound implements ArgumentType<Bound> {

    @Override
    public Bound parse(StringReader reader) throws CommandSyntaxException {
        Bound bound = new Bound();

        do {
            BoundComponent component = BoundComponent.parse(reader);
            bound.set(component);
        }
        while (reader.canRead() && reader.read() == ',');

        return bound;
    }
}
