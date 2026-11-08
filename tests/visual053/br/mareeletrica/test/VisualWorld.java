package br.mareeletrica.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Test only: creates the visual053 world when it is missing (quick play then fails back to the title screen). */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualWorld {
 static boolean done;
 static int t;
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(done||mc.level!=null||mc.getOverlay()!=null)return;if(++t<40)return;
  if(mc.getLevelSource().levelExists("visual053")){done=true;return;}done=true;System.out.println("[WORLD] creating, screen "+mc.screen);
  mc.execute(()->mc.createWorldOpenFlows().createFreshLevel("visual053",new LevelSettings("visual053",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(7407L,false,false),WorldPresets::createNormalWorldDimensions,new TitleScreen()));
 }
}
