package com.parrymod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side parry logic. */
@Mod.EventBusSubscriber(modid = ParryMod.MODID)
public class ParryHandler {
    /** Cooldown when nobody hits you during the parry window: 200 ticks = 10 seconds (fixed, not in config). */
    private static final int MISS_COOLDOWN_TICKS = 200;
    private static final Map<UUID, Long> PARRY_END = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_END = new HashMap<>();

    /** Called when a player presses the parry key. */
    public static void startParry(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return;
        long now = player.level().getGameTime();
        UUID id = player.getUUID();
        if (now < COOLDOWN_END.getOrDefault(id, 0L)) return;

        PARRY_END.put(id, now + ParryConfig.WINDOW_TICKS.get());
        COOLDOWN_END.put(id, now + MISS_COOLDOWN_TICKS);
        player.swing(InteractionHand.MAIN_HAND, true);
        sendCooldown(player, MISS_COOLDOWN_TICKS);
    }

    private static void sendCooldown(ServerPlayer player, int ticks) {
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CooldownPacket(ticks));
    }

    private static boolean isParrying(ServerPlayer player) {
        Long end = PARRY_END.get(player.getUUID());
        return end != null && player.level().getGameTime() <= end;
    }

    /** Melee (and other entity) attacks. */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isParrying(player)) return;

        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypeTags.IS_EXPLOSION)) return;
        if (source.getDirectEntity() instanceof Projectile) return; // handled in onProjectileImpact

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity living)) return;

        event.setCanceled(true);
        parrySuccess(player);

        double dx = player.getX() - living.getX();
        double dz = player.getZ() - living.getZ();
        living.knockback(ParryConfig.KNOCKBACK.get(), dx, dz);
        living.hurtMarked = true;
    }

    /** Arrows, tridents, snowballs, etc. are deflected back along the player's look direction. */
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer player)) return;
        if (!isParrying(player)) return;

        Projectile projectile = event.getProjectile();
        if (projectile.getOwner() == player) return;

        event.setCanceled(true);

        double speed = Math.max(projectile.getDeltaMovement().length(), 0.8D);
        Vec3 look = player.getLookAngle();
        projectile.setPos(player.getEyePosition().add(look.scale(1.0D)));
        projectile.setDeltaMovement(look.scale(speed));
        projectile.setOwner(player);
        projectile.hurtMarked = true;

        parrySuccess(player);
    }

    private static void parrySuccess(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        UUID id = player.getUUID();
        PARRY_END.remove(id);
        COOLDOWN_END.put(id, level.getGameTime() + ParryConfig.SUCCESS_COOLDOWN_TICKS.get());
        sendCooldown(player, ParryConfig.SUCCESS_COOLDOWN_TICKS.get());

        Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(0.8D));

        // Sound effect (parry.ogg)
        level.playSound(null, pos.x, pos.y, pos.z, ParryMod.PARRY_SOUND.get(),
                SoundSource.PLAYERS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);

        // Small spark effect
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 14, 0.25, 0.25, 0.25, 0.35);
        level.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 8, 0.2, 0.2, 0.2, 0.3);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PARRY_END.remove(id);
        COOLDOWN_END.remove(id);
    }
}
