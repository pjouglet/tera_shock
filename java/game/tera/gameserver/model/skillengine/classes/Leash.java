package tera.gameserver.model.skillengine.classes;

import rlib.geom.Coords;
import rlib.util.Rnd;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.MoveType;
import tera.gameserver.network.serverpackets.SkillLeash;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * @author Ronn
 */
public class Leash extends Strike
{
	public Leash(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		if(!info.isBlocked() && !target.isLeashImmunity())
		{
			boolean result = Rnd.chance(80);

			if(result)
			{
				target.stopMove();
				target.abortCast(true);

				int distance = (int) (attacker.getGeomRadius() + target.getGeomRadius());

				float newX = Coords.calcX(attacker.getX(), distance, attacker.getHeading());
				float newY = Coords.calcY(attacker.getY(), distance, attacker.getHeading());

				target.setXYZ(newX, newY, attacker.getZ());
				target.broadcastMove(target.getX(), target.getY(), target.getZ(), target.getHeading(), MoveType.STOP, target.getX(), target.getY(), target.getZ(), true);
			}

			attacker.broadcastPacket(SkillLeash.getInstance(attacker.getObjectId(), attacker.getSubId(), target.getObjectId(), target.getSubId(), result));
		}

		return info;
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		super.useSkill(character, targetX, targetY, targetZ);

		character.broadcastPacket(SkillStart.getInstance(character, template.getIconId(), castId, 1));
	}
}
