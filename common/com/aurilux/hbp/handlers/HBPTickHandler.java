package com.aurilux.hbp.handlers;

import java.util.EnumSet;

import com.aurilux.hbp.client.ui.HBPGui;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class HBPTickHandler implements ITickHandler {

	//private variable to ensure I only change the in-game gui once (no need to do it after the first)
    private boolean ticked = false;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		//this does nothing
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (!ticked && Minecraft.getMinecraft().ingameGUI != null) {
	        Minecraft.getMinecraft().ingameGUI = new HBPGui(Minecraft.getMinecraft());
	        ticked = true;
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT, TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "Hotbar Plus GUI";
	}
}
