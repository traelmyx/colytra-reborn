package com.illusivesoulworks.colytra.test;

import com.illusivesoulworks.caelus.api.CaelusApi;
import com.illusivesoulworks.colytra.common.CommonEvents;
import com.illusivesoulworks.colytra.common.CommonEventsListener;
import com.illusivesoulworks.colytra.common.ElytraTag;
import com.illusivesoulworks.colytra.common.crafting.ElytraAttachmentRecipe;
import com.illusivesoulworks.colytra.common.crafting.ElytraDetachmentRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ColytraTests.MOD_ID)
@PrefixGameTestTemplate(false)
public class ColytraTests {
    public static final String MOD_ID = "colytra_gametest";

    private static ItemStack colytra(GameTestHelper helper, ItemStack elytra) {
        CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND_CHESTPLATE), elytra));
        return new ElytraAttachmentRecipe(CraftingBookCategory.EQUIPMENT).assemble(input, helper.getLevel().registryAccess());
    }

    @GameTest(template = "empty")
    public static void attachAndDetach(GameTestHelper helper) {
        ElytraAttachmentRecipe attach = new ElytraAttachmentRecipe(CraftingBookCategory.EQUIPMENT);
        CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.ELYTRA)));
        helper.assertTrue(attach.matches(input, helper.getLevel()), "chestplate + elytra did not match");
        ItemStack chest = attach.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(chest.is(Items.DIAMOND_CHESTPLATE) && ElytraTag.hasUpgrade(chest), "result has no elytra attached: " + chest);
        CraftingInput twoElytras = CraftingInput.of(2, 1, List.of(new ItemStack(Items.ELYTRA), new ItemStack(Items.ELYTRA)));
        helper.assertFalse(attach.matches(twoElytras, helper.getLevel()), "two elytras matched");
        CraftingInput again = CraftingInput.of(2, 1, List.of(chest, new ItemStack(Items.ELYTRA)));
        helper.assertFalse(attach.matches(again, helper.getLevel()), "a second elytra could be attached");

        ElytraDetachmentRecipe detach = new ElytraDetachmentRecipe(CraftingBookCategory.EQUIPMENT);
        CraftingInput single = CraftingInput.of(1, 1, List.of(chest));
        helper.assertTrue(detach.matches(single, helper.getLevel()), "colytra chestplate did not match detach");
        ItemStack elytra = detach.assemble(single, helper.getLevel().registryAccess());
        helper.assertTrue(elytra.is(Items.ELYTRA), "detach did not give back the elytra");
        NonNullList<ItemStack> left = detach.getRemainingItems(single);
        helper.assertTrue(left.get(0).is(Items.DIAMOND_CHESTPLATE) && !ElytraTag.hasUpgrade(left.get(0)), "chestplate not returned clean: " + left);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wearingItLetsYouGlide(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CommonEventsListener listener = new CommonEventsListener();
        listener.onPlayerTick(new PlayerTickEvent.Post(player));
        helper.assertFalse(CaelusApi.getInstance().canFallFly(player, false), "player can glide with nothing on");
        player.setItemSlot(EquipmentSlot.CHEST, colytra(helper, new ItemStack(Items.ELYTRA)));
        listener.onPlayerTick(new PlayerTickEvent.Post(player));
        helper.assertTrue(CaelusApi.getInstance().canFallFly(player, false), "colytra chestplate does not allow gliding");
        ItemStack broken = new ItemStack(Items.ELYTRA);
        broken.setDamageValue(broken.getMaxDamage() - 1);
        player.setItemSlot(EquipmentSlot.CHEST, colytra(helper, broken));
        listener.onPlayerTick(new PlayerTickEvent.Post(player));
        helper.assertFalse(CaelusApi.getInstance().canFallFly(player, false), "a broken attached elytra still allows gliding");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void membraneRepairsAttachedElytra(GameTestHelper helper) {
        ItemStack damaged = new ItemStack(Items.ELYTRA);
        damaged.setDamageValue(300);
        ItemStack chest = colytra(helper, damaged);
        var result = CommonEvents.repairColytra(chest, new ItemStack(Items.PHANTOM_MEMBRANE, 3), "", helper.getLevel().registryAccess());
        helper.assertFalse(result.getLeft().isEmpty(), "anvil gave no result");
        int damage = ElytraTag.getElytra(result.getLeft()).getDamageValue();
        helper.assertTrue(damage < 300, "elytra not repaired: " + damage);
        helper.assertTrue(result.getMiddle() == 3, "expected 3 membranes used, got " + result.getMiddle());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivesSaveAndLoad(GameTestHelper helper) {
        ItemStack elytra = new ItemStack(Items.ELYTRA);
        elytra.setDamageValue(42);
        ItemStack chest = colytra(helper, elytra);
        var registries = helper.getLevel().registryAccess();
        Tag tag = chest.save(registries);
        ItemStack loaded = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) tag);
        helper.assertTrue(ElytraTag.hasUpgrade(loaded), "elytra lost on save/load");
        helper.assertTrue(ElytraTag.getElytra(loaded).getDamageValue() == 42, "elytra damage lost on save/load");
        helper.succeed();
    }
}
