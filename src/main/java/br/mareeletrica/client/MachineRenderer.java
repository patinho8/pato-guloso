package br.mareeletrica.client;

import br.mareeletrica.Kind;
import br.mareeletrica.MachineBlock;
import br.mareeletrica.MachineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class MachineRenderer implements BlockEntityRenderer<MachineEntity> {
    public MachineRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public boolean shouldRenderOffScreen(MachineEntity machine) {return machine.kind.large()||machine.kind.wind()||machine.kind==Kind.SHIELD||!machine.wireless.isEmpty()||machine.kind==Kind.GAS_REFILL||machine.kind==Kind.QUARRY||machine.kind==Kind.MORTAR;}
    @Override public int getViewDistance() {return 128;}
    /** NeoForge 1.21.1: the render box lives on the renderer. Big machines (giant wheel...) are drawn from their root block; with the default 1x1x1 box they vanished when that corner left the screen. */
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(MachineEntity machine) {if(machine.kind!=null&&machine.kind.large()){int r=Math.max(machine.kind.structureWidth(),machine.kind.structureHeight())+1;return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(r);}if(machine.kind==Kind.SHIELD)return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(br.mareeletrica.BaseControl.SHIELD_RADIUS+1);if(machine.kind!=null&&machine.kind.wind())return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(machine.kind==Kind.WIND_GIANT?8:3).expandTowards(0,machine.kind==Kind.WIND_GIANT?10:2,0);return new net.minecraft.world.phys.AABB(machine.getBlockPos()).inflate(1);}
    @Override public void render(MachineEntity machine, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        WirelessRenderer.render(machine,pose,buffers,light);
        if(machine.kind.hydro()){HydroRenderer.render(machine,partialTick,pose,buffers,light);return;}
        if(machine.kind==Kind.CORE_QUANTUM&&machine.getBlockState().getValue(MachineBlock.EXPANDED)){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-br.mareeletrica.PotabilizerStructure.turn(machine.getBlockState().getValue(MachineBlock.FACING))*90));pose.translate(1,1.5,1);pose.mulPose(Axis.YP.rotationDegrees(machine.angle+partialTick*3));pose.scale(2.6F,2.6F,2.6F);pose.translate(-.5,-.5,-.5);DmtGearLayer.model("quantum_crystal",pose,buffers,light);pose.popPose();return;}
        if(machine.kind==Kind.GAS_REFILL){
            int count=machine.inventory.getStackInSlot(0).is(br.mareeletrica.MareEletrica.EMPTY_GAS.get())?Math.min(10,machine.inventory.getStackInSlot(0).getCount()):0;
            pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-br.mareeletrica.PotabilizerStructure.turn(machine.getBlockState().getValue(MachineBlock.FACING))*90));
            for(int i=0;i<count;i++){pose.pushPose();pose.translate(.03+i%5*.53,i<5?1.02:1.92,.6);pose.scale(.47F,.63F,.47F);var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part("empty_gas_display")),1,1,1,light,overlay);pose.popPose();}pose.popPose();return;
        }
        if(machine.kind==Kind.LAMP){
            pose.pushPose();pose.translate(.5,.75,.5);pose.mulPose(Axis.YP.rotationDegrees(-machine.lampYaw));pose.mulPose(Axis.XP.rotationDegrees(-machine.lampPitch));pose.scale(.85F,.7F,.75F);pose.translate(-.5,-.5,-.5);var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part("floodlight_head")),1,1,1,light,overlay);pose.popPose();return;
        }
        if(machine.kind==Kind.MORTAR){
            pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-br.mareeletrica.PotabilizerStructure.turn(machine.getBlockState().getValue(MachineBlock.FACING))*90));
            for(int i=0;i<6;i++){
                pose.pushPose();pose.translate(i%3,.55,i/3+1);pose.mulPose(Axis.YP.rotationDegrees(-machine.mortarAzimuth[i]));pose.mulPose(Axis.XP.rotationDegrees(machine.mortarElevation[i]-90));pose.scale(1,1.5F,1);pose.translate(-.5,0,-.5);
                var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part("mortar_barrel")),1,1,1,light,overlay);pose.popPose();
            }pose.popPose();return;
        }
        if(machine.kind==Kind.QUARRY && machine.getBlockState().getValue(MachineBlock.EXPANDED)) {
            pose.pushPose();pose.translate(.5,0,.5);
            pose.mulPose(Axis.YP.rotationDegrees(-br.mareeletrica.PotabilizerStructure.turn(machine.getBlockState().getValue(MachineBlock.FACING))*90));
            float depth=machine.previousDrillDepth+(machine.drillDepth-machine.previousDrillDepth)*partialTick;
            pose.pushPose();pose.translate(2.5,4.25-depth,2.5);pose.scale(.45F,3.4F+depth,.45F);pose.translate(-.5,0,-.5);
            var shaftMc=Minecraft.getInstance();shaftMc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,shaftMc.getModelManager().getModel(ClientEvents.part("drill_shaft")),1,1,1,light,overlay);pose.popPose();
            pose.translate(2.5,.5-depth,2.5);pose.mulPose(Axis.YP.rotationDegrees(machine.angle+(machine.getBlockState().getValue(MachineBlock.ACTIVE)?13F*partialTick:0)));pose.scale(2F,4F,2F);pose.translate(-.5,0,-.5);
            var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,
                mc.getModelManager().getModel(ClientEvents.part("drill_bit")),1,1,1,light,overlay);
            pose.popPose();return;
        }
        if(machine.kind.wind()){WindRenderer.render(machine,partialTick,pose,buffers,light,overlay);return;}
        if(machine.kind==Kind.SHIELD){if(machine.getBlockState().getValue(MachineBlock.ACTIVE)){pose.pushPose();pose.translate(.5,.5,.5);BaseRenderers.dome(pose,buffers,machine.getLevel()==null?0:machine.getLevel().getGameTime()+partialTick);pose.popPose();}
            pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(machine.angle+(machine.getBlockState().getValue(MachineBlock.ACTIVE)?partialTick*7F:0)));pose.translate(-.5,0,-.5);DmtGearLayer.model("shield_emitter",pose,buffers,light);pose.popPose();return;}
        String model = switch(machine.kind) {
            case WATER_WHEEL -> "wheel_rotor";
            case SEA_TURBINE -> "sea_rotor";
            case CORE_SMALL, CORE_MEDIUM, CORE_LARGE, CORE_QUANTUM, HEALER -> "core_crystal";
            case CRUSHER -> "crusher_rotor";
            case IRRIGATOR -> "irrigator_rotor";
            case REPELLER, LIFT -> "repeller_rotor";
            default -> null;
        };
        if (model == null) return;
        boolean active = machine.getBlockState().getValue(MachineBlock.ACTIVE);
        float angle = machine.angle + (active ? partialTick * 7F : 0F);
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-machine.getBlockState().getValue(MachineBlock.FACING).toYRot()));
        if (machine.kind == Kind.WATER_WHEEL || machine.kind == Kind.CRUSHER) pose.mulPose(Axis.ZP.rotationDegrees(angle));
        else pose.mulPose(Axis.YP.rotationDegrees(angle));
        if (machine.kind == Kind.LIFT) { pose.translate(0, -0.35, 0); pose.scale(0.75F, 0.2F, 0.75F); }
        pose.translate(-0.5, -0.5, -0.5);
        Minecraft minecraft = Minecraft.getInstance();
        var baked = minecraft.getModelManager().getModel(ClientEvents.part(model));
        minecraft.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)), null, baked,
            1F, 1F, 1F, light, overlay);
        pose.popPose();
    }
}
