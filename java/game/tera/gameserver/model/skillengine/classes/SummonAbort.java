package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class SummonAbort extends AbstractSkill
{
	public SummonAbort(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		if(attacker.getSummon() == null)
		{
			attacker.sendMessage("У вас нет вызванных питомцев.");
			return false;
		}
		
		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}


	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		super.useSkill(character, targetX, targetY, targetZ);

		Summon summon = character.getSummon();
		
		if(summon == null)
			return;
		
		summon.getAI().abortAttack();
	}
}
