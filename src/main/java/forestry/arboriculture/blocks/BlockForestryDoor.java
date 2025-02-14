/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;

import forestry.arboriculture.ForestryWoodType;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.IWoodTyped;

public class BlockForestryDoor extends DoorBlock implements IWoodTyped {
	private final ForestryWoodType woodType;

	public BlockForestryDoor(ForestryWoodType woodType) {
		super(Block.Properties.of().strength(woodType.getHardness(), woodType.getHardness() * 1.5F).sound(SoundType.WOOD).noOcclusion(), woodType.getBlockSetType());
		this.woodType = woodType;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.DOOR;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return woodType;
	}
}
