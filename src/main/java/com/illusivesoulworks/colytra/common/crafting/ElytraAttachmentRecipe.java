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

package com.illusivesoulworks.colytra.common.crafting;

import com.illusivesoulworks.colytra.common.ColytraConfig;
import com.illusivesoulworks.colytra.common.ElytraTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class ElytraAttachmentRecipe extends CustomRecipe {

  public static final List<Item> VALID_ITEMS = new ArrayList<>();
  public static final RecipeSerializer<ElytraAttachmentRecipe> CRAFTING_ATTACH_ELYTRA =
      new SimpleCraftingRecipeSerializer<>(ElytraAttachmentRecipe::new);

  public ElytraAttachmentRecipe(CraftingBookCategory category) {
    super(category);
  }

  private static void mergeEnchantments(ItemStack source, ItemStack destination) {
    ItemEnchantments sourceEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(source);
    ItemEnchantments.Mutable merged =
        new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(destination));

    for (Object2IntMap.Entry<Holder<Enchantment>> entry : sourceEnchantments.entrySet()) {
      Holder<Enchantment> enchantment = entry.getKey();

      if (!enchantment.value().canEnchant(destination)) {
        continue;
      }
      int destLevel = merged.getLevel(enchantment);
      int srcLevel = entry.getIntValue();
      srcLevel = destLevel == srcLevel ? srcLevel + 1 : Math.max(srcLevel, destLevel);

      for (Holder<Enchantment> destEnch : merged.keySet()) {

        if (!destEnch.equals(enchantment) && !Enchantment.areCompatible(destEnch, enchantment)) {
          return;
        }
      }
      merged.set(enchantment, Math.min(srcLevel, enchantment.value().getMaxLevel()));
    }
    EnchantmentHelper.setEnchantments(destination, merged.toImmutable());
    EnchantmentHelper.setEnchantments(source, ItemEnchantments.EMPTY);
  }

  @Override
  public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn) {
    ItemStack itemstack = ItemStack.EMPTY;
    ItemStack elytra = ItemStack.EMPTY;

    for (int i = 0; i < inv.size(); ++i) {
      ItemStack currentStack = inv.getItem(i);

      if (currentStack.isEmpty()) {
        continue;
      }

      if (isValid(currentStack)) {

        if (!itemstack.isEmpty() || ElytraTag.hasUpgrade(currentStack)) {
          return false;
        }
        itemstack = currentStack;
      } else {

        if (!elytra.isEmpty() || !(currentStack.getItem() instanceof ElytraItem)) {
          return false;
        }
        elytra = currentStack;
      }
    }
    return !itemstack.isEmpty() && !elytra.isEmpty();
  }

  @Nonnull
  @Override
  public ItemStack assemble(@Nonnull CraftingInput inv,
                            @Nonnull HolderLookup.Provider registryAccess) {
    ItemStack itemstack = ItemStack.EMPTY;
    ItemStack elytra = ItemStack.EMPTY;

    for (int k = 0; k < inv.size(); ++k) {
      ItemStack currentStack = inv.getItem(k);

      if (currentStack.isEmpty()) {
        continue;
      }

      if (isValid(currentStack)) {

        if (!itemstack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        itemstack = currentStack.copy();
        itemstack.setCount(1);
      } else {

        if (!(currentStack.getItem() instanceof ElytraItem)) {
          return ItemStack.EMPTY;
        }
        elytra = currentStack.copy();
      }
    }

    if (!itemstack.isEmpty() && !elytra.isEmpty()) {

      if (ColytraConfig.SERVER.fusionType.get() != ColytraConfig.FusionType.NORMAL) {
        mergeEnchantments(elytra, itemstack);
        itemstack.set(DataComponents.REPAIR_COST, elytra.getOrDefault(DataComponents.REPAIR_COST, 0) +
            itemstack.getOrDefault(DataComponents.REPAIR_COST, 0));
        Component name = elytra.get(DataComponents.CUSTOM_NAME);
        elytra = new ItemStack(Items.ELYTRA);

        if (name != null) {
          elytra.set(DataComponents.CUSTOM_NAME, name);
        }
      }
      ElytraTag.setElytra(itemstack, elytra);
      return itemstack;
    } else {
      return ItemStack.EMPTY;
    }
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Nonnull
  @Override
  public RecipeSerializer<?> getSerializer() {
    return CRAFTING_ATTACH_ELYTRA;
  }

  private static boolean isValid(ItemStack stack) {
    ColytraConfig.ListType listType = ColytraConfig.SERVER.itemListType.get();
    boolean isBlacklist = listType == ColytraConfig.ListType.DENY;
    return isBlacklist != VALID_ITEMS.contains(stack.getItem()) &&
        isChestItem(stack) &&
        !(stack.getItem() instanceof ElytraItem);
  }

  private static boolean isChestItem(ItemStack stack) {
    EquipmentSlot slot = stack.getEquipmentSlot();

    if (slot == null) {
      Equipable equipable = Equipable.get(stack);
      slot = equipable != null ? equipable.getEquipmentSlot() : null;
    }
    return slot == EquipmentSlot.CHEST;
  }
}
