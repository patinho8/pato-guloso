package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/** Blue energy projections: bounded work per tick, no materials and no permanent blocks. */
public final class ConstructionProjector {
    private ConstructionProjector() {}
    private static BlockPos position(MachineEntity m,ProjectionPlans.Plan p,BlockPos cell) {
        Direction front=m.getBlockState().getValue(MachineBlock.FACING);
        return m.getBlockPos().relative(front,m.projectionDistance+cell.getZ()).relative(front.getClockWise(),cell.getX()-p.width()/2).above(cell.getY());
    }
    private static Rotation rotation(MachineEntity m) {
        return switch(m.getBlockState().getValue(MachineBlock.FACING)){case EAST->Rotation.CLOCKWISE_90;case SOUTH->Rotation.CLOCKWISE_180;case WEST->Rotation.COUNTERCLOCKWISE_90;default->Rotation.NONE;};
    }
    public static void pause(MachineEntity m) {
        if(m.getLevel() instanceof ServerLevel level)HologramIndex.get(level).stop(m.getBlockPos());
        m.projectionStamp=0;m.projectionCursor=0;
    }
    public static void stop(MachineEntity m) {pause(m);m.projecting=false;m.setChanged();}
    public static boolean tick(MachineEntity m) {
        if(!(m.getLevel() instanceof ServerLevel level))return false;
        var p=m.editor.plan(m);
        m.projectionWidth=p.width();m.projectionHeight=p.height();m.projectionDepth=p.depth();m.projectionTotal=p.cells().size();m.projectionRate=p.rate();
        if(p.cells().isEmpty()) {m.status=48;return false;}
        if(m.previewUntil>level.getGameTime())for(int x:new int[]{0,p.width()-1})for(int z:new int[]{0,p.depth()-1})for(int y=0;y<p.height();y+=2) {
            BlockPos pos=position(m,p,new BlockPos(x,y,z));if(level.hasChunkAt(pos))level.sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,1,0,0,0,0);
        }
        if(!m.projecting) {m.status=39;return false;}
        if(!m.clean.pay(p.rate()/2)) {pause(m);m.status=2;return false;}
        if(m.projectionStamp==0)m.projectionStamp=HologramIndex.get(level).nextStamp();
        var index=HologramIndex.get(level);index.renew(m.getBlockPos(),m.projectionStamp,level.getGameTime());
        m.lastRate=p.rate();m.status=40;
        var actor=FakePlayerFactory.getMinecraft(level);
        for(int n=0;n<256 && n<p.cells().size();n++) {
            var cell=p.cells().get(m.projectionCursor%p.cells().size());BlockPos pos=position(m,p,cell.pos());
            if(!level.hasChunkAt(pos)) {m.status=30;return true;}
            m.projectionCursor=(m.projectionCursor+1)%p.cells().size();
            if(level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)) {m.status=41;continue;}
            BlockState before=level.getBlockState(pos);boolean existing=before.is(MareEletrica.HOLOGRAM.get());
            if(existing&&index.current(pos,m.getBlockPos(),m.projectionStamp))continue;
            if((!before.isAir()&&!existing)||!level.getFluidState(pos).isEmpty()||!index.claimable(pos,m.getBlockPos())) {m.status=41;continue;}
            BlockState ghost=MareEletrica.HOLOGRAM.get().defaultBlockState().setValue(HologramBlock.STYLE,HologramBlock.style(cell.original().mirror(net.minecraft.world.level.block.Mirror.LEFT_RIGHT).rotate(rotation(m)))).setValue(HologramBlock.COLOR,m.editor.color(cell));
            if(!level.isUnobstructed(null,ghost.getCollisionShape(level,pos).move(pos.getX(),pos.getY(),pos.getZ()))) {m.status=41;continue;}
            if(existing&&before.equals(ghost)){index.claim(pos,m.getBlockPos(),m.projectionStamp);continue;}
            if(m.clean.stored()<8)break;
            if(!level.mayInteract(actor,pos)) {m.status=37;continue;}
            var snapshot=BlockSnapshot.create(level.dimension(),level,pos);
            if(!level.setBlock(pos,ghost,3))continue;
            if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(actor,snapshot,Direction.UP)) {snapshot.restore(3);m.status=37;continue;}
            m.clean.pay(8);index.claim(pos,m.getBlockPos(),m.projectionStamp);
        }
        return true;
    }
    public static void setDistance(MachineEntity m,int distance) {stop(m);m.projectionDistance=Math.max(2,Math.min(128,distance));m.setChanged();}
    public static boolean action(MachineEntity m,Player player,int action) {
        if(!(m.getLevel() instanceof ServerLevel level))return false;
        if(action==0||action==1) {stop(m);m.blueprint=Math.floorMod(m.blueprint+(action==0?-1:1),Blueprints.ALL.size());m.editor.reset();return true;}
        if(action==2) {m.previewUntil=level.getGameTime()+400;return true;}
        if(action==4) {m.toggle();if(!m.enabled)pause(m);return true;}
        if(action==5||action==6) {setDistance(m,m.projectionDistance+(action==5?-1:1));return true;}
        if(action!=3)return false;
        if(m.projecting) {stop(m);return true;}
        if(!m.enabled||level.hasNeighborSignal(m.getBlockPos()))return message(player,"builder_disabled");
        if(player.isSpectator()||!player.mayBuild())return false;
        var p=m.editor.plan(m);if(p.cells().isEmpty())return message(player,"template_missing");
        if(m.clean.stored()<p.rate()/2+8)return message(player,"builder_energy");
        var index=HologramIndex.get(level);
        for(var cell:p.cells()) {
            BlockPos pos=position(m,p,cell.pos());
            if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.mayInteract(player,pos))return message(player,"builder_space");
            var state=level.getBlockState(pos);
            if((!state.isAir()&&!state.is(MareEletrica.HOLOGRAM.get()))||!level.getFluidState(pos).isEmpty()||!index.claimable(pos,m.getBlockPos()))return message(player,"builder_space");
        }
        pause(m);m.projecting=true;m.setChanged();return true;
    }
    private static boolean message(Player p,String key) {p.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.mareeletrica."+key),true);return true;}
}
