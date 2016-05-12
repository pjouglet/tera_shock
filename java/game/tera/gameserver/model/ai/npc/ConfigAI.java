package tera.gameserver.model.ai.npc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.VarTable;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.ai.npc.taskfactory.TaskFactory;
import tera.gameserver.model.ai.npc.thinkaction.ThinkAction;

/**
 * Модель конфигурации АИ.
 *
 * @author Ronn
 */
public final class ConfigAI
{
	private static final Logger log = Loggers.getLogger(ConfigAI.class);

	public static final String TASK_FACTORY_PACKAGE = TaskFactory.class.getPackage().getName();
	public static final String THINK_ACTION_PACKAGE = ThinkAction.class.getPackage().getName();

	public static final String DEFAULT_NOTICE_MESSAGES = "DefaultNoticeMessages";
	public static final String DEFAULT_WALK_MESSAGES = "DefaultWalkMessages";
	public static final String DEFAULT_UPDATE_NOTICE_MESSAGES = "DefaultUpdateNoticeMessages";
	public static final String DEFAULT_SWITCH_TARGET_MESSAGES = "DefaultSwitchTargetMessages";

	public static final String DEFAULT_JUMP_SIDE_MESSAGES = "DefaultJumpSideMessages";
	public static final String DEFAULT_JUMP_BEHIND_MESSAGES = "DefaultJumpBehindMessages";
	public static final String DEFAULT_SHORT_ATTACK_MESSAGES = "DefaultShortAttackMessages";
	public static final String DEFAULT_LONG_ATTACK_MESSAGES = "DefaultLongAttackMessages";

	public static final String DEFAULT_RUN_AWAY_MESSAGES = "DefaultRunAwayMessages";

	public static final int DEFAULT_BATTLE_MAX_RANGE = 2000;
	public static final int DEFAULT_REACTION_MAX_RANGE = 1500;

	public static final int DEFAULT_RANDOM_MIN_WALK_RANGE = 50;
	public static final int DEFAULT_RANDOM_MAX_WALK_RANGE = 150;

	public static final int DEFAULT_RANDOM_MIN_WALK_DELAY = 5000;
	public static final int DEFAULT_RANDOM_MAX_WALK_DELAY = 25000;

	public static final int DEFAULT_NOTICE_RANGE = 150;
	public static final int DEFAULT_SHORT_RATE = 150;

	public static final int DEFAULT_AI_TASK_DELAY = 2000;

	public static final int DEFAULT_DISTANCE_TO_SPAWN_LOC = 40;

	public static final int DEFAULT_DISTANCE_TO_TELEPORT = 10000;

	public static final int DEFAULT_GROUP_CHANCE = 25;

	public static final int DEFAULT_LAST_ATTACKED_TIME = 60000;

	public static final int DEFAULT_CRITICAL_HP = 30;

	public static final int DEFAULT_REAR_RATE = 300;

	public static final int DEFAULT_RUN_AWAY_RATE = 300;

	public static final int DEFAULT_RUN_AWAY_OFFSET = 750;

	public static final int DEFAULT_MAX_MOST_HATED = 3;

	/** наор думальщиков */
	private ThinkAction[] thinks;
	/** набор создателей новых тасков */
	private TaskFactory[] factory;

	/** название конфига */
	private String name;

	/** интервалы работы АИ */
	private int[] intervals;

	/** является ли АИ глобальным */
	private boolean global;
	/** является ли АИ активным */
	private boolean runnable;

	@SuppressWarnings("unchecked")
	public ConfigAI(Node node)
	{
		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		this.name = vars.getString("name");

		// получаем все состояния АИ нпс
		NpcAIState[] states = NpcAIState.values();

		// создаем обработчики состояния
		this.thinks = new ThinkAction[states.length];
		this.factory = new TaskFactory[states.length];

		// создаем набор интервалов для состояний */
		this.intervals = new int[states.length];

		// начинаем парсить хмл
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			// если это не элемент узла, пропускаем
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("intervals".equals(child.getNodeName()))
			{
				// парсим атрибуты
				vars.parse(child);

				// получаем дефолтный интервал
				int def = vars.getInteger("default", DEFAULT_AI_TASK_DELAY);

				// перебираем интервалы
				for(Node interval = child.getFirstChild(); interval != null; interval = interval.getNextSibling())
				{
					// если это не интервал
					if(interval.getNodeType() != Node.ELEMENT_NODE || !"interval".equals(interval.getNodeName()))
						continue;

					// парсим атрибуты
					vars.parse(interval);

					// заносим новый интервал
					intervals[vars.getEnum("state", NpcAIState.class).ordinal()] = vars.getInteger("val");
				}

				// дозаполняем неуказанные интервалы
				for(int i = 0, length = intervals.length ; i < length; i++)
					if(intervals[i] < 1)
						intervals[i] = def;
			}
			else if("tasks".equals(child.getNodeName()))
			{
				// перебираем фабрики
				for(Node task = child.getFirstChild(); task != null; task = task.getNextSibling())
				{
					// если это не установка фабрики, пропускаем
					if(task.getNodeType() != Node.ELEMENT_NODE || !"task".equals(task.getNodeName()))
						continue;

					// парсим атрибуты
					vars.parse(task);

					try
					{
						// получаем класс фабрики
						Class<TaskFactory> type = (Class<TaskFactory>) Class.forName(TASK_FACTORY_PACKAGE + "." + vars.getString("factory"));

						// получаем конструктор
						Constructor<TaskFactory> constructor = type.getConstructor(Node.class);

						// заносим его в таблицу
						factory[vars.getEnum("state", NpcAIState.class).ordinal()] = constructor.newInstance(task);
					}
					catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e)
					{
						log.warning("name " + name);
						log.warning(e);
					}
				}
			}
			else if("thinks".equals(child.getNodeName()))
			{
				// перебираем фабрики
				for(Node think = child.getFirstChild(); think != null; think = think.getNextSibling())
				{
					// если это не установка фабрики, пропускаем
					if(think.getNodeType() != Node.ELEMENT_NODE || !"think".equals(think.getNodeName()))
						continue;

					// парсим атрибуты
					vars.parse(think);

					try
					{
						// получаем класс фабрики
						Class<ThinkAction> type = (Class<ThinkAction>) Class.forName(THINK_ACTION_PACKAGE + "." + vars.getString("action"));

						// получаем конструктор
						Constructor<ThinkAction> constructor = type.getConstructor(Node.class);

						// заносим его в таблицу
						thinks[vars.getEnum("state", NpcAIState.class).ordinal()] = constructor.newInstance(think);
					}
					catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e)
					{
						log.warning("name " + name);
						log.warning(e);
					}
				}
			}
		}

		vars.parse(node, "set", "name", "val");

		this.global = vars.getBoolean("global", false);
		this.runnable = vars.getBoolean("runnable", true);

		// дозаполняем
		for(int i = 0, length = states.length; i < length; i++)
		{
			if(thinks[i] == null)
				thinks[i] = states[i].getThink();

			if(factory[i] == null)
				factory[i] = states[i].getFactory();
		}
	}

	/**
	 * @return создатель тасков для указанного состояния АИ.
	 */
	public final TaskFactory getFactory(NpcAIState state)
	{
		return factory[state.ordinal()];
	}

	/**
	 * Получаем интервал работы АИ в соответствии с состоянием АИ.
	 *
	 * @param state состояние АИ.
	 * @return интервал его работы.
	 */
	public final int getInterval(NpcAIState state)
	{
		return intervals[state.ordinal()];
	}

	/**
	 * @return название конфига.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return соображалка для указанного состояния АИ.
	 */
	public final ThinkAction getThink(NpcAIState state)
	{
		return thinks[state.ordinal()];
	}

	/**
	 * @return глобальный ли АИ.
	 */
	public boolean isGlobal()
	{
		return global;
	}

	/**
	 * @return активный ли АИ.
	 */
	public boolean isRunnable()
	{
		return runnable;
	}

	@Override
	public String toString()
	{
		return "ConfigAI  name = " + name;
	}
}
