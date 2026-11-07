package br.mareeletrica.client;

import br.mareeletrica.Thermal;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Client side of the thermal goggles: who glows, in which colour, and the screen tint. */
public final class ThermalClient {
 private ThermalClient(){}
 public static boolean active(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.options.getCameraType().isFirstPerson()&&Thermal.wearing(mc.player);}
 public static boolean glows(Entity e){var mc=Minecraft.getInstance();return e instanceof LivingEntity&&e!=mc.player&&e.isAlive()&&e.distanceToSqr(mc.player)<96*96&&active();}
 /** Heat colour: undead are cold (blue), players and big animals hot (red/orange), small ones yellow. */
 public static int color(Entity e){
  if(e instanceof LivingEntity l&&l.isInvertedHealAndHarm())return 0x3A7BFF;
  if(e instanceof net.minecraft.world.entity.player.Player)return 0xFF3A1A;
  float size=e.getBbWidth()*e.getBbHeight();
  return size>1.2F?0xFF5A1E:size>.4F?0xFF9A2A:0xFFD84A;
 }
 public static void overlay(GuiGraphics g,DeltaTracker dt){
  if(!active())return;int w=g.guiWidth(),h=g.guiHeight();
  g.fill(0,0,w,h,0x5A001C2A);                                  // cold dark-teal tint over the world
  for(int y=0;y<h;y+=3)g.fill(0,y,w,y+1,0x14000000);            // faint scan lines
  int b=Math.max(w,h)/7;g.fill(0,0,w,b/3,0x66000000);g.fill(0,h-b/3,w,h,0x66000000);   // goggle frame
  g.drawString(Minecraft.getInstance().font,"TÉRMICO",6,6,0xFF7CFFB0,false);
 }
}
