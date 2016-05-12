package tera.gameserver.model.ai.npc.classes;

import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.npc.EventMonster;
import tera.gameserver.model.playable.Player;

/**
 * Ивентовая модель НПС АИ.
 *
 * @author Ronn
 */
public class EventMonsterAI extends AbstractNpcAI<EventMonster>
{
	public EventMonsterAI(EventMonster actor, ConfigAI config)
	{
		super(actor, config);
	}

	@Override
	public boolean checkAggression(Character target)
	{
		if(!target.isPlayer())
			return false;

		// получаем игрока
		Player player = target.getPlayer();

		return player.isEvent();
	}
}
