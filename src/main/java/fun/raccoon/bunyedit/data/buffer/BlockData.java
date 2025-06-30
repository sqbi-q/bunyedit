package fun.raccoon.bunyedit.data.buffer;

import javax.annotation.Nullable;

import com.mojang.nbt.tags.CompoundTag;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkPosition;

/**
 * Worldless representation of a block for storing and placing later.
 */
public class BlockData {
    /**
     * Block ID.
     */
    public final int id;

    /**
     * Block metadata.
     */
    public final int meta;

    /**
     * NBT data for a tile entity. null for regular blocks.
     */
    public final @Nullable CompoundTag nbt;

    /**
     * Air.
     */
    public BlockData() {
        this.id = 0;
        this.meta = 0;
        this.nbt = null;
    }

    public BlockData(@Nullable Block<?> block) {
        // Null block is basically air
        if (block == null) {
            this.id = 0;
        } else {
            this.id = block.id();
        }
        this.meta = 0;
        this.nbt = null;
    }

    public BlockData(int id, int meta, CompoundTag nbt) {
        this.id = id;
        this.meta = meta;
        this.nbt = nbt;
    }

    private BlockData(World world, int x, int y, int z) {
        this.id = world.getBlockId(x, y, z);
        this.meta = world.getBlockMetadata(x, y, z);

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        CompoundTag compoundTag;
        if (tileEntity == null) {
            compoundTag = null;
        } else {
            compoundTag = new CompoundTag();
            tileEntity.writeToNBT(compoundTag);
        }
        this.nbt = compoundTag;
    }

    public BlockData(World world, ChunkPosition pos) {
        this(world, pos.x, pos.y, pos.z);
    }

    /**
     * Get `Block` block by id from list of blocks.
     */
    public @Nullable Block<?> getBlock() {
        return Blocks.getBlock(id);
    }

    /**
     * Does the block at this location match this BlockData in ID and metadata?
     */
    public boolean idMetaMatches(World world, ChunkPosition pos) {
        int id = world.getBlockId(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);

        return this.id == id && this.meta == meta;
    }
}
