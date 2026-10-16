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
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class ElytraDetachmentRecipe extends CustomRecipe {

  public static final RecipeSerializer<ElytraDetachmentRecipe> CRAFTING_DETACH_ELYTRA =
      new SimpleCraftingRecipeSerializer<>(ElytraDetachmentRecipe::new);

  public ElytraDetachmentRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn) {

    if (ColytraConfig.SERVER.fusionType.get() != ColytraConfig.FusionType.NORMAL) {
      return false;
    }
    ItemStack itemstack = ItemStack.EMPTY;

    for (int i = 0; i < inv.size(); ++i) {
      ItemStack currentStack = inv.getItem(i);

      if (currentStack.isEmpty()) {
        continue;
      }

      if (!itemstack.isEmpty() || !ElytraTag.hasUpgrade(currentStack)) {
        return false;
      }
      itemstack = currentStack;
    }
    return !itemstack.isEmpty();
  }

  @Nonnull
  @Override
  public ItemStack assemble(@Nonnull CraftingInput inv,
                            @Nonnull HolderLookup.Provider registryAccess) {
    ItemStack itemstack = ItemStack.EMPTY;

    for (int k = 0; k < inv.size(); ++k) {
      ItemStack currentStack = inv.getItem(k);

      if (!currentStack.isEmpty()) {

        if (!itemstack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        itemstack = ElytraTag.getElytra(currentStack);
      }
    }

    if (!itemstack.isEmpty()) {
      return itemstack;
    } else {
      return ItemStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
    NonNullList<ItemStack> nonnulllist = NonNullList
        .withSize(inv.size(), ItemStack.EMPTY);

    for (int i = 0; i < nonnulllist.size(); ++i) {
      ItemStack currentStack = inv.getItem(i);

      if (!currentStack.isEmpty() && ElytraTag.hasUpgrade(currentStack)) {
        ItemStack chest = currentStack.copy();
        ElytraTag.removeElytra(chest);
        nonnulllist.set(i, chest);
        break;
      }
    }
    return nonnulllist;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Nonnull
  @Override
  public RecipeSerializer<?> getSerializer() {
    return CRAFTING_DETACH_ELYTRA;
  }
}
