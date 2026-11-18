package com.visualburst;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

    // All abilities below are visual effects or movement boosts for the player.
    // They do not damage entities, ignite blocks, or modify terrain.
    public static class BurstCoreItem extends Item {
        private static final String MODE_TAG = "BurstMode";
        private static final String[] MODE_NAMES = {
                "Explosive Dash", "Blast Jump", "Aerial Burst",
                "Cluster Mobility", "Howitzer Spiral"
        };

        public BurstCoreItem(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<ItemStack> use(
                Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (!level.isClientSide && level instanceof ServerLevel server) {
                if (player.isShiftKeyDown()) {
                    int mode = (stack.getOrCreateTag().getInt(MODE_TAG) + 1) % MODE_NAMES.length;
                    stack.getOrCreateTag().putInt(MODE_TAG, mode);
                    player.displayClientMessage(
                            Component.literal("Burst Core: " + MODE_NAMES[mode]), true);
                } else if (!player.getCooldowns().isOnCooldown(this)) {
                    int mode = stack.getOrCreateTag().getInt(MODE_TAG);
                    activateAbility(server, player, mode);
                    player.getCooldowns().addCooldown(this, mode == 3 || mode == 4 ? 45 : 20);
                }
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        private void activateAbility(ServerLevel level, Player player, int mode) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 velocity = player.getDeltaMovement();
            Vec3 pos = player.position();

            switch (mode) {
                case 0 -> {
                    // Explosive Speed: quick forward propulsion.
                    player.setDeltaMovement(velocity.scale(0.35).add(look.scale(1.35)).add(0, 0.12, 0));
                    emitBurst(level, pos, 26, 0.55);
                }
                case 1 -> {
                    // Blast Jump: vertical launch with a small horizontal carry.
                    player.setDeltaMovement(velocity.x * 0.55, 1.05, velocity.z * 0.55);
                    emitBurst(level, pos, 34, 0.7);
                }
                case 2 -> {
                    // Aerial Burst: steer in the direction the player is looking.
                    player.setDeltaMovement(velocity.add(look.scale(0.72)).add(0, 0.25, 0));
                    emitBurst(level, pos, 22, 0.48);
                }
                case 3 -> {
                    // Cluster Mobility: a stronger movement pulse with several visual pops.
                    player.setDeltaMovement(velocity.scale(0.25).add(look.scale(1.8)).add(0, 0.2, 0));
                    for (int i = 0; i < 5; i++) {
                        double x = pos.x + (level.random.nextDouble() - 0.5) * 1.1;
                        double y = pos.y + 0.4 + level.random.nextDouble() * 1.3;
                        double z = pos.z + (level.random.nextDouble() - 0.5) * 1.1;
                        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
                        level.sendParticles(ParticleTypes.FLAME, x, y, z, 8, 0.2, 0.2, 0.2, 0.025);
                    }
                }
                case 4 -> {
                    // Howitzer-inspired spiral: lift and forward momentum with a circular trail.
                    player.setDeltaMovement(velocity.scale(0.45).add(look.scale(0.9)).add(0, 0.82, 0));
                    for (int i = 0; i < 20; i++) {
                        double angle = Math.PI * 2.0 * i / 20.0;
                        double x = pos.x + Math.cos(angle) * 1.35;
                        double z = pos.z + Math.sin(angle) * 1.35;
                        level.sendParticles(ParticleTypes.FLAME, x, pos.y + 0.9, z,
                                2, 0.07, 0.12, 0.07, 0.015);
                        level.sendParticles(ParticleTypes.CLOUD, x, pos.y + 0.9, z,
                                1, 0.06, 0.08, 0.06, 0.01);
                    }
                }
                default -> emitBurst(level, pos, 20, 0.5);
            }

            player.hasImpulse = true;
            player.fallDistance = 0;
            level.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.65F, 1.1F);
        }

        private void emitBurst(ServerLevel level, Vec3 pos, int count, double spread) {
            double y = pos.y + 0.8;
            level.sendParticles(ParticleTypes.EXPLOSION, pos.x, y, pos.z, 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.FLAME, pos.x, y, pos.z,
                    count, spread, spread * 0.7, spread, 0.035);
            level.sendParticles(ParticleTypes.CLOUD, pos.x, y, pos.z,
                    count / 2, spread, spread * 0.5, spread, 0.025);
            level.sendParticles(ParticleTypes.END_ROD, pos.x, y, pos.z,
                    count / 3, spread * 0.7, spread * 0.7, spread * 0.7, 0.045);
        }
    }

    public static class ShockwaveCoreItem extends Item {
        public ShockwaveCoreItem(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && level instanceof ServerLevel server) {
                Vec3 center = player.position();
                server.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z,
                        90, 2.8, 0.35, 2.8, 0.12);
                server.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z,
                        1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z,
                        55, 2.2, 0.25, 2.2, 0.08);
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
