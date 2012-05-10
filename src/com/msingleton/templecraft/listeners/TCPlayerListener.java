package com.msingleton.templecraft.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.msingleton.templecraft.TCPermissionHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.games.Arena;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.Spleef;
import com.msingleton.templecraft.scoreboards.ScoreBoard;
import com.msingleton.templecraft.util.Translation;

//public class TCPlayerListener  extends PlayerListener
public class TCPlayerListener implements Listener
{
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{	

		if(!TempleManager.isEnabled)
		{
			return;
		}

		Player p = event.getPlayer();
		Action a = event.getAction();
		
		// Expands and contracts ScoreBoards
		if(p.isSneaking() && TCPermissionHandler.hasPermission(p, "templecraft.placesigns"))
		{
			// Expand
			if(a.equals(Action.RIGHT_CLICK_BLOCK))
			{
				Block b = event.getClickedBlock();
				if(b.getState() instanceof Sign)
				{
					Sign sign = (Sign) b.getState();
					ScoreBoard sb = TempleManager.SBManager.getScoreBoardBySign(sign);
					if(sb != null)
					{
						sb.expandBoard();
						event.setCancelled(true);
					}
				}
			}
			// Contract
			if(a.equals(Action.LEFT_CLICK_BLOCK))
			{
				Block b = event.getClickedBlock();
				if(b.getState() instanceof Sign)
				{
					Sign sign = (Sign) b.getState();
					ScoreBoard sb = TempleManager.SBManager.getScoreBoardBySign(sign);
					if(sb != null)
					{
						sb.contractBoard();
						event.setCancelled(true);
					}
				}
			}
		}

		if(p.getGameMode().equals(GameMode.CREATIVE))
		{
			if(!p.isSneaking())
			{
				Block b = event.getClickedBlock();
				if(b != null)
				{
					if(b.getState() instanceof Sign)
					{
						Sign sign = (Sign) b.getState();
						sign.update(true);
						String Line1 = sign.getLine(0);
						if(Line1.equals("[TempleCraft]") || Line1.equals("[TC]") || Line1.equals("[TCB]") || Line1.equals("[TempleCraftS]") || Line1.equals("[TCS]"))
						{
							event.setCancelled(true);
						}
					}
				}
			}
			return;
		}

		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Game game = tp.currentGame;

		// Special Use of TNT for Arena Mode
		if(game != null && (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)))
		{
			if(!game.isRunning || game.deadSet.contains(tp))
			{
				event.setCancelled(true);
				return;
			}
			
			if(game instanceof Arena)
			{
				if(p.getInventory().getItemInHand().getTypeId() == 46)
				{
					if(a.equals(Action.RIGHT_CLICK_BLOCK))
					{
						event.setCancelled(true);
					}
					ItemStack item = p.getInventory().getItemInHand();
					if(item.getAmount() <= 1)
					{
						p.getInventory().remove(item);
					}
					else
					{
						item.setAmount(item.getAmount()-1);
					}
					((Arena)tp.currentGame).throwTNT(p);
					event.setCancelled(true);
				}
			}
		}

		// All methods bellow have clicked blocks
		if(!event.hasBlock())
		{
			return;
		}

		Block b = event.getClickedBlock();

		// Break wool instantly if playing Spleef
		if(game != null && a.equals(Action.LEFT_CLICK_BLOCK))
		{
			if(game instanceof Spleef)
			{
				if(b.getTypeId() == 35)
				{
					((Spleef)game).brokenBlockMap.put(b.getLocation(), b.getTypeId()+":"+b.getData());
					b.setTypeId(0);
				}
			}
		}

		// Signs
		if (b.getState() instanceof Sign)
		{   
			// Cast the block to a sign to get the text on it.
			Sign sign = (Sign) event.getClickedBlock().getState();
			if(game == null)
			{
				handleSignClicked(p, sign);
			}
		}

		if (!TempleManager.playerSet.contains(p))
		{
			return;
		}

		if(game == null)
		{
			return;
		}

		// Start Block
		if (b.getTypeId() == 42 && game.lobbyLocMap.containsKey(b.getLocation()))
		{
			game.hitStartBlock(p,game.lobbyLocMap.get(b.getLocation()));
			// End Block
		}
		else if (!game.deadSet.contains(p) && b.getTypeId() == 42 && game.rewardLocMap.containsKey(b.getLocation()))
		{
			game.hitRewardBlock(p,game.rewardLocMap.remove(b.getLocation()));
			return;
		}
		else if (!game.deadSet.contains(p) && b.getTypeId() == 41 && game.endLocSet.contains(b.getLocation()))
		{
			event.setCancelled(true);
			game.hitEndBlock(p);
			return;
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if(TempleManager.templeEditMap.containsValue(event.getTo().getWorld()))
		{
			event.getPlayer().setGameMode(GameMode.CREATIVE);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player p = event.getPlayer();

		if(p.getGameMode().equals(GameMode.CREATIVE))
		{
			return;
		}

		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Temple temple = tp.currentTemple;

		// For Entering Temple
		if(temple == null)
		{
			if(event.getTo().distance(event.getFrom())>0.05)
			{
				Set<Sign> signs = new HashSet<Sign>();
				Block b = p.getLocation().getBlock();
				for(int i = -2; i<=2;i++)
				{
					for(int j = -3; j<0;j++)
					{
						for(int k = -2; k<=2;k++)
						{
							Block sign = b.getRelative(i,j,k);
							if(sign.getState() instanceof Sign)
								signs.add((Sign)sign.getState());
						}
					}
				}
				if(signs.isEmpty())
				{
					tp.sensedSign = null;
					tp.canAutoTele = true;
					tp.stopEnterTimer();
					return;
				}
				if(tp.canAutoTele)
				{
					if(tp.sensedSign == null)
					{
						for(Sign sign : signs)
						{
							if(sign.getLine(0).equals("[TCS]") || sign.getLine(0).equals("[TempleCraftS]") || sign.getLine(3).equals("sensor"))
							{
								tp.sensedSign = sign;
								TempleManager.tellPlayer(p, Translation.tr("playerListener.entranceFound"));
								tp.startEnterTimer(p);
							}
						}
					}
					else
					{
						tp.resetEnterTimer(p);
					}
				}
			}
			return;
		}

		if (!TCUtils.isTCWorld(event.getPlayer().getWorld()))
		{
			return;
		}

		if(!TempleManager.isEnabled || TempleManager.templeSet.isEmpty())
		{
			return;
		}

		Game game = tp.currentGame;

		if(game == null)
		{
			return;
		}

		game.onPlayerMove(event);
	}

	public static void handleSignClicked(Player p, Sign sign)
	{
		String Line1 = sign.getLine(0);
		String Line2 = sign.getLine(1);
		String Line3 = sign.getLine(2);
		//String Line4 = sign.getLine(3);

		if(TempleManager.playerSet.contains(p) || TCUtils.isTCEditWorld(p.getWorld()))
		{
			return;
		}

		if(!Line1.equals("[TempleCraft]") && !Line1.equals("[TC]") && !Line1.equals("[TCB]") && !Line1.equals("[TempleCraftS]") && !Line1.equals("[TCS]"))
		{
			return;
		}

		Temple temple = TCUtils.getTempleByName(Line2.toLowerCase());

		if(temple == null)
		{
			TempleManager.tellPlayer(p, Translation.tr("templeDNE", Line2));
			return;
		}

		if(!temple.isSetup)
		{
			TempleManager.tellPlayer(p, Translation.tr("templeNotSetup", temple.templeName));
			return;
		}

		if(temple.maxPlayersPerGame < 1 && temple.maxPlayersPerGame != -1)
		{
			TempleManager.tellPlayer(p, Translation.tr("templeFull", temple.templeName));
			return;
		}

		String mode;
		if(Line3.equals(""))
		{
			mode = "adventure";
		}
		else
		{
			mode = Line3.toLowerCase();
		}

		if(!TempleManager.modes.contains(mode))
		{
			TempleManager.tellPlayer(p, Translation.tr("modeDNE", mode));
			return;
		}

		for(Game game : TempleManager.gameSet)
		{
			if(!game.isRunning && game.maxPlayers != game.playerSet.size() && game.gameName.contains(temple.templeName) && game.gameName.contains(mode.substring(0,3)))
			{
				if(game.maxdeaths > -1 &&
					game.playerDeathMap.containsKey(p) &&
					game.playerDeathMap.get(p) > game.maxdeaths)
				{
					TempleManager.tellPlayer(p, Translation.tr("game.tooMuchDeaths"));
				}
				else
				{
					game.playerJoin(p);
					return;
				}
			}
		}

		if(!TempleManager.gameSet.isEmpty())
		{
			TempleManager.tellPlayer(p, Translation.tr("game.allInProgress"));
		}

		String gameName = TCUtils.getUniqueGameName(temple.templeName, mode);
		Game game = TCUtils.newGame(gameName, temple, mode);
		if(game == null)
		{
			TempleManager.tellPlayer(p, Translation.tr("newGameFail"));
		}
		else
		{
			TempleManager.tellPlayer(p, Translation.tr("newGame"));
			game.playerJoin(p);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		if(!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player p = (Player)event.getEntity();
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Game game = tp.currentGame;

		if (!TempleManager.playerSet.contains(p))
		{
			return;
		}

		if(game instanceof Arena || game instanceof Spleef)
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Adds liquid blocks to the blockset when players empty their buckets.
	 */
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{			
		Player p = event.getPlayer();

		if (!TempleManager.playerSet.contains(p))
		{
			return;
		}

		if (!TempleManager.isEnabled)
		{
			event.getBlockClicked().setTypeId(0);
			event.setCancelled(true);
			return;
		}

		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Game game = tp.currentGame;

		Block liquid = event.getBlockClicked();
		if(game.playerSet.contains(p))
		{
			game.tempBlockSet.add(liquid);
		}
	}

	@EventHandler
	public void onEntityTame(EntityTameEvent event)
	{	
	    if (event.getOwner() instanceof Player)
	    {
	        Player player = (Player) event.getOwner();
	        
	        if (!TempleManager.playerSet.contains(player))
			{
				return;
			}

			if (!TempleManager.isEnabled)
			{
				return;
			}

			TemplePlayer tp = TempleManager.templePlayerMap.get(player);
			
			tp.tamedMobSet.add(event.getEntity());
	    }
	}
}
