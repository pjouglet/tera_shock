package tera.gameserver.model.skillengine.classes;

import tera.gameserver.templates.SkillTemplate;

/**
 * Модель быстрого удара после зарядки.
 * 
 * @author Ronn
 */
public class ChargeStrike extends ChargeDam
{
	/**
	 * @param template темплейт скила.
	 */
	public ChargeStrike(SkillTemplate template)
	{
		super(template);
	}
}
