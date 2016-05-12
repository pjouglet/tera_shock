package tera.gameserver.tasks;

import rlib.util.SafeTask;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель отложенного выполнение приминения скила на объект.
 * 
 * @author Ronn
 */
public class SkillApplyTask extends SafeTask
{
	/** применяемый скилл */
	private Skill skill;
	
	/** атакующий */
	private Character attacker;
	/** атакуемый */
	private Character target;

	/**
	 * @param skill применяемый скилл.
	 * @param attacker атакующий.
	 * @param target атакуемый.
	 */
	public SkillApplyTask(Skill skill, Character attacker, Character target)
	{
		this.skill = skill;
		this.target = target;
		this.attacker = attacker;
	}

	@Override
	protected void runImpl()
	{
		skill.applySkill(attacker, target);
	}
}
