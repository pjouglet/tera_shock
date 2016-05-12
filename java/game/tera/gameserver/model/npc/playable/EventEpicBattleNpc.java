package tera.gameserver.model.npc.playable;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель НПС для ивента Эпичных Битв.
 *
 * @author Ronn
 */
public class EventEpicBattleNpc extends PlayerKiller
{
	public EventEpicBattleNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void addAggro(Character aggressor, long aggro, boolean damage)
	{
		// если это не урон от игрока
		if(!damage && aggressor.isPlayer())
		{
			// получаем игрока
			Player player = aggressor.getPlayer();

			// в зависимости от класса игрока
			switch(player.getPlayerClass())
			{
				case WARRIOR:
				case LANCER: aggro = 1; break;
				default:
					break;
			}
		}

		super.addAggro(aggressor, aggro, damage);
	}

	@Override
	public void causingDamage(Skill skill, AttackInfo info, Character attacker)
	{
		// пробуем получить игрока
		Player player = attacker.getPlayer();

		// если это игрок и он не на ивенте, выходим
		if(player != null && !player.isEvent())
			return;

		super.causingDamage(skill, info, attacker);
	}
}
