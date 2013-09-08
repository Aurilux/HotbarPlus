package com.aurilux.hbp.client.ui;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ARMOR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.BOSSHEALTH;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.CROSSHAIRS;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.EXPERIENCE;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTHMOUNT;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HOTBAR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.JUMPBAR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PORTAL;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.TEXT;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.crash.CallableMinecraftVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.aurilux.hbp.handlers.HBPKeyHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class HBPGui extends GuiIngameForge {
	
	/*the stored index for the item that will be switched when the V-key is pressed*/
	private int selection1;
	/*the stored index for the item that will be switched when the B-key is pressed*/
	private int selection2;
	
	/*determines whether to display the extended hotbar*/
	private boolean toggle;
	/*stores the player's inventory*/
	private InventoryPlayer inv;
	private float lastTick;
	
	//copied private variables
    private ScaledResolution res = null;
    private FontRenderer fontrenderer = null;
    private RenderGameOverlayEvent eventParent;

    private static final int WHITE = 0xFFFFFF;
    private static final String MC_VERSION = (new CallableMinecraftVersion(null)).minecraftVersion();

	public HBPGui(Minecraft mc) {
		super(mc);
		toggle = false;
		lastTick = 0;
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
	
	@Override
	protected void renderHotbar(int width, int height, float partialTicks) {
        if (pre(HOTBAR)) return;
        mc.mcProfiler.startSection("actionBar");

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.func_110577_a(new ResourceLocation("textures/gui/widgets.png"));
        inv = mc.thePlayer.inventory;
        
        //draws the default horizontal hotbar
        drawTexturedModalRect(width / 2 - 91, height - 22, 0, 0, 182, 22);

        if (toggle) {
        	//draws the vertical, extended hotbar
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
            
            //This if statement is just to ensure 'lastTick' doesn't get to obscenely high numbers if the player has the extended
            //hotbar open for long periods
            if (lastTick < 5) {
            	lastTick += partialTicks;
            }
            //TODO optimize the following
            if (HBPKeyHandler.firstOption.isPressed() && lastTick >= 5) {
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
                DataOutputStream outputStream = new DataOutputStream(bos);
                try {
                        outputStream.writeInt(selection1);
                        outputStream.writeInt(inv.currentItem);
                }
                catch (Exception ex) {
                        ex.printStackTrace();
                }
               
                Packet250CustomPayload packet = new Packet250CustomPayload();
                packet.channel = "HBP";
                packet.data = bos.toByteArray();
                packet.length = bos.size();
                PacketDispatcher.sendPacketToServer(packet);
                lastTick = 0;
            }
            else if (HBPKeyHandler.secondOption.isPressed() && lastTick >= 5) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
                DataOutputStream outputStream = new DataOutputStream(bos);
                try {
                        outputStream.writeInt(selection2);
                        outputStream.writeInt(inv.currentItem);
                }
                catch (Exception ex) {
                        ex.printStackTrace();
                }
               
                Packet250CustomPayload packet = new Packet250CustomPayload();
                packet.channel = "HBP";
                packet.data = bos.toByteArray();
                packet.length = bos.size();
                PacketDispatcher.sendPacketToServer(packet);
                lastTick = 0;
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
        post(HOTBAR);
	}
	
	//all I did here was move the rendering of the jump and experience bar to before the hotbar
    @Override
    public void renderGameOverlay(float partialTicks, boolean hasScreen, int mouseX, int mouseY){
        res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        eventParent = new RenderGameOverlayEvent(partialTicks, res, mouseX, mouseY);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        renderHealthMount = mc.thePlayer.ridingEntity instanceof EntityLivingBase;
        renderFood = mc.thePlayer.ridingEntity == null;
        renderJumpBar = mc.thePlayer.func_110317_t();

        right_height = 39;
        left_height = 39;

        if (pre(ALL)) return;

        fontrenderer = mc.fontRenderer;
        mc.entityRenderer.setupOverlayRendering();
        GL11.glEnable(GL11.GL_BLEND);

        if (Minecraft.isFancyGraphicsEnabled()) {
            renderVignette(mc.thePlayer.getBrightness(partialTicks), width, height);
        }
        else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        if (renderHelmet) {
        	renderHelmet(res, partialTicks, hasScreen, mouseX, mouseY);
        }

        if (renderPortal && !mc.thePlayer.isPotionActive(Potion.confusion)) {
            renderPortal(width, height, partialTicks);
        }

        if (renderJumpBar) {
            renderJumpBar(width, height);
        }
        else if (renderExperiance) {
            renderExperience(width, height);
        }

        if (!mc.playerController.enableEverythingIsScrewedUpMode()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            zLevel = -90.0F;
            rand.setSeed((long)(updateCounter * 312871));

            if (renderCrosshairs) renderCrosshairs(width, height);
            if (renderBossHealth) renderBossHealth();

            if (this.mc.playerController.shouldDrawHUD()) {
                if (renderHealth) renderHealth(width, height);
                if (renderArmor)  renderArmor(width, height);
                if (renderFood)   renderFood(width, height);
                if (renderHealthMount) renderHealthMount(width, height);
                if (renderAir)    renderAir(width, height);
            }
            if (renderHotbar) renderHotbar(width, height, partialTicks);
        }

        renderSleepFade(width, height);
        renderToolHightlight(width, height);
        renderHUDText(width, height);
        renderRecordOverlay(width, height, partialTicks);

        ScoreObjective objective = mc.theWorld.getScoreboard().func_96539_a(1);
        if (renderObjective && objective != null) {
            this.func_96136_a(objective, height, width, fontrenderer);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        renderChat(width, height);

        renderPlayerList(width, height);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        post(ALL);
    }
    
    @Override
    protected void renderToolHightlight(int width, int height) {
        if (this.mc.gameSettings.heldItemTooltips && !toggle)  {
            mc.mcProfiler.startSection("toolHighlight");

            if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
                String name = this.highlightingItemStack.getDisplayName();

                int opacity = (int)((float)this.remainingHighlightTicks * 256.0F / 10.0F);
                if (opacity > 255) opacity = 255;

                if (opacity > 0) {
                    int y = height - 59;
                    if (!mc.playerController.shouldDrawHUD()) y += 14;

                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    FontRenderer font = highlightingItemStack.getItem().getFontRenderer(highlightingItemStack);
                    if (font != null) {
                        int x = (width - font.getStringWidth(name)) / 2;
                        font.drawStringWithShadow(name, x, y, WHITE | (opacity << 24));
                    }
                    else  {
                        int x = (width - fontrenderer.getStringWidth(name)) / 2;
                        fontrenderer.drawStringWithShadow(name, x, y, WHITE | (opacity << 24));
                    }
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }
            mc.mcProfiler.endSection();
        }
    }
    
    //ANYTHING BELOW THIS POINT IS PERFECT COPIES FROM GuiIngameForge. DOING THIS SOLVES A LOT OF ERRORS. WILL CHANGE/FIX IN A LATER VERSION\\
    //why this wasn't made protected in the first place, I don't know
    protected void renderCrosshairs(int width, int height) {
        if (pre(CROSSHAIRS)) return;
        bind(Gui.field_110324_m);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
        drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
        GL11.glDisable(GL11.GL_BLEND);
        post(CROSSHAIRS);
    }

    @Override
    protected void renderBossHealth() {
        if (pre(BOSSHEALTH)) return;
        mc.mcProfiler.startSection("bossHealth");
        //The following 'if' statement was copied directly from GuiIngame
        if (BossStatus.bossName != null && BossStatus.statusBarLength > 0) {
            --BossStatus.statusBarLength;
            FontRenderer fontrenderer = this.mc.fontRenderer;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            short short1 = 182;
            int j = i / 2 - short1 / 2;
            int k = (int)(BossStatus.healthScale * (float)(short1 + 1));
            byte b0 = 12;
            this.drawTexturedModalRect(j, b0, 0, 74, short1, 5);
            this.drawTexturedModalRect(j, b0, 0, 74, short1, 5);

            if (k > 0) {
                this.drawTexturedModalRect(j, b0, 0, 79, k, 5);
            }

            String s = BossStatus.bossName;
            fontrenderer.drawStringWithShadow(s, i / 2 - fontrenderer.getStringWidth(s) / 2, b0 - 10, 16777215);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.func_110434_K().func_110577_a(field_110324_m);
        }
        mc.mcProfiler.endSection();
        post(BOSSHEALTH);
    }
    
    private void renderHelmet(ScaledResolution res, float partialTicks, boolean hasScreen, int mouseX, int mouseY) {
        if (pre(HELMET)) return;

        ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);

        if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() != null) {
            if (itemstack.itemID == Block.pumpkin.blockID) {
                renderPumpkinBlur(res.getScaledWidth(), res.getScaledHeight());
            }
            else {
                itemstack.getItem().renderHelmetOverlay(itemstack, mc.thePlayer, res, partialTicks, hasScreen, mouseX, mouseY);
            }
        }

        post(HELMET);
    }
    
    @Override
    protected void renderArmor(int width, int height) {
        if (pre(ARMOR)) return;
        mc.mcProfiler.startSection("armor");

        int left = width / 2 - 91;
        int top = height - left_height;

        int level = ForgeHooks.getTotalArmorValue(mc.thePlayer);
        for (int i = 1; level > 0 && i < 20; i += 2) {
            if (i < level) {
                drawTexturedModalRect(left, top, 34, 9, 9, 9);
            }
            else if (i == level) {
                drawTexturedModalRect(left, top, 25, 9, 9, 9);
            }
            else if (i > level) {
                drawTexturedModalRect(left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        left_height += 10;

        mc.mcProfiler.endSection();
        post(ARMOR);
    }
    
    @Override
    protected void renderPortal(int width, int height, float partialTicks) {
        if (pre(PORTAL)) return;

        float f1 = mc.thePlayer.prevTimeInPortal + (mc.thePlayer.timeInPortal - mc.thePlayer.prevTimeInPortal) * partialTicks;

        if (f1 > 0.0F) {
            func_130015_b(f1, width, height);
        }

        post(PORTAL);
    }
    
    @Override
    protected void renderAir(int width, int height) {
        if (pre(AIR)) return;
        mc.mcProfiler.startSection("air");
        int left = width / 2 + 91;
        int top = height - right_height;

        if (mc.thePlayer.isInsideOfMaterial(Material.water)) {
            int air = mc.thePlayer.getAir();
            int full = MathHelper.ceiling_double_int((double)(air - 2) * 10.0D / 300.0D);
            int partial = MathHelper.ceiling_double_int((double)air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i) {
                drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            right_height += 10;
        }

        mc.mcProfiler.endSection();
        post(AIR);
    }
    
    @Override
    public void renderHealth(int width, int height) {
        bind(field_110324_m);
        if (pre(HEALTH)) return;
        mc.mcProfiler.startSection("health");

        boolean highlight = mc.thePlayer.hurtResistantTime / 3 % 2 == 1;

        if (mc.thePlayer.hurtResistantTime < 10) {
            highlight = false;
        }

        AttributeInstance attrMaxHealth = this.mc.thePlayer.func_110148_a(SharedMonsterAttributes.field_111267_a);
        int health = MathHelper.ceiling_float_int(mc.thePlayer.func_110143_aJ());
        int healthLast = MathHelper.ceiling_float_int(mc.thePlayer.prevHealth);
        float healthMax = (float)attrMaxHealth.func_111126_e();
        float absorb = this.mc.thePlayer.func_110139_bj();

        int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed((long)(updateCounter * 312871));

        int left = width / 2 - 91;
        int top = height - left_height;
        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;

        int regen = -1;
        if (mc.thePlayer.isPotionActive(Potion.regeneration)) {
            regen = updateCounter % 25;
        }

        final int TOP =  9 * (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (mc.thePlayer.isPotionActive(Potion.poison))      MARGIN += 36;
        else if (mc.thePlayer.isPotionActive(Potion.wither)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceiling_float_int((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += rand.nextInt(2);
            if (i == regen) y -= 2;

            drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)  {
                if (i * 2 + 1 < healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                    drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
                else
                    drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
                absorbRemaining -= 2.0F;
            }
            else {
                if (i * 2 + 1 < health)
                    drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        mc.mcProfiler.endSection();
        post(HEALTH);
    }
    
    @Override
    public void renderFood(int width, int height) {
        if (pre(FOOD)) return;
        mc.mcProfiler.startSection("food");

        int left = width / 2 + 91;
        int top = height - right_height;
        right_height += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodStats stats = mc.thePlayer.getFoodStats();
        int level = stats.getFoodLevel();
        int levelLast = stats.getPrevFoodLevel();

        for (int i = 0; i < 10; ++i)  {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte backgound = 0;

            if (mc.thePlayer.isPotionActive(Potion.hunger)) {
                icon += 36;
                backgound = 13;
            }
            if (unused) backgound = 1; //Probably should be a += 1 but vanilla never uses this

            if (mc.thePlayer.getFoodStats().getSaturationLevel() <= 0.0F && updateCounter % (level * 3 + 1) == 0) {
                y = top + (rand.nextInt(3) - 1);
            }

            drawTexturedModalRect(x, y, 16 + backgound * 9, 27, 9, 9);

            if (unused) {
                if (idx < levelLast)
                    drawTexturedModalRect(x, y, icon + 54, 27, 9, 9);
                else if (idx == levelLast)
                    drawTexturedModalRect(x, y, icon + 63, 27, 9, 9);
            }

            if (idx < level)
                drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
        }
        mc.mcProfiler.endSection();
        post(FOOD);
    }

    protected void renderSleepFade(int width, int height) {
        if (mc.thePlayer.getSleepTimer() > 0) {
            mc.mcProfiler.startSection("sleep");
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            int sleepTime = mc.thePlayer.getSleepTimer();
            float opacity = (float)sleepTime / 100.0F;

            if (opacity > 1.0F) {
                opacity = 1.0F - (float)(sleepTime - 100) / 10.0F;
            }

            int color = (int)(220.0F * opacity) << 24 | 1052704;
            drawRect(0, 0, width, height, color);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            mc.mcProfiler.endSection();
        }
    }
    
    @Override
    protected void renderExperience(int width, int height) {
        bind(field_110324_m);
        if (pre(EXPERIENCE)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (mc.playerController.func_78763_f()) {
            mc.mcProfiler.startSection("expBar");
            int cap = this.mc.thePlayer.xpBarCap();
            int left = width / 2 - 91;

            if (cap > 0) {
                short barWidth = 182;
                int filled = (int)(mc.thePlayer.experience * (float)(barWidth + 1));
                int top = height - 32 + 3;
                drawTexturedModalRect(left, top, 0, 64, barWidth, 5);

                if (filled > 0) {
                    drawTexturedModalRect(left, top, 0, 69, filled, 5);
                }
            }

            this.mc.mcProfiler.endSection();


            if (mc.playerController.func_78763_f() && mc.thePlayer.experienceLevel > 0) {
                mc.mcProfiler.startSection("expLevel");
                boolean flag1 = false;
                int color = flag1 ? 16777215 : 8453920;
                String text = "" + mc.thePlayer.experienceLevel;
                int x = (width - fontrenderer.getStringWidth(text)) / 2;
                int y = height - 31 - 4;
                fontrenderer.drawString(text, x + 1, y, 0);
                fontrenderer.drawString(text, x - 1, y, 0);
                fontrenderer.drawString(text, x, y + 1, 0);
                fontrenderer.drawString(text, x, y - 1, 0);
                fontrenderer.drawString(text, x, y, color);
                mc.mcProfiler.endSection();
            }
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(EXPERIENCE);
    }
    
    @Override
    protected void renderJumpBar(int width, int height) {
        bind(field_110324_m);
        if (pre(JUMPBAR)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.mcProfiler.startSection("jumpBar");
        float charge = mc.thePlayer.func_110319_bJ();
        final int barWidth = 182;
        int x = (width / 2) - (barWidth / 2);
        int filled = (int)(charge * (float)(barWidth + 1));
        int top = height - 32 + 3;

        drawTexturedModalRect(x, top, 0, 84, barWidth, 5);

        if (filled > 0) {
            this.drawTexturedModalRect(x, top, 0, 89, filled, 5);
        }

        mc.mcProfiler.endSection();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(JUMPBAR);
    }
    
    @Override
    protected void renderHUDText(int width, int height) {
        mc.mcProfiler.startSection("forgeHudText");
        ArrayList<String> left = new ArrayList<String>();
        ArrayList<String> right = new ArrayList<String>();

        if (mc.isDemo()) {
            long time = mc.theWorld.getTotalWorldTime();
            if (time >= 120500L) {
                right.add(StatCollector.translateToLocal("demo.demoExpired"));
            }
            else {
                right.add(String.format(StatCollector.translateToLocal("demo.remainingTime"), StringUtils.ticksToElapsedTime((int)(120500L - time))));
            }
        }


        if (this.mc.gameSettings.showDebugInfo) {
            mc.mcProfiler.startSection("debug");
            GL11.glPushMatrix();
            left.add("Minecraft " + MC_VERSION + " (" + this.mc.debug + ")");
            left.add(mc.debugInfoRenders());
            left.add(mc.getEntityDebug());
            left.add(mc.debugInfoEntities());
            left.add(mc.getWorldProviderName());
            left.add(null); //Spacer

            long max = Runtime.getRuntime().maxMemory();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;

            right.add("Used memory: " + used * 100L / max + "% (" + used / 1024L / 1024L + "MB) of " + max / 1024L / 1024L + "MB");
            right.add("Allocated memory: " + total * 100L / max + "% (" + total / 1024L / 1024L + "MB)");

            int x = MathHelper.floor_double(mc.thePlayer.posX);
            int y = MathHelper.floor_double(mc.thePlayer.posY);
            int z = MathHelper.floor_double(mc.thePlayer.posZ);
            float yaw = mc.thePlayer.rotationYaw;
            int heading = MathHelper.floor_double((double)(mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

            left.add(String.format("x: %.5f (%d) // c: %d (%d)", mc.thePlayer.posX, x, x >> 4, x & 15));
            left.add(String.format("y: %.3f (feet pos, %.3f eyes pos)", mc.thePlayer.boundingBox.minY, mc.thePlayer.posY));
            left.add(String.format("z: %.5f (%d) // c: %d (%d)", mc.thePlayer.posZ, z, z >> 4, z & 15));
            left.add(String.format("f: %d (%s) / %f", heading, Direction.directions[heading], MathHelper.wrapAngleTo180_float(yaw)));

            if (mc.theWorld != null && mc.theWorld.blockExists(x, y, z)) {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(x, z);
                left.add(String.format("lc: %d b: %s bl: %d sl: %d rl: %d",
                  chunk.getTopFilledSegment() + 15,
                  chunk.getBiomeGenForWorldCoords(x & 15, z & 15, mc.theWorld.getWorldChunkManager()).biomeName,
                  chunk.getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15),
                  chunk.getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15),
                  chunk.getBlockLightValue(x & 15, y, z & 15, 0)));
            }
            else {
                left.add(null);
            }

            left.add(String.format("ws: %.3f, fs: %.3f, g: %b, fl: %d", mc.thePlayer.capabilities.getWalkSpeed(), mc.thePlayer.capabilities.getFlySpeed(), mc.thePlayer.onGround, mc.theWorld.getHeightValue(x, z)));
            right.add(null);
            for (String s : FMLCommonHandler.instance().getBrandings().subList(1, FMLCommonHandler.instance().getBrandings().size())) {
                right.add(s);
            }
            GL11.glPopMatrix();
            mc.mcProfiler.endSection();
        }

        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(eventParent, left, right);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            for (int x = 0; x < left.size(); x++) {
                String msg = left.get(x);
                if (msg == null) continue;
                fontrenderer.drawStringWithShadow(msg, 2, 2 + x * 10, WHITE);
            }

            for (int x = 0; x < right.size(); x++) {
                String msg = right.get(x);
                if (msg == null) continue;
                int w = fontrenderer.getStringWidth(msg);
                fontrenderer.drawStringWithShadow(msg, width - w - 10, 2 + x * 10, WHITE);
            }
        }

        mc.mcProfiler.endSection();
        post(TEXT);
    }
    
    @Override
    protected void renderRecordOverlay(int width, int height, float partialTicks) {
        if (recordPlayingUpFor > 0) {
            mc.mcProfiler.startSection("overlayMessage");
            float hue = (float)recordPlayingUpFor - partialTicks;
            int opacity = (int)(hue * 256.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(width / 2), (float)(height - 48), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                int color = (recordIsPlaying ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                fontrenderer.drawString(recordPlaying, -fontrenderer.getStringWidth(recordPlaying) / 2, -4, color | (opacity << 24));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }

            mc.mcProfiler.endSection();
        }
    }
    
    @Override
    protected void renderChat(int width, int height) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, (float)(height - 48), 0.0F);
        mc.mcProfiler.startSection("chat");
        persistantChatGUI.drawChat(updateCounter);
        mc.mcProfiler.endSection();
        GL11.glPopMatrix();
    }
    
    @Override
    @SuppressWarnings("rawtypes")
	protected void renderPlayerList(int width, int height) {
        ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);
        NetClientHandler handler = mc.thePlayer.sendQueue;

        if (mc.gameSettings.keyBindPlayerList.pressed && (!mc.isIntegratedServerRunning() || handler.playerInfoList.size() > 1 || scoreobjective != null)) {
            this.mc.mcProfiler.startSection("playerList");
            List players = handler.playerInfoList;
            int maxPlayers = handler.currentServerMaxPlayers;
            int rows = maxPlayers;
            int columns = 1;

            for (columns = 1; rows > 20; rows = (maxPlayers + columns - 1) / columns) {
                columns++;
            }

            int columnWidth = 300 / columns;

            if (columnWidth > 150) {
                columnWidth = 150;
            }

            int left = (width - columns * columnWidth) / 2;
            byte border = 10;
            drawRect(left - 1, border - 1, left + columnWidth * columns, border + 9 * rows, Integer.MIN_VALUE);

            for (int i = 0; i < maxPlayers; i++) {
                int xPos = left + i % columns * columnWidth;
                int yPos = border + i / columns * 9;
                drawRect(xPos, yPos, xPos + columnWidth - 1, yPos + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);

                if (i < players.size()) {
                    GuiPlayerInfo player = (GuiPlayerInfo)players.get(i);
                    ScorePlayerTeam team = mc.theWorld.getScoreboard().getPlayersTeam(player.name);
                    String displayName = ScorePlayerTeam.formatPlayerName(team, player.name);
                    fontrenderer.drawStringWithShadow(displayName, xPos, yPos, 16777215);

                    if (scoreobjective != null) {
                        int endX = xPos + fontrenderer.getStringWidth(displayName) + 5;
                        int maxX = xPos + columnWidth - 12 - 5;

                        if (maxX - endX > 5) {
                            Score score = scoreobjective.getScoreboard().func_96529_a(player.name, scoreobjective);
                            String scoreDisplay = EnumChatFormatting.YELLOW + "" + score.getScorePoints();
                            fontrenderer.drawStringWithShadow(scoreDisplay, maxX - fontrenderer.getStringWidth(scoreDisplay), yPos, 16777215);
                        }
                    }

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    mc.func_110434_K().func_110577_a(Gui.field_110324_m);
                    int pingIndex = 4;
                    int ping = player.responseTime;
                    if (ping < 0) pingIndex = 5;
                    else if (ping < 150) pingIndex = 0;
                    else if (ping < 300) pingIndex = 1;
                    else if (ping < 600) pingIndex = 2;
                    else if (ping < 1000) pingIndex = 3;

                    zLevel += 100.0F;
                    drawTexturedModalRect(xPos + columnWidth - 12, yPos, 0, 176 + pingIndex * 8, 10, 8);
                    zLevel -= 100.0F;
                }
            }
        }
    }
    
    @Override
    protected void renderHealthMount(int width, int height) {
        Entity tmp = mc.thePlayer.ridingEntity;
        if (!(tmp instanceof EntityLivingBase)) return;

        bind(field_110324_m);

        if (pre(HEALTHMOUNT)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        mc.mcProfiler.endStartSection("mountHealth");
        EntityLivingBase mount = (EntityLivingBase)tmp;
        int health = (int)Math.ceil((double)mount.func_110143_aJ());
        float healthMax = mount.func_110138_aP();
        int hearts = (int)(healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20) {
            int top = height - right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i) {
                int x = left_align - i * 8 - 9;
                drawTexturedModalRect(x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    drawTexturedModalRect(x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    drawTexturedModalRect(x, top, HALF, 9, 9, 9);
            }

            right_height += 10;
        }
        post(HEALTHMOUNT);
    }

    private boolean pre(ElementType type) {
        return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
    }
    private void post(ElementType type) {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
    }
    private void bind(ResourceLocation res) {
        mc.func_110434_K().func_110577_a(res);
    }
}