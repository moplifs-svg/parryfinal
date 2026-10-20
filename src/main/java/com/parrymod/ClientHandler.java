package com.parrymod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client-only: key binding (default F, rebindable in Options > Controls). */
public class ClientHandler {
    public static final KeyMapping PARRY_KEY = new KeyMapping(
            "key.parry.parry",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.parry");

    private static int cooldownTicks = 0;
    private static final ItemStack SWORD_ICON = new ItemStack(Items.IRON_SWORD);

    public static void setCooldown(int ticks) {
        cooldownTicks = ticks;
    }

    @Mod.EventBusSubscriber(modid = ParryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(PARRY_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = ParryMod.MODID, value = Dist.CLIENT)
    public static class ForgeEvents {
        /** Sword icon + seconds left (10, 9, 8 ...) under the crosshair. */
        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            if (cooldownTicks <= 0) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            GuiGraphics g = event.getGuiGraphics();
            int cx = event.getWindow().getGuiScaledWidth() / 2;
            int y = event.getWindow().getGuiScaledHeight() / 2 + 14;
            String seconds = String.valueOf((cooldownTicks + 19) / 20);

            int textWidth = mc.font.width(seconds);
            int total = 16 + 3 + textWidth;
            int x = cx - total / 2;
            g.renderItem(SWORD_ICON, x, y);
            g.drawString(mc.font, seconds, x + 19, y + 4, 0xFF5555, true);
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            // START phase runs before vanilla handles keybinds, so we can steal the F press
            if (event.phase != TickEvent.Phase.START) return;
            if (cooldownTicks > 0) cooldownTicks--;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            while (PARRY_KEY.consumeClick()) {
                if (mc.screen != null) continue;
                Network.CHANNEL.sendToServer(new ParryPacket());
                // Vanilla "swap hands" is also F by default: don't let it fire at the same time.
                if (PARRY_KEY.getKey().equals(mc.options.keySwapOffhand.getKey())) {
                    while (mc.options.keySwapOffhand.consumeClick()) { }
                }
            }
        }
    }
}
