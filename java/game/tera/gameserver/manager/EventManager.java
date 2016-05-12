package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.random.Random;
import rlib.util.random.Randoms;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.events.Event;
import tera.gameserver.events.EventType;
import tera.gameserver.events.NpcInteractEvent;
import tera.gameserver.events.Registered;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Менеджер ивентов.
 * 
 * @author Ronn
 * @created 04.03.2012
 */
public final class EventManager
{
	private static final Logger log = Loggers.getLogger(EventManager.class);

	private static EventManager instance;

	public static EventManager getInstance()
	{
		if(instance == null)
			instance = new EventManager();

		return instance;
	}

	/** рандоминайзер */
	private final Random rand;

	/** обработчик запусков ивентов */
	private final SafeTask NEXT_EVENT = new SafeTask()
	{
		@Override
		protected void runImpl()
		{
			Event event = autoable.get(rand.nextInt(0, autoable.size() - 1));

			if(event == null || runAutoEvent != null || !event.start())
			{
				ExecutorManager executor = ExecutorManager.getInstance();
				executor.scheduleGeneral(this, rand.nextInt(Config.EVENT_MIN_TIMEOUT, Config.EVENT_MAX_TIMEOUT) * 60 * 1000);
			}
		}
	};

	/** таблица всех ивентов */
	private final Table<String, Event> nameEvents;
	/** таблица всех ивентов с регистрацией */
	private final Table<String, Registered> registeredEvents;

	/** список всех доступных ивентов */
	private final Array<Event> events;
	/** список автозапускаемых ивентов */
	private final Array<Event> autoable;
	/** ивенты работающие с нпс */
	private final Array<NpcInteractEvent> npcInteractEvents;

	/** запущенный авто ивент */
	private volatile Event runAutoEvent;

	private EventManager()
	{
		rand = Randoms.newRealRandom();
		nameEvents = Tables.newObjectTable();
		registeredEvents = Tables.newObjectTable();
		events = Arrays.toArray(Event.class);
		autoable = Arrays.toArray(Event.class);
		npcInteractEvents = Arrays.toArray(NpcInteractEvent.class);

		for(EventType type : EventType.values())
		{
			Event example = type.get();

			if(example == null || !example.onLoad())
				continue;

			events.add(example);

			if(example.isAuto())
				autoable.add(example);

			nameEvents.put(example.getName(), example);

			if(example instanceof Registered)
				registeredEvents.put(example.getName(), (Registered) example);

			if(example instanceof NpcInteractEvent)
				npcInteractEvents.add((NpcInteractEvent) example);
		}

		log.info("loaded " + events.size() + " events.");

		if(!autoable.isEmpty())
		{
			int time = rand.nextInt(Config.EVENT_MIN_TIMEOUT, Config.EVENT_MAX_TIMEOUT) * 60 * 1000;

			ExecutorManager executor = ExecutorManager.getInstance();
			executor.scheduleGeneral(NEXT_EVENT, time);

			log.info("the nearest event in " + (time / 1000 / 60) + " minutes.");
		}
	}

	/**
	 * Получение ссылок для диалога с НПС.
	 * 
	 * @param links контейнер ссылок.
	 * @param npc нпс с которым говорит игрок.
	 * @param player игрок.
	 */
	public void addLinks(Array<Link> links, Npc npc, Player player)
	{
		NpcInteractEvent[] array = npcInteractEvents.array();

		for(int i = 0, length = npcInteractEvents.size(); i < length; i++)
			array[i].addLinks(links, npc, player);
	}

	/**
	 * @param event завершенный ивент.
	 */
	public void finish(Event event)
	{
		if(event == null)
			return;

		if(event.isAuto())
		{
			setRunAutoEvent(null);

			ExecutorManager executor = ExecutorManager.getInstance();
			executor.scheduleGeneral(NEXT_EVENT, rand.nextInt(Config.EVENT_MIN_TIMEOUT, Config.EVENT_MAX_TIMEOUT) * 60 * 1000);
		}
	}

	/**
	 * Регистрация игрока на ивент.
	 * 
	 * @param eventName название ивента.
	 * @param player игрок.
	 */
	public void registerPlayer(String eventName, Player player)
	{
		Registered event = registeredEvents.get(eventName);

		if(event != null)
			event.registerPlayer(player);
	}

	/**
	 * @param runAutoEvent работающий авто ивент.
	 */
	public void setRunAutoEvent(Event runAutoEvent)
	{
		this.runAutoEvent = runAutoEvent;
	}

	/**
	 * Оповещение о старте ивента.
	 * 
	 * @param event стартовавший ивент.
	 */
	public void start(Event event)
	{
		if(event.isAuto())
			setRunAutoEvent(event);
	}

	/**
	 * Запуск ивента с нужным названием.
	 * 
	 * @param eventName название ивента.
	 */
	public void startEvent(String eventName)
	{
		Event event = nameEvents.get(eventName);

		if(event == null || event.isAuto() && runAutoEvent != null)
			return;

		event.start();
	}

	/**
	 * Остановка ивента с нужным названием.
	 * 
	 * @param eventName название ивента.
	 */
	public void stopEvent(String eventName)
	{
		Event event = nameEvents.get(eventName);

		if(event == null)
			return;

		event.stop();
	}

	/**
	 * Отмена регистрации игрока на ивент.
	 * 
	 * @param eventName название ивента.
	 * @param player игрок.
	 */
	public void unregisterPlayer(String eventName, Player player)
	{
		Registered event = registeredEvents.get(eventName);

		if(event != null)
			event.unregisterPlayer(player);
	}
}
