package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class PartySummon extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public PartySummon(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		if(!character.isPlayer())
			return;

		// получаем игрока
		Player player = character.getPlayer();

		// если игрок на ивенте, выходим
		if(player.isEvent())
			return;

		// получаем группу игрока
		Party party = character.getParty();

		// если группы нет, выходим
		if(party == null)
			return;

		// получаем список членов группы
		Array<Player> members = party.getMembers();

		members.readLock();
		try
		{
			Player[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				Player member = array[i];

				if(!member.isDead() && !member.isEvent() && member != player)
					member.teleToLocation(player.getContinentId(), player.getX(), player.getY(), player.getZ());
			}
		}
		finally
		{
			members.readUnlock();
		}
	}
}
