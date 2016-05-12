package tera.gameserver.model.npc.interaction.conditions;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.quests.Quest;

/**
 * Базовая модель условия для ссылки.
 *
 * @author Ronn
 */
public abstract class AbstractCondition implements Condition
{
	protected static final Logger log = Loggers.getLogger(AbstractCondition.class);

	/** квест, для которого условие */
	protected Quest quest;

	public AbstractCondition(Quest quest)
	{
		this.quest = quest;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
