package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Простой баф, служит для применения статов без какой-нибудь логики
 * @author Ronn
 */
public class Buff extends AbstractEffect
{
	/**
	 * @param template
	 * @param effector
	 * @param effected
	 * @param skill
	 */
	public Buff(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}
}
