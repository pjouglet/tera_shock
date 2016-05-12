package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель ударного заряжающего скила.
 * 
 * @author Ronn
 */
public class ChargeDam extends Strike
{
	protected int chargeLevel;
	
	/**
	 * @param template
	 */
	public ChargeDam(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public int getPower()
	{
		return (int) ((template.getStartPower() + chargeLevel * template.getChargeMod()) * super.getPower());
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		castId = attacker.getCastId();
		chargeLevel = attacker.getChargeLevel();
		
		attacker.broadcastPacket(SkillStart.getInstance(attacker, getIconId(), castId, 0));
	}
}
