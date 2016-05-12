package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила, служащего только для активирования бафа.
 * 
 * @author Ronn
 */
public class TeleportJump extends Jump
{
	/**
	 * @param template темплейт скила.
	 */
	public TeleportJump(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		character.broadcastPacket(SkillStart.getInstance(character, template.getIconId(), castId, 1));
	}
}
