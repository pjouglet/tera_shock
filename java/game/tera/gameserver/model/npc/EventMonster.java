package tera.gameserver.model.npc;

import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.NpcTemplate;

/**
 * Модель монстра для ивентов.
 *
 * @author Ronn
 */
public class EventMonster extends Monster
{
	public EventMonster(int objectId, NpcTemplate template)
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
				case MYSTIC:
				case PRIEST: aggro *= 3; break;
				default : break;
			}
		}

		super.addAggro(aggressor, aggro, damage);
	}

	@Override
	public void causingDamage(Skill skill, AttackInfo info, Character attacker)
	{
		// полукчаем игрока из атакующего
		Player player = attacker.getPlayer();

		if(!player.isEvent())
			return;

		super.causingDamage(skill, info, attacker);
	}

	@Override
	public boolean checkTarget(Character target)
	{
		// полукчаем игрока из цели
		Player player = target.getPlayer();

		// если это игрок на ивенте ,то можно бить
		return player != null && player.isEvent();
	}
}
