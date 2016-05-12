package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель комплексного удара из 2х фаз.
 * 
 * @author Ronn
 */
public class ComplexModStrike extends Strike
{
	private int state;
	
	/**
	 * @param template темплейт скила.
	 */
	public ComplexModStrike(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);
		
		state = template.getStartState();
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		state++;
		
		if(state % template.getStateMod() == 0)
			character.broadcastPacket(SkillStart.getInstance(character, getIconId(), castId,  state / template.getStateMod()));
		
		super.useSkill(character, targetX, targetY, targetZ);
	}
}
