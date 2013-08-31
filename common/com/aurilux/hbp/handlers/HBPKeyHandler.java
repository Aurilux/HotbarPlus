package com.aurilux.hbp.handlers;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import com.aurilux.hbp.client.ui.HBPGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class HBPKeyHandler extends KeyHandler {
	public static KeyBinding key = new KeyBinding("Hotbar Toggle", Keyboard.KEY_LCONTROL);

    public HBPKeyHandler() {
        super(new KeyBinding[] {key}, new boolean[] {false});
    }
    
	@Override
	public String getLabel() {
		return "Hotbar Plus";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
		//does nothing
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        Minecraft mc = Minecraft.getMinecraft();
        if (tickEnd && kb.keyCode == key.keyCode && mc.ingameGUI != null && mc.ingameGUI instanceof HBPGui) {
        	HBPGui gui = ((HBPGui) mc.ingameGUI);
        	gui.toggle();
        }
	}

	@Override
	public EnumSet<TickType> ticks() {
        return EnumSet.allOf(TickType.class);
	}

}
