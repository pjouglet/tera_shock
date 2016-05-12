package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила итемов.
 * 
 * @author Ronn
 */
public class ItemBuff extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public ItemBuff(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public int getReuseDelay(Character caster)
	{
		return template.getReuseDelay();
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		addEffects(character, character);
	}
}
