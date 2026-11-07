package br.mareeletrica;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class FrontierMachines {
    private FrontierMachines() {}
    public static boolean tick(MachineEntity m) {
        if(!(m.getLevel() instanceof ServerLevel level))return false;
        return switch(m.kind){case GEOTHERMAL,ABYSSAL->generate(m,level);case LEVELER->level(m,level);case ROVER_BASE->rover(m,level);case MORTAR->{m.status=m.mortarAt>0?46:47;yield m.mortarAt>0;}default->false;};
    }
    private static boolean generate(MachineEntity m,ServerLevel level) {
        boolean ocean=m.kind==Kind.ABYSSAL;
        int sent=PowerNetwork.send(m,ocean,128_000,k->ocean?k==Kind.PURIFIER:k.core());m.lastRate=sent*2;
        int sources=0;
        for(int i=0;i<9;i++) {
            BlockPos p=LargeMachineStructure.position(m,i).below();
            if(!level.hasChunkAt(p))continue;
            if(!ocean&&(level.getBlockState(p).is(Blocks.MAGMA_BLOCK)||level.getFluidState(p).is(FluidTags.LAVA)))sources++;
        }
        if(ocean) {
            Direction front=m.getBlockState().getValue(MachineBlock.FACING),side=front.getClockWise();
            for(int i=0;i<3;i++) {BlockPos p=m.getBlockPos().relative(side,i).relative(front);if(WaterOrigin.eligible(level,p)&&level.getFluidState(p).isSource())sources++;}
            if(!level.getBiome(m.getBlockPos()).is(BiomeTags.IS_OCEAN)) {m.status=4;return false;}
        }
        if(sources<(ocean?3:4)) {m.status=ocean?44:43;return false;}
        EnergyTank tank=ocean?m.dirty:m.clean;int amount=ocean?80_000:10_000;
        int made=tank.add(amount);m.status=made>0?1:10;return made>0||sent>0;
    }
    private static boolean fits(MachineEntity m,List<ItemStack> drops) {
        var copy=new ItemStackHandler(9);for(int i=0;i<9;i++)copy.setStackInSlot(i,m.inventory.getStackInSlot(i).copy());
        for(var stack:drops)if(!ItemHandlerHelper.insertItemStacked(copy,stack.copy(),false).isEmpty())return false;return true;
    }
    private static boolean level(MachineEntity m,ServerLevel level) {
        int broken=0;m.setChanged();
        for(int inspected=0;inspected<128&&broken<8&&m.scanIndex<WorkArea.CELLS;inspected++) {
            int[] o=WorkArea.offset(m.scanIndex);BlockPos pos=m.getBlockPos().offset(o[0],o[1],o[2]);
            if(!level.hasChunkAt(pos)) {m.status=30;return broken>0;}
            BlockState state=level.getBlockState(pos);
            if(pos.equals(m.getBlockPos())||level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)
                ||state.isAir()||state.getDestroySpeed(level,pos)<0||level.getBlockEntity(pos)!=null||!level.getFluidState(pos).isEmpty()) {m.scanIndex++;continue;}
            if(m.clean.stored()<10_000) {m.status=2;return broken>0;}
            var actor=FakePlayerFactory.getMinecraft(level);ItemStack pick=new ItemStack(Items.NETHERITE_PICKAXE);
            var hand=actor.getMainHandItem();var location=actor.position();List<ItemStack> drops;boolean denied;
            try {
                actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,pick);actor.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
                denied=!level.mayInteract(actor,pos)||NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,state,actor)).isCanceled();
                drops=denied?List.of():Block.getDrops(state,level,pos,null,actor,pick);
            } finally {actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,hand);actor.setPos(location.x,location.y,location.z);}
            if(denied) {m.scanIndex++;m.status=37;continue;}
            if(!fits(m,drops)) {m.status=7;return broken>0;}
            if(level.getBlockState(pos).equals(state)&&level.removeBlock(pos,false)) {
                m.clean.pay(10_000);for(var drop:drops)ItemHandlerHelper.insertItemStacked(m.inventory,drop,false);broken++;
            }
            m.scanIndex++;
        }
        m.lastRate=broken*20_000;m.status=m.scanIndex>=WorkArea.CELLS?45:42;m.setChanged();return broken>0;
    }
    private static boolean rover(MachineEntity m,ServerLevel level) {
        ExplorerEntity explorer=m.roverId==null?null:level.getEntity(m.roverId) instanceof ExplorerEntity r?r:null;
        if(m.roverId!=null&&explorer==null) {m.roverState=4;m.status=50;return false;}
        if(explorer==null) {
            BlockPos spawn=ExplorerEntity.dock(m);
            if(!level.hasChunkAt(spawn)||!level.getBlockState(spawn).isAir()||!level.getBlockState(spawn.above()).isAir()) {m.status=18;return false;}
            explorer=new ExplorerEntity(MareEletrica.EXPLORER.get(),level);explorer.setPos(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5);explorer.home=m.getBlockPos();
            if(!level.noCollision(explorer)||!level.addFreshEntity(explorer)) {m.status=18;return false;}
            m.roverId=explorer.getUUID();m.roverPosition=spawn;m.setChanged();
        }
        m.roverPosition=explorer.blockPosition();m.roverBattery=explorer.battery;m.roverState=explorer.phase;m.roverScans=explorer.scans;m.status=49;
        return explorer.phase!=0;
    }
    public static boolean action(MachineEntity m,Player player) {
        if(m.kind==Kind.LEVELER) {m.scanIndex=0;m.setChanged();return true;}
        if(m.kind==Kind.MORTAR) {return arm(m,player);}
        if(m.kind==Kind.ROVER_BASE&&m.getLevel() instanceof ServerLevel level) {
            if(m.roverId!=null&&level.getEntity(m.roverId) instanceof ExplorerEntity explorer)explorer.returnHome();
            else player.displayClientMessage(Component.translatable("gui.mareeletrica.rover_wait"),true);
            return true;
        }
        return false;
    }
    private static boolean arm(MachineEntity m,Player player) {
        if(!(m.getLevel() instanceof ServerLevel level)||!m.enabled||m.mortarAt>0)return false;
        if(!m.inventory.getStackInSlot(0).is(MareEletrica.CANNONBALL.get())||m.inventory.getStackInSlot(0).getCount()<6||m.clean.stored()<6000) {
            m.status=51;if(player!=null)player.displayClientMessage(Component.translatable("gui.mareeletrica.mortar_ammo"),true);return true;
        }
        if(LargeMachineStructure.integrity(m)!=1)return false;
        m.mortarAt=level.getGameTime()+40;m.setChanged();return true;
    }
    public static void mortarClock(MachineEntity m) {
        if(!(m.getLevel() instanceof ServerLevel level))return;
        boolean signal=level.hasNeighborSignal(m.getBlockPos());
        if(signal&&!m.lastRedstone)arm(m,null);if(signal!=m.lastRedstone){m.lastRedstone=signal;m.setChanged();}
        if(m.mortarAt==0||level.getGameTime()<m.mortarAt)return;
        m.mortarAt=0;m.setChanged();
        if(!m.enabled||LargeMachineStructure.integrity(m)!=1||m.clean.stored()<6000||!m.inventory.getStackInSlot(0).is(MareEletrica.CANNONBALL.get())||m.inventory.getStackInSlot(0).getCount()<6)return;
        Direction front=m.getBlockState().getValue(MachineBlock.FACING),side=front.getClockWise();
        for(int i=0;i<6;i++) {
            BlockPos cell=m.getBlockPos().relative(side,i%3).relative(front.getOpposite(),i/3+1);
            double[] aim=MortarAim.direction(m.mortarElevation[i],m.mortarAzimuth[i],front.getStepX(),front.getStepZ());
            CannonballEntity shell=new CannonballEntity(MareEletrica.SHELL.get(),level);
            shell.setPos(cell.getX()+.5+aim[0]*MortarAim.BARREL_LENGTH,cell.getY()+.55+aim[1]*MortarAim.BARREL_LENGTH,cell.getZ()+.5+aim[2]*MortarAim.BARREL_LENGTH);
            shell.setDeltaMovement(aim[0]*MortarAim.SPEED,aim[1]*MortarAim.SPEED,aim[2]*MortarAim.SPEED);
            level.addFreshEntity(shell);level.sendParticles(ParticleTypes.FLAME,shell.getX(),shell.getY(),shell.getZ(),8,.15,.15,.15,.03);
        }
        m.inventory.extractItem(0,6,false);m.clean.pay(6000);
        level.playSound(null,m.getBlockPos(),net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),net.minecraft.sounds.SoundSource.BLOCKS,1.2F,.65F);
    }
}
