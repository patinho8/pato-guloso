package br.mareeletrica.client;

import br.mareeletrica.Kind;
import br.mareeletrica.BridgePower;
import br.mareeletrica.MachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private Button toggle;
    private Button action;
    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 210; imageHeight = 238;
        inventoryLabelX = 24; inventoryLabelY = 141;
        titleLabelX = 12; titleLabelY = 10;
    }
    @Override protected void init() {
        super.init();
        toggle = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }).bounds(leftPos + 139, topPos + 124, 59, 16).build());
        action = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + 12, topPos + 124, 120, 16).build());
        refreshButton();
    }
    private void refreshButton() {
        if (toggle != null) toggle.setMessage(Component.translatable(menu.data.get(6) == 1 ? "gui.mareeletrica.on" : "gui.mareeletrica.off"));
        if (action != null) {
            action.visible = menu.kind().actionButton();
            action.active = menu.kind() != Kind.ORE_SCANNER || menu.data.get(7) != 13;
            action.setMessage(switch (menu.kind()) {
                case ORE_SCANNER -> Component.translatable("gui.mareeletrica.scan");
                case LEVELER -> Component.translatable("gui.mareeletrica.leveler_restart");
                case ROVER_BASE -> Component.translatable("gui.mareeletrica.rover_return");
                case MORTAR -> Component.translatable("gui.mareeletrica.mortar_fire");
                case QUARRY -> Component.translatable("gui.mareeletrica.quarry_restart");
                case TELEPORT_PAD -> Component.translatable("gui.mareeletrica.channel", menu.data.get(13) + 1);
                case XP_COLLECTOR -> Component.translatable("gui.mareeletrica.withdraw");
                case WEATHER_CONTROLLER -> Component.translatable("gui.mareeletrica.weather");
                default -> Component.empty();
            });
        }
    }
    @Override protected void containerTick() { super.containerTick(); refreshButton(); }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF081922);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF122B36);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + 4, 0xFF35D9BE);
        bar(g, x + 12, y + 35, menu.data.get(0), menu.data.get(2), 0xFF38DBC5);
        if (menu.kind() == Kind.POTABILIZER) bar(g,x+12,y+61,menu.data.get(8),8,0xFFF1A95B);
        else bar(g, x + 12, y + 61, menu.data.get(1), menu.data.get(3), 0xFFF1A95B);
        for (int i = 0; i < 9; i++) if (menu.slotVisible(i)) slot(g, x + 23 + i * 18, y + 102);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slot(g, x + 23 + col * 18, y + 152 + row * 18);
        for (int col = 0; col < 9; col++) slot(g, x + 23 + col * 18, y + 210);
        if (menu.kind().processor()) {
            int duration = menu.kind().duration();
            int start = Math.max(50, 32 + 18 * menu.kind().inputs()), end = menu.kind() == Kind.GREENHOUSE ? 141 : 159, width = end - start;
            g.fill(x + start, y + 109, x + end, y + 113, 0xFF071B23);
            g.fill(x + start, y + 109, x + start + Math.min(width, menu.data.get(5) * width / duration), y + 113, 0xFF38DBC5);
        }
    }
    private void bar(GuiGraphics g, int x, int y, int value, int max, int color) {
        g.fill(x, y, x + 186, y + 9, 0xFF06151D);
        int filled = max <= 0 ? 0 : (int) Math.min(184L, Math.max(0L, (long) value * 184 / max));
        g.fill(x + 1, y + 1, x + 1 + filled, y + 8, color);
    }
    private void slot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF52727D);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF081D27);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, font.plainSubstrByWidth(title.getString(), 186), 12, 10, 0xE6FFFA, false);
        g.drawString(font, Component.translatable("gui.mareeletrica.clean", menu.data.get(0), menu.data.get(2)), 12, 25, 0xB0EBE1, false);
        if (menu.kind() == Kind.POTABILIZER)
            g.drawString(font, Component.translatable("gui.mareeletrica.potabilizer_filter",menu.data.get(8)),12,51,0xF2C494,false);
        else g.drawString(font, Component.translatable("gui.mareeletrica.dirty", menu.data.get(1), menu.data.get(3)), 12, 51, 0xF2C494, false);
        g.drawString(font, font.plainSubstrByWidth(Component.translatable("status.mareeletrica." + menu.data.get(7)).getString(), 186), 12, 78, 0xD8E9EC, false);
        int heat=menu.data.get(32);
        if(heat>=0){boolean burnt=heat>=1000;int pct=heat%1000;String txt=burnt?"PIFOU (bloco de ferro conserta)":"Calor "+pct+"%";int col=burnt?0xFF5050:pct>=90?0xFF5050:pct>=75?0xFFB040:0x9FD8FF;
         int w=font.width(txt);g.drawString(font,txt,188-w,64,col,false);if(!burnt){g.fill(188-w,73,188,75,0x60000000);g.fill(188-w,73,188-w+w*pct/100,75,0xFF000000|col);}}
        if (menu.kind() == Kind.GREENHOUSE) {
            g.drawString(font,Component.translatable("gui.mareeletrica.greenhouse_cycle"),12,91,0xC5D7DE,false);
        } else if (menu.kind() == Kind.QUARRY) {
            g.drawString(font,Component.translatable("gui.mareeletrica.quarry_range",Math.min(100,menu.data.get(13)*100/Math.max(1,menu.data.get(15)))),12,91,0xC5D7DE,false);
        } else if (menu.kind() == Kind.LEVELER) {
            g.drawString(font,Component.translatable("gui.mareeletrica.leveler_range",menu.data.get(13)*100/9261),12,91,0xC5D7DE,false);
        } else if (menu.kind() == Kind.MORTAR) {
            g.drawString(font,Component.translatable("gui.mareeletrica.mortar_delay",String.format(java.util.Locale.ROOT,"%.1f",menu.data.get(27)/20.0)),12,91,0xC5D7DE,false);
            g.drawString(font,Component.translatable("gui.mareeletrica.mortar_load"),48,108,0xC5D7DE,false);
        } else if (menu.kind() == Kind.ROVER_BASE) {
            g.drawString(font,Component.translatable("gui.mareeletrica.rover_info",Component.translatable("heading.mareeletrica."+menu.data.get(25)),menu.data.get(27)/1000),12,91,0xC5D7DE,false);
            g.drawString(font,font.plainSubstrByWidth(Component.translatable("rover.mareeletrica."+menu.data.get(26)).getString(),148),48,104,0x38DBC5,false);
            if(menu.data.get(14)>0)g.drawString(font,menu.data.get(10)+" / "+menu.data.get(11)+" / "+menu.data.get(12),48,115,0xC5D7DE,false);
        } else if (menu.kind() == Kind.POTABILIZER) {
            g.drawString(font,Component.translatable("gui.mareeletrica.water_"+menu.data.get(16)),12,91,0xC5D7DE,false);
        } else if (menu.kind() == Kind.PURIFIER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.filter", menu.data.get(8)), 48, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.ORE_SCANNER) {
            if (menu.data.get(14) > 0) {
                Component ore = net.minecraft.world.level.block.Block.stateById(menu.data.get(14)).getBlock().getName();
                g.drawString(font, font.plainSubstrByWidth(ore.getString(), 186), 12, 94, 0x38DBC5, false);
                g.drawString(font, menu.data.get(10) + " / " + menu.data.get(11) + " / " + menu.data.get(12), 12, 108, 0xC5D7DE, false);
            } else g.drawString(font, Component.translatable("gui.mareeletrica.scan_progress", menu.data.get(13) * 100 / 9248), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.XP_COLLECTOR) {
            g.drawString(font, Component.translatable("gui.mareeletrica.xp", menu.data.get(13)), 12, 108, 0x38DBC5, false);
        } else if (menu.kind() == Kind.TELEPORT_PAD) {
            g.drawString(font, Component.translatable("gui.mareeletrica.crouch"), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.WEATHER_CONTROLLER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.weather_cost"), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.FISHER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.fishing", menu.data.get(5) / 20), 12, 91, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.CHARGER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.charge_rate", menu.data.get(9)), 48, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.TURRET) {
            g.drawString(font, Component.translatable("gui.mareeletrica.turret"), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.ITEM_TRANSFER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.item_rate", menu.data.get(9)), 12, 91, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.BRIDGE_PROJECTOR) {
            g.drawString(font, Component.translatable("gui.mareeletrica.bridge", menu.data.get(15), BridgePower.reach(menu.data.get(0))), 12, 94, 0x38DBC5, false);
            g.drawString(font, Component.translatable("gui.mareeletrica.bridge_cost", menu.data.get(9)), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.PRESENCE_SENSOR) {
            g.drawString(font, Component.translatable("gui.mareeletrica.sensor", menu.data.get(7) == 28 ? 15 : 0), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.EFFECT_CLEANSER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.cleanser"), 12, 108, 0xC5D7DE, false);
        } else if (menu.kind() == Kind.MINING_BOOSTER) {
            g.drawString(font, Component.translatable("gui.mareeletrica.haste"), 12, 108, 0xC5D7DE, false);
        } else if (!menu.kind().storage()) {
            g.drawString(font, Component.translatable("gui.mareeletrica.rate", menu.data.get(9)), 12, 108, 0xC5D7DE, false);
        }
        if (!menu.kind().actionButton()) g.drawString(font, Component.translatable("gui.mareeletrica.control"), 12, 128, 0x819EA9, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xB8CED6, false);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (mouseY >= topPos + 99 && mouseY < topPos + 122) {
            if (menu.kind().processor() && menu.kind() != Kind.STONE_GENERATOR && mouseX >= leftPos + 22 && mouseX < leftPos + 24 + menu.kind().inputs() * 18) {
                int index = Math.max(0, (mouseX - leftPos - 24) / 18);
                String key = menu.kind() == Kind.ASSEMBLER ? "assembly_" + index : menu.kind() == Kind.REPAIRER ? "repair_" + index : menu.kind() == Kind.POTABILIZER ? "potabilizer_" + index : menu.kind() == Kind.GREENHOUSE ? "greenhouse_" + index : "input";
                g.renderTooltip(font, Component.translatable("gui.mareeletrica." + key), mouseX, mouseY);
            }
            else if (menu.kind() == Kind.GREENHOUSE && mouseX >= leftPos + 148 && mouseX < leftPos + 166)
                g.renderTooltip(font, Component.translatable("gui.mareeletrica.greenhouse_bottle"), mouseX, mouseY);
            else if (menu.kind().processor() && mouseX >= leftPos + 166 && mouseX < leftPos + 188)
                g.renderTooltip(font, Component.translatable("gui.mareeletrica.output"), mouseX, mouseY);
        }
    }
}
