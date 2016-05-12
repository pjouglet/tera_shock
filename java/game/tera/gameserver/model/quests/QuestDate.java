package tera.gameserver.model.quests;

import rlib.util.pools.Foldable;

/**
 * Временной штамп выполнения квеста.
 *
 * @author Ronn
 */
public class QuestDate implements Foldable, Comparable<QuestDate>
{
	/** выполненный квест */
	private Quest quest;

	/** время выполнения квеста */
	private long time;

	@Override
	public int compareTo(QuestDate date)
	{
		return quest.getId() - date.quest.getId();
	}

	@Override
	public void finalyze()
	{
		quest = null;
		time = 0;
	}

	/**
	 * @return выполненный квест.
	 */
	public Quest getQuest()
	{
		return quest;
	}

	/**
	 * @return ид квеста.
	 */
	public int getQuestId()
	{
		return quest.getId();
	}

	/**
	 * @return дата выполнения.
	 */
	public long getTime()
	{
		return time;
	}

	@Override
	public void reinit(){}

	/**
	 * @param quest выполненный квест.
	 */
	public void setQuest(Quest quest)
	{
		this.quest = quest;
	}

	/**
	 * @param time дата выполнения.
	 */
	public void setTime(long time)
	{
		this.time = time;
	}
}
