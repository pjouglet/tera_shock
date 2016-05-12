package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestActionType;

/**
 * Базовая модель квестового акшена.
 *
 * @author Ronn
 */
public abstract class AbstractQuestAction implements QuestAction
{
	protected static final Logger log = Loggers.getLogger(AbstractQuestAction.class);

	/** тип акшена */
	protected QuestActionType type;
	/** квест владелец */
	protected Quest quest;
	/** кондишен на выполнение акшена */
	protected Condition condition;

	public AbstractQuestAction(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		this.type = type;
		this.quest = quest;
		this.condition = condition;
	}

	@Override
	public QuestActionType getType()
	{
		return type;
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		return condition == null || condition.test(npc, player);
	}
}
