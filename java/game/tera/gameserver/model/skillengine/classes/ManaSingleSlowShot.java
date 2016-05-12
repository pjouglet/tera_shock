package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.SlowShot;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель медленно стреляющего скила.
 * 
 * @author Ronn
 */
public class ManaSingleSlowShot extends Strike
{
	/**
	 * @param template темплейт скила.
	 */
	public ManaSingleSlowShot(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		SlowShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
