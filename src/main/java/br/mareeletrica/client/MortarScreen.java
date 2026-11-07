package br.mareeletrica.client;

import br.mareeletrica.MortarAim;
import br.mareeletrica.MortarMenu;
import br.mareeletrica.RemotePackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;

public final class MortarScreen extends AbstractContainerScreen<MortarMenu> {
    private final EditBox[] elevations=new EditBox[6], azimuths=new EditBox[6];
    private final List<Button> steps=new ArrayList<>();
    private Button apply,fire,toggle;private boolean loaded;
    public MortarScreen(MortarMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=300;imageHeight=240;}
    private Button button(String text,int x,int y,int width,Runnable run){return addRenderableWidget(Button.builder(Component.literal(text),b->run.run()).bounds(leftPos+x,topPos+y,width,13).build());}
    private int number(EditBox box,int fallback){try{return Integer.parseInt(box.getValue());}catch(NumberFormatException ignored){return fallback;}}
    private void send(){if(!loaded||menu.data.get(1)>0)return;for(int i=0;i<6;i++){int e=MortarAim.elevation(number(elevations[i],menu.elevation(i))),a=MortarAim.azimuth(number(azimuths[i],menu.azimuth(i)));elevations[i].setValue(""+e);azimuths[i].setValue(""+a);PacketDistributor.sendToServer(new RemotePackets.Aim(menu.containerId,i,e,a));}}
    private void action(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void init(){
        super.init();loaded=false;steps.clear();
        for(int i=0;i<6;i++){
            final int barrel=i;int y=47+i*14;
            var e=addRenderableWidget(new EditBox(font,leftPos+91,topPos+y,43,13,Component.translatable("gui.mareeletrica.mortar_elevation")));e.setMaxLength(3);e.setFilter(v->v.matches("[0-9]{0,3}"));elevations[i]=e;
            var a=addRenderableWidget(new EditBox(font,leftPos+208,topPos+y,43,13,Component.translatable("gui.mareeletrica.mortar_azimuth")));a.setMaxLength(4);a.setFilter(v->v.matches("-?[0-9]{0,3}"));azimuths[i]=a;
            for(int delta:new int[]{-5,5}){
                steps.add(button(delta<0?"−":"+",delta<0?73:136,y,16,()->e.setValue(""+MortarAim.elevation(number(e,menu.elevation(barrel))+delta))));
                steps.add(button(delta<0?"−":"+",delta<0?190:253,y,16,()->a.setValue(""+MortarAim.azimuth(number(a,menu.azimuth(barrel))+delta))));
            }
        }
        apply=addRenderableWidget(Button.builder(Component.translatable("gui.mareeletrica.apply"),b->send()).bounds(leftPos+49,topPos+134,65,18).build());
        fire=addRenderableWidget(Button.builder(Component.translatable("gui.mareeletrica.mortar_fire"),b->{send();action(1);}).bounds(leftPos+118,topPos+134,108,18).build());
        toggle=addRenderableWidget(Button.builder(Component.empty(),b->action(0)).bounds(leftPos+230,topPos+134,58,18).build());
        containerTick();
    }
    @Override protected void containerTick(){
        super.containerTick();if(apply==null)return;
        if(!loaded&&menu.data.get(15)==1){for(int i=0;i<6;i++){elevations[i].setValue(""+menu.elevation(i));azimuths[i].setValue(""+menu.azimuth(i));}loaded=true;}
        boolean edit=loaded&&menu.data.get(1)==0;
        for(int i=0;i<6;i++){elevations[i].setEditable(edit);azimuths[i].setEditable(edit);}for(var b:steps)b.active=edit;
        apply.active=fire.active=edit;toggle.setMessage(Component.translatable(menu.data.get(2)==1?"gui.mareeletrica.on":"gui.mareeletrica.off"));
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key!=256)for(int i=0;i<6;i++)for(EditBox box:new EditBox[]{elevations[i],azimuths[i]})if(box.isFocused()){box.keyPressed(key,scan,modifiers);return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    @Override protected void renderBg(GuiGraphics g,float delta,int mouseX,int mouseY){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF122B36);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFFB784F5);
        for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,0xFF52727D);g.fill(x,y,x+16,y+16,0xFF081D27);}
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,12,9,0xE6FFFA,false);
        g.drawString(font,Component.translatable("gui.mareeletrica.mortar_power",menu.data.get(0)),12,22,0xB0EBE1,false);
        if(menu.data.get(1)>0)g.drawString(font,Component.translatable("gui.mareeletrica.mortar_delay",String.format(java.util.Locale.ROOT,"%.1f",menu.data.get(1)/20.0)),178,22,0xFFC079,false);
        g.drawString(font,Component.translatable("gui.mareeletrica.mortar_elevation"),73,37,0xB8CED6,false);g.drawString(font,Component.translatable("gui.mareeletrica.mortar_azimuth"),183,37,0xB8CED6,false);
        for(int i=0;i<6;i++)g.drawString(font,Component.translatable("gui.mareeletrica.barrel",i+1),12,51+i*14,0xE6FFFA,false);
        g.drawString(font,playerInventoryTitle,69,154,0xB8CED6,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float delta){super.render(g,x,y,delta);renderTooltip(g,x,y);}
}
