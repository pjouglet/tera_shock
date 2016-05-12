package tera.gameserver.model.npc.interaction.links;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.playable.Player;

/**
 * Модель квестовой ссылки.
 *
 * @author Ronn
 */
public class QuestLink extends AbstractLink
{
	/** кондишен линка */
	private Condition condition;
	/** ид линка */
	private int id;

	public QuestLink(String name, IconType icon, int id, Reply reply, Condition condition)
	{
		super(name, LinkType.QUEST, icon, reply, condition);

		this.id = id;
		this.condition = condition;
	}

	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		if(condition == null || condition.test(npc, player))
			return true;

		return false;
	}
}
