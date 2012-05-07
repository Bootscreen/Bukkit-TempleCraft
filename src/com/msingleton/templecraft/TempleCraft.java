package com.msingleton.templecraft;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.steeleyes.catacombs.Catacombs;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.msingleton.templecraft.listeners.TCBlockListener;
import com.msingleton.templecraft.listeners.TCDamageListener;
import com.msingleton.templecraft.listeners.TCDisconnectListener;
import com.msingleton.templecraft.listeners.TCInventoryListener;
import com.msingleton.templecraft.listeners.TCMonsterListener;
import com.msingleton.templecraft.listeners.TCPlayerListener;
import com.msingleton.templecraft.listeners.TCTeleportListener;
import com.msingleton.templecraft.util.MobArenaClasses;
import com.msingleton.templecraft.util.Translation;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

/**
 * TempleCraft
 *
 * @author bootscreen
 * @author msingleton
 */
public class TempleCraft extends JavaPlugin
{
	/* Array of commands used to determine if a command belongs to TempleCraft
	 * or Mean Admins. */
	public Logger log;
	public static Logger debuglog;
	public List<String> ENABLED_COMMANDS;
	public double newVersion;
	public double currentVersion;
	public String newVersionString;
	public String currentVersionString;
	public static BukkitScheduler TCScheduler = null;
	public static TempleCraft TCPlugin = null;
	public static Permission permission = null;
	public static MVWorldManager MVWM = null;
	public static Catacombs catacombs = null;
	public static Economy economy = null;
	public static CharacterManager heroManager;
	public static String language;
	public static String fileExtention = ".tcf";
	public static ChatColor c1 = ChatColor.DARK_AQUA;
	public static ChatColor c2 = ChatColor.WHITE;
	public static ChatColor c3 = ChatColor.GREEN;
	public static boolean debugMode = false;

	public void onEnable()
	{	 	
		PluginDescriptionFile pdfFile = this.getDescription();

		currentVersionString = pdfFile.getVersion();
		currentVersion = TCUtils.convertVersion(currentVersionString);

		log = getServer().getLogger();

		// Create event listeners.
		PluginManager pm = getServer().getPluginManager();

		// Initialize convenience variables in ArenaManager.
		TempleManager.init(this);

		TCScheduler = getServer().getScheduler();
		TCPlugin = this;
		// Schedule to check the version every 30 minutes for an update. This is to update the most recent 
		// version so if an admin reconnects they will be warned about newer versions.
		// Thanks Sleaker for the permission to use his updatecheck code from vault 
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				try {
					newVersionString = TCUtils.updateCheck(currentVersionString);
					currentVersion = TCUtils.convertVersion(currentVersionString);
					newVersion = TCUtils.convertVersion(newVersionString);
					if (newVersion > currentVersion) {
						log.warning("[" + getDescription().getName() + "] TempleCraft " + newVersionString + " is out! You are running: TempleCraft " + currentVersionString);
						log.warning("[" + getDescription().getName() + "] Update TempleCraft at: http://dev.bukkit.org/server-mods/templecraft-bootscreen");
					}
				} catch (Exception e) {
					e.printStackTrace();
					// ignore exceptions
				}
			}

		}, 0, 432000);

		setupTranslations();
		setupPermissions();
		setupEconomy();
		setupHeroes();
		setupMultiverse();
		setupCatacombs();

		ENABLED_COMMANDS = TCUtils.getEnabledCommands();

		// Bind the /tc and /tcraft commands to MACommands.
		getCommand("tct").setExecutor(new TCCommands(this));

		pm.registerEvents(new TCEnabledCommands(this), this);
		pm.registerEvents(new TCPlayerListener(), this);
		pm.registerEvents(new MobArenaClasses(this), this);
		pm.registerEvents(new TCTeleportListener(), this);
		pm.registerEvents(new TCDisconnectListener(this), this);
		pm.registerEvents(new TCBlockListener(), this);
		pm.registerEvents(new TCDamageListener(), this);
		pm.registerEvents(new TCMonsterListener(this), this);
		pm.registerEvents(new TCInventoryListener(), this);

		System.out.println(Translation.tr("enableMessage", pdfFile.getName(), pdfFile.getVersion()));

		debugMode = TCUtils.getBoolean(TCUtils.getConfig("config"), "settings.debug", false);
		if(debugMode)
		{
			try 
			{
				new File("plugins/TempleCraft/debug").mkdir();
				FileHandler fh = new FileHandler("plugins/TempleCraft/debug/debug.log", true);
				//fh.setFormatter(new SimpleFormatter());
				fh.setFormatter(new Formatter() {
					public String format(LogRecord record) {
						SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
						String dateString = sd.format(new Date(record.getMillis())); 
						String split[] = record.getMessage().split(" - ", 4);
						return dateString + " " + split[0] + " " + split[1] + " Line " + split[2] + "\n"
						+ record.getLevel() + ": " + split[3] + "\n\n";
					}
				});
				debuglog = Logger.getAnonymousLogger();
				debuglog.setUseParentHandlers(false);
				for(Handler h :debuglog.getHandlers())
				{
					debuglog.removeHandler(h);
				}
				debuglog.addHandler(fh);
				System.out.println("[TempleCraft] DEBUG MODE enabled.");
				String message = "Debug gestartet. Liste aller Plugins:\n";
				for(Plugin pl : getServer().getPluginManager().getPlugins())
				{
					message += "- " + pl.getDescription().getName() + " v." + pl.getDescription().getVersion() + "\n";
				}
				TCUtils.debugMessage(message);
			} 
			catch (SecurityException e) 
			{
				debugMode = false;
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				debugMode = false;
				e.printStackTrace();
			}
		}
	}

	public void onDisable()
	{
		TCUtils.debugMessage("Debug beendet.");
		if(debuglog != null)
		{		
			for(Handler h :debuglog.getHandlers())
			{
				debuglog.removeHandler(h);
				h.close();
			}
		}
		//permissionHandler = null;
		TempleManager.SBManager.save();
		TempleManager.removeAll();
		TCUtils.deleteTempWorlds();
		TCUtils.cleanConfigFiles();
	}

	private void setupTranslations()
	{			
		File configFile = TCUtils.getConfig("config");
		language = TCUtils.getString(configFile, "settings.language", "en-US");
		Translation.reload(new File(getDataFolder(), "templecraft-"+language+".csv"));

		if(Translation.getVersion()<1)
		{
			TCUtils.copyFromJarToDisk("templecraft-"+language+".csv", getDataFolder());
			log.log(Level.INFO, "[TempleCraft] copied new translation file for "+language+" to disk.");
			Translation.reload(new File(getDataFolder(), "templecraft-"+language+".csv"));
		}
	}

	private void setupMultiverse()
	{
		Plugin multiverse = this.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if (multiverse == null)
		{
			return;
		}

		MVWM = ((MultiverseCore) multiverse).getMVWorldManager();
		if(MVWM != null)
		{
			System.out.println("[TempleCraft] Hooked into " + multiverse.getDescription().getName() + " Version "+ multiverse.getDescription().getVersion());
		}
	}

	private void setupCatacombs()
	{
		Plugin Cataplugin = this.getServer().getPluginManager().getPlugin("Catacombs");
		if (Cataplugin == null)
		{
			return;
		}
		else
		{
			System.out.println("[TempleCraft] Hooked into " + Cataplugin.getDescription().getName() + " Version "+ Cataplugin.getDescription().getVersion());
		}
	}

	private Boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null)
		{
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private Boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
		{
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	private void setupHeroes()
	{
		Plugin heroes = this.getServer().getPluginManager().getPlugin("Heroes");
		if (heroes == null)
		{
			return;
		}

		heroManager = ((Heroes) heroes).getCharacterManager();
		if(heroManager != null)
		{
			System.out.println("[TempleCraft] Hooked into Heroes");
		}
	}

	public File getPluginFile()
	{
		return getFile();
	}
}