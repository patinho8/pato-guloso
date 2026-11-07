package br.mareeletrica.client;

import br.mareeletrica.Hydration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/** Uses the same height reservation as food and air, so bubbles move up automatically. */
public final class ThirstHud {
    private ThirstHud() {}
    public static void render(GuiGraphics g, DeltaTracker time) {
        Minecraft mc=Minecraft.getInstance();
        if (mc.player==null || mc.gameMode==null || mc.options.hideGui || !mc.gameMode.canHurtPlayer()) return;
        int y=g.guiHeight()-mc.gui.rightHeight;
        mc.gui.rightHeight+=10;
        int right=g.guiWidth()/2+91;
        for (int i=0;i<10;i++) {
            int x=right-i*8-9, value=Hydration.clientWater-i*2;
            // A nine-pixel droplet silhouette, drawn without sampling the block atlas.
            for (int row=0;row<8;row++) {
                int half=row<3?row/2:row<6?3:row==6?2:1;
                g.fill(x+4-half,y+row,x+5+half,y+row+1,0xFF102836);
                if (half>0 && value>0) {
                    int end=value>=2?x+4+half:x+5;
                    g.fill(x+5-half,y+row,end,y+row+1,0xFF40C9FF);
                }
            }
        }
    }
}
