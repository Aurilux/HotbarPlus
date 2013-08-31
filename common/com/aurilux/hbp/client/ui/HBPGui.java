package com.aurilux.hbp.client.ui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraft.util.ResourceLocation;

public class HBPGui extends GuiIngameForge {
	
	private boolean toggle;
	private int storedInvLoc;
	private int hbpHighlight;
	private InventoryPlayer inv;

	public HBPGui(Minecraft mc) {
		super(mc);
		
		toggle = false;
		storedInvLoc = 0;
		hbpHighlight = 0;
	}
	
	@Override
	protected void renderHotbar(int width, int height, float partialTicks) {
        mc.mcProfiler.startSection("actionBar");

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.func_110577_a(new ResourceLocation("textures/gui/widgets.png"));

        inv = mc.thePlayer.inventory;
        //draws the entire hotbar
        drawTexturedModalRect(width / 2 - 91, height - 22, 0, 0, 182, 22);
        //draw the highlight rectangle
        //drawTexturedModalRect(width / 2 - 91 - 1 + inv.currentItem * 20, height - 22 - 1, 0, 22, 24, 22);

        if (toggle) {
        	changeCurrentItem(Mouse.getDWheel());
        	drawTexturedVerticalModalRect(width / 2 - 91  + storedInvLoc * 20, height - 62, 0, 0, 22, 62);
            drawTexturedModalRect(width / 2 - 91 - 1 + storedInvLoc * 20, height - 62 - 1 + hbpHighlight * 20, 0, 22, 24, 22);
            
            //draws the items for the extended hotbar
            for (int i = 0; i < 9; ++i) {
                int x = width / 2 - 90 + 2;
                int y = height - 16 - 3 - i * 20;
                renderInventorySlot(i * 3, x, y, partialTicks);
            }
        }
        else {
        	//draw the normal highlighted/selected item/slot
            drawTexturedModalRect(width / 2 - 91 - 1 + inv.currentItem * 20, height - 22 - 1, 0, 22, 24, 22);
        }
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < 9; ++i) {
            int x = width / 2 - 90 + i * 20 + 2;
            int z = height - 16 - 3;
            renderInventorySlot(i, x, z, partialTicks);
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        mc.mcProfiler.endSection();
	}
	
	public void toggle() {
		toggle = !toggle;
		if (toggle) {
			//store the current selected item on the hotbar
			storedInvLoc = mc.thePlayer.inventory.currentItem;
			//reset the highlight to be the bottom-most item
			hbpHighlight = 2;
		}
		else {
			//switch the items here
			inv.currentItem = storedInvLoc;
		}
	}
	
	//private helper to ensure the currently selected item from the extended hotbar is clamped between 0 and 2
	private void changeCurrentItem(int index)
    {
        if (index > 0) {
            index = 1;
        }
        if (index < 0) {
            index = -1;
        }

        for (this.hbpHighlight -= index; this.hbpHighlight < 0; this.hbpHighlight += 3) {
            ;
        }

        while (this.hbpHighlight >= 3) {
            this.hbpHighlight -= 3;
        }
    }
	
	//private helper to use the same texture as the hotbar, but draw it vertically
	private void drawTexturedVerticalModalRect(int x, int y, int u, int v, int w, int h) {
		float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + h), (double)this.zLevel, (double)((float)(u + h) * f), (double)((float)(v + 0) * f1));
        tessellator.addVertexWithUV((double)(x + w), (double)(y + h), (double)this.zLevel, (double)((float)(u + h) * f), (double)((float)(v + w) * f1));
        tessellator.addVertexWithUV((double)(x + w), (double)(y + 0), (double)this.zLevel, (double)((float)(u + 0) * f), (double)((float)(v + w) * f1));
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)this.zLevel, (double)((float)(u + 0) * f), (double)((float)(v + 0) * f1));
        tessellator.draw();
	}
}