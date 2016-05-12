package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Модель для просмотра счетчика убийств.
 *
 * @author Ronn
 */
public class ReplyKillStatistic extends AbstractReply
{
	public ReplyKillStatistic(Node node)
	{
		super(node);
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		player.sendMessage("Number of players killed: " + player.getPvpCount() + ".");
		player.sendMessage("Number of monsters killed: " + player.getPveCount() + ".");
	}
}
