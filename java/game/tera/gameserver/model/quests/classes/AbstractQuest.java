package tera.gameserver.model.quests.classes;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Objects;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.document.DocumentQuestCondition;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.links.QuestLink;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestAction;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventListener;
import tera.gameserver.model.quests.QuestEventType;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.model.quests.QuestType;
import tera.gameserver.model.quests.Reward;
import tera.gameserver.network.serverpackets.QuestCompleted;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;
import tera.util.LocalObjects;

/**
 * Базовая модель для реализации квеста.
 *
 * @author Ronn
 */
public abstract class AbstractQuest implements Quest
{
	protected static final Logger log = Loggers.getLogger(Quest.class);

	protected static final DocumentQuestCondition conditionParser = DocumentQuestCondition.getInstance();

	/** название квеста */
	protected String name;
	/** тип квеста */
	protected QuestType type;
	/** награда за квест */
	protected Reward reward;

	/** список обрабатываемых событий */
	protected QuestEventListener[] events;
	/** список ссылок квестовых */
	protected Link[] links;

	/** ид квеста */
	protected int id;

	/** можно ли отменить квест */
	protected boolean cancelable;

	public AbstractQuest(QuestType type, Node node)
	{
		try
		{
			// парсим атрибуты квеста
			VarTable vars = VarTable.newInstance(node);

			this.type = type;
			this.name = vars.getString("name");
			this.id = vars.getInteger("id");

			if(name.isEmpty())
				System.out.println("found empty name for quest " + id);

			this.cancelable = vars.getBoolean("cancelable", type.isCancelable());
			this.reward = new Reward();

			// создаем список ссылок
			Array<Link> links = Arrays.toArray(Link.class);

			// создаем список ивентов
			Array<QuestEventListener> events = Arrays.toArray(QuestEventListener.class);

			// получаем таблицу НПС
			NpcTable npcTable = NpcTable.getInstance();

			// пребираем внутринние элементы квеста
			for(Node nd = node.getFirstChild(); nd != null; nd = nd.getNextSibling())
			{
				switch(nd.getNodeName())
				{
					// если это набор нпс, учавствующих в квесте
					case "npcs":
					{
						// перебираем нпс
						for(Node npc = nd.getFirstChild(); npc != null; npc = npc.getNextSibling())
						{
							if(!"npc".equals(npc.getNodeName()))
								continue;

							// парсим атрибуты нпс
							vars.parse(npc);

							// получаем темплейт нпс
							NpcTemplate template = npcTable.getTemplate(vars.getInteger("id"), vars.getInteger("type"));

							// если такой темплейт есть
							if(template != null)
								// добавляем ему этот квест
								template.addQuest(this);
						}

						break;
					}
					// если это набор награды
					case "rewards":
					{
						// перебираем акшены с наградами
						for(Node act = nd.getFirstChild(); act != null; act = act.getNextSibling())
							if("action".equals(act.getNodeName()))
								// добавляем акшен
								reward.addReward(parseAction(act));

						break;
					}
					// если это набор ссылок
					case "links":
					{
						// перебираем ссылки
						for(Node link = nd.getFirstChild(); link != null; link = link.getNextSibling())
							if("link".equals(link.getNodeName()))
								links.add(parseLink(link));

						break;
					}
					// если это набор ивентов квеста
					case "events":
					{
						// перебираем ивенты
						for(Node evt = nd.getFirstChild(); evt != null; evt = evt.getNextSibling())
							if("event".equals(evt.getNodeName()))
								events.add(parseEvent(evt));

						break;
					}
				}
			}

			links.trimToSize();
			events.trimToSize();

			this.links = links.array();
			this.events = events.array();
		}
		catch(Exception e)
		{
			log.warning(e);
		}
	}

	@Override
	public final void addLinks(Array<Link> container, Npc npc, Player player)
	{
		// получаем ссылки квеста
		Link[] links = getLinks();

		// перебираем ссылки квеста
		for(int i = 0, length = links.length; i < length; i++)
		{
			// получаем ссылку
			Link link = links[i];

			// если условия для отображения ссылки не выполнены, пропускаем
			if(!link.test(npc, player))
				continue;

			// добавляем в контейнер ссылку
			container.add(link);
		}
	}

	@Override
	public void cancel(QuestEvent event, boolean force)
	{
		// получаем игрока
		Player player = event.getPlayer();
		// получаем нпс
		Npc npc = event.getNpc();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// если квест нельзя отменить
		if(!force && !cancelable)
		{
			// отправляем сообщени об этом
			player.sendPacket(SystemMessage.getInstance(MessageType.QUEST_NAME_CANT_BE_ABANDONED).addQuestName(name), true);
			return;
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем дополнительный квестовый ивент
		QuestEvent newEvent = local.getNextQuestEvent();

		// ставим тип финиша
		newEvent.setType(QuestEventType.CANCELED_QUEST);
		// запоминаем игрока
		newEvent.setPlayer(player);
		// запоминаем нпс
		newEvent.setNpc(npc);
		// запоминаем квест
		newEvent.setQuest(this);

		// отправляем новый ивент
		notifyQuest(newEvent);

		// получаем квест лист игрока
		QuestList questList = player.getQuestList();
		// получаем текущее состояние квеста
		QuestState questState = questList.getQuestState(this);

		// удаляем квест из панели и книги
		player.sendPacket(QuestCompleted.getInstance(questState, true), true);

		// удаляем с панели
		player.updateQuestInPanel(questState, QuestPanelState.REMOVED);

		// финишируем его
		questList.finishQuest(this, questState, true);

		// если нпс есть
		if(npc != null)
			// обновляем его значек над головой
			npc.updateQuestInteresting(player, true);

		// отправляем сообщени об этом
		player.sendPacket(SystemMessage.getInstance(MessageType.ABANDONED_QUEST_NAME).addQuestName(name), true);

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие отмены квеста игроком
		gameLogger.writeQuestLog(player.getName() + " cancel quest [id = " + getId() + ", name = " + getName() + "]");
	}

	@Override
	public final void finish(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();
		// получаем нпс
		Npc npc = event.getNpc();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return;
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем дополнительный квестовый ивент
		QuestEvent newEvent = local.getNextQuestEvent();

		// ставим тип финиша
		newEvent.setType(QuestEventType.FINISHED_QUEST);
		// запоминаем игрока
		newEvent.setPlayer(player);
		// запоминаем нпс
		newEvent.setNpc(npc);
		// запоминаем квест
		newEvent.setQuest(this);

		// отправляем новый ивент
		notifyQuest(newEvent);

		// получаем квест лист игрока
		QuestList questList = player.getQuestList();
		// получаем текущее состояние квеста
		QuestState questState = questList.getQuestState(this);

		// удаляем с панели
		player.updateQuestInPanel(questState, QuestPanelState.REMOVED);

		// устанавливаем этот квест как выполненный
		questList.complete(this);

		// финишируем его
		questList.finishQuest(this, questList.getQuestState(this), false);

		// обрабатываем награду
		reward.giveReward(event);

		// если нпс есть
		if(npc != null)
			// обновляем его значек над головой
			npc.updateQuestInteresting(player, true);

		// отправляем сообщени об этом
		player.sendPacket(SystemMessage.getInstance(MessageType.CONGRATULATIONS_QUEST_NAME_COMPLETED).addQuestName(id), true);

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие завершения квеста игроком
		gameLogger.writeQuestLog(player.getName() + " finish quest [id = " + getId() + ", name = " + getName() + "]");
	}

	/**
	 * @return список ивентов квеста.
	 */
	protected final QuestEventListener[] getEvents()
	{
		return events;
	}

	/**
	 * @return ид квеста.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return список ссылок квеста.
	 */
	protected final Link[] getLinks()
	{
		return links;
	}

	/**
	 * @return название квеста.
	 */
	public final String getName()
	{
		return name;
	}

	@Override
	public final Reward getReward()
	{
		return reward;
	}

	/**
	 * @return тип квеста.
	 */
	public final QuestType getType()
	{
		return type;
	}

	@Override
	public final boolean isAvailable(Npc npc, Player player)
	{
		// получаем ссылки квеста
		Link[] links = getLinks();

		// перебираем ссылки квеста для поиска начальной
		for(int i = 0, length = links.length; i < length; i++)
		{
			// получам ссылку
			Link link = links[i];

			// если это  начальная
			if(link.getId() == 1)
				// возращаем результат кондишена ссылки
				return link.test(npc, player);
		}

		return false;
	}

	@Override
	public final void notifyQuest(QuestEvent event)
	{
		notifyQuest(event.getType(), event);
	}

	@Override
	public final void notifyQuest(QuestEventType type, QuestEvent event)
	{
		// получаем ивенты квеста
		QuestEventListener[] events = getEvents();

		for(int i = 0, length = events.length; i < length; i++)
		{
			// получаем ивент
			QuestEventListener listener = events[i];

			// если ивент соотвествующего типа
			if(listener.getType() == type)
				// уведомляем его
				listener.notifyQuest(event);
		}
	}

	/**
	 * @return отпаршенный акшен.
	 */
	private QuestAction parseAction(Node node)
	{
		// парсим атрибуты акшена
		VarTable vars = VarTable.newInstance(node);

		// получаем тип акшена
		QuestActionType actionType;

		try
		{
			actionType = vars.getEnum("name", QuestActionType.class);
		}
		catch(Exception e)
		{
			return null;
		}

		// парсим кондишен для акшена
		Condition condition = conditionParser.parseCondition(node, this);

		// создаем акшен
		QuestAction action = actionType.newInstance(this, condition, node);

		return action;
	}

	/**
	 * @return отпаршенный ивент.
	 */
	private QuestEventListener parseEvent(Node node)
	{
		// парсим атрибуты ивента
		VarTable vars = VarTable.newInstance(node);

		// создаем список акшенов внутри ивента
		Array<QuestAction> actions = Arrays.toArray(QuestAction.class);

		// перебираем акшены
		for(Node act = node.getFirstChild(); act != null; act = act.getNextSibling())
			if("action".equals(act.getNodeName()))
				// парсим и добавляем акшен
				actions.add(parseAction(act));

		// сжимаем список акшенов
		actions.trimToSize();

		// получаем тип ивента
		QuestEventType eventType = vars.getEnum("name", QuestEventType.class);

		return eventType.newInstance(this, actions.array(), node);
	}

	/**
	 * @return отпаршенная ссылка.
	 */
	private Link parseLink(Node node)
	{
		// парсим атрибуты ссылки
		VarTable vars = VarTable.newInstance(node);

		// получаем имя ссылки
		String name = vars.getString("name");

		// получаем ид ссылки
		int id = vars.getInteger("id");

		// получаем иконку ссылки
		IconType icon = vars.getEnum("icon", IconType.class);

		// получаем кондишен ссылки
		Condition condition = conditionParser.parseCondition(node, this);

		// создаем и возвращаем новую ссылку
		return new QuestLink(name, icon, id, this, condition);
	}

	@Override
	public final void reload(Quest update)
	{
		Objects.reload((Quest) this, update);
	}

	@Override
	public final void reply(Npc npc, Player player, Link link)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем событие квеста
		QuestEvent event = local.getNextQuestEvent();

		// получаем последнюю ссылку игрока
		Link lastLink = player.getLastLink();

		// если последняя ссылка есть, то это нажатие на ссылку
		if(lastLink != null)
		{
			// ставим тип события " нажатие на ссылку "
			event.setType(QuestEventType.SELECT_LINK);
			// запоминаем нажатую ссылку
			event.setLink(link);
			// запоминаем нпс
			event.setNpc(npc);
			// запоминаем игрока
			event.setPlayer(player);
			// запоминаем квест
			event.setQuest(this);
		}
		// иначе это было нажатие на кнопку в квест диалоге
		else
		{
			// ставим флаг нажатия на кнопку
			event.setType(QuestEventType.SELECT_BUTTON);
			// запоминаем нажатую ссылку
			event.setLink(link);
			// запоминаем нпс
			event.setNpc(npc);
			// запоминаем игрока
			event.setPlayer(player);
			// запоминаем квест
			event.setQuest(this);
		}

		// запускаемсобытие на обработку
		notifyQuest(event);
	}

	@Override
	public final void start(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();
		// получаем нпс
		Npc npc = event.getNpc();

		// если игрока нет, выходим
		if(player == null)
			return;

		// получаем квест лист
		QuestList questList = player.getQuestList();

		// запускаем квест у игрока
		QuestState quest = questList.startQuest(this);

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем дополнительный квестовый ивент
		QuestEvent newEvent = local.getNextQuestEvent();

		// ставим тип ивента взятие квеста
		newEvent.setType(QuestEventType.ACCEPTED_QUEST);
		// запоминаем игрока
		newEvent.setPlayer(player);
		// запоминаем нпс
		newEvent.setNpc(npc);
		// запоминаем квест
		newEvent.setQuest(this);

		// если нпс есть
		if(npc != null)
			// обновляем значек над ним
			npc.updateQuestInteresting(player, true);

		// запускаем новый ивент
		notifyQuest(newEvent);

		// обновляем на панели
		player.updateQuestInPanel(quest, QuestPanelState.ACCEPTED);

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие взятие квеста игроком
		gameLogger.writeQuestLog(player.getName() + " accepted quest [id = " + getId() + ", name = " + getName() + "]");
	}

	@Override
	public String toString()
	{
		return "AbstractQuest name = " + name + ", type = " + type + ", reward = " + reward + ", events = " + Arrays.toString(events) + ", links = " + Arrays.toString(links) + ", id = " + id;
	}
}
