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

package com.illusivesoulworks.colytra.client;

import com.illusivesoulworks.colytra.common.ColytraConfig;
import com.illusivesoulworks.colytra.common.ElytraTag;
import com.illusivesoulworks.colytra.platform.Services;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class ClientEvents {

  public static void addColytraTooltip(ItemStack chestStack, List<Component> tooltip) {

    if (!ElytraTag.hasUpgrade(chestStack)) {
      return;
    }
    ItemStack elytraStack = ElytraTag.getElytra(chestStack);

    if (elytraStack.isEmpty()) {
      return;
    }
    tooltip.add(Component.literal(""));

    if (elytraStack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
      Component display = elytraStack.getHoverName();

      if (display instanceof MutableComponent) {
        ((MutableComponent) display).withStyle(ChatFormatting.AQUA)
            .withStyle(ChatFormatting.ITALIC);
      }
      tooltip.add(display);
    } else {
      tooltip.add(Component.translatable("item.minecraft.elytra").withStyle(ChatFormatting.AQUA));
    }

    if (ColytraConfig.SERVER.fusionType.get() == ColytraConfig.FusionType.NORMAL) {

      net.minecraft.world.item.enchantment.ItemEnchantments enchantments =
          elytraStack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,
              net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

      for (var entry : enchantments.entrySet()) {
        tooltip.add(Component.literal(" ")
            .append(Enchantment.getFullname(entry.getKey(), entry.getIntValue())));
      }
      if (ElytraTag.isUseable(chestStack, elytraStack)) {
        tooltip.add(Component.literal(" ").append((Component.translatable("item.durability",
            elytraStack.getMaxDamage() - elytraStack.getDamageValue(),
            elytraStack.getMaxDamage()))));
      } else {
        tooltip.add(Component.literal(" ").append(
            Component.translatable("tooltip.colytra.broken").withStyle(ChatFormatting.RED)));
      }
    }
  }
}
