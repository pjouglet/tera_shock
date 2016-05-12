package tera.gameserver.model;

import rlib.util.pools.Foldable;
import tera.gameserver.model.skillengine.Skill;
import tera.util.Location;

/**
 * Модель ожидающего каста скила.
 * 
 * @author Ronn
 */
public class WaitCastSkill implements Foldable
{
	/** точка ,куда кастовать */
	private Location targetLoc;
	
	/** скил, которым кастовать */
	private Skill skill;
	
	public WaitCastSkill()
	{
		this.targetLoc = new Location();
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj == this || obj == skill;
	}

	@Override
	public void finalyze()
	{
		skill = null;	
	}

	/**
	 * @return кастуемый скил.
	 */
	public final Skill getSkill()
	{
		return skill;
	}
	
	/**
	 * @return целевая точка.
	 */
	public final Location getTargetLoc()
	{
		return targetLoc;
	}

	@Override
	public void reinit(){}

	/**
	 * @param skill кастуемый скил.
	 */
	public final void setSkill(Skill skill)
	{
		this.skill = skill;
	}

	/**
	 * @param targetLoc целевая точка.
	 */
	public final void setTargetLoc(float x, float y, float z, int heading)
	{
		this.targetLoc.setXYZH(x, y, z, heading);
	}
	
	@Override
	public String toString()
	{
		return skill.getName();
	}
}
