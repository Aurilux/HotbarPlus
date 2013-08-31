package com.aurilux.hbp.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class HBPGui extends GuiIngameForge {
	
	/*the stored index for the item that will be switched when the V-key is pressed*/
	private int selection1;
	/*the stored index for the item that will be switched when the B-key is pressed*/
	private int selection2;
	
	/*determines whether to display the extended hotbar*/
	private boolean toggle;
	/*stores the player's inventory*/
	private InventoryPlayer inv;

	public HBPGui(Minecraft mc) {
		super(mc);
		
		toggle = false;
	}
	
	@Override
	protected void renderHotbar(int width, int height, float partialTicks) {
        mc.mcProfiler.startSection("actionBar");

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.func_110577_a(new ResourceLocation("textures/gui/widgets.png"));
        inv = mc.thePlayer.inventory;
        
        //draws the default horizontal hotbar
        drawTexturedModalRect(width / 2 - 91, height - 22, 0, 0, 182, 22);

        if (toggle) {
        	drawTexturedVerticalModalRect(width / 2 - 91 + inv.currentItem * 20, height - 62, 0, 0, 22, 62);
            
            //draws the items for the extended hotbar
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < 2; ++i) {
            	//the x-coord to start drawing the item
                int x = width / 2 - 90 + 2 + inv.currentItem * 20;
                //the y-coord to start drawing the item
                int y = height - 16 - 23 - (i * 20);
                int itemIndex = inv.currentItem + (27 - (i * 9));
                renderInventorySlot(itemIndex, x, y, partialTicks);
                
                if (i == 0) {
                	selection1 = itemIndex;
                }
                else { //i == 1
                	selection2 = itemIndex;
                }
            }
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_BLEND);
            
            //responds to the specified key presses and then resets the toggle to prevent repeating
            if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
            	ItemStack toSwitch = inv.mainInventory[selection1]; //stores the selected item
            	ItemStack hotbarItem = inv.mainInventory[inv.currentItem]; //stores the item from the hotbar
            	
            	inv.mainInventory[inv.currentItem] = toSwitch; //move the item from the inventory to the hotbar
            	inv.mainInventory[selection1] = hotbarItem; //move the hotbar item to the inventory
            	toggle();
            }
            else if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
            	ItemStack toSwitch = inv.mainInventory[selection2]; //stores the selected item
            	ItemStack hotbarItem = inv.mainInventory[inv.currentItem]; //stores the item from the hotbar
            	
            	inv.mainInventory[inv.currentItem] = toSwitch; //move the item from the inventory to the hotbar
            	inv.mainInventory[selection2] = hotbarItem; //move the hotbar item to the inventory
            	toggle();
            }
        }
        else {
        	//draw the normal highlighted/selected item/slot
            drawTexturedModalRect(width / 2 - 91 - 1 + inv.currentItem * 20, height - 22 - 1, 0, 22, 24, 22);
        }
        
        //draws the items on the default hotbar
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