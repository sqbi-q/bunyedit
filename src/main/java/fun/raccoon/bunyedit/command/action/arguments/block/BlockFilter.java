package fun.raccoon.bunyedit.command.action.arguments.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.data.buffer.BlockData;

public class BlockFilter implements ArgumentType<Predicate<BlockData>> {

    public static ArgumentType<Predicate<BlockData>> blocksPredicate() {
        return new BlockFilter();
    }


    @Override
    public Predicate<BlockData> parse(StringReader reader) throws CommandSyntaxException {
        List<Predicate<BlockData>> predicates = new ArrayList<>();

        BunyBlockArgumentParser blockParser = new BunyBlockArgumentParser(reader);

        do {
            boolean invert = false;

            if (isBlockInverted(reader)) {
                reader.skip(); // skip negation '!'
                invert = true;
            }

            Predicate<BlockData> blocks = blockParser.parseFilter();            
            if (invert) {
                blocks = blocks.negate();
            }
            predicates.add(blocks);

            if (!reader.canRead() || reader.peek() != '/') {
                break;
            }
            reader.skip(); // skip '/'
        }
        while (reader.canRead());

        return join(predicates);
    }


    private static boolean isBlockInverted(StringReader reader) {
        return reader.peek() == '!';
    }

    private @Nonnull Predicate<BlockData> join(@Nonnull List<Predicate<BlockData>> blocks) {
        return blocks.stream().reduce((a, b) -> a.or(b)).get();
    }
}
