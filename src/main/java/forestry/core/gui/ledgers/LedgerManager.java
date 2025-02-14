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
package forestry.core.gui.ledgers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.core.IError;
import forestry.api.core.IErrorSource;
import forestry.api.core.tooltips.ToolTip;
import forestry.core.config.SessionVars;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.GuiUtil;

@OnlyIn(Dist.CLIENT)
public class LedgerManager {
	private final List<Ledger> ledgers = new ArrayList<>();
	private final List<ErrorLedger> errorLedgers = new ArrayList<>();

	private IErrorSource errorSource;
	private int maxWidth;

	public final GuiForestry<?> gui;

	public LedgerManager(GuiForestry<?> gui) {
		this.gui = gui;
		this.errorSource = IErrorSource.EMPTY;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void add(IErrorSource errorSource) {
		this.errorSource = errorSource;
		int maxErrorLedgerCount = (gui.getSizeY() - 10) / Ledger.minHeight;
		for (int i = 0; i < maxErrorLedgerCount; i++) {
			errorLedgers.add(new ErrorLedger(this));
		}
	}

	public void clear() {
		this.ledgers.clear();
	}

	public void add(Ledger ledger) {
		this.ledgers.add(ledger);
		if (SessionVars.getOpenedLedger() != null && ledger.getClass().equals(SessionVars.getOpenedLedger())) {
			ledger.setFullyOpen();
		}
	}

	public void onClose() {
		for (Ledger ledger : ledgers) {
			ledger.onGuiClosed();
		}
	}

	/**
	 * Inserts a ledger into the next-to-last position.
	 */
	public void insert(Ledger ledger) {
		this.ledgers.add(ledgers.size() - 1, ledger);
	}

	@Nullable
	private Ledger getAtPosition(double mX, double mY) {
		if (!ledgers.isEmpty()) {
			final int xShift = gui.getGuiLeft() + gui.getSizeX();
			int yShift = gui.getGuiTop() + 8;

			for (Ledger ledger : ledgers) {
				if (!ledger.isVisible()) {
					continue;
				}

				ledger.currentShiftX = xShift;
				ledger.currentShiftY = yShift;
				if (ledger.intersects(mX, mY)) {
					return ledger;
				}

				yShift += ledger.getHeight();
			}
		}

		final int xShiftError = gui.getGuiLeft();
		int yShiftError = gui.getGuiTop() + 8;

		for (ErrorLedger errorLedger : errorLedgers) {
			if (!errorLedger.isVisible()) {
				continue;
			}

			errorLedger.currentShiftX = xShiftError - errorLedger.getWidth();
			errorLedger.currentShiftY = yShiftError;
			if (errorLedger.intersects(mX, mY)) {
				return errorLedger;
			}

			yShiftError += errorLedger.getHeight();
		}

		return null;
	}

	public List<Rect2i> getLedgerAreas() {
		List<Rect2i> areas = new ArrayList<>();
		for (Ledger ledger : ledgers) {
			if (ledger.isVisible()) {
				Rect2i area = ledger.getArea();
				areas.add(area);
			}
		}
		return areas;
	}

	public void drawLedgers(GuiGraphics transform) {
		int yPos = 8;
		for (Ledger ledger : ledgers) {

			ledger.update();
			if (!ledger.isVisible()) {
				continue;
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			ledger.setPosition(gui.getSizeX(), yPos);
			ledger.draw(transform);
			yPos += ledger.getHeight();
		}

		List<IError> errorStates = new ArrayList<>(errorSource.getErrors());

		yPos = 8;
		int index = 0;
		for (ErrorLedger errorLedger : errorLedgers) {
			if (index >= errorStates.size()) {
				errorLedger.setState(null);
				continue;
			}
			IError errorState = errorStates.get(index++);
			errorLedger.setState(errorState);

			errorLedger.update();
			if (!errorLedger.isVisible()) {
				continue;
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			errorLedger.draw(transform, yPos, -errorLedger.getWidth());
			yPos += errorLedger.getHeight();
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	public void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		Ledger ledger = getAtPosition(mouseX, mouseY);
		if (ledger != null) {
			ToolTip toolTip = new ToolTip();
			toolTip.add(ledger.getTooltip());
			GuiUtil.drawToolTips(graphics, gui, null, toolTip, mouseX, mouseY);
		}
	}

	public void handleMouseClicked(double x, double y, int mouseButton) {

		if (mouseButton == 0) {

			Ledger ledger = this.getAtPosition(x, y);

			// Default action only if the mouse click was not handled by the
			// ledger itself.
			if (ledger != null && !ledger.handleMouseClicked(x, y, mouseButton)) {

				List<? extends Ledger> toggleLedgers;
				if (ledgers.contains(ledger)) {
					toggleLedgers = ledgers;
				} else {
					toggleLedgers = errorLedgers;
				}

				for (Ledger other : toggleLedgers) {
					if (other != ledger && other.isOpen()) {
						other.toggleOpen();
					}
				}
				ledger.toggleOpen();
			}
		}

	}

	public boolean hasOpenedLedger() {
		for (Ledger ledger : ledgers) {
			if (ledger.isOpen()) {
				return true;
			}
		}
		return false;
	}

	public int getMaxWidth() {
		return maxWidth;
	}
}
