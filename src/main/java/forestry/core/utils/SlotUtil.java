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
package forestry.core.utils;

import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import forestry.core.gui.slots.WorktableSlot;
import forestry.core.gui.slots.SlotForestry;

public abstract class SlotUtil {
	public static boolean isSlotInRange(int slotIndex, int start, int count) {
		return slotIndex >= start && slotIndex < start + count;
	}

	public static ItemStack slotClickPhantom(SlotForestry slot, int mouseButton, ClickType clickTypeIn, Player player) {
		ItemStack stack = ItemStack.EMPTY;

		ItemStack stackSlot = slot.getItem();
		if (!stackSlot.isEmpty()) {
			stack = stackSlot.copy();
		}

		if (mouseButton == 2) {
			fillPhantomSlot(slot, ItemStack.EMPTY, mouseButton);
		} else if (mouseButton == 0 || mouseButton == 1) {
			ItemStack stackHeld = player.containerMenu.getCarried();

			if (stackSlot.isEmpty()) {
				if (!stackHeld.isEmpty() && slot.mayPlace(stackHeld)) {
					fillPhantomSlot(slot, stackHeld, mouseButton);
				}
			} else if (stackHeld.isEmpty()) {
				adjustPhantomSlot(slot, mouseButton, clickTypeIn);
			} else if (slot.mayPlace(stackHeld)) {
				if (ItemStackUtil.isIdenticalItem(stackSlot, stackHeld)) {
					adjustPhantomSlot(slot, mouseButton, clickTypeIn);
				} else {
					fillPhantomSlot(slot, stackHeld, mouseButton);
				}
			}
		} else if (mouseButton == 5) {
			ItemStack stackHeld = player.containerMenu.getCarried();
			if (!slot.hasItem()) {
				fillPhantomSlot(slot, stackHeld, mouseButton);
			}
		}
		return stack;
	}

	public static ItemStack transferStackInSlot(List<Slot> inventorySlots, Player player, int slotIndex) {
		Slot slot = inventorySlots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			// nothing left to transfer
			return ItemStack.EMPTY;
		}

		boolean fromCraftingSlot = slot instanceof ResultSlot || slot instanceof WorktableSlot;

		int numSlots = inventorySlots.size();
		ItemStack stackInSlot = slot.getItem();
		ItemStack originalStack = stackInSlot.copy();

		if (!shiftItemStack(inventorySlots, stackInSlot, slotIndex, numSlots, fromCraftingSlot)) {
			return ItemStack.EMPTY;
		}

		slot.onQuickCraft(stackInSlot, originalStack);
		if (stackInSlot.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (stackInSlot.getCount() == originalStack.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, stackInSlot);
		return originalStack;
	}

	private static boolean shiftItemStack(List<Slot> inventorySlots, ItemStack stackInSlot, int slotIndex, int numSlots, boolean fromCraftingSlot) {
		if (isInPlayerInventory(slotIndex)) {
			if (shiftToMachineInventory(inventorySlots, stackInSlot, numSlots)) {
				return true;
			}

			if (isInPlayerHotbar(slotIndex)) {
				return shiftToPlayerInventoryNoHotbar(inventorySlots, stackInSlot);
			} else {
				return shiftToHotbar(inventorySlots, stackInSlot);
			}
		} else {
			if (fromCraftingSlot) {
				if (shiftToMachineInventory(inventorySlots, stackInSlot, numSlots)) {
					return true;
				}
			}
			return shiftToPlayerInventory(inventorySlots, stackInSlot);
		}
	}

	private static void adjustPhantomSlot(SlotForestry slot, int mouseButton, ClickType clickTypeIn) {
		if (!slot.canAdjustPhantom()) {
			return;
		}
		ItemStack stackSlot = slot.getItem();
		int stackSize;
		if (clickTypeIn == ClickType.QUICK_MOVE) {
			stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
		} else {
			stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
		}

		if (stackSize > slot.getMaxStackSize()) {
			stackSize = slot.getMaxStackSize();
		}

		stackSlot.setCount(stackSize);

		slot.set(stackSlot);
	}

	private static void fillPhantomSlot(SlotForestry slot, ItemStack stackHeld, int mouseButton) {
		if (!slot.canAdjustPhantom()) {
			return;
		}

		if (stackHeld.isEmpty()) {
			slot.set(ItemStack.EMPTY);
			return;
		}

		int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
		if (stackSize > slot.getMaxStackSize()) {
			stackSize = slot.getMaxStackSize();
		}
		ItemStack phantomStack = stackHeld.copy();
		phantomStack.setCount(stackSize);

		slot.set(phantomStack);
	}

	private static boolean shiftItemStackToRange(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
		boolean changed = shiftItemStackToRangeMerge(inventorySlots, stackToShift, start, count);
		changed |= shiftItemStackToRangeOpenSlots(inventorySlots, stackToShift, start, count);
		return changed;
	}

	private static boolean shiftItemStackToRangeMerge(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
		if (!stackToShift.isStackable() || stackToShift.isEmpty()) {
			return false;
		}

		boolean changed = false;
		for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) {
			Slot slot = inventorySlots.get(slotIndex);
			ItemStack stackInSlot = slot.getItem();
			if (!stackInSlot.isEmpty() && ItemStackUtil.isIdenticalItem(stackInSlot, stackToShift)) {
				int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
				int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize());
				if (resultingStackSize <= max) {
					stackToShift.setCount(0);
					stackInSlot.setCount(resultingStackSize);
					slot.setChanged();
					changed = true;
				} else if (stackInSlot.getCount() < max) {
					stackToShift.shrink(max - stackInSlot.getCount());
					stackInSlot.setCount(max);
					slot.setChanged();
					changed = true;
				}
			}
		}
		return changed;
	}

	private static boolean shiftItemStackToRangeOpenSlots(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
		if (stackToShift.isEmpty()) {
			return false;
		}

		boolean changed = false;
		for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) {
			Slot slot = inventorySlots.get(slotIndex);
			ItemStack stackInSlot = slot.getItem();
			if (stackInSlot.isEmpty()) {
				int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize());
				stackInSlot = stackToShift.copy();
				stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
				stackToShift.shrink(stackInSlot.getCount());
				slot.set(stackInSlot);
				slot.setChanged();
				changed = true;
			}
		}
		return changed;
	}

	private static final int PLAYER_INVENTORY_SIZE = 36;
	private static final int PLAYER_HOTBAR_SIZE = 9;

	private static boolean isInPlayerInventory(int slotIndex) {
		return slotIndex < PLAYER_INVENTORY_SIZE;
	}

	private static boolean isInPlayerHotbar(int slotIndex) {
		return SlotUtil.isSlotInRange(slotIndex, PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE, PLAYER_INVENTORY_SIZE);
	}

	private static boolean shiftToPlayerInventory(List<Slot> inventorySlots, ItemStack stackInSlot) {
		int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;

		// try to merge with existing stacks, hotbar first
		boolean shifted = shiftItemStackToRangeMerge(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
		shifted |= shiftItemStackToRangeMerge(inventorySlots, stackInSlot, 0, playerHotbarStart);

		// shift to open slots, hotbar first
		shifted |= shiftItemStackToRangeOpenSlots(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
		shifted |= shiftItemStackToRangeOpenSlots(inventorySlots, stackInSlot, 0, playerHotbarStart);
		return shifted;
	}

	private static boolean shiftToPlayerInventoryNoHotbar(List<Slot> inventorySlots, ItemStack stackInSlot) {
		int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;
		return shiftItemStackToRange(inventorySlots, stackInSlot, 0, playerHotbarStart);
	}

	private static boolean shiftToHotbar(List<Slot> inventorySlots, ItemStack stackInSlot) {
		int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;
		return shiftItemStackToRange(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
	}

	private static boolean shiftToMachineInventory(List<Slot> inventorySlots, ItemStack stackToShift, int numSlots) {
		boolean success = false;
		if (stackToShift.isStackable()) {
			success = shiftToMachineInventory(inventorySlots, stackToShift, numSlots, true);
		}
		if (!stackToShift.isEmpty()) {
			success |= shiftToMachineInventory(inventorySlots, stackToShift, numSlots, false);
		}
		return success;
	}

	// if mergeOnly = true, don't shift into empty slots.
	private static boolean shiftToMachineInventory(List<Slot> inventorySlots, ItemStack stackToShift, int numSlots, boolean mergeOnly) {
		for (int machineIndex = PLAYER_INVENTORY_SIZE; machineIndex < numSlots; machineIndex++) {
			Slot slot = inventorySlots.get(machineIndex);
			if (mergeOnly && slot.getItem().isEmpty()) {
				continue;
			}
			if (slot instanceof SlotForestry slotForestry) {
				if (!slotForestry.canShift() || slotForestry.isPhantom()) {
					continue;
				}
			}
			if (!slot.mayPlace(stackToShift)) {
				continue;
			}
			if (shiftItemStackToRange(inventorySlots, stackToShift, machineIndex, 1)) {
				return true;
			}
		}
		return false;
	}
}
