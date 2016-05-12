package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.Config;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;

/**
 * Акшен для выдачи экспы игроку.
 *
 * @author Ronn
 */
public class ActionAddExp extends AbstractQuestAction
{
	/** выдамаемая экспа */
	private int exp;

	public ActionAddExp(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			this.exp = (int) (VarTable.newInstance(node).getInteger("exp") * Config.SERVER_RATE_QUEST_REWARD);
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		int exp = getExp();

		if(Config.ACCOUNT_PREMIUM_QUEST && player.hasPremium())
			exp *= Config.ACCOUNT_PREMIUM_QUEST_RATE;

		// выдаем ему экспу
		player.addExp(exp, null, quest.getName());
	}

	/**
	 * @return кол-во выдаваемой экспы.
	 */
	public int getExp()
	{
		return exp;
	}

	@Override
	public String toString()
	{
		return "ActionAddExp exp = " + exp;
	}
}
