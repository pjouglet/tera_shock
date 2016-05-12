package tera.gameserver.model.quests;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;

/**
 * Контейнер инфы об событии.
 *
 * @author Ronn
 */
public final class QuestEvent
{
	/** тип события */
	private QuestEventType type;
	/** нажатая ссылка */
	private Link link;
	/** квест */
	private Quest quest;
	/** игрок */
	private Player player;
	/** нпс */
	private Npc npc;
	/** итем */
	private ItemInstance item;
	/** ресурс */
	private ResourseInstance resourse;
	/** значение чего-нибудь */
	private int value;

	/**
	 * Очистка.
	 */
	public QuestEvent clear()
	{
		link = null;
		player = null;
		quest = null;
		type = null;
		item = null;
		resourse = null;

		value = 0;

		return this;
	}

	/**
	 * @return итем.
	 */
	public ItemInstance getItem()
	{
		return item;
	}

	/**
	 * @return нажатая ссылка.
	 */
	public Link getLink()
	{
		return link;
	}

	/**
	 * @return нпс.
	 */
	public Npc getNpc()
	{
		return npc;
	}

	/**
	 * @return игрок.
	 */
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * @return квест.
	 */
	public Quest getQuest()
	{
		return quest;
	}

	/**
	 * @return ресурс.
	 */
	public final ResourseInstance getResourse()
	{
		return resourse;
	}

	/**
	 * @return тип события.
	 */
	public QuestEventType getType()
	{
		return type;
	}

	/**
	 * @return значение чего-нибудь.
	 */
	public final int getValue()
	{
		return value;
	}

	/**
	 * @param item итем.
	 */
	public void setItem(ItemInstance item)
	{
		this.item = item;
	}

	/**
	 * @param link нажатая ссылка.
	 */
	public void setLink(Link link)
	{
		this.link = link;
	}

	/**
	 * @param npc нпс.
	 */
	public void setNpc(Npc npc)
	{
		this.npc = npc;
	}

	/**
	 * @param player игрок.
	 */
	public void setPlayer(Player player)
	{
		this.player = player;
	}

	/**
	 * @param quest квест.
	 */
	public void setQuest(Quest quest)
	{
		this.quest = quest;
	}

	/**
	 * @param resourse ресурс.
	 */
	public final void setResourse(ResourseInstance resourse)
	{
		this.resourse = resourse;
	}

	/**
	 * @param type тип события.
	 */
	public void setType(QuestEventType type)
	{
		this.type = type;
	}

	/**
	 * @param value значение чего-нибудь.
	 */
	public final void setValue(int value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return "QuestEvent type = " + type + ", link = " + link + ", quest = " + quest + ", player = " + player + ", npc = " + npc + ", item = " + item + ", resourse = " + resourse;
	}
}
