package com.msingleton.templecraft.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;

/**
 * This listener acts when a player is kicked or disconnected
 * from the server. If 15 seconds pass, and the player hasn't
 * reconnected, the player is forced to leave the arena.
 */
public class TCDisconnectListener extends PlayerListener
{
    public TCDisconnectListener(TempleCraft instance)
    {
    }
    
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	if(!TempleManager.isEnabled)
    		return;
    	
    	Player p = event.getPlayer();
		if(TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.restorePlayerInventory(p);
		if (TempleManager.playerSet.contains(p))
			TempleManager.playerLeave(p);
		if(TempleManager.locationMap.containsKey(p))
			p.teleport(TempleManager.locationMap.get(p));
    }
    
    public void onPlayerKick(PlayerKickEvent event)
    {
    	if(!TempleManager.isEnabled)
    		return;
    	
    	Player p = event.getPlayer();
		if(TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.restorePlayerInventory(p);
		if (TempleManager.playerSet.contains(p))
			TempleManager.playerLeave(p);
		if(TempleManager.locationMap.containsKey(p))
			p.teleport(TempleManager.locationMap.get(p));
    }

	public void onPlayerJoin(PlayerJoinEvent event)
    {		
    	if(!TempleManager.isEnabled)
    		return;
    	
    	final Player p = event.getPlayer();
    	TempleManager.templePlayerMap.put(p, new TemplePlayer(p));
        
        if (!TempleManager.checkUpdates)
            return;
        
        if (!p.isOp())
            return;
        
        TempleManager.server.getScheduler().scheduleSyncDelayedTask(TempleManager.plugin,
            new Runnable()
            {
                public void run()
                {
                    TCUtils.checkForUpdates(p, false);
                }
            }, 100);
    }
}