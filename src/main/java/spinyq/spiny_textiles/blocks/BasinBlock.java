package spinyq.spiny_textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import spinyq.spiny_textiles.tiles.BasinTile;

public class BasinBlock extends Block {

	public BasinBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BasinTile();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
}
