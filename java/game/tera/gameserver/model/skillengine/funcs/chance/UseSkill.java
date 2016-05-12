package tera.gameserver.model.skillengine.funcs.chance;

import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;

/**
 * @author Ronn
 */
public class UseSkill extends AbstractChanceFunc
{
	public UseSkill(VarTable vars, Condition cond)
	{
		super(vars, cond);
	}

	@Override
	public boolean apply(Character attacker, Character attacked, Skill eventSkill)
	{
		if(skill != null && super.apply(attacker, attacked, eventSkill))
		{
			skill.useSkill(attacker, attacked.getX(), attacked.getY(), attacked.getZ());
			return true;
		}
		
		return false;
	}
}
