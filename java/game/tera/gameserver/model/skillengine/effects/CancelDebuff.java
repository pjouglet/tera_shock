package tera.gameserver.model.skillengine.effects;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Эффект, снимающий дебафы с окружающих игроков.
 *
 * @author Ronn
 */
public class CancelDebuff extends AbstractEffect
{
	/**
	 * @param template
	 * @param effector
	 * @param effected
	 * @param skill
	 */
	public CancelDebuff(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		EffectList effectList = effected.getEffectList();

		if(effectList == null || effectList.size() < 1)
			return true;

		Array<Effect> effects = effectList.getEffects();

		Effect[] array = effects.array();

		for(int g = 0, length = effects.size(); g < length; g++)
		{
			Effect effect = array[g];

			if(effect == null || effect.isAura() || !effect.isDebuff())
				continue;

			effect.exit();
		}

		return true;
	}
}
