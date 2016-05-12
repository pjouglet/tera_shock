package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель быстрого удара после зарядки.
 * 
 * @author Ronn
 */
public class ChargeVampStrike extends ChargeDam
{
	/**
	 * @param template темплейт скила.
	 */
	public ChargeVampStrike(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);
		
		if(!info.isBlocked() && info.getDamage() > 0)
				attacker.effectHealHp((int) (info.getDamage() * 0.5F), target);
		
		return info;
	}
}
