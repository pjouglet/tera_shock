package tera.gameserver.model.skillengine.conditions;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;

/**
 * Кондишен, является ли атакующий playable
 *
 * @author Ronn
 */
public class ConditionAttackerEffectorEffectId extends AbstractCondition
{
	private int effectId;

	/**
	 * @param effectId
	 */
	public ConditionAttackerEffectorEffectId(int effectId)
	{
		this.effectId = effectId;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		if(attacker == null || attacked == null)
			return false;

		EffectList effectList = attacker.getEffectList();

		if(effectList == null || effectList.size() < 1)
			return false;

		Array<Effect> effects = effectList.getEffects();

		Effect[] array = effects.array();

		for(int i = 0, length = effects.size(); i < length; i++)
		{
			Effect effect = array[i];

			if(effect != null && effect.getEffectId() == effectId && effect.getEffector() == attacked)
				return true;
		}

		return false;
	}
}
