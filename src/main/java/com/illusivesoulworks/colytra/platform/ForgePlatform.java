/*
 * Copyright (C) 2017-2022 Illusive Soulworks
 *
 * Colytra is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Colytra is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Colytra. If not, see <https://www.gnu.org/licenses/>.
 */


package com.illusivesoulworks.colytra.platform;

import com.illusivesoulworks.colytra.common.ColytraConfig;
import com.illusivesoulworks.colytra.platform.services.IPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ForgePlatform implements IPlatform {

  @Override
  public Item getItem(ResourceLocation resourceLocation) {
    Item item = resourceLocation == null ? Items.AIR : BuiltInRegistries.ITEM.get(resourceLocation);
    return item == Items.AIR ? null : item;
  }

  @Override
  public void extractEnergy(ItemStack stack) {
    IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

    if (energy != null) {
      energy.extractEnergy(ColytraConfig.SERVER.energyCost.get(), false);
    }
  }

  @Override
  public boolean hasEnergy(ItemStack stack) {
    return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
  }

  @Override
  public boolean canExtractEnergy(ItemStack stack) {
    IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
    return energy != null && energy.canExtract() &&
        energy.getEnergyStored() > ColytraConfig.SERVER.energyCost.get();
  }
}
