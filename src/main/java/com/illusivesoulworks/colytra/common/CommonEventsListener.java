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

import com.illusivesoulworks.caelus.api.CaelusApi;
import com.illusivesoulworks.colytra.ColytraConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.commons.lang3.tuple.Triple;

public class CommonEventsListener {

  private static final AttributeModifier FLIGHT_MODIFIER = new AttributeModifier(
      ResourceLocation.fromNamespaceAndPath(ColytraConstants.MOD_ID, "flight"), 1.0d,
      AttributeModifier.Operation.ADD_VALUE);

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onColytraAnvil(final AnvilUpdateEvent evt) {
    Triple<ItemStack, Integer, Integer> result =
        CommonEvents.repairColytra(evt.getLeft(), evt.getRight(), evt.getName(),
            evt.getPlayer().level().registryAccess());

    if (!result.getLeft().isEmpty()) {
      evt.setOutput(result.getLeft());
      evt.setMaterialCost(result.getMiddle());
      evt.setCost(result.getRight());
    }
  }

  @SubscribeEvent
  public void onPlayerTick(final PlayerTickEvent.Post evt) {
    Player player = evt.getEntity();

    if (player.level().isClientSide()) {
      return;
    }
    AttributeInstance attributeInstance =
        player.getAttribute(CaelusApi.getInstance().getFallFlyingAttribute());

    if (attributeInstance != null) {
      attributeInstance.removeModifier(FLIGHT_MODIFIER);

      if (CommonEvents.updateColytra(player, true)) {
        attributeInstance.addTransientModifier(FLIGHT_MODIFIER);
      }
    }
  }

  @SubscribeEvent
  public void onPlayerXPPickUp(final PlayerXpEvent.PickupXp evt) {
    ExperienceOrb orb = evt.getOrb();

    if (CommonEvents.repairColytraWithXp(orb, evt.getEntity(), (val) -> orb.value = val)) {
      evt.setCanceled(true);
    }
  }
}
