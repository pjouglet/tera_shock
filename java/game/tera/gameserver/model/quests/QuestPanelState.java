package tera.gameserver.model.quests;

/**
 * Состояние квеста относительно панели квестов.
 *
 * @author Ronn
 */
public enum QuestPanelState
{
	REMOVED,
	ADDED,
	ACCEPTED,
	UPDATE,
	NONE;

	public static final QuestPanelState valueOf(int index)
	{
		QuestPanelState[] values = values();

		if(index < 0 || index >= values.length)
			return NONE;

		return values()[index];
	}
}
