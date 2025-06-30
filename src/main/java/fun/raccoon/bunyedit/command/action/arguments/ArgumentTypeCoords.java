package fun.raccoon.bunyedit.command.action.arguments;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.core.net.command.arguments.ArgumentTypeIntegerCoordinates;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.net.command.helpers.IntegerCoordinate;

public class ArgumentTypeCoords implements ArgumentType<Coords> {

    private boolean areCoordinatesLocal(StringReader reader) {
        return reader.canRead() && reader.peek() == '^';
    }

    private IntegerCoordinate parseLocalCoordinate(StringReader reader) 
    throws CommandSyntaxException {
        // expecting ^
        if (!reader.canRead()) {
            throw CommandExceptions.incomplete().createWithContext(reader);
        }

        if (reader.peek() != '^') {
            // can't mix local and relative/absolute coords
            // TODO rename to avoid name collision
            throw fun.raccoon.bunyedit.command.CommandExceptions.INVALID_COORDS.createWithContext(reader);
        }

        // local coordinates handling
        reader.skip(); // skip ^

        if (!reader.canRead()) {
            return new IntegerCoordinate(false, 0);
        }

        if (reader.peek() == ' ' || reader.peek() == ',') {
            reader.skip(); // skip space or comma
            return new IntegerCoordinate(false, 0);
        }

        int coord = reader.readInt();
        reader.skip(); // skip space or comma
        return new IntegerCoordinate(false, coord);
    }

    private IntegerCoordinate parseCoordinate(StringReader reader)
    throws CommandSyntaxException {
        IntegerCoordinate coord = IntegerCoordinate.parse(reader);
        reader.skip(); // skip space
        return coord;
    }


    @Override
    public Coords parse(StringReader reader) throws CommandSyntaxException {
        if (areCoordinatesLocal(reader)) {
            return new Coords(
                true, 
                parseLocalCoordinate(reader),
                parseLocalCoordinate(reader),
                parseLocalCoordinate(reader)
            );
        }

        return new Coords(
            false,
            parseCoordinate(reader),
            parseCoordinate(reader),
            parseCoordinate(reader)
        );
    }


    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return new ArgumentTypeIntegerCoordinates().getExamples();
    }
}
