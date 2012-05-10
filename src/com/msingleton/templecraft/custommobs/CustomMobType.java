package com.msingleton.templecraft.custommobs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;

import com.msingleton.templecraft.util.Pair;


public class CustomMobType{

	private EntityType mobtype = null;
	private int maxhealth = 0;
	private int dmgmulti = 0;
	private int count = 1;
	private int range = 20;
	private int size = 0;
	private String name = "";
	private String oldabilitys = "";
	private List<Pair<CustomMobAbility,Integer>> abilities_random = new ArrayList<Pair<CustomMobAbility,Integer>>();
	private List<Pair<CustomMobAbility,Integer>> abilities_rotation = new ArrayList<Pair<CustomMobAbility,Integer>>();

	public EntityType getMobtype() {
		return mobtype;
	}

	public void setMobtype(EntityType mobtype) {
		this.mobtype = mobtype;
	}

	public int getMaxhealth() {
		return maxhealth;
	}

	public void setMaxhealth(int maxhealth) {
		this.maxhealth = maxhealth;
	}

	public int getDmgmulti() {
		return dmgmulti;
	}

	public void setDmgmulti(int dmgmulti) {
		this.dmgmulti = dmgmulti;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOldabilitys() {
		return oldabilitys;
	}

	public void setOldabilitys(String oldabilitys) {
		this.oldabilitys = oldabilitys;
	}

	public List<Pair<CustomMobAbility,Integer>> getAbilities_rotation() {
		return abilities_rotation;
	}

	public List<Pair<CustomMobAbility,Integer>> getAbilities_random() {
		return abilities_random;
	}

	public void setAbilities_random(List<Pair<CustomMobAbility,Integer>> abilities_random) {
		this.abilities_random = abilities_random;
	}
	public void addAbilities_random(CustomMobAbility cma, int i) 
	{
		this.abilities_random.add(new Pair<CustomMobAbility,Integer>(cma, i));
	}

	public void setAbilities_rotation(List<Pair<CustomMobAbility,Integer>> abilities_rotation) {
		this.abilities_rotation = abilities_rotation;
	}

	public void addAbilities_rotation(CustomMobAbility cma, int i) 
	{
		this.abilities_rotation.add(new Pair<CustomMobAbility,Integer>(cma, i));
	}
}