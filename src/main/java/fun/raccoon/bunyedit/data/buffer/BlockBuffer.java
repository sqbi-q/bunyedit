package fun.raccoon.bunyedit.data.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkPosition;

/**
 * Mapping from {@link ChunkPosition} to {@link BlockData}.
 */
public class BlockBuffer extends HashMap<ChunkPosition, BlockData> {
    public BlockBuffer() {
        super();
    }

    public void placeRaw(World world, ChunkPosition pos, BlockData blockData) {
        this.put(pos, blockData);

        // TODO inform player that coords are outside bounds
        world.setBlockRaw(pos.x, pos.y, pos.z, blockData.id);
        world.setBlockMetadata(pos.x, pos.y, pos.z, blockData.meta);

        if (blockData.nbt == null) {
            // clean any tile entity left there
            world.removeBlockTileEntity(pos.x, pos.y, pos.z);
            return;
        }

        Block<?> asBlock = blockData.getBlock();
        if (asBlock == null)                return; // air.
        if (asBlock.entitySupplier == null) return; // no tile entity supplier
        
        // Prefer block position over read blockData.nbt xyz position, but so 
        // far it doesn't cause any problem. Need to look how NBTs look after save
        TileEntity entityFromBlock = asBlock.entitySupplier.get();
        entityFromBlock.readFromNBT(blockData.nbt);

        world.setTileEntity(
            pos.x, pos.y, pos.z,
            // TileEntity.createAndLoadEntity(blockData.nbt)
            entityFromBlock
        );
    }

    public void finalize(World world) {
        List<Pair<Integer, Integer>> recalced = new ArrayList<>();

        for (Entry<ChunkPosition, BlockData> entry : this.entrySet()) {
            ChunkPosition pos = entry.getKey();
            BlockData blockData = entry.getValue();

            Pair<Integer, Integer> chunkPos = Pair.of(
                Math.floorDiv(pos.x, 16),
                Math.floorDiv(pos.z, 16));
            if (!recalced.contains(chunkPos)) {
                recalced.add(chunkPos);
                world.getChunkFromChunkCoords(
                        chunkPos.getLeft(),
                        chunkPos.getRight())
                    .recalcHeightmap();
            }
            world.scheduleLightingUpdate(LightLayer.Sky, pos.x, pos.y, pos.z, pos.x, pos.y, pos.z);

            //Block block = Block.getBlock(blockData.id);
            //if (block != null)
            //    block.onBlockAdded(world, pos.x, pos.y, pos.z);

            world.notifyBlockChange(pos.x, pos.y, pos.z, blockData.id);
        }
    }
}
