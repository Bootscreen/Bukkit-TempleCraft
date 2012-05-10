package com.msingleton.templecraft.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.custommobs.CustomMob;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.tasks.AbilityTask;

public class TCChunkListener implements Listener 
{

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		if(TCUtils.isTCWorld(event.getWorld()))
		{
			Game game = TCUtils.getGameByWorld(event.getWorld());
			
			if(game != null && !game.tempMobLoc.isEmpty())
			{
				for(Entity e : game.tempMobLoc.get(event.getChunk()))
				{
					if(e instanceof LivingEntity)
					{
						LivingEntity le = event.getWorld().spawnCreature(e.getLocation(), e.getType());
						
						CustomMob cmold = game.customMobManager.getMob(e);
						
						if(cmold != null)
						{
							CustomMob cmnew = new CustomMob(le);
							cmnew.setHealth(cmold.getMaxHealth());
							cmnew.setAbilitys(cmold.getAbilitys());
							cmnew.setDMGMultiplikator(cmold.getDMGMultiplikator());
							cmnew.setSize(cmold.getSize());
							cmnew.setSpawnProperties(cmold.getSpawnProperties());

							int id = TempleCraft.TCScheduler.scheduleAsyncRepeatingTask(TempleCraft.TCPlugin, new AbilityTask(game, cmnew), 100L, 100L);
							AbilityTask.taskID = id;
							game.AbilityTaskIDs.put(cmnew,id);
							
							if(le instanceof Slime)
							{
								Slime slime = (Slime) le;
								slime.setSize(cmold.getSize());
							}
							
							game.customMobManager.RemoveMob(cmold);
						}
					}
				}
				game.tempMobLoc.remove(event.getChunk());			
			}
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		if(TCUtils.isTCWorld(event.getWorld()))
		{
			Game game = TCUtils.getGameByWorld(event.getWorld());
			
			if(game != null)
			{
				if(!game.isEnding)
				{
					game.tempMobLoc.put(event.getChunk(), event.getChunk().getEntities());		
					for(Entity e : event.getChunk().getEntities())
					{
						CustomMob cmob = game.customMobManager.getMob(e);
						
						if(cmob != null)
						{
							TempleCraft.TCScheduler.cancelTask(game.AbilityTaskIDs.get(cmob));
							game.AbilityTaskIDs.remove(cmob);
						}
					}
				}
			}
		}
	}
}
