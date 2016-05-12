package tera.gameserver.model.skillengine.conditions;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillName;

/**
 * @author Ronn
 */
public final class ConditionPlayerCastSkillName extends AbstractCondition
{
	/** нужное имя скила */
	private SkillName name;
	
	/**
	 * @param name название скила.
	 */
	public ConditionPlayerCastSkillName(SkillName name)
	{
		this.name = name;
	}

	@Override
	public boolean test(Character attacker, Character attacked, Skill skill, float val)
	{
		Skill castingSkill = attacker.getCastingSkill();
		
		return castingSkill != null && castingSkill.getSkillName() == name;
	}
}
