package spinyq.spinytextiles.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import spinyq.spinytextiles.tiles.BasinTile;
import spinyq.spinytextiles.tiles.BasinTile.BasinStateVisitor;
import spinyq.spinytextiles.tiles.BasinTile.BleachState;
import spinyq.spinytextiles.tiles.BasinTile.FilledState;

public class BasinBlock extends Block {

	// Ala Cauldron
	private static final VoxelShape INSIDE = makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(),
			VoxelShapes.or(makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
					makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
					makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE),
			IBooleanFunction.ONLY_FIRST);

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		// Check that the player is indeed interacting with the tile entity
		TileEntity tile = world.getTileEntity(pos);
		// Pass logic over to tile entity
		if (tile instanceof BasinTile) {
			return ((BasinTile) tile).onInteract(player, handIn, hit);
		}
		return ActionResultType.PASS;
	}

	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		// Check that we have a valid tile entity
		TileEntity tile = worldIn.getTileEntity(pos);

		if (tile instanceof BasinTile) {

			BasinTile basin = (BasinTile) tile;

			// Spawn particles if basin is bleaching some shi
			BasinStateVisitor<Void> blockAnimator = new BasinStateVisitor<Void>() {

				@Override
				public Void visit(BleachState state) {
					double bubbleChance = state.getBleachLevel();
					if (rand.nextDouble() < bubbleChance) {
						double x = (double) pos.getX() + 2.0 / 16.0 + 12.0 / 16.0 * rand.nextDouble(),
								y = (double) pos.getY() + ((FilledState) state.getSuperState()).getWaterHeight(),
								z = (double) pos.getZ() + 2.0 / 16.0 + 12.0 / 16.0 * rand.nextDouble();
						worldIn.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.0D, 0.0D);
					}
					return null;
				}

			};

			// Visit basin state
			basin.accept(blockAnimator);
		}
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
