package fun.raccoon.bunyedit.command.action.arguments.block;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.data.buffer.BlockData;

// Buny prefix to differentiate from ArgumentTypeBlock from Brigadier.
// Also unused for now.
public class BunyArgumentTypeBlock implements ArgumentType<BlockData> {
    private static final List<String> EXAMPLES = Arrays.asList("stone", "1", "log.oak:1");

    public static ArgumentType<BlockData> block() {
        return new BunyArgumentTypeBlock();
    }

    // TODO suggestions

    @Override
    public BlockData parse(StringReader reader) throws CommandSyntaxException {
        BunyBlockArgumentParser parser = new BunyBlockArgumentParser(reader);
        return parser.parsePattern();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
    
}