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
package forestry.factory.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.SocketWidget;
import forestry.core.gui.widgets.TankWidget;
import forestry.factory.tiles.TileSqueezer;

public class GuiSqueezer extends GuiForestryTitled<ContainerSqueezer> {
	private final TileSqueezer tile;

	public GuiSqueezer(ContainerSqueezer container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/squeezersocket.png", container, inventory, title);
		this.tile = container.getTile();
		widgetManager.add(new TankWidget(this.widgetManager, 122, 18, 0));
		widgetManager.add(new SocketWidget(this.widgetManager, 75, 20, tile, 0));
	}

	@Override
	protected void drawWidgets(GuiGraphics graphics) {
		//TODO: Make this more consistent
		int progress = tile.getProgressScaled(43);
		graphics.blit(this.textureFile, 75, 41, 176, 60, progress, 18);

		super.drawWidgets(graphics);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
		addPowerLedger(tile.getEnergyManager());
		addHintLedger("squeezer");
	}
}
