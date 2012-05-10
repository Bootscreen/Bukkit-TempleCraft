package com.msingleton.templecraft.custommobs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import com.msingleton.templecraft.TCUtils;

public class CustomMobUtils 
{
	protected static File configFile				= null;
	public static Set<CustomMobType> custommobs	= new HashSet<CustomMobType>();
	public static Set<String> custommobnames		= new HashSet<String>();

	public static void loadBosses()
	{
		configFile	= TCUtils.getConfig("bosses");
		if(configFile.exists())
		{
			custommobnames = getBossNames();
			if(custommobnames != null && !custommobnames.isEmpty())
			{
				custommobs  = getBosses(configFile, "bosses.");
			}
		}
	}

	private static Set<String> getBossNames()
	{
		YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);

		if(!c.isSet("bosses"))
		{
			try 
			{
				c.set("bosses.testBoss.Spawning.MobType", "Zombie");
				c.set("bosses.testBoss.Spawning.DMGMultiplicator", 3);
				c.set("bosses.testBoss.Spawning.MaxHealth", 200);
				c.set("bosses.testBoss.Spawning.Size", 0);
				c.set("bosses.testBoss.Spawning.Count", 1);
				c.set("bosses.testBoss.Spawning.SpawnRange", 5);
				c.set("bosses.testBoss.Spawning.Abilities", "");
				c.save(configFile);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//return null;

		return c.getConfigurationSection("bosses").getKeys(false);
	}

	private static Set<CustomMobType> getBosses(File configFile, String path)
	{
		YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);

		Set<CustomMobType> result = new HashSet<CustomMobType>();
		//List<String> abilities_random_string			  = new ArrayList<String>();
		//List<String> abilities_rotation_string			  = new ArrayList<String>();
		Map<String, Object> abilities_random_set			  = new HashMap<String, Object>();
		Map<String, Object> abilities_rotation_set			  = new HashMap<String, Object>();
		for (String s : custommobnames)
		{
			CustomMobType cmt = new CustomMobType();
			try
			{
				cmt.setName(s);
				cmt.setMobtype(EntityType.fromName(c.getString(path + s + ".Spawning.MobType", null)));
				cmt.setDmgmulti(c.getInt(path + s + ".Spawning.DMGMultiplicator", 0));
				cmt.setMaxhealth(c.getInt(path + s + ".Spawning.MaxHealth", 0));
				cmt.setSize(c.getInt(path + s + ".Spawning.Size", 0));
				cmt.setCount(c.getInt(path + s + ".Spawning.Count", 1));
				cmt.setRange(c.getInt(path + s + ".Spawning.SpawnRange", 1));
				cmt.setOldabilitys(c.getString(path + s + ".Abilities", null));
				if(c.isSet(path + s + ".Rotation"))
				{
					abilities_rotation_set = c.getConfigurationSection(path + s + ".Rotation").getValues(false);
					for(String ability : abilities_rotation_set.keySet())
					{
						if(CustomMobAbility.fromString(ability) != null)
						{
							cmt.addAbilities_rotation(CustomMobAbility.fromString(ability), Integer.parseInt(abilities_rotation_set.get(ability).toString()));
						}
					}
				}
				if(c.isSet(path + s + ".Random"))
				{
					abilities_random_set = c.getConfigurationSection(path + s +".Random").getValues(false);
					for(String ability : abilities_random_set.keySet())
					{
						if(CustomMobAbility.fromString(ability) != null)
						{
							cmt.addAbilities_random(CustomMobAbility.fromString(ability), Integer.parseInt(abilities_random_set.get(ability).toString()));
						}
					}
				}
				result.add(cmt);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			//result.put(s, c.getString(path + s + "." + type, null));
		}

		return result;
	}

	public Set<CustomMobType> getBosses()
	{
		return custommobs;
	}

	public static CustomMobType getBoss(String name)
	{
		for(CustomMobType cmt : custommobs)
		{
			if(cmt.getName().equals(name))
			{
				return cmt;
			}
		}
		return null;
	}

	public static boolean bossTypeExists(String name)
	{
		for(CustomMobType cmt : custommobs)
		{
			if(cmt.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}


}
