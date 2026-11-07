package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Ground navigation, real wheel travel and a persistent four-direction recharge cycle. */
public final class ExplorerEntity extends PathfinderMob implements net.minecraft.world.MenuProvider {
    public static final int CAPACITY=100_000;
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(ExplorerEntity.class,EntityDataSerializers.INT);
    public java.util.UUID controller;
    public int ammunition,gasTicks,reloadDelay;
    public boolean flightRequested;public float climb;private boolean droppedCargo;
    private static final EntityDataAccessor<Integer> FUEL=SynchedEntityData.defineId(ExplorerEntity.class,EntityDataSerializers.INT);
    public int fuelSeconds(){return entityData.get(FUEL)/20;}
    public final net.neoforged.neoforge.items.ItemStackHandler cargo=new net.neoforged.neoforge.items.ItemStackHandler(5){public boolean isItemValid(int slot,net.minecraft.world.item.ItemStack stack){return slot>=2||stack.is(slot==0?MareEletrica.ROVER_BULLET.get():MareEletrica.GAS.get());}};
    public final net.minecraft.world.inventory.ContainerData menuData=new net.minecraft.world.inventory.ContainerData(){public int get(int i){return switch(i){case 0->battery;case 1->ammunition;case 2->gasTicks;default->mode();};}public void set(int i,int v){}public int getCount(){return 4;}};
    public void toggleFlight(){if(flightRequested){flightRequested=false;return;}if(gasTicks>0||cargo.getStackInSlot(1).is(MareEletrica.GAS.get()))flightRequested=true;}
    @Override public net.minecraft.network.chat.Component getDisplayName(){return net.minecraft.network.chat.Component.literal("Explorador");}
    @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,net.minecraft.world.entity.player.Inventory inv,net.minecraft.world.entity.player.Player p){return new ExplorerMenu(id,inv,this);}
    public void openCargo(net.minecraft.server.level.ServerPlayer player){player.openMenu(this,b->b.writeVarInt(getId()));}
    private void dropCargo(){if(droppedCargo||level().isClientSide)return;droppedCargo=true;for(int i=0;i<5;i++){var stack=cargo.extractItem(i,64,false);if(!stack.isEmpty())spawnAtLocation(stack);}while(ammunition>0){int n=Math.min(64,ammunition);ammunition-=n;spawnAtLocation(new net.minecraft.world.item.ItemStack(MareEletrica.ROVER_BULLET.get(),n));}}
    @Override public void remove(net.minecraft.world.entity.Entity.RemovalReason reason){if(reason==net.minecraft.world.entity.Entity.RemovalReason.DISCARDED||reason==net.minecraft.world.entity.Entity.RemovalReason.KILLED)dropCargo();super.remove(reason);}
    public float driveForward,driveYaw,drivePitch,wingFold,previousWingFold,boatFold,previousBoatFold;
    public boolean trigger;
    private int shotCooldown;
    private Vec3 autoTarget;
    public int mode(){return entityData.get(MODE);}
    public boolean manual(){return controller!=null;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MODE,VehicleRules.LAND);builder.define(FUEL,0);}
    public void control(java.util.UUID player){controller=player;phase=5;driveYaw=getYRot();drivePitch=0;driveForward=0;trigger=false;getNavigation().stop();}
    public void release(){flightRequested=false;climb=0;controller=null;driveForward=0;trigger=false;phase=2;stuck=0;getNavigation().stop();}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){fallDistance=0;return false;}
    @Override protected int decreaseAirSupply(int air){return getMaxAirSupply();}
    @Override public boolean isPushedByFluid(){return false;}
    public BlockPos home=BlockPos.ZERO;
    /** The dock's owner decides over ammunition and gas. */
    public java.util.UUID owner;
    public boolean isOwner(net.minecraft.world.entity.player.Player p){
        var m=level().hasChunkAt(home)&&level().getBlockEntity(home) instanceof MachineEntity me?me:null;
        if(m!=null&&m.owner!=null)owner=m.owner;   // remember the dock owner, also for when the dock is far away
        if(m!=null)return m.isOwner(p);
        return owner!=null&&owner.equals(p.getUUID());
    }
    public int battery,phase,trip,scans;
    private int waypoint=8,stuck,scanCursor;
    private double lastX,lastZ;
    public float wheelAngle,wheelStep;
    public ExplorerEntity(EntityType<? extends ExplorerEntity> type,Level level) {
        super(type,level);setPersistenceRequired();setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0F);getNavigation().setCanFloat(true);
    }
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(MareEletrica.EXPLORER.get(),createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.ARMOR,8)
            .add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,24).add(Attributes.STEP_HEIGHT,1.05).build());
    }
    @Override protected void registerGoals() {}
    public void returnHome() {if(manual() || phase==0)return;phase=2;stuck=0;getNavigation().stop();}
    private MachineEntity base(ServerLevel level) {
        return level.hasChunkAt(home)&&level.getBlockEntity(home) instanceof MachineEntity m&&m.kind==Kind.ROVER_BASE?m:null;
    }
    public static BlockPos dock(MachineEntity m) {Direction front=m.getBlockState().getValue(MachineBlock.FACING);return m.getBlockPos().relative(front,2).relative(front.getClockWise(),m.getBlockState().getValue(MachineBlock.EXPANDED)?1:0);}
    @Override public void tick() {
        if(!level().isClientSide) {
            if(reloadDelay>0)reloadDelay--;
            if(reloadDelay==0&&tickCount%10==0&&cargo.getStackInSlot(0).is(MareEletrica.ROVER_BULLET.get())&&ammunition<VehicleRules.MAGAZINE)ammunition+=cargo.extractItem(0,VehicleRules.MAGAZINE-ammunition,false).getCount();
            boolean flight=manual()&&flightRequested&&!isInWater()&&battery>0;
            if(flight&&gasTicks<=0&&cargo.getStackInSlot(1).is(MareEletrica.GAS.get())){cargo.extractItem(1,1,false);var empty=new net.minecraft.world.item.ItemStack(MareEletrica.EMPTY_GAS.get());for(int slot=2;slot<5&&!empty.isEmpty();slot++)empty=cargo.insertItem(slot,empty,false);if(!empty.isEmpty())spawnAtLocation(empty);gasTicks=600;}
            if(flight&&gasTicks>0){gasTicks--;entityData.set(MODE,VehicleRules.PLANE);}else{flightRequested=false;entityData.set(MODE,VehicleRules.mode(isInWater(),onGround(),getDeltaMovement().y,fallDistance,mode()==VehicleRules.PLANE?VehicleRules.GLIDER:mode()));}
            entityData.set(FUEL,gasTicks);
            if(manual()){getNavigation().stop();setYRot(VehicleRules.turn(getYRot(),driveYaw));setXRot(VehicleRules.pitch(drivePitch));}
            else if((mode()==VehicleRules.BOAT || mode()==VehicleRules.GLIDER) && autoTarget!=null) {Vec3 delta=autoTarget.subtract(position());setYRot(VehicleRules.turn(getYRot(),(float)Math.toDegrees(Math.atan2(-delta.x,delta.z))));}
            yBodyRot=getYRot();yHeadRot=getYRot();
        }
        super.tick();previousWingFold=wingFold;wingFold+=((mode()==VehicleRules.GLIDER||mode()==VehicleRules.PLANE?1:0)-wingFold)*.3F;
        previousBoatFold=boatFold;boatFold+=((mode()==VehicleRules.BOAT?1:0)-boatFold)*.25F;
        double moved=Math.sqrt((getX()-xo)*(getX()-xo)+(getZ()-zo)*(getZ()-zo));wheelStep=(float)(moved*190);wheelAngle=(wheelAngle+wheelStep)%360;
        if(!(level() instanceof ServerLevel server))return;
        MachineEntity m=base(server);
        if(m==null) {getNavigation().stop();if(server.hasChunkAt(home))discard();return;}
        if(m.roverId==null||!getUUID().equals(m.roverId)){discard();return;}
        if(tickCount%20==0){m.roverPosition=blockPosition();m.roverBattery=battery;m.roverState=phase;m.setChanged();}
        if(shotCooldown>0)shotCooldown--;
        if(manual()){battery=Math.max(0,battery-(Math.abs(driveForward)>.01?20:5));if(trigger)fire(server);return;}
        if(!m.enabled||server.hasNeighborSignal(home)) {if(phase==1)returnHome();if(phase==0){getNavigation().stop();return;}}
        BlockPos dock=dock(m);
        if(phase==0) {
            getNavigation().stop();if(tickCount%10!=0)return;
            var supply=m.inventory.getStackInSlot(0);
            if(supply.is(MareEletrica.ROVER_BULLET.get()))ammunition+=m.inventory.extractItem(0,Math.min(32,VehicleRules.MAGAZINE-ammunition),false).getCount();
            int accepted=Math.min(5000,Math.min(CAPACITY-battery,m.clean.stored()));m.clean.take(accepted);battery+=accepted;m.setChanged();
            m.roverBattery=battery;m.roverState=0;
            if(battery>=CAPACITY&&m.enabled&&!server.hasNeighborSignal(home)) {phase=1;trip=m.roverHeading;waypoint=8;stuck=0;}
            return;
        }
        if(battery>0)battery=Math.max(0,battery-5);else {getNavigation().stop();phase=3;}
        if(phase==3)return;
        if(phase==1&&battery<25_000)returnHome();
        if(tickCount%20==0) {
            double displacement=(getX()-lastX)*(getX()-lastX)+(getZ()-lastZ)*(getZ()-lastZ);lastX=getX();lastZ=getZ();
            stuck=displacement<.04?stuck+20:0;
            if(stuck>=200&&phase==1)returnHome();else if(stuck>=400&&phase==2){phase=3;getNavigation().stop();}
            if(phase==1)scan(server,m);
            m.roverBattery=battery;m.roverState=phase;m.roverScans=scans;m.setChanged();
        }
        if(tickCount%20!=0||phase==3)return;
        BlockPos target;
        if(phase==1) {
            int[] dir=WorkArea.direction(trip);
            if(waypoint>96) {returnHome();return;}
            int x=home.getX()+dir[0]*waypoint,z=home.getZ()+dir[1]*waypoint;
            BlockPos column=new BlockPos(x,home.getY(),z);
            if(!server.hasChunkAt(column)) {returnHome();return;}
            int y=server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);target=new BlockPos(x,y,z);
            if(Math.abs(getX()-(x+.5))+Math.abs(getZ()-(z+.5))<3) {waypoint+=8;return;}
        } else {
            target=dock;
            if(distanceToSqr(dock.getCenter())<6.25) {phase=0;stuck=0;m.roverHeading=WorkArea.heading(trip+1);m.roverState=0;m.setChanged();return;}
            // Return by nearby waypoints so one search never spans the entire expedition.
            double dx=target.getX()+.5-getX(),dz=target.getZ()+.5-getZ(),length=Math.sqrt(dx*dx+dz*dz);
            if(length>10) {
                int x=(int)Math.floor(getX()+dx/length*8),z=(int)Math.floor(getZ()+dz/length*8);
                if(!server.hasChunkAt(new BlockPos(x,blockPosition().getY(),z))){getNavigation().stop();return;}
                target=new BlockPos(x,server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
            }
        }
        autoTarget=target.getCenter();
        if(mode()==VehicleRules.BOAT || mode()==VehicleRules.GLIDER)return;
        BlockPos here=blockPosition();
        if(!server.hasChunksAt(new BlockPos(Math.min(here.getX(),target.getX())-16,here.getY(),Math.min(here.getZ(),target.getZ())-16),
            new BlockPos(Math.max(here.getX(),target.getX())+16,here.getY(),Math.max(here.getZ(),target.getZ())+16))) {if(phase==1)returnHome();else getNavigation().stop();return;}
        if(!getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1.0)&&phase==1)returnHome();
    }
    @Override public void travel(Vec3 input) {
        if(level().isClientSide || (!manual() && mode()==VehicleRules.LAND)){super.travel(input);return;}
        Vec3 old=getDeltaMovement();double forward=manual()?VehicleRules.axis(driveForward):(phase==1 || phase==2)?.8:0;
        if(battery<=0)forward=0;double speed=mode()==VehicleRules.PLANE?.95:mode()==VehicleRules.BOAT?.48:mode()==VehicleRules.GLIDER?.45:.34;
        if(mode()==VehicleRules.GLIDER||mode()==VehicleRules.PLANE)forward=Math.max(.35,forward);
        double radians=Math.toRadians(getYRot()),dx=-Math.sin(radians)*speed*forward,dz=Math.cos(radians)*speed*forward,dy;
        if(mode()==VehicleRules.PLANE){dy=climb*.42-Math.sin(Math.toRadians(drivePitch))*.45;fallDistance=0;}else if(mode()==VehicleRules.BOAT){
            dy=Math.max(-.04,Math.min(.12,(getFluidHeight(FluidTags.WATER)-.35)*.12));fallDistance=0;
            BlockPos shore=BlockPos.containing(getX()-Math.sin(radians)*Math.signum(forward)*1.1,getY(),getZ()+Math.cos(radians)*Math.signum(forward)*1.1);
            if(forward!=0&&horizontalCollision&&!level().getBlockState(shore).getCollisionShape(level(),shore).isEmpty()&&level().getBlockState(shore.above()).isAir()&&level().getBlockState(shore.above(2)).isAir())dy=.28;
        }else if(mode()==VehicleRules.GLIDER){dy=Math.max(-.085,old.y*.92);fallDistance=0;}
        else dy=onGround()?Math.min(0,old.y):(old.y-.08)*.98;
        Vec3 next=new Vec3(old.x*.35+dx*.65,dy,old.z*.35+dz*.65);BlockPos ahead=BlockPos.containing(getX()+next.x,getY(),getZ()+next.z);
        if(!level().hasChunkAt(ahead) || !level().getWorldBorder().isWithinBounds(ahead))next=new Vec3(0,dy,0);
        setDeltaMovement(next);move(MoverType.SELF,next);if(horizontalCollision)setDeltaMovement(0,getDeltaMovement().y,0);calculateEntityAnimation(false);
    }
    private void fire(ServerLevel level) {
        if(!VehicleRules.canShoot(manual(),ammunition,battery,shotCooldown))return;
        var player=level.getServer().getPlayerList().getPlayer(controller);if(player==null || !RemoteSessions.controls(player,this))return;
        Vec3 direction=Vec3.directionFromRotation(0,getYRot());
        RoverBulletEntity bullet=new RoverBulletEntity(MareEletrica.BULLET.get(),level);bullet.setOwner(player);bullet.roverId=getId();
        Vec3 muzzle=getEyePosition().add(direction.scale(1.1));bullet.setPos(muzzle.x,muzzle.y,muzzle.z);bullet.setDeltaMovement(direction.scale(6.0));
        if(level.addFreshEntity(bullet)){ammunition--;battery-=VehicleRules.SHOT_ENERGY;shotCooldown=VehicleRules.SHOT_DELAY;
            level.playSound(null,blockPosition(),net.minecraft.sounds.SoundEvents.DISPENSER_LAUNCH,net.minecraft.sounds.SoundSource.NEUTRAL,.6F,1.6F);}
    }
    private void scan(ServerLevel level,MachineEntity m) {
        if(battery<100)return;battery-=100;
        for(int i=0;i<32;i++) {
            int n=Math.floorMod(scanCursor++,9*9*13);
            BlockPos p=blockPosition().offset(n%9-4,-(n/81),n/9%9-4);
            if(!level.hasChunkAt(p)||level.isOutsideBuildHeight(p))continue;scans++;
            var state=level.getBlockState(p);
            if(AdvancedMachines.isOre(state)){m.orePosition=p;m.oreStateId=net.minecraft.world.level.block.Block.getId(state);}
        }
    }
    @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(!stack.is(MareEletrica.PORTABLE_BATTERY.get())){if(player instanceof net.minecraft.server.level.ServerPlayer sp)openCargo(sp);return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);}
        if(!stack.is(MareEletrica.PORTABLE_BATTERY.get()))return super.mobInteract(player,hand);
        if(!level().isClientSide) {
            var power=PortableBatteryItem.energy(stack);int amount=power.extractEnergy(Math.min(10_000,CAPACITY-battery),false);battery+=amount;
            if(amount>0&&phase==3)returnHome();
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public void die(DamageSource source) {
        if(level() instanceof ServerLevel server) {MachineEntity m=base(server);if(m!=null&&getUUID().equals(m.roverId)){m.roverId=null;m.setChanged();}}
        dropCargo();super.die(source);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.put("Cargo",cargo.serializeNBT(registryAccess()));tag.putInt("GasTicks",gasTicks);tag.putInt("Ammunition",ammunition);tag.putLong("Home",home.asLong());tag.putInt("Battery",battery);tag.putInt("Phase",phase);tag.putInt("Trip",trip);
        if(owner!=null)tag.putUUID("Owner",owner);tag.putInt("Waypoint",waypoint);tag.putInt("Scans",scans);tag.putInt("ScanCursor",scanCursor);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);if(tag.contains("Cargo"))cargo.deserializeNBT(registryAccess(),tag.getCompound("Cargo"));gasTicks=Math.max(0,Math.min(600,tag.getInt("GasTicks")));home=BlockPos.of(tag.getLong("Home"));battery=Math.max(0,Math.min(CAPACITY,tag.getInt("Battery")));
        ammunition=Math.max(0,Math.min(VehicleRules.MAGAZINE,tag.getInt("Ammunition")));phase=tag.getInt("Phase")==5?2:Math.max(0,Math.min(3,tag.getInt("Phase")));trip=WorkArea.heading(tag.getInt("Trip"));waypoint=Math.max(8,Math.min(104,tag.getInt("Waypoint")));
        owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;scans=Math.max(0,tag.getInt("Scans"));scanCursor=Math.floorMod(tag.getInt("ScanCursor"),1053);
    }
}
