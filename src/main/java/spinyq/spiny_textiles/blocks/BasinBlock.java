package spinyq.spiny_textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import spinyq.spiny_textiles.TextileMod;
import spinyq.spiny_textiles.tiles.BasinTile;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class BasinBlock extends Block {

	// Ala Cauldron
	private static final VoxelShape INSIDE = makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(),
			VoxelShapes.or(makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
					makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
					makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE),
			IBooleanFunction.ONLY_FIRST);

	@SuppressWarnings("deprecation")
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		// Check that the player is indeed interacting with the tile entity
		TextileMod.LOGGER.info("BasinBlock Activated...");
		TileEntity tile = world.getTileEntity(pos);
		
		if (tile instanceof BasinTile) {
			
			BasinTile basin = (BasinTile) tile;
			ItemStack itemstack = player.getHeldItem(handIn);
			// Only care about clicking with items
			if (itemstack.isEmpty()) {
				return ActionResultType.PASS;
			} else {
				// Adapted from cauldron code
				Item item = itemstack.getItem();
				if (item == Items.WATER_BUCKET && basin.isEmpty()) {
					TextileMod.LOGGER.info("BasinBlock Water Bucket...");
					
					if (!world.isRemote) {
						if (!player.abilities.isCreativeMode) {
							player.setHeldItem(handIn, new ItemStack(Items.BUCKET));
						}
						basin.fill();
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					
					return ActionResultType.SUCCESS;
				}
			}
		}
		
		return super.onBlockActivated(state, world, pos, player, handIn, hit);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return INSIDE;
	}

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

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}

}
