package fun.raccoon.bunyedit.command.action.arguments.bound;

import java.util.EnumSet;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.net.command.exceptions.CommandExceptions;

public class BoundComponent { 
    public final EnumSet<BoundDirection> directions;
    public final int magnitude;

    public BoundComponent(EnumSet<BoundDirection> directions, int magnitude) {
        this.directions = directions;
        this.magnitude = magnitude;
    }

    public static BoundComponent parse(StringReader reader)
    throws CommandSyntaxException {
        EnumSet<BoundDirection> directions = parseDirections(reader);
        int magnitude = 0;

        if (reader.canRead() && isMagnitude(reader.peek())) {
            magnitude = reader.readInt(); // cursor lands outside number
        }

        return new BoundComponent(directions, magnitude);
    }


    private static EnumSet<BoundDirection> parseDirections(StringReader reader)
    throws CommandSyntaxException {
        // expecting N E S W U D, *, or F B L R
        if (!reader.canRead()) {
            throw CommandExceptions.incomplete().createWithContext(reader);
        }

        EnumSet<BoundDirection> set = EnumSet.noneOf(BoundDirection.class);
        char ch = reader.peek();

        // needs to start with abbrev
        if (!isValidAbbrev(ch)) {
            throw fun.raccoon.bunyedit.command.CommandExceptions.INVALID_BOUND.create();
        }

        while (reader.canRead()) {
            ch = reader.peek();
            BoundDirection dir = BoundDirection.fromAbbrev(ch);
            
            // valid direction abbrev
            if (dir != null) {
                set.add(dir);
                reader.skip();
            }
            // all six directions abbrev
            else if (ch == '*') {
                set = BoundDirection.allDirections();
                reader.skip();
            }
            // start of magnitude
            else if (isMagnitude(ch)) {
                break;
            }
            // illegal character
            else {
                throw fun.raccoon.bunyedit.command.CommandExceptions.INVALID_BOUND.create();
            }
        }

        return set;
    }

    private static boolean isValidAbbrev(char ch) {
        return (ch == '*') || BoundDirection.fromAbbrev(Character.toUpperCase(ch)) != null;
    }

    private static boolean isMagnitude(char ch) {
        return StringReader.isAllowedNumber(ch);
    }
}