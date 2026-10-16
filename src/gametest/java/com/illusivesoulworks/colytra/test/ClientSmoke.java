package com.illusivesoulworks.colytra.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import com.illusivesoulworks.colytra.common.ElytraTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Files;

// Run with -Dsmoketest=true: creates a flat world, takes a screenshot and quits. Exit code 1 on failure.
public final class ClientSmoke {

    public static final boolean ENABLED = Boolean.getBoolean("smoketest");
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    private static int stage;
    private static int ticks;

    private ClientSmoke() {
    }

    public static boolean start() {
        if (!ENABLED) {
            return false;
        }
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
                fail("timed out");
            } catch (InterruptedException ignored) {
            }
        }, "smoketest-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        return true;
    }

    public static void tick(Minecraft mc) {
        try {
            switch (stage) {
                case 0 -> {
                    Screen screen = mc.screen;
                    if (screen != null && mc.getOverlay() == null && ++ticks >= 20) {
                        if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
                            fail("mod loading errors");
                        }
                        ticks = 0;
                        mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
                        createWorld(mc);
                        stage = 1;
                    }
                }
                case 1 -> {
                    if (mc.level != null && mc.player != null && mc.screen == null) {
                        stage = 2;
                    }
                }
                case 2 -> {
                    ticks++;
                    if (ticks == 40) {
                        setup(mc);
                    }
                    if (ticks == 60) {
                        mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                        mc.options.hideGui = true;
                    }
                    if (ticks == 140) {
                        Screenshot.grab(mc.gameDirectory, "wearing.png", mc.getMainRenderTarget(), msg -> {});
                        mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                        mc.options.hideGui = false;
                    }
                    if (ticks >= 160) {
                        if (!ElytraTag.hasUpgrade(mc.player.getItemBySlot(EquipmentSlot.CHEST))) {
                            fail("client player is not wearing the colytra chestplate");
                        }
                        Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
                        stage = 3;
                        mc.stop();
                    }
                }
                default -> {
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    private static void createWorld(Minecraft mc) {
        LevelSettings settings = new LevelSettings("smoke", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            mc.screen);
    }

    // the player wears a diamond chestplate with an elytra attached, and so does an armor stand next to them
    private static void setup(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayers().get(0);
            var level = player.serverLevel();
            ItemStack chest = new ItemStack(Items.DIAMOND_CHESTPLATE);
            ElytraTag.setElytra(chest, new ItemStack(Items.ELYTRA));
            player.setItemSlot(EquipmentSlot.CHEST, chest.copy());
            var stand = net.minecraft.world.entity.EntityType.ARMOR_STAND.create(level);
            stand.moveTo(player.getX() + 1.5, player.getY(), player.getZ(), player.getYRot() + 180, 0);
            stand.setItemSlot(EquipmentSlot.CHEST, chest.copy());
            level.addFreshEntity(stand);
        });
    }

    public static void fail(String why) {
        System.err.println("Smoke test failed: " + why);
        System.out.println("[clientsmoke] FAILED: " + why);
        System.err.flush();
        Runtime.getRuntime().halt(1);
    }
}
