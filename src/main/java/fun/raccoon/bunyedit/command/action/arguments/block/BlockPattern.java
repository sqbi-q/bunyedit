package fun.raccoon.bunyedit.command.action.arguments.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.data.buffer.BlockData;

public class BlockPattern implements ArgumentType<WeightedBlock[]> {
    private static final List<String> EXAMPLES = Arrays.asList("stone[/...]", "1[/...]", "2*wool:5[/...]");

    public static ArgumentType<WeightedBlock[]> weightedBlocks() {
        return new BlockPattern();
    }

    @Override
    public WeightedBlock[] parse(StringReader reader) throws CommandSyntaxException {
        List<WeightedBlock> blocks = new ArrayList<>();

        BunyBlockArgumentParser blockParser = new BunyBlockArgumentParser(reader);

        do {
            @Nullable Integer weight = getBlockWeight(reader);
            BlockData block = blockParser.parsePattern(); // changes passed reader state
            blocks.add(new WeightedBlock(block, weight));

            if (!reader.canRead() || reader.peek() != '/') {
                break;
            }
            reader.skip(); // skip '/'
        }
        while (reader.canRead());

        WeightedBlock[] blocksArray = new WeightedBlock[blocks.size()];
        blocks.toArray(blocksArray);

        return blocksArray;
    }

    /**
     * Looksup for `<weight>*` which can be ambiguous with numerical id of block.
     * @return Integer weight or null on invalid weight.
     * @throws CommandSyntaxException
     */ 
    public @Nullable Integer getBlockWeight(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        
        if (!reader.canRead() || !StringReader.isAllowedNumber(reader.peek())) {
            return null;
        }

        int weight = reader.readInt(); // Shouldn't throw...
        
        if (!reader.canRead() || weight < 0 || reader.peek() != '*') {
            // It is not weighted, we are reading id!
            reader.setCursor(start);
            return null;
        }

        reader.skip(); // skip '*'
        return weight;
    }


    public static WeightedBlock getRandomBlock(@Nonnull WeightedBlock[] blocks) {
        int weightTotal = 0;

        for (final WeightedBlock block : blocks) {
            weightTotal += block.getWeight();
        }

        int rand = new Random().nextInt(weightTotal);

        for (WeightedBlock block : blocks) {
            if (rand < block.getWeight()) {
                return block;
            }
            rand -= block.getWeight();
        }

        throw new IllegalArgumentException("Pattern must contain blocks, found no element array.");
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
