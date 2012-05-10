package com.msingleton.templecraft.util;

/* 
	This JavaFile WorldManager.java stands under the MIT-License and 
	was part of the project MyWorld from bergerkiller (http://dev.bukkit.org/profiles/bergerkiller):
	http://dev.bukkit.org/server-mods/my-worlds
	http://forums.bukkit.org/threads/myworlds.31718
	http://wiki.bukkit.org/MyWorlds-Plugin
	https://github.com/bergerkiller/MyWorlds
	
	
	The MIT License

	Copyright (c) <2012> <bergerkiller>

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

 */

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.server.RegionFile;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.chunkstore.ChunkStore;
import org.getspout.spoutapi.chunkstore.SimpleChunkDataManager;
import org.getspout.spoutapi.chunkstore.SimpleRegionFile;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TempleCraft;

//import com.bergerkiller.bukkit.mw.Tag.Type;

@SuppressWarnings("rawtypes")
public class WorldManager
{
	private static HashMap regionfiles;
	private static Field rafField;
	
	public static boolean init()
	{
    	TempleCraft.TCPlugin.log.info("[TempleCraft] Try to init WorldManager!");
    	TCUtils.debugMessage("Try to init WorldManager.", Level.INFO);
		try 
		{
        	Field a = net.minecraft.server.RegionFileCache.class.getDeclaredField("a");
        	a.setAccessible(true);
			regionfiles = (HashMap) a.get(null);
			rafField = net.minecraft.server.RegionFile.class.getDeclaredField("c");
			rafField.setAccessible(true);
        	TempleCraft.TCPlugin.log.info("[TempleCraft] Successfully bound variable to region file cache.");
        	TempleCraft.TCPlugin.log.info("[TempleCraft] File references to unloaded worlds will be cleared!");
        	TCUtils.debugMessage("Successfully bound variable to region file cache.");
			TCUtils.debugMessage("File references to unloaded worlds will be cleared!");
			return true;
		} 
		catch (Throwable t)
		{
			TempleCraft.TCPlugin.log.warning("[TempleCraft] Failed to bind to region file cache.");
			TempleCraft.TCPlugin.log.warning("[TempleCraft] Files will stay referenced after being unloaded!");
			TCUtils.debugMessage("Failed to bind to region file cache.", Level.WARNING);
			TCUtils.debugMessage("Files will stay referenced after being unloaded!", Level.WARNING);
			t.printStackTrace();
			return false;
		}
	}
	
	public static void deinit()
	{
		regionfiles = null;
		rafField = null;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean clearWorldReference(World world) 
	{
		String worldname = world.getName();
		TempleCraft.TCPlugin.log.info("[TempleCraft] try to remove world reference for '" + worldname + "'!");
		TCUtils.debugMessage("[TempleCraft] try to remove world reference for '" + worldname + "'!");
		if (regionfiles == null)
		{
			return false;
		}
		if (rafField == null) 
		{
			return false;
		}
		ArrayList<Object> removedKeys = new ArrayList<Object>();
		try
		{
			for (Object o : regionfiles.entrySet()) 
			{
				Map.Entry e = (Map.Entry) o;
				File f = (File) e.getKey();
				if (f.toString().startsWith("." + File.separator + worldname)) 
				{
					SoftReference ref = (SoftReference) e.getValue();
					try 
					{
						RegionFile file = (RegionFile) ref.get();
						if (file != null)
						{
							RandomAccessFile raf = (RandomAccessFile) rafField.get(file);
							raf.close();
							removedKeys.add(f);
						}
					}
					catch (Exception ex) 
					{
						TempleCraft.TCPlugin.log.warning("[TempleCraft] Exception while removing world reference for '" + worldname + "'!");
						TCUtils.debugMessage("Exception while removing world reference for '" + worldname + "'!\n" + ex.getMessage(), Level.WARNING);
						ex.printStackTrace();
					}
				}
			}
		} 
		catch (Exception ex)
		{ 
			TempleCraft.TCPlugin.log.warning("[TempleCraft] Exception while removing world reference for '" + worldname + "'!");
			TCUtils.debugMessage("Exception while removing world reference for '" + worldname + "'!\n" + ex.getMessage(), Level.WARNING);
			ex.printStackTrace();
		}
		for (Object key : removedKeys)
		{
			regionfiles.remove(key);
		}
		
		//Spout
		try 
		{
			if (Bukkit.getServer().getPluginManager().isPluginEnabled("Spout")) 
			{
				//Close the friggin' meta streams!
				SimpleChunkDataManager manager = (SimpleChunkDataManager) SpoutManager.getChunkDataManager();
				Field chunkstore = SimpleChunkDataManager.class.getDeclaredField("chunkStore");
				chunkstore.setAccessible(true);
				ChunkStore store = (ChunkStore) chunkstore.get(manager);
				Field regionfilesspout = ChunkStore.class.getDeclaredField("regionFiles");
				regionfilesspout.setAccessible(true);
				HashMap<UUID, HashMap<Long, SimpleRegionFile>> regionFiles;
				regionFiles = (HashMap<UUID, HashMap<Long, SimpleRegionFile>>) regionfilesspout.get(store);
				//operate on region files
				HashMap<Long, SimpleRegionFile> data = regionFiles.remove(world.getUID());
				if (data != null) 
				{
					//close streams...
					for (SimpleRegionFile file : data.values()) 
					{
						file.close();
					}
				}
			}
		}
		catch (Exception e) 
		{
			TCUtils.debugMessage("Exception while removing Spout world reference for '" + world.getName() + "'!\n" + e.getMessage(), Level.WARNING);
			e.printStackTrace();
		}
		
		return true;
	}
}