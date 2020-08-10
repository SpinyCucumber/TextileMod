package spinyq.immersivetextiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import spinyq.immersivetextiles.TextileMod;
import spinyq.immersivetextiles.tileentities.TileEntityBasin;

public class BlockBasin extends Block {

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityBasin();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public BlockBasin() {
		super(Material.IRON, MapColor.STONE);
		this.setRegistryName(new ResourceLocation(ImmersiveTextiles.TextileMod.MODID, "basin"));
		this.setUnlocalizedName("basin");
	}
	
}
