package tera.gameserver.model.skillengine.funcs.chance;

import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель приминяемого шансового скила.
 *
 * @author Ronn
 */
public final class ApplySkill extends AbstractChanceFunc
{
	public ApplySkill(VarTable vars, Condition cond)
	{
		super(vars, cond);
	}

	@Override
	public boolean apply(Character attacker, Character attacked, Skill attackerSkill)
	{
		// получаем шансовый скил
		Skill skill = getSkill();

		if(skill != null && super.apply(attacker, attacked, attackerSkill))
		{
			switch(skill.getTargetType())
			{
				case TARGET_ONE: skill.applySkill(attacker, attacked); break;
				case TARGET_SELF: skill.applySkill(attacker, attacker); break;
				default:
					break;
			}

			return true;
		}

		return false;
	}
}
