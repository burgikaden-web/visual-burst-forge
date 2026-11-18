package com.visualburst;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(VisualBurstMod.MOD_ID)
public class VisualBurstMod {
    public static final String MOD_ID = "visualburst";

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> BURST_CORE = ITEMS.register(
            "burst_core", () -> new BurstCoreItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHOCKWAVE_CORE = ITEMS.register(
            "shockwave_core", () -> new ShockwaveCoreItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LAUNCH_CORE = ITEMS.register(
            "launch_core", () -> new LaunchCoreItem(new Item.Properties().stacksTo(1)));

    public VisualBurstMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BURST_CORE);
            event.accept(SHOCKWAVE_CORE);
            event.accept(LAUNCH_CORE);
        }
    }

    // Arcade-style game knockback only. No Minecraft Explosion is created,
    // and blocks are never damaged, ignited, or destroyed.
    private static void pushNearbyMobs(ServerLevel level, Vec3 center,
                                       double radius, double strength, double lift,
                                       Player user) {
        AABB bounds = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class, bounds, entity -> entity != user)) {
            Vec3 offset = target.position().add(0.0, target.getBbHeight() * 0.35, 0.0)
                    .subtract(center);
            double distance = offset.length();
            if (distance > radius || distance < 0.001) continue;

            double falloff = 1.0 - (distance / radius);
            Vec3 impulse = offset.normalize().scale(strength * (0.35 + 0.65 * falloff));
            target.setDeltaMovement(target.getDeltaMovement().add(
                    impulse.x, Math.max(lift, impulse.y + lift), impulse.z));
            target.hasImpulse = true;
        }
    }

    public static class BurstCoreItem extends Item {
        public BurstCoreItem(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && level instanceof ServerLevel server) {
                Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(3.0));
                server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z,
                        1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z,
                        45, 0.65, 0.55, 0.65, 0.045);
                server.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z,
                        35, 0.8, 0.65, 0.8, 0.04);
                server.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z,
                        22, 0.55, 0.55, 0.55, 0.07);
                pushNearbyMobs(server, center, 5.0, 1.25, 0.18, player);
                server.playSound(null, center.x, center.y, center.z,
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 1.15F);
                player.getCooldowns().addCooldown(this, 25);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    public static class ShockwaveCoreItem extends Item {
        public ShockwaveCoreItem(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && level instanceof ServerLevel server) {
                Vec3 center = player.position().add(0, 0.15, 0);
                server.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z,
                        90, 2.8, 0.35, 2.8, 0.12);
                server.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z,
                        1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z,
                        55, 2.2, 0.25, 2.2, 0.08);
                pushNearbyMobs(server, center, 8.0, 1.75, 0.32, player);
                server.playSound(null, center.x, center.y, center.z,
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.8F);
                player.getCooldowns().addCooldown(this, 60);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    public static class LaunchCoreItem extends Item {
        public LaunchCoreItem(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && level instanceof ServerLevel server) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(player.getDeltaMovement().add(
                        look.x * 1.15, 0.95 + Math.max(0.0, look.y * 0.45), look.z * 1.15));
                player.hasImpulse = true;
                Vec3 origin = player.position();
                server.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y, origin.z,
                        32, 0.45, 0.35, 0.45, 0.12);
                server.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z,
                        18, 0.3, 0.25, 0.3, 0.035);
                server.playSound(null, origin.x, origin.y, origin.z,
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.8F, 1.25F);
                player.getCooldowns().addCooldown(this, 35);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
}
