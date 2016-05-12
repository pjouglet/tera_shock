package tera.gameserver.model.quests;

/**
 * Интерфейс для реализации квестовых событий.
 * 
 * @author Ronn
 */
public interface QuestEventListener
{
	/**
	 * @return тип слушателя.
	 */
	public QuestEventType getType();
	
	/**
	 * @param event содержание события.
	 */
	public void notifyQuest(QuestEvent event);
}
