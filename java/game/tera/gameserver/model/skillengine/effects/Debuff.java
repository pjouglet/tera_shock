package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Исчпользовать для дебаффов
 * @author Ronn
 */
public class Debuff extends AbstractEffect
{
	/**
	 * @param template
	 * @param effector
	 * @param effected
	 * @param skill
	 */
	public Debuff(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}
}
