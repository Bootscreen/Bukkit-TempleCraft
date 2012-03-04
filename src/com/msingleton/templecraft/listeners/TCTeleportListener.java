package com.msingleton.templecraft.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.util.Translation;

/**
 * This listener prevents players from warping out of the arena, if
 * they are in the arena session.
 */
// TO-DO: Fix the bug that causes the message when people get stuck in walls.
//public class TCTeleportListener extends PlayerListener
public class TCTeleportListener implements Listener
{
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		Player p = event.getPlayer();
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);

		if (!TempleManager.playerSet.contains(p))
		{
			return;
		}

		Temple temple = tp.currentTemple;

		if(temple == null)
		{
			return;
		}

		Location to = event.getTo();
		Location from = event.getFrom();

		if ((!TCUtils.isTCWorld(from.getWorld()) && TCUtils.isTCWorld(to.getWorld())) || to.getWorld().equals(p.getWorld()))
		{
			return;
		}

		TempleManager.tellPlayer(p, Translation.tr("teleportListener.deny"));
		event.setCancelled(true);
	}
}