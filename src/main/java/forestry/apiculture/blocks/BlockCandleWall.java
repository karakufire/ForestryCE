package forestry.apiculture.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public class BlockCandleWall extends BlockCandle {
	public static final DirectionProperty FACING = HorizontalBlock.FACING;

	public BlockCandleWall() {
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(STATE, State.OFF));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return WallTorchBlock.getShape(state);
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader reader, BlockPos pos) {
		return Blocks.WALL_TORCH.canSurvive(state, reader, pos);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState blockState, IWorld world, BlockPos pos, BlockPos blockPos) {
		return Blocks.WALL_TORCH.updateShape(state, direction, blockState, world, pos, blockPos);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = Blocks.WALL_TORCH.getStateForPlacement(context);
		return state == null ? null : this.defaultBlockState().setValue(FACING, state.getValue(FACING));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return Blocks.WALL_TORCH.rotate(state, rotation);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return Blocks.WALL_TORCH.mirror(state, mirror);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}
}
