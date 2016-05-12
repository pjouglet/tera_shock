package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.gameserver.events.EventConstant;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Модель для просмотра очков славы.
 *
 * @author Ronn
 */
public class ReplyHeroPoint extends AbstractReply
{
	public ReplyHeroPoint(Node node)
	{
		super(node);
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		int val = player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0);

		if(val == 0)
			player.sendMessage("You don't have Fame points.");
		else if(val > 0)
			player.sendMessage("Number of Fame point: " + val +".");
	}
}
