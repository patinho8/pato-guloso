package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MachineEntity extends BlockEntity implements MenuProvider {
    public final Kind kind;
    public final EnergyTank clean;
    public final EnergyTank dirty;
    public boolean enabled = true;
    public int progress,batchRemaining,batchTotal,batchCount,pendingOre=-1;
    public final ItemStackHandler gasInventory=new ItemStackHandler(10){
        protected void onContentsChanged(int slot){setChanged();}
        public boolean isItemValid(int slot,ItemStack stack){return stack.is(MareEletrica.GAS.get())&&(kind==Kind.QUARRY||kind==Kind.EXTERMINATOR&&slot==0);}
        public int getSlotLimit(int slot){return kind==Kind.QUARRY?1:64;}
    };
    public int filterLeft;
    /** Giant wheel: ticks of rotation left (given by cranks). Power controller: limit percent, seconds on, seconds off. */
    public int drive,ctrlLimit=100,ctrlOn,ctrlOff;
    /** Giant wheel: a hand crank is mounted on its axle. */
    public boolean crank;
    /** Power controller: devices linked with a pendrive. */
    public final java.util.List<ControlLinks.Link> links=new java.util.ArrayList<>();
    public int status;
    public int routeCursor;
    public final java.util.List<WirelessPower.Plate> wireless=new java.util.ArrayList<>();
    public long wirelessEpoch=-1;public final int[] wirelessUsed=new int[3];
    public final java.util.Set<BlockPos> lampLights=new java.util.HashSet<>();public long beamUpdated=-100;
    public int lastRate;
    public final net.neoforged.neoforge.energy.IEnergyStorage energyPort;
    public final net.neoforged.neoforge.items.IItemHandler itemPort;
    public float angle;
    private int clock;
    public int workCursor;
    public int storedXp;
    public int channel;
    public boolean scanning;
    public int scanIndex;
    public BlockPos orePosition;
    public int oreStateId;
    public long weatherCooldown;
    public int projectedLength;
    public int waterProfile;
    public int blueprint;
    public final ProjectionEditor editor=new ProjectionEditor();
    public long previewUntil;
    public int projectionDistance=2;
    public boolean projecting;
    public int projectionCursor, projectionTotal, projectionWidth, projectionHeight, projectionDepth, projectionRate;
    public long projectionStamp;
    public float lampYaw,lampPitch;
    public long mortarAt;
    public boolean lastRedstone;
    public final int[] mortarElevation={70,70,70,70,70,70}, mortarAzimuth=new int[6];
    public BlockPos roverPosition;
    public java.util.UUID roverId;
    /** Who placed the machine (null on machines placed before 0.61: then anyone may use them). */
    public java.util.UUID owner;
    /** Only whoever placed it (machines placed before owners existed stay free). */
    public boolean isOwner(net.minecraft.world.entity.player.Player p){return owner==null||owner.equals(p.getUUID());}
    public int roverHeading, roverState, roverBattery, roverScans;
    public float drillDepth,previousDrillDepth;
    public static final int DATA_COUNT = 33;
    /** Overheating: ticks of continuous work until the machine burns out (10 min), cooling is 3x faster than heating. */
    public static final int HEAT_MAX = 12000;
    public int heat; public boolean burnt;
    /** Over-voltage: grows each half second the machine is fed by a core stronger than it takes; at 20 it burns out. */
    public int overvolt; private int volts;
    /** Called by the network when energy arrives from a core of this voltage. */
    public void powered(int voltage){volts=Math.max(volts,voltage);}
    public boolean voltageSensitive(){return kind.consumer&&kind!=Kind.POWER_CONTROLLER&&kind!=Kind.AUTO_CRANK;}
    private void voltageCheck(){
        if(!voltageSensitive()||burnt){volts=0;return;}
        int over=volts-kind.maxVoltage();volts=0;
        if(over>0){
            overvolt+=over;setChanged();
            if(level instanceof net.minecraft.server.level.ServerLevel sl){var c=worldPosition.getCenter();sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,c.x,c.y+.5,c.z,3+over*3,.3,.3,.3,.15);
                if(clock%20==0)level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,net.minecraft.sounds.SoundSource.BLOCKS,.6F,1.8F);}
            if(overvolt>=20){burnt=true;overvolt=0;setChanged();
                level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),net.minecraft.sounds.SoundSource.BLOCKS,.8F,1.6F);
                if(level instanceof net.minecraft.server.level.ServerLevel sl){var c=worldPosition.getCenter();sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,c.x,c.y+.6,c.z,20,.3,.3,.3,.04);sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,c.x,c.y+.6,c.z,6,.2,.2,.2,0);}}
        }else if(overvolt>0){overvolt--;setChanged();}
    }
    public boolean overheats(){return kind.consumer&&kind!=Kind.POWER_CONTROLLER&&kind!=Kind.PRESENCE_SENSOR&&kind!=Kind.MORTAR&&kind!=Kind.ROVER_BASE&&kind!=Kind.ITEM_TRANSFER&&kind!=Kind.ARMOR_STATION&&kind!=Kind.HANGAR&&kind!=Kind.SHIELD;}
    public int heatPercent(){return Math.min(100,heat*100/HEAT_MAX);}
    /** Repair a burnt machine (iron block). */
    public boolean repair(){if(!burnt)return false;burnt=false;heat=HEAT_MAX/2;setChanged();return true;}
    private final ItemStack[] previousInputs = { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };
    public final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override protected void onContentsChanged(int slot) {
            if (kind != null && kind.processor() && slot < kind.inputs()) {
                ItemStack current = getStackInSlot(slot);
                if (!ItemStack.isSameItemSameComponents(previousInputs[slot], current)) progress = 0;
                previousInputs[slot] = current.isEmpty() ? ItemStack.EMPTY : current.copyWithCount(1);
            }
            setChanged();if(kind==Kind.GAS_REFILL&&level!=null&&!level.isClientSide)syncAim();
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (kind == null) return false;
            if (kind.outputInventory()) return true;
            if (kind.industrial()) return IndustrialMachines.validInput(kind, slot, stack);
            if (kind.utilityProcessor()) return UtilityMachines.validInput(kind, slot, stack);
            if (kind == Kind.POTABILIZER) return WaterPotabilizer.validInput(slot,stack);
            if (kind == Kind.GREENHOUSE) return LargeMachines.validInput(slot,stack);
            if (kind == Kind.GAS_REFILL) return slot==0&&stack.is(MareEletrica.EMPTY_GAS.get());
            if (kind == Kind.BUILDER) return false;
            if (kind == Kind.MORTAR) return slot==0 && stack.is(MareEletrica.CANNONBALL.get());
            if (kind == Kind.ROVER_BASE) return slot==0 && stack.is(MareEletrica.ROVER_BULLET.get());
            if (kind == Kind.FOOD_STATION) return UtilityMachines.isMeal(stack);
            if (kind == Kind.ANIMAL_FEEDER) return IndustrialMachines.isFeed(stack);
            if (kind == Kind.PLANTER) return AdvancedMachines.cropFor(stack) != null;
            if (kind == Kind.CHARGER) return slot == 0 && stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM, null) != null;
            if (kind == Kind.PURIFIER) return slot == 0 && (stack.is(Items.CHARCOAL) || stack.is(Items.COAL));
            return kind.processor() && kind != Kind.STONE_GENERATOR && slot == 0;
        }
    };
    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch(index) {
                case 0 -> clean.stored(); case 1 -> dirty.stored();
                case 2 -> clean.capacity(); case 3 -> dirty.capacity();
                case 4 -> kind.ordinal(); case 5 -> progress;
                case 6 -> enabled ? 1 : 0; case 7 -> status;
                case 8 -> filterLeft; case 9 -> lastRate;
                case 10 -> orePosition == null ? 0 : orePosition.getX();
                case 11 -> orePosition == null ? 0 : orePosition.getY();
                case 12 -> orePosition == null ? 0 : orePosition.getZ();
                case 13 -> kind == Kind.XP_COLLECTOR ? storedXp : kind == Kind.TELEPORT_PAD ? channel : scanIndex;
                case 14 -> oreStateId; case 15 -> projectedLength; case 16 -> waterProfile; case 17 -> blueprint;
                case 18 -> projectionDistance; case 19 -> projectionTotal; case 20 -> projectionRate;
                case 21 -> projectionWidth; case 22 -> projectionHeight; case 23 -> projectionDepth;
                case 24 -> projecting?1:0; case 25 -> roverHeading; case 26 -> roverState;
                case 27 -> kind==Kind.MORTAR && level!=null ? (int)Math.max(0,mortarAt-level.getGameTime()) : roverBattery;
                case 28 -> roverScans;case 29 -> batchRemaining;case 30 -> batchTotal;case 31 -> batchCount; case 32 -> (overheats()?heatPercent():-1)+(burnt?1000:0); default -> 0;
            };
        }
        @Override public void set(int index, int value) { /* Server values cannot be set by menu data packets. */ }
        @Override public int getCount() { return DATA_COUNT; }
    };
    public MachineEntity(BlockPos pos, BlockState state) {
        super(MareEletrica.MACHINE_ENTITY.get(), pos, state);
        kind = ((MachineBlock) state.getBlock()).kind;lampYaw=(state.getValue(MachineBlock.FACING).toYRot()+180)%360;
        clean = new EnergyTank(kind.capacity); dirty = new EnergyTank(kind.dirtyCapacity);
        energyPort = MachinePorts.energy(this); itemPort = MachinePorts.items(this);
        if (kind == Kind.QUARRY || kind == Kind.LEVELER || kind == Kind.ROVER_BASE || kind==Kind.EXTERMINATOR) enabled = false;
        clock = (int) Math.floorMod(pos.asLong(), 10L);
    }
    public void toggle() { enabled = !enabled; setChanged(); }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            boolean active=getBlockState().getValue(MachineBlock.ACTIVE);
            if (active) angle = (angle + (kind.hydro()?HydroGenerators.speed(kind):kind.windTurbine()?WindPower.spin(kind):kind==Kind.QUARRY?13F:7F)) % 360F;
            previousDrillDepth=drillDepth;
            if(kind==Kind.QUARRY) drillDepth=Math.max(0,Math.min(4.75F,drillDepth+(active?.045F:-.07F)));
            return;
        }
        if(kind==Kind.MORTAR) FrontierMachines.mortarClock(this);
        if(kind==Kind.GIANT_WHEEL){enabled=true;if(!WaterWheelMigration.upgrade(this))return;}
        if (++clock % 10 != 0) return;
        WirelessPower.register(this);WirelessPower.receive(this);
        if(kind.wind())WindPower.register(this);
        if(kind==Kind.BUILDER && (!enabled || level.hasNeighborSignal(worldPosition))) ConstructionProjector.pause(this);
        int structure = kind == Kind.POTABILIZER ? PotabilizerStructure.integrity(this) : kind.large() ? LargeMachineStructure.integrity(this) : 1;
        if (structure == 0) { level.destroyBlock(worldPosition,true); return; }
        voltageCheck();
        int beforeClean = clean.stored(), beforeDirty = dirty.stored();
        int beforeProgress = progress, beforeFilter = filterLeft, beforeWater = waterProfile;
        status = 0; lastRate = 0; projectedLength = 0;
        boolean active = false;
        if (structure < 0) status = 30;
        else if (burnt) status = 60;
        else if (!enabled) status = 8;
        else if (kind!=Kind.MORTAR && kind!=Kind.GIANT_WHEEL && level.hasNeighborSignal(worldPosition)) status = 12;
        else {
            switch (kind) {
                case WATER_WHEEL, SEA_TURBINE -> active = generate();
                case PURIFIER -> active = purify();
                case CORE_SMALL, CORE_MEDIUM, CORE_LARGE, CORE_QUANTUM, WIND_BATTERY, WIND_INVERTER -> {
                    // 0.51.0 nerf: cores hand out much less per half second
                    lastRate = PowerNetwork.send(this, false, kind==Kind.CORE_QUANTUM?250_000:kind==Kind.WIND_INVERTER?40_000:kind==Kind.WIND_BATTERY?16_000:kind == Kind.CORE_LARGE ? 40_000 : kind == Kind.CORE_MEDIUM ? 10_000 : 3_000, target -> target.consumer&&target!=Kind.AUTO_CRANK) * 2;
                    active = clean.stored() > 0 || lastRate > 0;
                    status = active ? 1 : 2;
                }
                case FURNACE, CRUSHER, COOKER, RECYCLER, STONE_GENERATOR -> active = process();
                case IRRIGATOR -> active = irrigate();
                case MAGNET -> active = collect();
                case HEALER -> active = heal();
                case LIFT -> active = lift();
                case LAMP -> {
                    active = clean.pay(250); status = active ? 1 : 2;
                    if(active)FloodlightBeam.refresh(this);else FloodlightBeam.clear(this);
                }
                case OXYGEN_STATION -> active = oxygen();
                case REPELLER -> active = repel();
                case HARVESTER, PLANTER, FISHER, ORE_SCANNER, TELEPORT_PAD, CHARGER, XP_COLLECTOR, WEATHER_CONTROLLER, TURRET -> active = AdvancedMachines.tick(this);
                case PRESS, SAWMILL, FREEZER, REPAIRER, DISENCHANTER, ASSEMBLER, ANIMAL_FEEDER, SHEARER, EXTINGUISHER, BRIDGE_PROJECTOR -> active = IndustrialMachines.tick(this);
                case ITEM_TRANSFER -> active = ItemLogistics.tick(this);
                case SOLAR_PANEL, COMPACTOR, BUCKET_FILLER, EFFECT_CLEANSER, FOOD_STATION, PRESENCE_SENSOR, APIARY_EXTRACTOR, MINING_BOOSTER -> active = UtilityMachines.tick(this);
                case POTABILIZER -> active = WaterPotabilizer.tick(this);
                case GREENHOUSE, QUARRY -> active = LargeMachines.tick(this);
                case BUILDER -> active = ConstructionProjector.tick(this);
                case EXTERMINATOR -> active=ExterminatorEntity.baseTick(this);
                case GAS_REFILL -> active=GasRefiller.tick(this);
                case GIANT_WHEEL,CURRENT_TURBINE,FALL_TURBINE,PRESSURE_TURBINE -> active=HydroGenerators.tick(this);
                case POWER_CONTROLLER -> active=ControlLinks.tick(this);
                case AUTO_CRANK -> active=ControlMachines.crank(this);
                case WIND_SMALL, WIND_MEDIUM, WIND_GIANT, WINDMILL, WIND_ROOF, WIND_MAST, WIND_PANEL -> active=WindPower.tick(this);
                case ARMOR_STATION, HANGAR -> active=ServiceMachines.tick(this);
                case SHIELD -> active=BaseControl.shieldTick(this);
                case LEVELER, GEOTHERMAL, ABYSSAL, ROVER_BASE, MORTAR -> active = FrontierMachines.tick(this);
            }
        }
        if(overheats())active=heat(active);
        if(!burnt&&overvolt>0&&status<=1)status=62;
        if(kind==Kind.LAMP&&!active)FloodlightBeam.clear(this);
        if (getBlockState().getValue(MachineBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(MachineBlock.ACTIVE, active), 3);
            if (kind == Kind.PRESENCE_SENSOR) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            if (kind == Kind.POTABILIZER) PotabilizerStructure.syncActive(this,active);
            if (kind.large()) LargeMachineStructure.syncActive(this,active);
        }
        if (beforeClean != clean.stored() || beforeDirty != dirty.stored() || beforeProgress != progress || beforeFilter != filterLeft || beforeWater != waterProfile) setChanged();
    }
    /** Heat bookkeeping every 10 ticks: warms while working, cools when idle (faster when switched off); burns out at 100%. */
    private boolean heat(boolean active){
        if(burnt){if(level instanceof net.minecraft.server.level.ServerLevel sl){var c=worldPosition.getCenter();sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,c.x,c.y+.6,c.z,2,.25,.1,.25,.01);if(clock%40==0)sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,c.x,c.y+.4,c.z,6,.3,.3,.3,.2);}return false;}
        int before=heatPercent();
        heat=Math.max(0,Math.min(HEAT_MAX,heat+(active?10:enabled?-30:-60)));
        int pct=heatPercent();
        if(pct!=before)setChanged();
        if(level instanceof net.minecraft.server.level.ServerLevel sl){
            var c=worldPosition.getCenter();
            if(pct>=75)sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,c.x,c.y+.6,c.z,pct>=90?4:1,.25,.1,.25,.01);
            if(pct>=90&&clock%40==0)level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.5F,1.6F);
        }
        if(active&&pct>=75&&status<=1)status=61;
        if(heat>=HEAT_MAX){
            burnt=true;setChanged();status=60;
            level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,1.8F);
            if(level instanceof net.minecraft.server.level.ServerLevel sl){var c=worldPosition.getCenter();sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,c.x,c.y+.5,c.z,8,.3,.2,.3,0);}
            return false;
        }
        return active;
    }
    private boolean generate() {
        boolean ocean = level.getBiome(worldPosition).is(BiomeTags.IS_OCEAN);
        int water = 0; boolean flowing = false;
        for (BlockPos p : List.of(worldPosition, worldPosition.north(), worldPosition.south(), worldPosition.east(), worldPosition.west(), worldPosition.above(), worldPosition.below())) {
            if (!level.hasChunkAt(p)) continue;
            var fluid = level.getFluidState(p);
            if (WaterOrigin.eligible(level,p)) {
                water++;
                if (!fluid.isSource() || fluid.getFlow(level, p).lengthSqr() > 0.001) flowing = true;
            }
        }
        // Export an existing buffer even if the water has just stopped.
        int exported = PowerNetwork.send(this, false, 2_000, Kind::core)
            + PowerNetwork.send(this, true, 2_000, k -> k == Kind.PURIFIER);
        lastRate = exported * 2;
        if (kind == Kind.SEA_TURBINE && !ocean) { status = 4; return false; }
        if (water < (kind == Kind.WATER_WHEEL ? 2 : 3)) { status = 3; return false; }
        if (kind == Kind.WATER_WHEEL && !flowing) { status = 9; return false; }
        EnergyTank tank = ocean || kind == Kind.SEA_TURBINE ? dirty : clean;
        int generated = tank.add(kind == Kind.SEA_TURBINE ? 1_200 : 400);
        status = generated > 0 ? 1 : 10;
        return generated > 0;
    }
    private boolean purify() {
        boolean industrial=getBlockState().getValue(MachineBlock.EXPANDED)||WirelessPower.hasTier(this,1);
        for(Direction side:Direction.values()) {
            BlockPos p=worldPosition.relative(side);
            if(level.hasChunkAt(p)&&level.getBlockState(p).getBlock() instanceof CableBlock cable&&cable.cyber)industrial=true;
        }
        lastRate = PowerNetwork.send(this,false,industrial?128_000:2_000,Kind::core)*2;
        int budget=industrial?80_000:600,converted=0;
        while(budget>=5) {
            if(dirty.stored()<5){status=converted>0?1:6;break;}
            if(clean.room()<4){status=converted>0?1:10;break;}
            if(filterLeft<=0) {
                ItemStack filter=inventory.getStackInSlot(0);
                if(!(filter.is(Items.CHARCOAL)||filter.is(Items.COAL))){status=5;break;}
                inventory.extractItem(0,1,false);filterLeft=24_000;
            }
            int used=dirty.purifyInto(clean,Math.min(budget,filterLeft));if(used==0)break;
            filterLeft-=used;budget-=used;converted+=used;status=1;
        }
        return converted>0;
    }
    private boolean process() {
        ItemStack input = inventory.getStackInSlot(0);
        ItemStack result = ItemStack.EMPTY;
        int duration = kind.duration();
        boolean consumesInput = kind != Kind.STONE_GENERATOR;
        int cost = kind == Kind.COOKER ? 100 : kind == Kind.STONE_GENERATOR ? 75 : kind == Kind.RECYCLER ? 50 : 200;
        if (kind == Kind.STONE_GENERATOR) {
            if (!hasWater()) { status = 3; return false; }
            result = new ItemStack(Items.COBBLESTONE);
        }
        if (!input.isEmpty()) {
            if (kind == Kind.FURNACE) {
                SingleRecipeInput recipeInput = new SingleRecipeInput(input);
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, level);
                if (recipe.isPresent()) result = recipe.get().value().assemble(recipeInput, level.registryAccess());
            } else if (kind == Kind.COOKER) {
                SingleRecipeInput recipeInput = new SingleRecipeInput(input);
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, recipeInput, level);
                if (recipe.isPresent()) result = recipe.get().value().assemble(recipeInput, level.registryAccess());
            } else if (kind == Kind.RECYCLER) {
                if (input.is(Items.ROTTEN_FLESH) || input.is(Items.KELP) || input.is(Items.WHEAT_SEEDS) || input.is(Items.PUMPKIN_SEEDS) || input.is(Items.MELON_SEEDS)) result = new ItemStack(Items.BONE_MEAL);
                else if (input.is(Items.GRAVEL)) result = new ItemStack(Items.FLINT);
                else if (input.is(Items.COBBLESTONE)) result = new ItemStack(Items.GRAVEL);
            } else if (kind == Kind.CRUSHER && input.is(Items.RAW_IRON)) result = new ItemStack(MareEletrica.IRON_GRIT.get(), 2);
            else if (kind == Kind.CRUSHER && input.is(Items.RAW_COPPER)) result = new ItemStack(MareEletrica.COPPER_GRIT.get(), 2);
            else if (kind == Kind.CRUSHER && input.is(Items.RAW_GOLD)) result = new ItemStack(MareEletrica.GOLD_GRIT.get(), 2);
        }
        if (result.isEmpty()) { progress = 0; status = 6; return false; }
        ItemStack output = inventory.getStackInSlot(8);
        if ((!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, result)) || output.getCount() + result.getCount() > result.getMaxStackSize()) {
            status = 7; return false;
        }
        if (!clean.pay(cost)) { status = 2; return false; }
        progress += 10; status = 1;
        if (progress >= duration) {
            if (consumesInput) inventory.extractItem(0, 1, false);
            if (output.isEmpty()) inventory.setStackInSlot(8, result.copy());
            else inventory.setStackInSlot(8, output.copyWithCount(output.getCount() + result.getCount()));
            progress = 0;
        }
        return true;
    }
    private boolean hasWater() {
        if (level.getFluidState(worldPosition).is(FluidTags.WATER)) return true;
        for (Direction direction : Direction.values()) {
            BlockPos pos = worldPosition.relative(direction);
            if (level.hasChunkAt(pos) && level.getFluidState(pos).is(FluidTags.WATER)) return true;
        }
        return false;
    }
    private boolean oxygen() {
        if (clean.stored() < 30) { status = 2; return false; }
        boolean worked = false;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(6), p -> p.isAlive() && !p.isSpectator() && p.isInWater())) {
            if (!clean.pay(30)) break;
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, false, false));
            worked = true;
        }
        if (worked) particles(ParticleTypes.BUBBLE, 8, 0.5);
        status = worked ? 1 : 0;
        return worked;
    }
    private boolean irrigate() {
        boolean water = level.getFluidState(worldPosition).is(FluidTags.WATER);
        for (Direction direction : Direction.values()) {
            BlockPos p = worldPosition.relative(direction);
            if (level.hasChunkAt(p) && level.getFluidState(p).is(FluidTags.WATER)) water = true;
        }
        if (!water) { status = 3; return false; }
        if (clean.stored() < 40) { status = 2; return false; }
        boolean worked = false;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-4, -2, -4), worldPosition.offset(4, 0, 4))) {
            if (!level.hasChunkAt(p)) continue;
            BlockState state = level.getBlockState(p);
            if (state.getBlock() instanceof FarmBlock && state.getValue(FarmBlock.MOISTURE) < 7 && clean.pay(5)) {
                level.setBlock(p, state.setValue(FarmBlock.MOISTURE, 7), 3); worked = true;
            }
        }
        // At most one random crop per two seconds. Do not scan or tick unloaded chunks.
        if (clock % 40 == 0 && clean.stored() >= 120) {
            for (int i = 0; i < 12; i++) {
                BlockPos p = worldPosition.offset(level.random.nextInt(9) - 4, level.random.nextInt(3) - 1, level.random.nextInt(9) - 4);
                if (!level.hasChunkAt(p)) continue;
                BlockState state = level.getBlockState(p);
                if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                    clean.pay(120); crop.performBonemeal((ServerLevel) level, level.random, p, state); worked = true; break;
                }
            }
        }
        if (worked) particles(ParticleTypes.SPLASH, 12, 2.0);
        status = worked ? 1 : 0;
        return worked;
    }
    private boolean collect() {
        if (clean.stored() < 5) { status = 2; return false; }
        boolean worked = false;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition).inflate(6), e -> e.isAlive() && !e.hasPickUpDelay())) {
            int allowed = Math.min(entity.getItem().getCount(), Math.min(64, clean.stored() / 5));
            if (allowed <= 0) break;
            ItemStack offer = entity.getItem().copyWithCount(allowed);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, offer, false);
            int inserted = allowed - remainder.getCount();
            if (inserted > 0) {
                clean.pay(inserted * 5);
                int left = entity.getItem().getCount() - inserted;
                if (left <= 0) entity.discard(); else entity.setItem(entity.getItem().copyWithCount(left));
                worked = true;
            }
        }
        status = worked ? 1 : 0;
        return worked;
    }
    private boolean heal() {
        if (clean.stored() < 400) { status = 2; return false; }
        if (clock % 40 != 0) {
            boolean recent = getBlockState().getValue(MachineBlock.ACTIVE);
            status = recent ? 1 : 0; return recent;
        }
        boolean worked = false;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(5), p -> p.isAlive() && !p.isSpectator() && p.getHealth() < p.getMaxHealth())) {
            if (!clean.pay(400)) break;
            player.heal(1F); worked = true;
        }
        if (worked) particles(ParticleTypes.HEART, 3, 0.4);
        status = worked ? 1 : 0; return worked;
    }
    private boolean lift() {
        if (clean.stored() < 40) { status = 2; return false; }
        boolean worked = false;
        AABB column = new AABB(worldPosition).inflate(0.35, 0, 0.35).expandTowards(0, 8, 0);
        for (Player player : level.getEntitiesOfClass(Player.class, column, p -> p.isAlive() && !p.isSpectator() && !p.isShiftKeyDown())) {
            if (!clean.pay(40)) break;
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
            worked = true;
        }
        if (worked) particles(ParticleTypes.CLOUD, 8, 0.3);
        status = worked ? 1 : 0; return worked;
    }
    private boolean repel() {
        if (clean.stored() < 80) { status = 2; return false; }
        boolean worked = false;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(worldPosition).inflate(6), m -> m instanceof Enemy && m.isAlive())) {
            Vec3 away = mob.position().subtract(Vec3.atCenterOf(worldPosition));
            if (away.lengthSqr() > 36 || !clean.pay(80)) continue;
            Vec3 push = new Vec3(away.x, 0, away.z).normalize().scale(0.7);
            mob.setDeltaMovement(mob.getDeltaMovement().add(push.x, 0.12, push.z)); mob.hurtMarked = true;
            worked = true;
        }
        if (worked) particles(ParticleTypes.ELECTRIC_SPARK, 8, 0.9);
        status = worked ? 1 : 0; return worked;
    }
    private void particles(net.minecraft.core.particles.SimpleParticleType particle, int count, double spread) {
        if (level instanceof ServerLevel server) server.sendParticles(particle, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, count, spread, 0.25, spread, 0.03);
    }
    @Override public Component getDisplayName() { return Component.translatable("block.mareeletrica." + kind.id); }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {return saveWithoutMetadata(registries);}
    public void syncAim() {setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}
    @Override public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        if(kind==Kind.BUILDER && !player.level().isClientSide) {
            for(int i=0;i<9;i++) { ItemStack old=inventory.extractItem(i,64,false);if(!old.isEmpty())playerInventory.placeItemBackInInventory(old); }
        }
        return kind==Kind.QUARRY||kind==Kind.EXTERMINATOR ? new ServiceMenu(id,playerInventory,this) : kind == Kind.MORTAR ? new MortarMenu(id,playerInventory,this) : kind == Kind.BUILDER ? new ConstructionMenu(id,playerInventory,this) : new MachineMenu(id, playerInventory, this);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);tag.put("WirelessPlates",WirelessPower.save(this));
        tag.putInt("Clean", clean.stored()); tag.putInt("Dirty", dirty.stored());
        tag.putInt("Progress", progress); tag.putInt("Filter", filterLeft); tag.putBoolean("Enabled", enabled);tag.putInt("Heat",heat);tag.putBoolean("Burnt",burnt);tag.putInt("Overvolt",overvolt);
        tag.put("Inventory", inventory.serializeNBT(registries));tag.put("GasInventory",gasInventory.serializeNBT(registries));tag.putInt("BatchRemaining",batchRemaining);tag.putInt("BatchTotal",batchTotal);tag.putInt("BatchCount",batchCount);tag.putInt("PendingOre",pendingOre);
        tag.putInt("WorkCursor", workCursor); tag.putInt("StoredXp", storedXp);
        tag.putInt("Channel", channel); tag.putBoolean("Scanning", scanning); tag.putInt("ScanIndex", scanIndex);
        if (orePosition != null) tag.putLong("OrePosition", orePosition.asLong());
        tag.putInt("OreStateId", oreStateId); tag.putLong("WeatherCooldown", weatherCooldown);
        tag.putInt("WaterProfile",waterProfile);tag.putInt("Drive",drive);tag.putBoolean("Crank",crank);tag.put("Links",ControlLinks.save(links));tag.putInt("CtrlLimit",ctrlLimit);tag.putInt("CtrlOn",ctrlOn);tag.putInt("CtrlOff",ctrlOff);
        tag.putInt("Blueprint",blueprint);tag.put("ProjectionEditor",editor.save());
        tag.putInt("ProjectionDistance",projectionDistance);tag.putBoolean("Projecting",projecting);
        tag.putFloat("LampYaw",lampYaw);tag.putFloat("LampPitch",lampPitch);
        tag.putIntArray("MortarElevation",mortarElevation);tag.putIntArray("MortarAzimuth",mortarAzimuth);
        if(roverPosition!=null)tag.putLong("RoverPosition",roverPosition.asLong());
        tag.putLong("MortarAt",mortarAt);tag.putBoolean("LastRedstone",lastRedstone);
        if(roverId!=null)tag.putUUID("RoverId",roverId);
        if(owner!=null)tag.putUUID("Owner",owner);
        tag.putInt("RoverHeading",roverHeading);tag.putInt("RoverScans",roverScans);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);WirelessPower.load(this,tag.getList("WirelessPlates",net.minecraft.nbt.Tag.TAG_COMPOUND));
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));if(tag.contains("GasInventory"))gasInventory.deserializeNBT(registries,tag.getCompound("GasInventory"));batchRemaining=Math.max(0,Math.min(1100,tag.getInt("BatchRemaining")));batchTotal=Math.max(batchRemaining,Math.min(1100,tag.getInt("BatchTotal")));batchCount=Math.max(0,tag.getInt("BatchCount"));pendingOre=tag.contains("PendingOre")?Math.max(-1,Math.min(6,tag.getInt("PendingOre"))):-1;
        for (int i = 0; i < previousInputs.length; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            previousInputs[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        }
        clean.load(tag.getInt("Clean")); dirty.load(tag.getInt("Dirty"));drive=tag.getInt("Drive");crank=tag.getBoolean("Crank");ControlLinks.load(links,tag.getList("Links",net.minecraft.nbt.Tag.TAG_COMPOUND));ctrlLimit=tag.contains("CtrlLimit")?tag.getInt("CtrlLimit"):100;ctrlOn=tag.getInt("CtrlOn");ctrlOff=tag.getInt("CtrlOff");
        progress = Math.max(0, Math.min(kind == Kind.FISHER ? 400 : kind == Kind.GREENHOUSE || kind == Kind.GAS_REFILL ? 100 : 80, tag.getInt("Progress")));
        filterLeft = Math.max(0, Math.min(kind == Kind.POTABILIZER ? PotableWaterCycle.FILTER_USES : 24_000, tag.getInt("Filter")));
        blueprint = Math.floorMod(tag.getInt("Blueprint"),Blueprints.ALL.size());editor.load(tag.getCompound("ProjectionEditor"));
        projectionDistance=Math.max(2,Math.min(128,tag.contains("ProjectionDistance")?tag.getInt("ProjectionDistance"):2));
        if(tag.contains("LampYaw")){lampYaw=tag.getFloat("LampYaw");lampPitch=tag.getFloat("LampPitch");}
        int[] elevations=tag.getIntArray("MortarElevation"), azimuths=tag.getIntArray("MortarAzimuth");
        for(int i=0;i<6;i++){mortarElevation[i]=i<elevations.length?MortarAim.elevation(elevations[i]):70;mortarAzimuth[i]=i<azimuths.length?MortarAim.azimuth(azimuths[i]):0;}
        roverPosition=tag.contains("RoverPosition")?BlockPos.of(tag.getLong("RoverPosition")):null;
        projecting=tag.getBoolean("Projecting");mortarAt=tag.getLong("MortarAt");lastRedstone=tag.getBoolean("LastRedstone");
        roverId=tag.hasUUID("RoverId")?tag.getUUID("RoverId"):null;
        owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;
        roverHeading=Math.floorMod(tag.getInt("RoverHeading"),4);roverScans=Math.max(0,tag.getInt("RoverScans"));
        waterProfile = Math.max(0, Math.min(2,tag.getInt("WaterProfile")));
        heat=tag.getInt("Heat");burnt=tag.getBoolean("Burnt");overvolt=tag.getInt("Overvolt");
        enabled = tag.contains("Enabled") ? tag.getBoolean("Enabled") : kind != Kind.QUARRY && kind != Kind.LEVELER && kind != Kind.ROVER_BASE && kind!=Kind.EXTERMINATOR;
        workCursor = Math.max(0, tag.getInt("WorkCursor")); storedXp = Math.max(0, Math.min(10000, tag.getInt("StoredXp")));
        channel = Math.floorMod(tag.getInt("Channel"), 16); scanning = tag.getBoolean("Scanning");
        scanIndex = Math.max(0, Math.min(20000, tag.getInt("ScanIndex")));
        orePosition = tag.contains("OrePosition") ? BlockPos.of(tag.getLong("OrePosition")) : null;
        oreStateId = Math.max(0, tag.getInt("OreStateId")); weatherCooldown = tag.getLong("WeatherCooldown");
    }
}
