package tera.gameserver.model.quests;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.table.FuncValue;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.playable.Player;

/**
 * Модель хранителя состояния выполнения квеста.
 *
 * @author Ronn
 */
public final class QuestState implements Foldable
{
	private static final FuncValue<Wrap> FOLD_WRAPS = new FuncValue<Wrap>()
	{
		@Override
		public void apply(Wrap value)
		{
			value.fold();
		}
	};

	/** таблица переменных */
	private final Table<String, Wrap> variables;

	/** список названий переменных */
	private final Array<String> varNames;

	/** игрок, выполняющий квест */
	private Player player;

	/** выполняемый квест */
	private Quest quest;

	/** состояние нахождения на панели */
	private QuestPanelState panelState;

	/** обджект ди квеста */
	private int objectId;
	/** состояние квеста */
	private int state;

	public QuestState()
	{
		this.panelState = QuestPanelState.REMOVED;
		this.variables = Tables.newConcurrentObjectTable();
		this.varNames = Arrays.toArray(String.class);
	}

	@Override
	public void finalyze()
	{
		player = null;
		quest = null;

		// складируем все обертки
		variables.apply(FOLD_WRAPS);

		// очищаем таблицу
		variables.clear();
	}

	/**
	 * Очитска от переменных.
	 */
	public void finish()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// очищаем переменные
		dbManager.clearQuestVar(this);
	}

	/**
	 * @return обджект ди квеста.
	 */
	public int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return состояние нахождения квеста на панели.
	 */
	public QuestPanelState getPanelState()
	{
		return panelState;
	}

	/**
	 * @return состояние нахождения квеста на панели.
	 */
	public int getPanelStateId()
	{
		return panelState.ordinal();
	}

	/**
	 * @return игрок, проходящий квест.
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
	 * @return ид квеста.
	 */
	public int getQuestId()
	{
		return quest.getId();
	}

	/**
	 * @return стадия квеста.
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @param name название переменной.
	 * @return значение переменной.
	 */
	public Wrap getVar(String name)
	{
		return variables.get(name);
	}

	/**
	 * удаление переменной.
	 *
	 * @param name название перемеенной.
	 */
	public void removeWar(String name)
	{
		// получаем удаленную переменную
		Wrap wrap = variables.remove(name);

		// если такая есть
		if(wrap != null)
			// складируем в пул
			wrap.fold();
	}

	/**
	 * @return таблица переменных квеста.
	 */
	public Table<String, Wrap> getVariables()
	{
		return variables;
	}

	/**
	 * @return список названий переменных квеста.
	 */
	public Array<String> getVarNames()
	{
		return varNames;
	}

	/**
	 * Подготовить переменные.
	 */
	public void prepare()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// загружаем переменные
		dbManager.restoreQuestVar(this);
	}

	@Override
	public void reinit(){}

	/**
	 * Сохранение переменных.
	 */
	public void save()
	{
		// получаем таблицу переменных квеста
		Table<String, Wrap> variables = getVariables();

		// если она пуста, выходим
		if(variables.isEmpty())
			return;

		variables.readLock();
		try
		{
			// получаем спиоск названий переменных
			Array<String> names = getVarNames();

			// очищаем его
			names.clear();

			// запонляем его
			variables.keyArray(names);

			// получаем массив названий
			String[] array = names.array();

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// перебираем названия переменных
			for(int i = 0, length = names.size(); i < length; i++)
			{
				// получаем название переменной
				String name = array[i];

				// обновляем ее в БД
				dbManager.storyQuestVar(this, name, variables.get(name));
			}
		}
		finally
		{
			variables.readUnlock();
		}
	}

	/**
	 * @param objectId уникальный ид выполняемого квеста.
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param panelState состояние нахождения квеста на панели.
	 */
	public void setPanelState(QuestPanelState panelState)
	{
		this.panelState = panelState;
	}

	/**
	 * @param player игрок, который выполнчет квест.
	 */
	public void setPlayer(Player player)
	{
		this.player = player;
	}

	/**
	 * @param quest выполняемый квест.
	 */
	public void setQuest(Quest quest)
	{
		if(quest == null)
			new Exception("not found quest").printStackTrace();

		this.quest = quest;
	}

	/**
	 * @param state стадия квеста.
	 */
	public void setState(int state)
	{
		this.state = state;
	}

	/**
	 * @param name название переменной.
	 * @param wrap значение переменной.
	 */
	public void setVar(String name, Wrap wrap)
	{
		variables.put(name, wrap);
	}

	@Override
	public String toString()
	{
		return "QuestState  variables = " + variables + ", player = " + player + ", quest = " + quest + ", objectId = " + objectId + ", state = " + state;
	}
}
