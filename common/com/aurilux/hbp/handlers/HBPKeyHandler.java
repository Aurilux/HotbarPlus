package com.aurilux.hbp.handlers;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import com.aurilux.hbp.client.ui.HBPGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class HBPKeyHandler extends KeyHandler {
	public static KeyBinding toggleKey = new KeyBinding("Hotbar Toggle", Keyboard.KEY_LCONTROL);
	public static KeyBinding firstOption = new KeyBinding("V-key", Keyboard.KEY_V);
	public static KeyBinding secondOption = new KeyBinding("B-key", Keyboard.KEY_B);
	
	public static boolean wasPressed;
	
	private Minecraft mc;

    public HBPKeyHandler() {
        super(new KeyBinding[] {toggleKey}, new boolean[] {false});
        wasPressed = false;
        mc = Minecraft.getMinecraft();
    }
    
    public static boolean optionKeyPressed() {
    	return firstOption.isPressed() || secondOption.isPressed();
    }
    
	@Override
	public String getLabel() {
		return "Hotbar Plus";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if (tickEnd && mc.ingameGUI != null && mc.ingameGUI instanceof HBPGui) {
        	if (kb.keyCode == firstOption.keyCode || kb.keyCode == secondOption.keyCode) {
        		wasPressed = true;
        	}
        }
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        if (tickEnd && mc.ingameGUI != null && mc.ingameGUI instanceof HBPGui) {
        	if (kb.keyCode == toggleKey.keyCode) {
        		HBPGui gui = ((HBPGui) mc.ingameGUI);
        		gui.toggle();
        	}
        }
	}

	@Override
	public EnumSet<TickType> ticks() {
        return EnumSet.allOf(TickType.class);
	}

}
