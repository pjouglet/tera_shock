package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель комплексного удара из 2х фаз.
 *
 * @author Ronn
 */
public class StageStrike extends Strike
{
	/**
	 * @param template темплейт скила.
	 */
	public StageStrike(SkillTemplate template)
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
			super.useSkill(character, targetX, targetY, targetZ);
		else
			applyOrder++;
	}
}
