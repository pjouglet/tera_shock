package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class LockOn extends AbstractSkill
{
	public LockOn(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		super.endSkill(attacker, targetX, targetY, targetZ, force);
		
		attacker.setLockOnSkill(null);
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
		
		attacker.setLockOnSkill(this);
	}
	
	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ){}
}
