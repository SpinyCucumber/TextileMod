package spinyq.spinytextiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
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
import spinyq.spinytextiles.items.IDyeableItem;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.utility.Color3f;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class BasinBlock extends Block {

	// Ala Cauldron
	private static final VoxelShape INSIDE = makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(),
			VoxelShapes.or(makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
					makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
					makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE),
			IBooleanFunction.ONLY_FIRST);

	private static final float GLOWSTONE_SAT_AMT = 0.2f;
	
	@SuppressWarnings("deprecation")
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		// Check that the player is indeed interacting with the tile entity
		TileEntity tile = world.getTileEntity(pos);
		
		if (tile instanceof BasinTile) {
			
			BasinTile basin = (BasinTile) tile;
			ItemStack itemstack = player.getHeldItem(handIn);
			// Only care about clicking with items
			if (itemstack.isEmpty()) {
				return ActionResultType.PASS;
			} else {
				// Adapted from cauldron code
				// Interacting on a basin with a water bucket fills the basin
				Item item = itemstack.getItem();
				if (item == Items.WATER_BUCKET && basin.isEmpty()) {
					
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
				// Interacting on a basin with dye consumes the dye and changes the color of the basin
				// The basin must be full and heated
				else if (item instanceof DyeItem && !basin.isSaturated() && basin.isFull() && basin.isHeated()) {
					if (!world.isRemote) {
						// Consume one item if player is not in creative
						if (!player.abilities.isCreativeMode) {
							itemstack.shrink(1);
						}
						// Retrieve the color of the dye
						DyeItem dye = (DyeItem) item;
						Color3f dyeColor = Color3f.fromDye(dye.getDyeColor());
						// Mix the color into the basin
						basin.mixDye(dyeColor);
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					
					return ActionResultType.SUCCESS;
					
				}
				// Interacting on a basin with a dyeable item dyes the item and consumes some water
				// The basin must also be heated
				else if (item instanceof IDyeableItem && basin.canDye((IDyeableItem) item) && !basin.isEmpty() && basin.isHeated()) {
					IDyeableItem dyeable = (IDyeableItem) item;
					if (!world.isRemote) {
						// Dye the item
						basin.dye(itemstack, player.inventory, dyeable);
						world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					
					return ActionResultType.SUCCESS;
					
				}
				// Interacting on a basin with glowstone dust boosts the saturation of the color
				// Basin must be full and heated
				else if (item == Items.GLOWSTONE_DUST && basin.canBoostSaturation(GLOWSTONE_SAT_AMT)
						&& basin.isFull() && basin.isHeated()) {
					if (!world.isRemote) {
						// Consume one item if player is not in creative
						if (!player.abilities.isCreativeMode) {
							itemstack.shrink(1);
						}
						// Boost saturation
						basin.boostColorSaturation(GLOWSTONE_SAT_AMT);
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
