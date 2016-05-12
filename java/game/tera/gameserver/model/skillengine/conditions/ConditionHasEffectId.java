package tera.gameserver.model.skillengine.conditions;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;

/**
 * Условие на имение рабочего эффекта с указанным ид
 *
 * @author Ronn
 * @created 13.03.2012
 */
public class ConditionHasEffectId extends AbstractCondition
{
	private int effectId;

	/**
	 * @param effectId
	 */
	public ConditionHasEffectId(int effectId)
	{
		this.effectId = effectId;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		EffectList effectList = attacker.getEffectList();

		if(effectList == null || effectList.size() < 1)
			return false;

		Array<Effect> effects = effectList.getEffects();

		Effect[] array = effects.array();

		for(int i = 0, length = effects.size(); i < length; i++)
		{
			Effect effect = array[i];

			if(effect != null && effect.getEffectId() == effectId)
				return true;
		}

		return false;
	}
}
