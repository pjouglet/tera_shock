package tera.gameserver.model.quests;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Набор квестов для данного нпс.
 *
 * @author Ronn
 */
public class QuestData
{
	/** массив квестов */
	private Quest[] quests;

	/**
	 * Добавление нужных ссылок для диалога.
	 *
	 * @param container контейнер ссылок.
	 * @param npc нпс, у которого открывается диалог.
	 * @param player игрок, который хочет поговорить.
	 */
	public void addLinks(Array<Link> container, Npc npc, Player player)
	{
		// получаем доступные квесты
		Quest[] quests = getQuests();

		// если их нет ,выходим
		if(quests == null)
			return;

		// перебираем квесты и добавляем с них ссылки
		for(int i = 0, length = quests.length; i < length; i++)
			quests[i].addLinks(container, npc, player);
	}

	/**
	 * Добавление квеста к НПС.
	 *
	 * @param quest добавляемый квест.
	 */
	public void addQuest(Quest quest)
	{
		// получаем доступные квесты
		Quest[] quests = getQuests();

		// если они есть
		if(quests == null)
			setQuests((Quest[]) Arrays.toGenericArray(quest));
		else
		{
			// проверяем, добавлен ли уже такой квест
			for(int i = 0, length = quests.length; i < length; i++)
				if(quests[i] == quest)
					return;

			// добавляем новый квест
			setQuests(Arrays.addToArray(quests, quest, Quest.class));
		}
	}

	/**
	 * @return доступные квесты.
	 */
	private final Quest[] getQuests()
	{
		return quests;
	}

	/**
	 * Проверяет наличие квеста для игрока.
	 *
	 * @param npc нпс.
	 * @param player игрок.
	 */
	public QuestType hasQuests(Npc npc, Player player)
	{
		// получаем доступные квесты
		Quest[] quests = getQuests();

		// если их нет, выходим
		if(quests == null)
			return null;

		// перебираем квест
		for(int i = 0, length = quests.length; i < length; i++)
		{
			// получаем квест
			Quest quest = quests[i];

			// если этот квест можно взять
			if(quest.isAvailable(npc, player))
				// возвращаем его тип
				return quest.getType();
		}

		return null;
	}

	/**
	 * @param quests доступные квесты.
	 */
	private final void setQuests(Quest[] quests)
	{
		this.quests = quests;
	}
}
