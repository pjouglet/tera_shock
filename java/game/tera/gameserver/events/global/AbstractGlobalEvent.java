package tera.gameserver.events.global;

import rlib.util.array.Array;
import tera.gameserver.events.Event;
import tera.gameserver.events.NpcInteractEvent;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель глобального мероприятия.
 *
 * @author Ronn
 */
public abstract class AbstractGlobalEvent implements Event, NpcInteractEvent
{
	@Override
	public void addLinks(Array<Link> links, Npc npc, Player player){}

	@Override
	public boolean isAuto()
	{
		return false;
	}

	@Override
	public boolean onLoad()
	{
		return true;
	}

	@Override
	public boolean onReload()
	{
		return false;
	}

	@Override
	public boolean onSave()
	{
		return false;
	}

	@Override
	public boolean start()
	{
		return false;
	}

	@Override
	public boolean stop()
	{
		return false;
	}
}
