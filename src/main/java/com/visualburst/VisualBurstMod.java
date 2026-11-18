package com.visualburst;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
            "burst_core",
            () -> new BurstCoreItem(new Item.Properties().stacksTo(1))
    );

    public VisualBurstMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BURST_CORE);
        }
    }

    public static class BurstCoreItem extends Item {
        public BurstCoreItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(
                Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                double x = player.getX() + player.getLookAngle().x * 2.5;
                double y = player.getEyeY() + player.getLookAngle().y * 2.5;
                double z = player.getZ() + player.getLookAngle().z * 2.5;

                // Purely visual particles: this does not create an Explosion,
                // damage entities, or modify terrain.
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                        1, 0.0, 0.0, 0.0, 0.0);
                serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z,
                        36, 0.55, 0.45, 0.55, 0.035);
                serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z,
                        28, 0.65, 0.55, 0.65, 0.025);
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z,
                        18, 0.5, 0.5, 0.5, 0.06);

                player.getCooldowns().addCooldown(this, 30);
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
}
