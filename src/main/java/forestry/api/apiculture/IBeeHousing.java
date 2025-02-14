/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.apiculture;

import javax.annotation.Nullable;

import net.minecraft.world.phys.Vec3;

import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.climate.IBiomeProvider;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.ILocationProvider;

public interface IBeeHousing extends IErrorLogicSource, IClimateProvider, IBiomeProvider, ILocationProvider {

	/**
	 * Used by {@link IBeeSpeciesType#createBeeHousingModifier(IBeeHousing)}
	 * to combine bee modifiers from several sources that can change over time.
	 *
	 * @return IBeeModifiers from the housing, frames, etc.
	 */
	Iterable<IBeeModifier> getBeeModifiers();

	/**
	 * Used by {@link IBeeSpeciesType#createBeeHousingListener(IBeeHousing)}
	 * to combine bee listeners from several sources that can change over time.
	 *
	 * @return IBeeListeners from the housing, multiblock parts, etc.
	 */
	Iterable<IBeeListener> getBeeListeners();

	IBeeHousingInventory getBeeInventory();

	IBeekeepingLogic getBeekeepingLogic();

	int getBlockLightValue();

	boolean canBlockSeeTheSky();

	boolean isRaining();

	@Nullable
	GameProfile getOwner();

	/**
	 * @return exact coordinates where bee particle FX should spawn from
	 * @since Forestry 4.2
	 */
	Vec3 getBeeFXCoordinates();
}
