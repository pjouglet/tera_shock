package tera.gameserver.model.quests;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.array.FuncElement;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import rlib.util.table.FuncValue;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.QuestCompleteList;
import tera.gameserver.network.serverpackets.QuestSplit;
import tera.gameserver.network.serverpackets.QuestStarted;

/**
 * Модель списка выполненных квестов.
 *
 * @author Ronn
 */
public final class QuestList implements Foldable
{
	private static final Logger log = Loggers.getLogger(QuestList.class);

	private static final FoldablePool<QuestList> pool = Pools.newConcurrentFoldablePool(QuestList.class);

	public static QuestList newInstance(Player owner)
	{
		QuestList list = pool.take();

		if(list == null)
			list = new QuestList();

		list.owner = owner;

		return list;
	}

	/** пул временных штампов выполнения квеста */
	private final FoldablePool<QuestDate> questDatePool;
	/** пул состояний квестов */
	private final FoldablePool<QuestState> questStatePool;

	/** таблица выполненых квестов */
	private final Table<IntKey, QuestDate> completed;

	/** функция складывания всех штампов в пул */
	private final FuncValue<QuestDate> questDateFunc;

	/** список выполняемых квестов */
	private final Array<QuestState> active;

	/** функция складирования состояний активных квестов */
	private final FuncElement<QuestState> questStateFunc;

	/** владелец листа */
	private Player owner;

	public QuestList()
	{
		this.questDatePool = Pools.newConcurrentFoldablePool(QuestDate.class);
		this.questStatePool = Pools.newConcurrentFoldablePool(QuestState.class);
		this.completed = Tables.newConcurrentIntegerTable();
		this.active = Arrays.toConcurrentArray(QuestState.class);

		this.questDateFunc = new FuncValue<QuestDate>()
		{
			@Override
			public void apply(QuestDate value)
			{
				questDatePool.put(value);
			}
		};

		this.questStateFunc = new FuncElement<QuestState>()
		{
			@Override
			public void apply(QuestState element)
			{
				questStatePool.put(element);
			}
		};
	}

	/**
	 * Добавление уже активного квеста.
	 *
	 * @param state состояние квеста.
	 */
	public void addActiveQuest(QuestState state)
	{
		active.add(state);
	}

	/**
	 * @param date выполненный квест.
	 */
	public void addCompleteQuest(QuestDate date)
	{
		completed.put(date.getQuestId(), date);
	}

	/**
	 * Записать выполненный квест.
	 *
	 * @param quest выполненный квест.
	 */
	public void complete(Quest quest)
	{
		// получаем дату прошлого выполнения этого квеста
		QuestDate old = completed.get(quest.getId());

		// получаем текущее время
		long time = System.currentTimeMillis();

		// если этот квест уже выполнялся
		if(old != null)
			// обновляем дату
			old.setTime(time);
		else
			// иначе создаем новый штам и вставляем в таблицу
			completed.put(quest.getId(), newQuestDate(time, quest));
	}

	@Override
	public void finalyze()
	{
		owner = null;

		// складываем в пул все штампы
		completed.apply(questDateFunc);

		// очищаем таблицу штампов
		completed.clear();

		// складываем в пул все состояния
		active.apply(questStateFunc);

		// очищаем список активных
		active.clear();
	}

	/**
	 * Удаление активного квеста.
	 *
	 * @param quest завершенный квест.
	 * @param state состояние квеста.
	 * @param canceled был ли отменен.
	 */
	public void finishQuest(Quest quest, QuestState state, boolean canceled)
	{
		// получаем владельца списка квестов
		Player owner = getOwner();

		// если его нету
		if(owner == null)
		{
			log.warning("not found owner.");
			return;
		}

		// если его нету, выходим
		if(state == null)
		{
			log.warning("not found quest " + quest);
			return;
		}

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// если квест был отменен
		if(canceled)
			// удаляем из БД
			dbManager.removeQuest(owner, quest);
		else
			// обновляем в БД квест
			dbManager.finishQuest(owner, completed.get(quest.getId()));

		// удаляем из активных
		active.fastRemove(state);

		// завершаем
		state.finish();

		// слаживаем в пул
		questStatePool.put(state);
	}

	/**
	 * Складировать в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return список активных квестов.
	 */
	public Array<QuestState> getActiveQuests()
	{
		return active;
	}

	/**
	 * @return владелец списка квестов.
	 */
	public Player getOwner()
	{
		return owner;
	}

	/**
	 * Получение даты выполнения квеста.
	 *
	 * @param questId ид квеста.
	 * @return датавыполнения.
	 */
	public QuestDate getQuestDate(int questId)
	{
		return completed.get(questId);
	}

	/**
	 * Получение состояния выполняемого квеста.
	 *
	 * @param objectId уник ид квеста.
	 * @return состояние квеста.
	 */
	public QuestState getQuestState(int objectId)
	{
		// получаем список активных квестов
		Array<QuestState> active = getActiveQuests();

		active.readLock();
		try
		{
			// получаем массив состояний
			QuestState[] array = active.array();

			// перебираем стейты
			for(int i = 0, length = active.size(); i < length; i++)
			{
				// получаем стейт
				QuestState state = array[i];

				// если он для этого квеста, возвращаем его
				if(state.getObjectId() == objectId)
					return state;
			}

			return null;
		}
		finally
		{
			active.readUnlock();
		}
	}

	/**
	 * Получение состояния выполняемого квеста.
	 *
	 * @param quest выполняемый квест.
	 * @return состояние квеста.
	 */
	public QuestState getQuestState(Quest quest)
	{
		// получаем список активных квестов
		Array<QuestState> active = getActiveQuests();

		active.readLock();
		try
		{
			// получаем массив состояний
			QuestState[] array = active.array();

			// перебираем стейты
			for(int i = 0, length = active.size(); i < length; i++)
			{
				// получаем стейт
				QuestState state = array[i];

				// если он для этого квеста, возвращаем его
				if(state.getQuestId() == quest.getId())
					return state;
			}

			return null;
		}
		finally
		{
			active.readUnlock();
		}
	}

	/**
	 * @return есть ли активные квесты.
	 */
	public boolean hasActiveQuest()
	{
		return !active.isEmpty();
	}

	/**
	 * Выполнен ли квест.
	 *
	 * @param qurestId проверяемый квест.
	 * @return выполнен ли он.
	 */
	public boolean isCompleted(int questId)
	{
		return completed.containsKey(questId);
	}

	/**
	 * Выполнен ли квест.
	 *
	 * @param quest проверяемый квест.
	 * @return выполнен ли он.
	 */
	public boolean isCompleted(Quest quest)
	{
		return completed.containsKey(quest.getId());
	}

	/**
	 * Создание нового штампа времени выполнения квеста.
	 *
	 * @param time время выполнения.
	 * @param quest выполненный квест.
	 * @return новый штамп.
	 */
	public QuestDate newQuestDate(long time, Quest quest)
	{
		QuestDate date = questDatePool.take();

		if(date == null)
			date = new QuestDate();

		date.setTime(time);
		date.setQuest(quest);

		return date;
	}

	/**
	 * Создание нового состояния выполнения квеста.
	 *
	 * @param owner выполняющий квест.
	 * @param quest выполняемый квест.
	 * @param stage стадия квеста.
	 * @return новое состояние квеста.
	 */
	public QuestState newQuestState(Player owner, Quest quest, int stage)
	{
		QuestState state = questStatePool.take();

		if(state == null)
			state = new QuestState();

		state.setState(stage);
		state.setPlayer(owner);
		state.setQuest(quest);

		IdFactory idFactory = IdFactory.getInstance();

		state.setObjectId(idFactory.getNextQuestId());

		return state;
	}

	@Override
	public void reinit(){}

	/**
	 * Удаление отметки о пройденности квеста.
	 *
	 * @param id ид квеста.
	 */
	public void removeQuestComplete(int id)
	{
		completed.remove(id);
	}

	/**
	 * Сохранить переменные квестов.
	 */
	public void save()
	{
		// получаем список активных квестов
		Array<QuestState> active = getActiveQuests();

		active.readLock();
		try
		{
			QuestState[] array = active.array();

			for(int i = 0, length = active.size(); i < length; i++)
				array[i].save();
		}
		finally
		{
			active.readUnlock();
		}
	}

	/**
	 * Добавление активного квеста.
	 *
	 * @param quest взятый квест.
	 */
	public QuestState startQuest(Quest quest)
	{
		// создаем новый стейт для квеста
		QuestState state = newQuestState(owner, quest, 1);

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// создаем запись в БД
		dbManager.createQuest(state);

		// добавляем в активные
		active.add(state);

		return state;
	}

	/**
	 * Обновление отображения взятых квестов.
	 */
	public void updateQuestList()
	{
		// получаем владельца
		Player owner = getOwner();

		// получаем список активных квестов
		Array<QuestState> active = getActiveQuests();

		active.readLock();
		try
		{
			// получаем список активных квестов
			QuestState[] array = active.array();

			// отправляем пакеты о том, что эти квесты в процессе
			for(int i = 0, length = active.size(); i < length; i++)
				owner.sendPacket(QuestStarted.getInstance(array[i], 0, 0, 0, 0, 0), true);

			owner.sendPacket(QuestSplit.getInstance(), true);

			// отправляем список пройденных квестов
			owner.sendPacket(QuestCompleteList.getInstance(completed), true);
		}
		finally
		{
			active.readUnlock();
		}
	}
}
