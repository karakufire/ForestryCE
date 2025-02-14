package forestry.modules.features;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import forestry.api.core.IBlockSubtype;
import forestry.core.utils.datastructures.TriFunction;

public class FeatureBlockTable<B extends Block, R extends IBlockSubtype, C extends IBlockSubtype> extends FeatureTable<FeatureBlockTable.Builder<B, R, C>, FeatureBlock<B, BlockItem>, R, C> {
	public FeatureBlockTable(Builder<B, R, C> builder) {
		super(builder);
	}

	@Override
	protected FeatureBlock<B, BlockItem> createFeature(Builder<B, R, C> builder, R rowType, C columnType) {
		return builder.registry.block(() -> builder.constructor.apply(rowType, columnType), (block) -> builder.itemConstructor != null ? builder.itemConstructor.apply(block, rowType, columnType) : null, builder.getIdentifier(rowType, columnType));
	}

	public Collection<B> getBlocks() {
		ArrayList<B> blocks = new ArrayList<>(this.featureByTypes.size());
		for (FeatureBlock<B, BlockItem> feature : featureByTypes.values()) {
			blocks.add(feature.block());
		}
		return blocks;
	}

	public Collection<BlockItem> getItems() {
		ArrayList<BlockItem> list = new ArrayList<>();
		for (FeatureBlock<B, BlockItem> feature : featureByTypes.values()) {
			list.add(feature.item());
		}
		return list;
	}

	public Collection<B> getRowBlocks(R rowType) {
		return getRowFeatures(rowType).stream().map(IBlockFeature::block).collect(Collectors.toList());
	}

	public Collection<B> getColumnBlocks(C columnType) {
		return getColumnFeatures(columnType).stream().map(IBlockFeature::block).collect(Collectors.toList());
	}

	public static class Builder<B extends Block, R extends IBlockSubtype, C extends IBlockSubtype> extends FeatureTable.Builder<R, C, FeatureBlockTable<B, R, C>> {
		private final IFeatureRegistry registry;
		private final BiFunction<R, C, B> constructor;
		@Nullable
		private TriFunction<B, R, C, BlockItem> itemConstructor;

		public Builder(IFeatureRegistry registry, BiFunction<R, C, B> constructor) {
			super(registry);
			this.registry = registry;
			this.constructor = constructor;
		}

		public Builder<B, R, C> itemWithType(TriFunction<B, R, C, BlockItem> itemConstructor) {
			this.itemConstructor = itemConstructor;
			return this;
		}

		public Builder<B, R, C> item(Function<B, BlockItem> itemConstructor) {
			this.itemConstructor = (block, rowType, columnType) -> itemConstructor.apply(block);
			return this;
		}

		public FeatureBlockTable<B, R, C> create() {
			return new FeatureBlockTable<>(this);
		}
	}
}
