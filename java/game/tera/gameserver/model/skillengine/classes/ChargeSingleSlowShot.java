package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.SlowShot;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель медленно стреляющего скила.
 * 
 * @author Ronn
 */
public class ChargeSingleSlowShot extends ChargeDam
{
	/**
	 * @param template темплейт скила.
	 */
	public ChargeSingleSlowShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		setImpactX(character.getX());
		setImpactY(character.getY());
		setImpactZ(character.getZ());
		
		SlowShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
