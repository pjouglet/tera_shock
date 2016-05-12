package tera.util;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.ServerThread;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.AggroInfo;
import tera.gameserver.model.npc.Minion;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestEvent;

/**
 * Модель локальных объектов для серверного потока.
 *
 * @author Ronn
 */
public final class LocalObjects
{
	private static final int DEFAULT_BUFFER_SIZE = 20;

	public static LocalObjects get()
	{
		return ServerThread.currentThread().getLocal();
	}

	/** буффер списков игроков */
	private Array<Player>[] playerLists;
	/** буффер списков минионов */
	private Array<Minion>[] minionLists;
	/** буффер списков итемов */
	private Array<ItemInstance>[] itemLists;
	/** буффер списков нпс */
	private Array<Npc>[] npcLists;
	/** буффер списков персонажей */
	private Array<Character>[] charLists;
	/** буффер списков объектов */
	private Array<TObject>[] objectLists;
	/** буфер списков ссылок */
	private Array<Link>[] linkLists;
	/** буфер агро инфы */
	private Array<AggroInfo>[] aggroInfoLists;

	/** буффер квестовых ивентов */
	private QuestEvent[] questEvents;
	/** буффер атак инф */
	private AttackInfo[] attackInfos;

	/** индекс след. свободного списка игроков */
	private int playerListIndex;
	/** индекс след. свободного списка минионов */
	private int minionListIndex;
	/** индекс след. свободного списка итемов */
	private int itemListIndex;
	/** индекс след. свободного списка нпс */
	private int npcListIndex;
	/** индекс след. свободного списка персонадей */
	private int charListIndex;
	/** индекс след. свободного списко объектов */
	private int objectListIndex;
	/** индекс след. свободного списка ссылок */
	private int linkListIndex;
	/** индекс след. свободного списка аггро инфы */
	private int aggroInfoListIndex;

	/** индекс след. свободного квестового ивента */
	private int questEventIndex;
	/** индекс след. свободного атак инфо */
	private int attackInfotIndex;

	@SuppressWarnings("unchecked")
	public LocalObjects()
	{
		this.playerLists = new Array[DEFAULT_BUFFER_SIZE];
		this.minionLists = new Array[DEFAULT_BUFFER_SIZE];
		this.itemLists = new Array[DEFAULT_BUFFER_SIZE];
		this.npcLists = new Array[DEFAULT_BUFFER_SIZE];
		this.charLists = new Array[DEFAULT_BUFFER_SIZE];
		this.objectLists = new Array[DEFAULT_BUFFER_SIZE];
		this.linkLists = new Array[DEFAULT_BUFFER_SIZE];
		this.aggroInfoLists = new Array[DEFAULT_BUFFER_SIZE];

		this.questEvents = new QuestEvent[DEFAULT_BUFFER_SIZE];
		this.attackInfos = new AttackInfo[DEFAULT_BUFFER_SIZE];

		for(int i = 0, length = DEFAULT_BUFFER_SIZE; i < length; i++)
		{
			playerLists[i] = Arrays.toArray(Player.class);
			minionLists[i] = Arrays.toArray(Minion.class);
			itemLists[i] = Arrays.toArray(ItemInstance.class);
			npcLists[i] = Arrays.toArray(Npc.class);
			charLists[i] = Arrays.toArray(Character.class);
			objectLists[i] = Arrays.toArray(TObject.class);
			linkLists[i] = Arrays.toArray(Link.class);
			aggroInfoLists[i] = Arrays.toArray(AggroInfo.class);

			questEvents[i] = new QuestEvent();
			attackInfos[i] = new AttackInfo();
		}
	}

	/**
	 * @return свободный список ссылок.
	 */
	public Array<AggroInfo> getNextAggroInfoList()
	{
		if(aggroInfoListIndex == DEFAULT_BUFFER_SIZE)
			aggroInfoListIndex = 0;

		return aggroInfoLists[aggroInfoListIndex++].clear();
	}

	/**
	 * @return свободный атак инфо.
	 */
	public AttackInfo getNextAttackInfo()
	{
		if(attackInfotIndex == DEFAULT_BUFFER_SIZE)
			attackInfotIndex = 0;

		return attackInfos[attackInfotIndex++].clear();
	}

	/**
	 * @return свободный список игроков.
	 */
	public Array<Character> getNextCharList()
	{
		if(charListIndex == DEFAULT_BUFFER_SIZE)
			charListIndex = 0;

		return charLists[charListIndex++].clear();
	}

	/**
	 * @return свободный список итемлв.
	 */
	public Array<ItemInstance> getNextItemList()
	{
		if(itemListIndex == DEFAULT_BUFFER_SIZE)
			itemListIndex = 0;

		return itemLists[itemListIndex++].clear();
	}

	/**
	 * @return свободный список ссылок.
	 */
	public Array<Link> getNextLinkList()
	{
		if(linkListIndex == DEFAULT_BUFFER_SIZE)
			linkListIndex = 0;

		return linkLists[linkListIndex++].clear();
	}

	/**
	 * @return свободный список минионов.
	 */
	public Array<Minion> getNextMinionList()
	{
		if(minionListIndex == DEFAULT_BUFFER_SIZE)
			minionListIndex = 0;

		return minionLists[minionListIndex++].clear();
	}

	/**
	 * @return свободный список игроков.
	 */
	public Array<Npc> getNextNpcList()
	{
		if(npcListIndex == DEFAULT_BUFFER_SIZE)
			npcListIndex = 0;

		return npcLists[npcListIndex++].clear();
	}

	/**
	 * @return свободный список объектов.
	 */
	public Array<TObject> getNextObjectList()
	{
		if(objectListIndex == DEFAULT_BUFFER_SIZE)
			objectListIndex = 0;

		return objectLists[objectListIndex++].clear();
	}

	/**
	 * @return свободный список игроков.
	 */
	public Array<Player> getNextPlayerList()
	{
		if(playerListIndex == DEFAULT_BUFFER_SIZE)
			playerListIndex = 0;

		return playerLists[playerListIndex++].clear();
	}

	/**
	 * @return свободный квестовый ивент.
	 */
	public QuestEvent getNextQuestEvent()
	{
		if(questEventIndex == DEFAULT_BUFFER_SIZE)
			questEventIndex = 0;

		return questEvents[questEventIndex++].clear();
	}
}
