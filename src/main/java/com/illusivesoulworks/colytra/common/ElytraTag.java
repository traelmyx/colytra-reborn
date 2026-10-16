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


package com.illusivesoulworks.colytra.common;

import com.illusivesoulworks.colytra.ColytraForgeMod;
import com.illusivesoulworks.colytra.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ElytraTag {

  public static boolean hasUpgrade(ItemStack stack) {
    return stack.has(ColytraForgeMod.ELYTRA.get());
  }

  public static ItemStack getElytra(ItemStack stack) {
    return stack.getOrDefault(ColytraForgeMod.ELYTRA.get(), ItemContainerContents.EMPTY).copyOne();
  }

  public static void setElytra(ItemStack chestStack, ItemStack elytraStack) {
    chestStack.set(ColytraForgeMod.ELYTRA.get(), ItemContainerContents.fromItems(List.of(elytraStack.copy())));
  }

  public static void removeElytra(ItemStack chestStack) {
    chestStack.remove(ColytraForgeMod.ELYTRA.get());
  }

  public static void damageElytra(LivingEntity livingEntity, ItemStack chestStack,
                                  ItemStack elytraStack, int amount) {
    ColytraConfig.FusionType fusionType = ColytraConfig.SERVER.fusionType.get();

    if (fusionType == ColytraConfig.FusionType.NORMAL) {

      if (livingEntity.level() instanceof ServerLevel level) {
        elytraStack.hurtAndBreak(amount, level, livingEntity, item -> {});
      }
    } else if (fusionType == ColytraConfig.FusionType.UNISON) {

      if (Services.PLATFORM.hasEnergy(chestStack)) {
        Services.PLATFORM.extractEnergy(chestStack);
      } else {
        chestStack.hurtAndBreak(amount, livingEntity, EquipmentSlot.CHEST);
      }
    }
    setElytra(chestStack, elytraStack);
  }

  public static boolean isUseable(ItemStack chestStack, ItemStack elytraStack) {

    if (elytraStack.isEmpty()) {
      return false;
    }
    ColytraConfig.FusionType fusionType = ColytraConfig.SERVER.fusionType.get();

    if (fusionType == ColytraConfig.FusionType.NORMAL) {
      return elytraStack.getItem() instanceof ElytraItem && ElytraItem.isFlyEnabled(elytraStack);
    } else if (fusionType == ColytraConfig.FusionType.UNISON) {

      if (Services.PLATFORM.hasEnergy(chestStack)) {
        return Services.PLATFORM.canExtractEnergy(chestStack);
      } else {
        return !chestStack.isDamageableItem() || (chestStack.getDamageValue()
            < chestStack.getMaxDamage() - 1);
      }
    }
    return true;
  }
}
