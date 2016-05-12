package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.shots.ObjectShot;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель медленно стреляющего скила.
 *
 * @author Ronn
 */
public class NpcSingleSlowShot extends SingleSlowShot
{
	/**
	 * @param template темплейт скила.
	 */
	public NpcSingleSlowShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		int stage = getStage();

		if(stage > 0)
			character.broadcastPacket(SkillStart.getInstance(character, getIconId(), getCastId(),  stage));

		if(isApply())
		{
			setImpactX(character.getX());
			setImpactY(character.getY());
			setImpactZ(character.getZ());

			ObjectShot.startShot(character, this, targetX, targetY, targetZ);
		}

		applyOrder++;
	}
}
