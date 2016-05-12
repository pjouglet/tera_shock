package tera.gameserver.model.skillengine.targethandler;

import tera.gameserver.model.Character;
import tera.gameserver.model.Party;

/**
 * Модель для реализации рассчета целей в области от точки применения.
 *
 * @author Ronn
 */
public final class AuraOwnerTargetHandler extends AuraTargetHandler
{
	@Override
	protected boolean checkTarget(Character caster, Character target)
	{
		// если кастующий не самон
		if(!caster.isSummon())
			return false;

		// получаем владельца самона
		Character owner = caster.getOwner();

		// если его нет, выходим
		if(owner == null)
			return false;

		// если это он, выходим
		if(owner == target)
			return true;

		// получаем пати владельца
		Party party = owner.getParty();

		// если цель из пати владельца, выходим
		if(party != null && target.getParty() == party)
			return true;

		return false;
	}
}
