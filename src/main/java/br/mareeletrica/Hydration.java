package br.mareeletrica;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import io.netty.buffer.ByteBuf;

@EventBusSubscriber(modid=MareEletrica.ID)
public final class Hydration {
    private static final String KEY="MareHydration";
    public static int clientWater=20;
    private Hydration() {}
    public record Sync(int value) implements CustomPacketPayload {
        public static final Type<Sync> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"hydration"));
        public static final StreamCodec<ByteBuf,Sync> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Sync::value,Sync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Sync.TYPE,Sync.CODEC,(packet,context)->clientWater=Math.max(0,Math.min(20,packet.value())));
    }
    private static HydrationRules.State read(Player player) {
        CompoundTag root=player.getPersistentData();
        if (!root.contains(KEY)) return new HydrationRules.State(20,0);
        CompoundTag data=root.getCompound(KEY);
        return new HydrationRules.State(data.getInt("Water"),data.getInt("Effort"));
    }
    private static void write(Player player,HydrationRules.State state) {
        CompoundTag data=new CompoundTag();data.putInt("Water",state.water());data.putInt("Effort",state.effort());
        player.getPersistentData().put(KEY,data);
    }
    public static void drink(Player player,int amount) {
        if (!(player instanceof ServerPlayer server)) return;
        var next=HydrationRules.drink(read(player),amount);write(player,next);
        PacketDistributor.sendToPlayer(server,new Sync(next.water()));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount%20!=0 || !player.isAlive()) return;
        boolean exempt=player.isCreative() || player.isSpectator() || player.level().getDifficulty()==Difficulty.PEACEFUL;
        var next=HydrationRules.step(read(player),player.isSprinting() || player.isSwimming(),player.level().dimensionType().ultraWarm(),exempt);
        write(player,next);
        if (!exempt && next.water()<=4) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,0,false,false,true));
        if (!exempt && next.water()==0 && player.tickCount%80==0 && player.getHealth()>1)
            player.hurt(player.damageSources().starve(),Math.min(1,player.getHealth()-1));
        PacketDistributor.sendToPlayer(player,new Sync(next.water()));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player,new Sync(read(player).water()));
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        write(event.getEntity(),event.isWasDeath()?new HydrationRules.State(20,0):read(event.getOriginal()));
    }
    @SubscribeEvent public static void consumed(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var item=event.getItem();
        if (item.is(Items.POTION)) {
            var contents=item.get(DataComponents.POTION_CONTENTS);
            if (contents!=null && contents.is(Potions.WATER)) drink(player,4);
        } else if (item.is(Items.MILK_BUCKET)) drink(player,6);
    }
}
