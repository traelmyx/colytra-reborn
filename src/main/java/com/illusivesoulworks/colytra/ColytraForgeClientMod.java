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

import com.illusivesoulworks.caelus.api.RenderCapeEvent;
import com.illusivesoulworks.colytra.client.ClientEvents;
import com.illusivesoulworks.colytra.client.ColytraLayer;
import com.illusivesoulworks.colytra.common.ElytraTag;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ColytraForgeClientMod {

  public static void setup(IEventBus eventBus) {
    eventBus.addListener(ColytraForgeClientMod::onAddLayers);
    NeoForge.EVENT_BUS.addListener(ColytraForgeClientMod::onItemTooltip);
    NeoForge.EVENT_BUS.addListener(ColytraForgeClientMod::onRenderCape);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void onAddLayers(final EntityRenderersEvent.AddLayers evt) {

    for (PlayerSkin.Model skin : evt.getSkins()) {
      LivingEntityRenderer renderer = evt.getSkin(skin);

      if (renderer != null) {
        renderer.addLayer(new ColytraLayer<>(renderer, evt.getEntityModels()));
      }
    }
    if (evt.getRenderer(EntityType.ARMOR_STAND) instanceof LivingEntityRenderer renderer) {
      renderer.addLayer(new ColytraLayer<>(renderer, evt.getEntityModels()));
    }
  }

  private static void onItemTooltip(final ItemTooltipEvent evt) {
    ClientEvents.addColytraTooltip(evt.getItemStack(), evt.getToolTip());
  }

  private static void onRenderCape(final RenderCapeEvent evt) {

    if (!ElytraTag.getElytra(evt.getEntity().getItemBySlot(EquipmentSlot.CHEST)).isEmpty()) {
      evt.setCanceled(true);
    }
  }
}
