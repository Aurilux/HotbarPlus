package com.aurilux.hbp;

import com.aurilux.hbp.handlers.HBPKeyHandler;
import com.aurilux.hbp.handlers.HBPPacketHandler;
import com.aurilux.hbp.handlers.HBPTickHandler;
import com.aurilux.hbp.lib.HBP_Ref;
import com.aurilux.hbp.proxy.CommonHBPProxy;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = HBP_Ref.MOD_ID, name = HBP_Ref.MOD_NAME, version = HBP_Ref.VERSION)
@NetworkMod(channels = {"HBP"}, clientSideRequired = true, serverSideRequired = false, packetHandler = HBPPacketHandler.class)

public class HotbarPlus_ModBase {
	
	@Instance(HBP_Ref.MOD_ID)
    public static HotbarPlus_ModBase instance;

    @SidedProxy(clientSide = HBP_Ref.CLIENT_PROXY_CLASS, serverSide = HBP_Ref.SERVER_PROXY_CLASS)
    public static CommonHBPProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
        KeyBindingRegistry.registerKeyBinding(new HBPKeyHandler());
        TickRegistry.registerTickHandler(new HBPTickHandler(), Side.CLIENT);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
	}
}