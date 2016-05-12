package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила боевой оборонительной стойки.
 * 
 * @author Ronn
 */
public class Defense extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public Defense(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		if(isToggle() && attacker.isDefenseStance())
			return true;
		
		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		super.endSkill(attacker, targetX, targetY, targetZ, force);
		
		attacker.setDefenseStance(false);
	}

	@Override
	public boolean isWaitable()
	{
		return false;
	}
	
	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);
		
		attacker.setDefenseStance(true);
	}
}
