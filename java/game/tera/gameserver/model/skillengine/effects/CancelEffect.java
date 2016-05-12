package tera.gameserver.model.skillengine.effects;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Эффект для снятие эффектов с цели.
 *
 * @author Ronn
 */
public class CancelEffect extends AbstractEffect
{
	/**
	 * @param effectTemplate темплейт эффекта.
	 * @param effector тот, кто наложил эффект.
	 * @param effected тот, на кого наложили эффект,
	 * @param skill скил, которым был наложен эффект.
	 */
	public CancelEffect(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
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

		effectList.lock();
		try
		{
			switch(template.getOptions())
			{
				case "debuff":
				{
					for(int g = 0, length = effects.size(); g < length; g++)
					{
						Effect effect = array[g];

						if(effect == null || !effect.isDebuff())
							continue;

						effect.exit();
						length--;
						g--;
					}

					break;
				}
				case "buff":
				{
					for(int g = 0, length = effects.size(); g < length; g++)
					{
						Effect effect = array[g];

						if(effect == null || effect.isDebuff())
							continue;

						effect.exit();
						length--;
						g--;
					}
				}
			}
		}
		finally
		{
			effectList.unlock();
		}

		return false;
	}
}
