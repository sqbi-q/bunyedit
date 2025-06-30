package fun.raccoon.bunyedit.command.action.arguments.block;

import javax.annotation.Nullable;

import fun.raccoon.bunyedit.data.buffer.BlockData;

public class WeightedBlock extends BlockData {
    private final int weight;

    public WeightedBlock(BlockData blockInput, @Nullable Integer weight) {
        super(blockInput.id, blockInput.meta, null);
        this.weight = (weight == null) ? 1 : weight;
    }

    public int getWeight() {
        return weight;
    }
}
