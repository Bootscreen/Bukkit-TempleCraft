package com.msingleton.templecraft.tasks;

import org.bukkit.entity.Player;

import com.msingleton.templecraft.games.Game;

public class EndTask implements Runnable{
	public Game game;
	public Player player;

	public EndTask(Game game, Player p)
	{
		this.game = game;
		this.player = p;

	}
	
	public static int taskID;

	@Override
	public void run() 
	{
		try
		{
			game.hitEndBlock(player);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}

	}
}
