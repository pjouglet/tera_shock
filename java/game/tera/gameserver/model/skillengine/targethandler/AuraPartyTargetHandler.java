package tera.gameserver.model.skillengine.targethandler;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.Party;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;

/**
 * Модель для реализации рассчета целей в области.
 *
 * @author Ronn
 */
public class AuraPartyTargetHandler extends AuraTargetHandler
{
	@Override
	protected void addAllTargets(Array<Character> targets, Character caster, int radius)
	{
		// получаем группу кастера
		Party party = caster.getParty();

		// если группы нет
		if(party == null)
		{
			// добавляем только кастера
			targets.add(caster);

			// получаем суммона
			Summon summon = caster.getSummon();

			// если он есть
			if(summon != null)
				// добавляем и его
				targets.add(summon);
		}
		else
		{
			// получаем членов группы
			Array<Player> members = party.getMembers();

			members.readLock();
			try
			{
				// перебираем членов группы
				for(Player member : members.array())
				{
					// если закончились, выходим
					if(member == null)
						break;

					// добавляем члена группы в список
					targets.add(member);

					// получаем его суммона
					Summon summon = member.getSummon();

					// если он есть
					if(summon != null)
						// тоже добавляем
						targets.add(summon);
				}
			}
			finally
			{
				members.readUnlock();
			}
		}
	}

	@Override
	protected boolean checkTarget(Character caster, Character target)
	{
		return true;
	}
}
