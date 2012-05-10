package com.msingleton.templecraft.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.util.WorldManager;

public class TCWorldListener implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldUnload(WorldUnloadEvent event) 
	{
		if (!event.isCancelled() && TCUtils.isTCWorld(event.getWorld()))
		{
			WorldManager.clearWorldReference(event.getWorld());
			
			/*if(TempleCraft.worldguard != null)
			{
				TempleCraft.worldguard.getGlobalRegionManager().unload(event.getWorld().getName());
			}*/
		}
	}

	/*@EventHandler
	public void onWorldLoad(WorldLoadEvent event) 
	{
		if (TCUtils.isTCWorld(event.getWorld()))
		{
			if(TempleCraft.worldguard != null)
			{
				TempleCraft.worldguard.getGlobalRegionManager().unload(event.getWorld().getName());
			}
		}
	}*/
}
