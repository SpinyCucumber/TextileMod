package spinyq.spinytextiles.blocks;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import spinyq.spinytextiles.client.render.RenderTypeHelper;
import spinyq.spinytextiles.client.render.RenderTypeHelper.BlockRenderMode;
import spinyq.spinytextiles.tiles.SpinningWheelTile;

public class SpinningWheelBlock extends Block {

	public static final VoxelShape SHAPE_NORTH_SOUTH = VoxelShapes.or(Block.makeCuboidShape(3.5, 0, 6, 5.5, 12, 10),
			Block.makeCuboidShape(10.5, 0, 6, 12.5, 12, 10), Block.makeCuboidShape(6.5, 2, 1, 9.5, 16, 15),
			Block.makeCuboidShape(5.5, 8, 7, 10.5, 10, 9)),

			SHAPE_EAST_WEST = VoxelShapes.or(Block.makeCuboidShape(6, 0, 3.5, 10, 12, 5.5),
					Block.makeCuboidShape(6, 0, 10.5, 10, 12, 12.5), Block.makeCuboidShape(1, 2, 6.5, 15, 16, 9.5),
					Block.makeCuboidShape(7, 8, 5.5, 9, 10, 10.5));

	public static final Map<Direction, VoxelShape> SHAPE_MAP = new EnumMap<>(
			ImmutableMap.of(Direction.NORTH, SHAPE_NORTH_SOUTH, Direction.SOUTH, SHAPE_NORTH_SOUTH, Direction.EAST,
					SHAPE_EAST_WEST, Direction.WEST, SHAPE_EAST_WEST));

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final BooleanProperty SPINNING = BooleanProperty.create("spinning");

	public SpinningWheelBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(SPINNING, Boolean.valueOf(false)));
		// Use a cutout render type
		RenderTypeHelper.setRenderMode(this, BlockRenderMode.CUTOUT);
	}

	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE_MAP.get(state.get(FACING));
	}

	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	/**
	 * Spinning wheels must be supported on the ground
	 */
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, SPINNING);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SpinningWheelTile();
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		// Check that the player is indeed interacting with the tile entity
		TileEntity tile = worldIn.getTileEntity(pos);
		// Pass logic over to tile entity
		if (tile instanceof SpinningWheelTile) {
			Optional<ActionResultType> result = ((SpinningWheelTile) tile).onBlockActivated(state, worldIn, pos, player, handIn, hit);
			if (result.isPresent()) return result.get();
		}
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
	}

}
