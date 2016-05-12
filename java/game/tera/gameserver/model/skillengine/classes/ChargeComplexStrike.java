package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель составного удара заряжающего скила.
 * 
 * @author Ronn
 */
public class ChargeComplexStrike extends ChargeDam
{
	/** состояние скила */
	private int state;
	
	/**
	 * @param template темплейт скила.
	 */
	public ChargeComplexStrike(SkillTemplate template)
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
		
		super.useSkill(character, targetX, targetY, targetZ);

		character.broadcastPacket(SkillStart.getInstance(character, template.getIconId(), castId, state));
	}
}
