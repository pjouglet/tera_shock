package tera.gameserver.model.skillengine.targethandler;

import tera.gameserver.model.Character;
import tera.gameserver.model.Guild;
import tera.gameserver.model.Party;

/**
 * Модель для реализации рассчета целей в области от точки применения.
 *
 * @author Ronn
 */
public class AreaPartyGuildTargetHandler extends AreaTargetHandler
{
	@Override
	protected boolean checkTarget(Character caster, Character target)
	{
		Character owner = target.getOwner();

		if(owner != null)
			target = owner;

		Guild guild = caster.getGuild();

		if(guild != null && target.getGuild() == guild)
			return true;

		Party party = caster.getParty();

		if(party != null && caster.getParty() == target.getParty())
			return true;

		return false;
	}
}
