package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.DismountPlayer;
import tera.gameserver.network.serverpackets.SitOnTransport;
import tera.gameserver.templates.SkillTemplate;

/**
 * Скил для трансформаций
 *
 * @author Ronn
 * @created 12.04.2012
 */
public class Transform extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public Transform(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ){}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(character.getActivateSkill() == this)
			character.broadcastPacket(DismountPlayer.getInstance(character, this));
		else
			character.broadcastPacket(SitOnTransport.getInstance(character, this));
	}
}
