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
public class NpcSingleFastShot extends Strike
{
	private Character target;

	/**
	 * @param template темплейт скила.
	 */
	public NpcSingleFastShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		target = attacker.getTarget();

		if(target == null)
			return false;

		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		impactX = attacker.getX();
		impactY = attacker.getY();
		impactZ = attacker.getZ();

		attacker.broadcastPacket(StartFastShot.getInstance(attacker, this, castId, target.getX(), target.getY(), target.getZ() + (target.getGeomHeight() * 0.5F)));
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(target != null)
			FastShot.startShot(character, this, target.getX(), target.getY(), target.getZ() + (target.getGeomHeight() * 0.5F));

		target = null;
	}
}
