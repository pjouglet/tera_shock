package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель подготовленного удара.
 *
 * @author Ronn
 */
public class PrepareStrike extends Strike
{
	private int state;

	/**
	 * @param template темплейт скила.
	 */
	public PrepareStrike(SkillTemplate template)
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
		if(state <= template.getEndState())
		{
			character.broadcastPacket(SkillStart.getInstance(character, getIconId(), castId, state++));

			return;
		}

		//character.broadcastPacket(SkillStart.getInstance(character, getIconId(), castId, state));

		state++;

		super.useSkill(character, targetX, targetY, targetZ);
	}
}
