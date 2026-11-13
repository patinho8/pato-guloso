package br.mareeletrica;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MareEletrica.ID)
public final class MareEletrica {
    public static final String ID = "mareeletrica";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,ID);
    public static final Supplier<PressureWaterFluid.Source> PRESSURIZED_WATER=FLUIDS.register("pressurized_water",PressureWaterFluid.Source::new);
    public static final Supplier<PressureWaterFluid.Flowing> FLOWING_PRESSURIZED_WATER=FLUIDS.register("flowing_pressurized_water",PressureWaterFluid.Flowing::new);
    public static final DeferredBlock<PressureWaterBlock> PRESSURIZED_WATER_BLOCK=BLOCKS.register("pressurized_water",()->new PressureWaterBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).noLootTable()));
    public static final DeferredBlock<FaucetStreamBlock> FAUCET_STREAM=BLOCKS.register("faucet_stream",()->new FaucetStreamBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion().replaceable().noLootTable().strength(-1F,3600000F)));
    public static final DeferredBlock<WaterDuctBlock> WATER_INTAKE=BLOCKS.register("water_intake",()->new WaterDuctBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion(),1));
    public static final DeferredBlock<WaterDuctBlock> WATER_OUTLET=BLOCKS.register("water_outlet",()->new WaterDuctBlock(BlockBehaviour.Properties.of().strength(2).sound(SoundType.METAL).noOcclusion(),2));
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredBlock<CableBlock> WATER_SHAFT=BLOCKS.register("water_shaft",()->new WaterShaftBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredItem<BlockItem> WATER_SHAFT_ITEM=ITEMS.register("water_shaft",()->new WaterShaftItem(WATER_SHAFT.get(),new Item.Properties()));
    public static final DeferredBlock<LedBedBlock> LED_BED=BLOCKS.register("led_bed",()->new LedBedBlock(BlockBehaviour.Properties.of().strength(.7F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).lightLevel(s->12)));
    public static final DeferredItem<BlockItem> LED_BED_ITEM=ITEMS.register("led_bed",()->new BlockItem(LED_BED.get(),new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BlockItem> WATER_INTAKE_ITEM=ITEMS.register("water_intake",()->new BlockItem(WATER_INTAKE.get(),new Item.Properties()));
    public static final DeferredItem<BlockItem> WATER_OUTLET_ITEM=ITEMS.register("water_outlet",()->new BlockItem(WATER_OUTLET.get(),new Item.Properties()));
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final Supplier<BlockEntityType<LedBedEntity>> LED_BED_ENTITY=ENTITIES.register("led_bed",()->BlockEntityType.Builder.of(LedBedEntity::new,LED_BED.get()).build(null));
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final Map<Kind, DeferredBlock<MachineBlock>> MACHINES = new EnumMap<>(Kind.class);
    static {
        for (Kind kind : Kind.values()) {
            DeferredBlock<MachineBlock> block = BLOCKS.register(kind.id, () -> machineBlock(kind,
                BlockBehaviour.Properties.of().strength(3.5F, 8F).sound(kind==Kind.GIANT_WHEEL?SoundType.WOOD:SoundType.METAL).noOcclusion()
                    .pushReaction(kind == Kind.POTABILIZER || kind.large() ? PushReaction.BLOCK : PushReaction.NORMAL)
                    .lightLevel(state -> kind!=Kind.GIANT_WHEEL&&state.getValue(MachineBlock.ACTIVE) ? (kind == Kind.LAMP ? 0 : 5) : 0)));
            MACHINES.put(kind, block);
            ITEMS.register(kind.id, () -> new MachineItem(block.get(), new Item.Properties()));
        }
    }
    public static final Map<Kind, DeferredBlock<LargeMachinePartBlock>> LARGE_PARTS = new EnumMap<>(Kind.class);
    static {
        for (Kind kind : java.util.stream.Stream.concat(java.util.stream.Stream.of(Kind.CORE_QUANTUM,Kind.PURIFIER,Kind.GREENHOUSE,Kind.QUARRY,Kind.GEOTHERMAL,Kind.ABYSSAL,Kind.MORTAR,Kind.ROVER_BASE,Kind.EXTERMINATOR,Kind.GAS_REFILL,Kind.GIANT_WHEEL,Kind.CURRENT_TURBINE,Kind.FALL_TURBINE,Kind.PRESSURE_TURBINE),java.util.Arrays.stream(Kind.values()).filter(Kind::scalable)).toArray(Kind[]::new)) LARGE_PARTS.put(kind,BLOCKS.register(kind.id+"_part", () -> {
            var properties=BlockBehaviour.Properties.of().strength(3.5F,8F).sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK);
            if(kind.scalable())return kind.bigSize()==2?new ScaledMachinePartBlock.Two(kind,properties):new ScaledMachinePartBlock.Three(kind,properties);
            return kind==Kind.PURIFIER||kind==Kind.CORE_QUANTUM?new CompactMachinePartBlock(kind,properties):kind==Kind.GIANT_WHEEL?new HydroMachinePartBlock.Giant(kind,properties):kind.waterStructure()?new HydroMachinePartBlock(kind,properties):new LargeMachinePartBlock(kind,properties);
        }));
    }
    public static final Supplier<BlockEntityType<LargeMachinePartEntity>> LARGE_PART_ENTITY = ENTITIES.register("large_machine_part",
        () -> BlockEntityType.Builder.of(LargeMachinePartEntity::new,LARGE_PARTS.values().stream().map(Supplier::get).toArray(Block[]::new)).build(null));
    public static final DeferredItem<Item> HONEY_BREAD = ITEMS.register("honey_bread", () -> new MaterialItem("honey_bread",new Item.Properties().food(
        new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build())));
    public static final DeferredItem<Item> HONEY_CANDY = ITEMS.register("honey_candy", () -> new MaterialItem("honey_candy",new Item.Properties().food(
        new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).fast().build())));
    public static final DeferredItem<HoneyWaterItem> HONEY_WATER = ITEMS.register("honey_water", () -> new HoneyWaterItem(new Item.Properties()));
    public static final Supplier<MenuType<ConstructionMenu>> CONSTRUCTION_MENU = MENUS.register("construction",
        () -> new MenuType<>(ConstructionMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredBlock<CableBlock> CYBER_CABLE = BLOCKS.register("cyber_cable", () -> new CableBlock(
        BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).noOcclusion().lightLevel(s->5),true));
    public static final DeferredItem<BlockItem> CYBER_CABLE_ITEM = ITEMS.register("cyber_cable", () -> new CableItem(CYBER_CABLE.get(),new Item.Properties()));
    /** Wind cable (0.46.0): white with teal stripes, carries 32 000 FE per half second (4x the normal cable). */
    public static final DeferredBlock<CableBlock> WIND_CABLE = BLOCKS.register("wind_cable", () -> new CableBlock(
        BlockBehaviour.Properties.of().strength(2F).sound(SoundType.METAL).noOcclusion(),false,32_000));
    public static final DeferredItem<BlockItem> WIND_CABLE_ITEM = ITEMS.register("wind_cable", () -> new CableItem(WIND_CABLE.get(),new Item.Properties()));
    /** Transformers (0.51.0): cable pieces that step the voltage down to low / medium / high. */
    public static final java.util.List<DeferredBlock<CableBlock>> TRANSFORMERS=new java.util.ArrayList<>();
    public static final java.util.List<DeferredItem<BlockItem>> TRANSFORMER_ITEMS=new java.util.ArrayList<>();
    static{for(int v=1;v<=3;v++){final int vv=v;String id="transformer_"+new String[]{"","low","medium","high"}[v];var b=BLOCKS.register(id,()->new CableBlock(BlockBehaviour.Properties.of().strength(2F).sound(SoundType.METAL).noOcclusion(),false,vv==1?8_000:vv==2?32_000:128_000).limit(vv));TRANSFORMERS.add(b);TRANSFORMER_ITEMS.add(ITEMS.register(id,()->new CableItem(b.get(),new Item.Properties())));}}
    public static final DeferredItem<Item> CANNONBALL = ITEMS.register("cannonball", () -> new MaterialItem("cannonball",new Item.Properties().stacksTo(32)));
    public static final DeferredItem<RemoteControlItem> REMOTE = ITEMS.register("rover_remote", () -> new RemoteControlItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<AimRemoteItem> AIM_REMOTE=ITEMS.register("aim_remote",()->new AimRemoteItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<DmtItem> DMT=ITEMS.register("dmt",()->new DmtItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.minecraft.world.item.SwordItem> DMT_BLADE=ITEMS.register("dmt_blade",()->new net.minecraft.world.item.SwordItem(net.minecraft.world.item.Tiers.IRON,new Item.Properties().durability(512).attributes(net.minecraft.world.item.SwordItem.createAttributes(net.minecraft.world.item.Tiers.IRON,3,-2.2F))));
    public static final DeferredItem<SniperItem> SNIPER=ITEMS.register("precision_sniper",()->new SniperItem(new Item.Properties().stacksTo(1)));
    /** 0.67.4: the hunting rifle modelled by the player — same mechanics as the precision sniper, its own look */
    public static final DeferredItem<SniperItem> HUNTER=ITEMS.register("hunter_sniper",()->new SniperItem(new Item.Properties().stacksTo(1)));
    public static boolean isSniper(net.minecraft.world.item.ItemStack s){return s.is(SNIPER.get())||s.is(HUNTER.get());}
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,ID);
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> SMOKE_CLOUD=PARTICLES.register("smoke_cloud",()->new net.minecraft.core.particles.SimpleParticleType(true));
    public static final Supplier<net.minecraft.core.particles.SimpleParticleType> MUZZLE_SMOKE=PARTICLES.register("muzzle_smoke",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,ID);
    public static final Supplier<net.minecraft.sounds.SoundEvent> SNIPER_SHOT=SOUNDS.register("sniper_shot",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"sniper_shot")));
    public static final Supplier<net.minecraft.sounds.SoundEvent> SNIPER_SILENCED=SOUNDS.register("sniper_silenced",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"sniper_silenced")));
    public static final Supplier<net.minecraft.sounds.SoundEvent> REVOLVER_SHOT=SOUNDS.register("revolver_shot",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"revolver_shot")));
    public static final int[] SCOPE_ZOOMS={2,3,4,6,8,10,12,16,20,24,7,9,14,18,28,32,40,48};
    public static final java.util.List<DeferredItem<RifleAttachmentItem>> SCOPES=new java.util.ArrayList<>(),SUPPRESSORS=new java.util.ArrayList<>(),STICKERS=new java.util.ArrayList<>();
    static {for(int zoom:SCOPE_ZOOMS)SCOPES.add(ITEMS.register("rifle_scope_"+zoom+"x",()->new RifleAttachmentItem(RifleAttachmentItem.Type.SCOPE,zoom)));for(int tier=1;tier<=5;tier++){final int t=tier;SUPPRESSORS.add(ITEMS.register("rifle_suppressor_"+t,()->new RifleAttachmentItem(RifleAttachmentItem.Type.SUPPRESSOR,t)));}for(int i=1;i<=16;i++){final int n=i;STICKERS.add(ITEMS.register("rifle_sticker_"+n,()->new RifleAttachmentItem(RifleAttachmentItem.Type.STICKER,n)));}}
    public static final DeferredItem<RifleAttachmentItem> BIPOD=ITEMS.register("rifle_bipod",()->new RifleAttachmentItem(RifleAttachmentItem.Type.BIPOD,1));
    public static final DeferredItem<RevolverItem> REVOLVER=ITEMS.register("revolver",()->new RevolverItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> REVOLVER_ROUND=ITEMS.register("revolver_round",()->new MaterialItem("revolver_round",new Item.Properties()));
    public static final Supplier<MenuType<RifleMenu>> RIFLE_MENU=MENUS.register("rifle_loadout",()->new MenuType<>(RifleMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<Item> SNIPER_ROUND=ITEMS.register("precision_round",()->new MaterialItem("precision_round",new Item.Properties()));
    public static final DeferredItem<Item> DMT_BLADES=ITEMS.register("dmt_dual_blades",()->new MaterialItem("dmt_dual_blades",new Item.Properties().durability(1024)));
    public static final DeferredItem<Item> DMT_GUN=ITEMS.register("dmt_gas_gun",()->new MaterialItem("dmt_gas_gun",new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> THUNDER_SPEAR=ITEMS.register("thunder_spear",()->new MaterialItem("thunder_spear",new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> PULL_HOOK=ITEMS.register("pull_hook",()->new MaterialItem("pull_hook",new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> DMT_CARTRIDGE=ITEMS.register("dmt_cartridge",()->new MaterialItem("dmt_cartridge",new Item.Properties()));
    public static final Supplier<MenuType<DmtMenu>> DMT_MENU=MENUS.register("dmt_loadout",()->new MenuType<>(DmtMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<Item> EMPTY_GAS=ITEMS.register("empty_gas",()->new MaterialItem("empty_gas",new Item.Properties()));
    public static final DeferredItem<Item> GAS=ITEMS.register("compressed_gas",()->new MaterialItem("compressed_gas",new Item.Properties().craftRemainder(EMPTY_GAS.get())));
    public static final Supplier<MenuType<ServiceMenu>> SERVICE_MENU=MENUS.register("service",()->new MenuType<>(ServiceMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<Item> ROVER_BULLET = ITEMS.register("rover_bullet", () -> new MaterialItem("rover_bullet",new Item.Properties()));
    public static final Supplier<MenuType<ExplorerMenu>> EXPLORER_MENU=MENUS.register("explorer_inventory",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(ExplorerMenu::new));
    public static final Supplier<MenuType<MortarMenu>> MORTAR_MENU = MENUS.register("mortar", () -> new MenuType<>(MortarMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredBlock<HologramBlock> HOLOGRAM = BLOCKS.register("hologram", () -> new HologramBlock(
        BlockBehaviour.Properties.of().strength(-1F,3600000F).noOcclusion().noLootTable().lightLevel(s->7).pushReaction(PushReaction.BLOCK)));
    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ACTORS = DeferredRegister.create(Registries.ENTITY_TYPE,ID);
    public static final Supplier<net.minecraft.world.entity.EntityType<DmtProjectileEntity>> DMT_PROJECTILE=ACTORS.register("dmt_projectile",()->net.minecraft.world.entity.EntityType.Builder.<DmtProjectileEntity>of(DmtProjectileEntity::new,net.minecraft.world.entity.MobCategory.MISC).sized(.18F,.18F).clientTrackingRange(10).updateInterval(1).build(ID+":dmt_projectile"));
    public static final Supplier<net.minecraft.world.entity.EntityType<ExterminatorEntity>> EXTERMINATOR=ACTORS.register("exterminator",()->net.minecraft.world.entity.EntityType.Builder.<ExterminatorEntity>of(ExterminatorEntity::new,net.minecraft.world.entity.MobCategory.MISC).sized(.85F,2.8F).clientTrackingRange(12).updateInterval(2).build(ID+":exterminator"));
    public static final Supplier<net.minecraft.world.entity.EntityType<ExplorerEntity>> EXPLORER = ACTORS.register("explorer", () ->
        net.minecraft.world.entity.EntityType.Builder.<ExplorerEntity>of(ExplorerEntity::new,net.minecraft.world.entity.MobCategory.MISC)
            .sized(1.8F,1.45F).eyeHeight(1.355F).clientTrackingRange(10).updateInterval(2).build(ID+":explorer"));
    public static final Supplier<net.minecraft.world.entity.EntityType<CannonballEntity>> SHELL = ACTORS.register("cannonball", () ->
        net.minecraft.world.entity.EntityType.Builder.<CannonballEntity>of(CannonballEntity::new,net.minecraft.world.entity.MobCategory.MISC)
            .sized(.35F,.35F).clientTrackingRange(12).updateInterval(1).build(ID+":cannonball"));
    public static final Supplier<net.minecraft.world.entity.EntityType<RoverBulletEntity>> BULLET = ACTORS.register("rover_bullet", () ->
        net.minecraft.world.entity.EntityType.Builder.<RoverBulletEntity>of(RoverBulletEntity::new,net.minecraft.world.entity.MobCategory.MISC)
            .sized(.15F,.15F).clientTrackingRange(8).updateInterval(1).build(ID+":rover_bullet"));
    public static final DeferredBlock<CableBlock> CABLE = BLOCKS.register("cable", () -> new CableBlock(
        BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredItem<BlockItem> CABLE_ITEM = ITEMS.register("cable", () -> new CableItem(CABLE.get(), new Item.Properties()));
    public static final DeferredBlock<ItemDuctBlock> ITEM_DUCT = BLOCKS.register("item_duct", () -> new ItemDuctBlock(
        BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion()));
    public static final Supplier<BlockEntityType<ItemDuctEntity>> ITEM_DUCT_ENTITY=ENTITIES.register("item_duct",()->BlockEntityType.Builder.of(ItemDuctEntity::new,ITEM_DUCT.get()).build(null));
    public static final DeferredItem<BlockItem> ITEM_DUCT_ITEM = ITEMS.register("item_duct", () -> new BlockItem(ITEM_DUCT.get(), new Item.Properties()));
    public static final DeferredItem<Item> IRON_GRIT = ITEMS.register("iron_grit", () -> new MaterialItem("iron_grit", new Item.Properties()));
    public static final DeferredItem<Item> COPPER_GRIT = ITEMS.register("copper_grit", () -> new MaterialItem("copper_grit", new Item.Properties()));
    public static final DeferredItem<Item> GOLD_GRIT = ITEMS.register("gold_grit", () -> new MaterialItem("gold_grit", new Item.Properties()));
    public static final java.util.List<DeferredItem<WirelessPlateItem>> WIRELESS_PLATES=java.util.List.of(ITEMS.register("wireless_plate",()->new WirelessPlateItem(0,new Item.Properties())),ITEMS.register("cyber_wireless_plate",()->new WirelessPlateItem(1,new Item.Properties())),ITEMS.register("hydraulic_wireless_plate",()->new WirelessPlateItem(2,new Item.Properties())));
    public static final String[] ADVANCED_IDS={"reinforced_alloy","superconducting_coil","quantum_circuit","synthetic_crystal","pressure_chamber","servo_motor","plasma_cell","dimensional_matrix","singularity_seed"};
    public static final java.util.Map<String,DeferredItem<Item>> ADVANCED=new java.util.LinkedHashMap<>();
    static {for(String name:ADVANCED_IDS)ADVANCED.put(name,ITEMS.register(name,()->new MaterialItem(name,new Item.Properties())));}
    public static final DeferredItem<Item> IRON_PLATE = ITEMS.register("iron_plate", () -> new MaterialItem("iron_plate", new Item.Properties()));
    public static final DeferredItem<Item> COPPER_COIL = ITEMS.register("copper_coil", () -> new MaterialItem("copper_coil", new Item.Properties()));
    public static final DeferredItem<Item> GOLD_CONTACT = ITEMS.register("gold_contact", () -> new MaterialItem("gold_contact", new Item.Properties()));
    public static final DeferredItem<Item> CONTROL_MODULE = ITEMS.register("control_module", () -> new MaterialItem("control_module", new Item.Properties()));
    public static final DeferredItem<PortableBatteryItem> PORTABLE_BATTERY = ITEMS.register("portable_battery", () -> new PortableBatteryItem(new Item.Properties()));
    public static final DeferredItem<PotableWaterItem> POTABLE_WATER = ITEMS.register("potable_water", () -> new PotableWaterItem(new Item.Properties()));
    public static final DeferredBlock<PotabilizerPartBlock> POTABILIZER_PART = BLOCKS.register("water_potabilizer_part", () -> new PotabilizerPartBlock(
        BlockBehaviour.Properties.of().strength(3.5F,8F).sound(SoundType.METAL).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK)
            .lightLevel(state -> state.getValue(MachineBlock.ACTIVE) ? 3 : 0)));
    public static final Supplier<BlockEntityType<PotabilizerPartEntity>> POTABILIZER_PART_ENTITY = ENTITIES.register("water_potabilizer_part",
        () -> BlockEntityType.Builder.of(PotabilizerPartEntity::new,POTABILIZER_PART.get()).build(null));
    public static final Supplier<BlockEntityType<MachineEntity>> MACHINE_ENTITY = ENTITIES.register("machine",
        () -> BlockEntityType.Builder.of(MachineEntity::new, MACHINES.values().stream().map(Supplier::get).toArray(Block[]::new)).build(null));
    public static final DeferredBlock<BeamLightBlock> BEAM_LIGHT = BLOCKS.register("beam_light", () -> new BeamLightBlock(
        BlockBehaviour.Properties.of().air().noCollission().noOcclusion().replaceable().noLootTable().lightLevel(state -> 15)));
    public static final DeferredBlock<BridgeTileBlock> BRIDGE_TILE = BLOCKS.register("bridge_tile", () -> new BridgeTileBlock(
        BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.GLASS).noOcclusion().replaceable().noLootTable().lightLevel(state -> 6)));
    public static final Supplier<BlockEntityType<BeamLightEntity>> BEAM_ENTITY = ENTITIES.register("beam_light",
        () -> BlockEntityType.Builder.of(BeamLightEntity::new, BEAM_LIGHT.get(), BRIDGE_TILE.get()).build(null));
    public static final Supplier<MenuType<MachineMenu>> MACHINE_MENU = MENUS.register("machine",
        () -> new MenuType<>(MachineMenu::new, FeatureFlags.DEFAULT_FLAGS));


    public static final DeferredItem<SmgItem> SMG=ITEMS.register("submachine_gun",()->new SmgItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SMG_ROUND=ITEMS.register("smg_round",()->new MaterialItem("smg_round",new Item.Properties()));
    public static final java.util.List<DeferredItem<SmgAttachmentItem>> SMG_SCOPES=new java.util.ArrayList<>(),SMG_SKINS=new java.util.ArrayList<>();
    static {for(int z=1;z<=4;z++){final int n=z;SMG_SCOPES.add(ITEMS.register("smg_scope_"+z+"x",()->new SmgAttachmentItem(true,n)));}for(int i=1;i<=6;i++){final int n=i;SMG_SKINS.add(ITEMS.register("smg_sticker_"+i,()->new SmgAttachmentItem(false,n)));}}
    public static final Supplier<MenuType<SmgMenu>> SMG_MENU=MENUS.register("smg_loadout",()->new MenuType<>(SmgMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<net.minecraft.sounds.SoundEvent> FLASH_RING=SOUNDS.register("flash_ring",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"flash_ring")));
    public static final Supplier<net.minecraft.sounds.SoundEvent> FRAG_BLAST=sound("frag_blast");
    public static final Supplier<net.minecraft.sounds.SoundEvent> C4_BLAST=sound("c4_blast");
    public static final Supplier<net.minecraft.sounds.SoundEvent> GRENADE_ROLL=sound("grenade_roll");
    public static final Supplier<net.minecraft.sounds.SoundEvent> KNIFE_SWING=sound("knife_swing");
    public static final Supplier<net.minecraft.sounds.SoundEvent> JET_ENGINE=sound("jet_engine");
    public static final Supplier<net.minecraft.sounds.SoundEvent> JET_GUN=sound("jet_gun");
    public static final Supplier<net.minecraft.sounds.SoundEvent> LAUNCHER_FIRE=sound("launcher_fire");
    public static final Supplier<net.minecraft.sounds.SoundEvent> SHELL_WHISTLE=sound("shell_whistle");
    public static final Supplier<net.minecraft.sounds.SoundEvent> SIREN_SOUND=sound("alarm_siren");
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> SILENT=sound("silent");
    private static net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.sounds.SoundEvent,net.minecraft.sounds.SoundEvent> sound(String n){return SOUNDS.register(n,()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,n)));}
    public static final Supplier<net.minecraft.sounds.SoundEvent> SMG_SHOT=SOUNDS.register("smg_shot",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"smg_shot")));
    public static final Supplier<net.minecraft.sounds.SoundEvent> SMG_SILENCED=SOUNDS.register("smg_silenced",()->net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ID,"smg_silenced")));
    private static void weaponItems(CreativeModeTab.Output out){for(var gun:Arsenal.guns())if(gun!=REVOLVER)out.accept(gun.get());out.accept(Arsenal.USP_ROUND.get());out.accept(Arsenal.RIFLE_ROUND.get());out.accept(Arsenal.SHOTGUN_SHELL.get());out.accept(ROVER_BULLET.get());out.accept(SMG.get());out.accept(SMG_ROUND.get());out.accept(SNIPER.get());out.accept(HUNTER.get());out.accept(SNIPER_ROUND.get());out.accept(REVOLVER.get());out.accept(REVOLVER_ROUND.get());SCOPES.forEach(x->out.accept(x.get()));ArmoryParts.creative(out,false);out.accept(BIPOD.get());}
    private static MachineBlock machineBlock(Kind kind,BlockBehaviour.Properties props){return kind==Kind.GIANT_WHEEL?new GiantWheelBlock(kind,props):new MachineBlock(kind,props);}

    public MareEletrica(IEventBus bus) {
        Arsenal.register(bus);PARTICLES.register(bus);SOUNDS.register(bus);DmtEquipment.register(bus);FLUIDS.register(bus); BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus);
        MENUS.register(bus); TABS.register(bus); ACTORS.register(bus);
        bus.addListener(MachinePorts::register);
        bus.addListener(Hydration::register);
        bus.addListener(MachinePackets::register);bus.addListener(EditorPackets::register);
        bus.addListener(ScopePackets::register);bus.addListener(GunPackets::register);bus.addListener(RemotePackets::register);bus.addListener(DmtPackets::register);
        bus.addListener(ExplorerEntity::attributes);bus.addListener(ExterminatorEntity::attributes);
    }
}
