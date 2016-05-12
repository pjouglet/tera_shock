package tera.gameserver.model.quests;

import rlib.util.array.Array;

/**
 * Набор утильны методов для работы с квестами.
 *
 * @author Ronn
 */
public final class QuestUtils
{
	/**
	 * Уведомить квесты о событии.
	 *
	 * @param quests список активных квестов.
	 * @param event событие.
	 */
	public static void notifyQuests(Array<QuestState> quests, QuestEvent event)
	{
		quests.readLock();
		try
		{
			// получаем массив квестов
			QuestState[] array = quests.array();

			// перебираем активные квесты
			for(int i = 0, length = quests.size(); i < length; i++)
			{
				// получаем активный квест
				Quest quest = array[i].getQuest();

				if(quest == null)
				{
					System.out.println("not found quest to " + array[i]);
					continue;
				}

				// указываем событию квест
				event.setQuest(quest);

				// уведомляет квест о событии
				quest.notifyQuest(event.getType(), event);
			}
		}
		finally
		{
			quests.readUnlock();
		}
	}

	private QuestUtils()
	{
		throw new IllegalArgumentException();
	}
}
