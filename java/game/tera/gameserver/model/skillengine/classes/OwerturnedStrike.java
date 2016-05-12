package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class OwerturnedStrike extends Strike
{
	/**
	 * @param template темплейт скила.
	 */
	public OwerturnedStrike(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		if(attacker.isOwerturned())
			attacker.cancelOwerturn();
		
		super.startSkill(attacker, targetX, targetY, targetZ);
	}
}
