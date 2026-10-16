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


package com.illusivesoulworks.colytra;

import com.illusivesoulworks.colytra.common.ColytraConfig;
import com.illusivesoulworks.colytra.common.CommonEventsListener;
import com.illusivesoulworks.colytra.common.crafting.ElytraAttachmentRecipe;
import com.illusivesoulworks.colytra.common.crafting.ElytraDetachmentRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ColytraConstants.MOD_ID)
public class ColytraForgeMod {

  private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, ColytraConstants.MOD_ID);
  private static final DeferredRegister.DataComponents COMPONENTS =
      DeferredRegister.createDataComponents(ColytraConstants.MOD_ID);

  // the elytra attached to a chestplate
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> ELYTRA =
      COMPONENTS.registerComponentType("elytra", builder -> builder
          .persistent(ItemStack.OPTIONAL_CODEC)
          .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC));

  static {
    RECIPE_SERIALIZERS.register(ColytraConstants.ATTACH_ELYTRA,
        () -> ElytraAttachmentRecipe.CRAFTING_ATTACH_ELYTRA);
    RECIPE_SERIALIZERS.register(ColytraConstants.DETACH_ELYTRA,
        () -> ElytraDetachmentRecipe.CRAFTING_DETACH_ELYTRA);
  }

  public ColytraForgeMod(IEventBus eventBus, ModContainer container, Dist dist) {
    container.registerConfig(ModConfig.Type.SERVER, ColytraConfig.SERVER_SPEC);
    eventBus.addListener((ModConfigEvent evt) -> {

      if (evt.getConfig().getSpec() == ColytraConfig.SERVER_SPEC) {
        ColytraConfig.reload();
      }
    });
    RECIPE_SERIALIZERS.register(eventBus);
    COMPONENTS.register(eventBus);
    NeoForge.EVENT_BUS.register(new CommonEventsListener());

    if (dist.isClient()) {
      ColytraForgeClientMod.setup(eventBus);
    }
  }
}
