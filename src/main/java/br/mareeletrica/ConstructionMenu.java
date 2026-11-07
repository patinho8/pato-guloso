package br.mareeletrica;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ConstructionMenu extends AbstractContainerMenu {
    private long lastEdit=-100;private int sentRevision=-1;
    public final ContainerData data;private final MachineEntity machine;
    public ConstructionMenu(int id,Inventory inventory) {this(id,inventory,null,new SimpleContainerData(MachineEntity.DATA_COUNT));}
    public ConstructionMenu(int id,Inventory inventory,MachineEntity machine) {this(id,inventory,machine,machine.data);}
    private ConstructionMenu(int id,Inventory inventory,MachineEntity machine,ContainerData data) {
        super(MareEletrica.CONSTRUCTION_MENU.get(),id);this.machine=machine;this.data=data;
        addDataSlots(data);
    }
    public void edit(net.minecraft.server.level.ServerPlayer p,EditorPackets.Edit edit){
        if(machine==null||!p.mayBuild())return;long now=p.level().getGameTime();if(now-lastEdit<2){EditorPackets.send(p,this,machine);return;}lastEdit=now;
        if(edit.action()==0||edit.revision()!=machine.editor.revision){EditorPackets.send(p,this,machine);return;}
        boolean ok=switch(edit.action()){
            case 1 -> machine.editor.paint(machine,edit.a(),edit.b(),edit.c()==1);
            case 2 -> machine.editor.resize(machine,edit.a(),edit.b(),edit.c());
            case 3 -> {ConstructionProjector.stop(machine);machine.editor.reset();yield true;}
            default -> false;};
        if(!ok)p.displayClientMessage(net.minecraft.network.chat.Component.literal("Limite: 128 por eixo, 65.536 blocos e volume de 524.288."),true);sentRevision=-1;
    }
    @Override public void broadcastChanges(){super.broadcastChanges();if(machine!=null&&machine.editor.revision!=sentRevision){for(var p:((net.minecraft.server.level.ServerLevel)machine.getLevel()).players())if(p.containerMenu==this)EditorPackets.send(p,this,machine);sentRevision=machine.editor.revision;}}
    public void stroke(net.minecraft.server.level.ServerPlayer p,EditorPackets.Stroke packet){
        if(machine==null)return;long now=p.level().getGameTime();if(now-lastEdit<2||packet.revision()!=machine.editor.revision){EditorPackets.send(p,this,machine);return;}lastEdit=now;
        if(machine.editor.paintBatch(machine,packet.keys(),packet.color())){sentRevision=machine.editor.revision;net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,new EditorPackets.Paint(containerId,sentRevision,packet.color(),packet.keys()));}else EditorPackets.send(p,this,machine);
    }
    public Blueprints.Plan plan() {return Blueprints.ALL.get(Math.floorMod(data.get(17),Blueprints.ALL.size()));}
    @Override public boolean stillValid(Player player) {return machine==null||!machine.isRemoved()&&machine.getLevel()==player.level()&&player.distanceToSqr(machine.getBlockPos().getCenter())<=64;}
    @Override public boolean clickMenuButton(Player player,int id) {return machine!=null&&stillValid(player)&&ConstructionProjector.action(machine,player,id);}
    public void distance(Player player,int value) {if(machine!=null&&stillValid(player))ConstructionProjector.setDistance(machine,value);}
    @Override public ItemStack quickMoveStack(Player player,int index) {return ItemStack.EMPTY;}
}
