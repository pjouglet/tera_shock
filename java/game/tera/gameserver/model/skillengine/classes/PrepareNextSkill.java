package tera.gameserver.model.skillengine.classes;

import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.RequestSkillStart;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель подготовительного скила для запуска другого.
 * 
 * @author Ronn
 */
public class PrepareNextSkill extends Strike
{
	public PrepareNextSkill(SkillTemplate template)
	{
		super(template);
	}
	
	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		template.removeCastFuncs(attacker);
		
		if(force || attacker.isAttackBlocking() || attacker.isOwerturned())
		{
			attacker.broadcastPacket(SkillEnd.getInstance(attacker, castId, template.getId()));
			return;
		}
		
		attacker.setCastId(castId);
		attacker.sendPacket(RequestSkillStart.getInstance(template.getId() + template.getOffsetId()), true);
	}
}
