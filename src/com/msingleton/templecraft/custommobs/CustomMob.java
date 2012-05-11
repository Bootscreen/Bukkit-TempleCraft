package com.msingleton.templecraft.custommobs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.util.MobSpawnProperties;
import com.msingleton.templecraft.util.Pair;


public class CustomMob{

	private LivingEntity entity;
	private List<CustomMobAbility> abilitys = new ArrayList<CustomMobAbility>();
	private boolean dead = false;
	private int health = 0;
	private int maxhealth = 0;
	private int dmgmulti = 0;
	private int size = 0;
	private List<Pair<CustomMobAbility,Integer>> abilities_random = new ArrayList<Pair<CustomMobAbility,Integer>>();
	private List<Pair<CustomMobAbility,Integer>> abilities_rotation = new ArrayList<Pair<CustomMobAbility,Integer>>();
	private MobSpawnProperties spawnProperties = null;

	public CustomMob(LivingEntity entity) 
	{
		this.entity = entity;
	}
	
	public void remove()
	{
		this.entity.remove();
	}

	public boolean isEntityDead() 
	{
		return entity.isDead();
	}
	
	public boolean isDead() 
	{
		return dead;
	}

	public void setDead(boolean dead) 
	{
		this.dead = dead;
	}

	public void setHealth(int health) 
	{
		this.health = health;
		this.maxhealth = health;
	}

	public int getHealth() 
	{
		return health;
	}

	public int getMaxHealth() 
	{
		return maxhealth;
	}

	public void setDMGMultiplikator(int dmgmulti) 
	{
		this.dmgmulti = dmgmulti;
	}

	public int getDMGMultiplikator() 
	{
		return dmgmulti;
	}

	public void setSize(int size) 
	{
		this.size = size;
	}

	public int getSize() 
	{
		return size;
	}

	public void damage(int value, Entity damager) 
	{
		if(!entity.equals(damager))
		{
			health -= value;
			if (health <= 0) 
			{
				this.dead = true;
				entity.damage(entity.getMaxHealth());
			}
		}
	}

	public LivingEntity getLivingEntity()
	{
		return this.entity;
	}

	public List<CustomMobAbility> getAbilitys()
	{
		return this.abilitys;
	}

	public void setAbilitys(List<CustomMobAbility> abilitys)
	{
		if(abilitys != null)
		{
			this.abilitys = abilitys;
		}
	}

	public void addAbility(CustomMobAbility ability)
	{
		if(ability != null)
		{
			this.abilitys.add(ability);
		}
	}

	public void addAbilitysfromString(String abilitys)
	{
		String[] ability_split = abilitys.split(",");
		for(String ability : ability_split)
		{
			addAbility(CustomMobAbility.fromShortName(ability));
		}
	}

	public LivingEntity getTarget()
	{
		return TCUtils.getTarget(this.entity);
	}

	public List<Pair<CustomMobAbility,Integer>> getAbilities_rotation() 
	{
		return abilities_rotation;
	}

	public void setAbilities_rotation(List<Pair<CustomMobAbility,Integer>> abilities_rotation) 
	{
		this.abilities_rotation = abilities_rotation;
	}

	public List<Pair<CustomMobAbility,Integer>> getAbilities_random()
	{
		return abilities_random;
	}

	public void setAbilities_random(List<Pair<CustomMobAbility,Integer>> abilities_random) 
	{
		this.abilities_random = abilities_random;
	}

	public MobSpawnProperties getSpawnProperties() 
	{
		return spawnProperties;
	}
	

	public void setSpawnProperties(MobSpawnProperties spawnProperties)
	{
		this.spawnProperties = spawnProperties;
	}
}