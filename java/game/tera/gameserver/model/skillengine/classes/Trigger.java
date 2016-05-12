package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Обработчик тригеров.
 * 
 * @author Ronn
 */
public class Trigger extends Effect
{
	/**
	 * @param template темплейт скила.
	 */
	public Trigger(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ){}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ){}
}
