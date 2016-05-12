package tera.gameserver.model.npc.interaction.links;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки обычного диалога.
 * 
 * @author Ronn
 */
public class NpcLink extends AbstractLink
{
	public NpcLink(String name, LinkType type, IconType icon, Reply reply)
	{
		super(name, type, icon, reply, null);
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "NpcLink [name=" + name + "]";
	}
}
