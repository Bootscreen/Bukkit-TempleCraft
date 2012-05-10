package com.msingleton.templecraft.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.custommobs.CustomMob;
import com.msingleton.templecraft.custommobs.CustomMobAbility;
import com.msingleton.templecraft.games.Game;

public class AbilityTask implements Runnable 
{
	public Game game;
	public CustomMob custommob;
	private List<CustomMobAbility> abilitys = new ArrayList<CustomMobAbility>();
	private CustomMobAbility ability = null;

	public AbilityTask(Game game, CustomMob cm)
	{
		this.game = game;
		this.custommob = cm;
		this.abilitys = cm.getAbilitys();
	}

	public AbilityTask(Game game, CustomMob cm, CustomMobAbility ability)
	{
		this.game = game;
		this.custommob = cm;
		this.ability = ability;
	}


	public static int taskID;

	@Override
	public void run() 
	{
		try
		{
			if(!game.isRunning || custommob.getLivingEntity().isDead() || custommob.getHealth() <= 0)
			{
				TempleCraft.TCScheduler.cancelTask(taskID);
				game.AbilityTaskIDs.remove(custommob);
			}
			else
			{
				if(!abilitys.isEmpty())
				{
					Random random = new Random();
					int index = random.nextInt(abilitys.size());
					CustomMobAbility cma = abilitys.get(index);
					cma.run(game,custommob.getLivingEntity());
				}
				else if(ability != null)
				{
					ability.run(game,custommob.getLivingEntity());
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
