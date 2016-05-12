package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.FastShot;
import tera.gameserver.network.serverpackets.StartFastShot;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель быстро стреляющего скила.
 * 
 * @author Ronn
 */
public class ChargeSingleShot extends ChargeDam
{
	/**
	 * @param template темплейт скила.
	 */
	public ChargeSingleShot(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);
		
		attacker.broadcastPacket(StartFastShot.getInstance(attacker, this, castId, targetX, targetY, targetZ));
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		setImpactX(character.getX());
		setImpactY(character.getY());
		setImpactZ(character.getZ());
		
		FastShot.startShot(character, this, targetX, targetY, targetZ);
	}
}
