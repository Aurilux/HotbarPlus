package com.aurilux.hbp.handlers;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class HBPPacketHandler implements IPacketHandler {

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player playerEntity) {
    	if (packet.channel.equals("HBP")) {
            switchItems(packet, playerEntity);
    	}
    }
    
    private void switchItems(Packet250CustomPayload packet, Player playerEntity) {
    	if(packet.data != null) {
    		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
	    	
	        int selection;
	        int currentItem;
	       
	        try {
	            selection = inputStream.readInt();
	            currentItem = inputStream.readInt();
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	            return;
	        }
	    	EntityPlayerMP player = (EntityPlayerMP) playerEntity;
	    	InventoryPlayer inv = player.inventory;
	        
	        ItemStack toSwitch = inv.mainInventory[selection]; //stores the selected item
	    	ItemStack hotbarItem = inv.mainInventory[currentItem]; //stores the item from the hotbar
	    	
	    	inv.mainInventory[currentItem] = toSwitch; //move the item from the inventory to the hotbar
	    	inv.mainInventory[selection] = hotbarItem; //move the hotbar item to the inventory
	    }
    }
}