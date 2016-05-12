package tera.gameserver.model.npc.interaction;

import rlib.util.Strings;
import rlib.util.array.Array;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Модель структуры диалогов нпс.
 *
 * @author Ronn
 */
public final class DialogData
{
	public static final String INDEX_LINK = Strings.EMPTY;

	/** набор ссылок */
	private Link[] links;

	/** нпс ид к которому они принадлежат */
	private int npcId;
	/** тип нпс */
	private int type;

	/**
	 * @param links массив ссылок.
	 * @param npcId ид нпс.
	 * @param type тип нпс.
	 */
	public DialogData(Link[] links, int npcId, int type)
	{
		this.links = links;
		this.npcId = npcId;
		this.type = type;
	}

	/**
	 * Добавление нужных ссылок для диалога.
	 *
	 * @param container контейнер ссылок.
	 * @param npc нпс, у которого открывается диалог.
	 * @param player игрок, который хочет поговорить.
	 */
	public void addLinks(Array<Link> container, Npc npc, Player player)
	{
		// получаем ссылки диалога
		Link[] links = getLinks();

		// если их нет, выходим
		if(links.length < 1)
			return;

		// если есть, перебираем
		for(int i = 0, length = links.length; i < length; i++)
		{
			Link link = links[i];

			// если условия не выполняются, пропускаем
			if(!link.test(npc, player))
				continue;

			// добавляем
			container.add(link);
		}
	}

	/**
	 * @return ссылки диалога.
	 */
	public final Link[] getLinks()
	{
		return links;
	}

	/**
	 * @return ид нпс.
	 */
	public int getNpcId()
	{
		return npcId;
	}

	/**
	 * @return тип нпс.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param links массив ссылок.
	 */
	public final void setLinks(Link[] links)
	{
		this.links = links;
	}

	/**
	 * @param npcId ид нпс.
	 */
	public void setNpcId(int npcId)
	{
		this.npcId = npcId;
	}
}
