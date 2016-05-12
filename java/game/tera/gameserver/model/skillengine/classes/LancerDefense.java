package tera.gameserver.model.skillengine.classes;

import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила боевой оборонительной стойки.
 * 
 * @author Ronn
 */
public class LancerDefense extends Defense
{
	/**
	 * @param template темплейт скила.
	 */
	public LancerDefense(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public int blockMpConsume(int damage)
	{
		return damage < 1? 0 : (int) Math.sqrt(damage);
	}
}
