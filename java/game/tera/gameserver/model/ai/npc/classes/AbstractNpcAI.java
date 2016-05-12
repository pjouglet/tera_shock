package tera.gameserver.model.ai.npc.classes;

import java.util.concurrent.ScheduledFuture;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MoveType;
import tera.gameserver.model.NpcAIState;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.ai.AbstractCharacterAI;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.ai.npc.Task;
import tera.gameserver.model.ai.npc.TaskType;
import tera.gameserver.model.ai.npc.taskfactory.TaskFactory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Minion;
import tera.gameserver.model.npc.MinionLeader;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.TargetType;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Базовая модель АИ нпс.
 *
 * @author Ronn
 */
public abstract class AbstractNpcAI<T extends Npc> extends AbstractCharacterAI<T> implements NpcAI<T>, Runnable
{
	private static final Integer[] ATTACK_HEADINGS =
	{
		Integer.valueOf(-12000),
		Integer.valueOf(-9000),
		Integer.valueOf(-6000),
		Integer.valueOf(-3000),
		Integer.valueOf(0),
		Integer.valueOf(3000),
		Integer.valueOf(6000),
		Integer.valueOf(9000),
		Integer.valueOf(12000),
	};

	/** пул заданий */
	protected final FoldablePool<Task> taskPool;
	/** список заданий */
	protected final Array<Task> taskList;

	/** текущая цель */
	protected Character target;
	/** текущее состояние АИ */
	protected NpcAIState currentState;
	/** конфиг АИ */
	protected ConfigAI config;

	/** ссылка на таск работы АИ */
	private volatile ScheduledFuture<? extends AbstractNpcAI<T>> schedule;
	/** направление атаки */
	private volatile Integer attackHeading;

	/** статус работы АИ */
	private volatile int running;

	/** время очистки агр листа */
	protected long clearAggro;
	/** время следующего рандомного движения */
	protected long nextRandomWalk;
	/** время последней атаки */
	protected long lastAttacked;
	/** время отправки последней осведомляющей иконки */
	protected long lastNotifyIcon;
	/** время последнего сообщения */
	protected long lastMessage;
	/** время перехода к след. точке маршрута */
	protected long nextRoutePoint;

	/** счетчик аттак на НПС */
	protected int attackedCount;
	/** счетчик догонений */
	protected int followCounter;
	/** индекс позиции на маршруте */
	protected int routeIndex;

	public AbstractNpcAI(T actor, ConfigAI config)
	{
		super(actor);

		this.config = config;
		this.taskList = Arrays.toConcurrentArray(Task.class);
		this.taskPool = Pools.newFoldablePool(Task.class);
		this.currentState = NpcAIState.WAIT;

		if(config == null)
		{
			log.warning(this, new IllegalArgumentException("not found config."));
			throw new IllegalArgumentException("not found config.");
		}
	}

	@Override
	public long getNextRoutePoint()
	{
		return nextRoutePoint;
	}

	@Override
	public void setNextRoutePoint(long nextRoutePoint)
	{
		this.nextRoutePoint = nextRoutePoint;
	}

	@Override
	public int getRouteIndex()
	{
		return routeIndex;
	}

	@Override
	public void setRouteIndex(int routeIndex)
	{
		this.routeIndex = routeIndex;
	}

	@Override
	public void setLastMessage(long time)
	{
		this.lastMessage = time;
	}

	@Override
	public long getLastMessage()
	{
		return lastMessage;
	}

	/**
	 * @return список задач АИ.
	 */
	public Array<Task> getTaskList()
	{
		return taskList;
	}

	@Override
	public void addCastMoveTask(float x, float y, float z, Skill skill, Character target)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(TaskType.MOVE_ON_CAST);
			newTask.setSkill(skill);
			newTask.setTarget(target);
			newTask.setX(x).setY(y).setZ(z);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addCastTask(Skill skill, Character target)
	{
		addCastTask(skill, target, Strings.EMPTY);
	}

	@Override
	public void addCastTask(Skill skill, Character target, int heading)
	{
		addCastTask(skill, target, heading, Strings.EMPTY);
	}

	@Override
	public void addCastTask(Skill skill, Character target, int heading, String message)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(TaskType.CAST_ON_HEADING);
			newTask.setTarget(target);
			newTask.setSkill(skill);
			newTask.setHeading(heading);
			newTask.setMessage(message);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addCastTask(Skill skill, Character target, String message)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(TaskType.CAST);
			newTask.setTarget(target);
			newTask.setSkill(skill);
			newTask.setMessage(message);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addMoveTask(float x, float y, float z, boolean update)
	{
		addMoveTask(x, y, z, update, Strings.EMPTY);
	}

	@Override
	public void addMoveTask(float x, float y, float z, boolean update, String message)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(update? TaskType.MOVE_UPDATE_HEADING : TaskType.MOVE_NOT_UPDATE_HEADING);
			newTask.setX(x).setY(y).setZ(z);
			newTask.setMessage(message);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addMoveTask(float x, float y, float z, Skill skill, Character target)
	{
		addMoveTask(x, y, z, skill, target, Strings.EMPTY);
	}

	@Override
	public void addMoveTask(float x, float y, float z, Skill skill, Character target, String message)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(TaskType.MOVE_ON_CAST);
			newTask.setX(x).setY(y).setZ(z);
			newTask.setTarget(target);
			newTask.setSkill(skill);
			newTask.setMessage(message);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addMoveTask(Location loc, boolean update)
	{
		addMoveTask(loc, update, Strings.EMPTY);
	}

	@Override
	public void addMoveTask(Location loc, boolean update, String message)
	{
		addMoveTask(loc.getX(), loc.getY(), loc.getZ(), update, message);
	}

	@Override
	public void addNoticeTask(Character target, boolean fast)
	{
		addNoticeTask(target, fast, Strings.EMPTY);
	}

	@Override
	public void addNoticeTask(Character target, boolean fast, String message)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		taskList.writeLock();
		try
		{
			Task newTask = taskPool.take();

			if(newTask == null)
				newTask = new Task();

			newTask.setType(fast? TaskType.NOTICE_FAST : TaskType.NOTICE);
			newTask.setTarget(target);
			newTask.setMessage(message);

			taskList.add(newTask);
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	@Override
	public void addTask(Task task)
	{
		taskList.add(task);
	}

	@Override
	public boolean checkAggression(Character target)
	{
		// получаем НПС
		T actor = getActor();

		// если его нет или нет цели, или цель не в оне агра
		if(actor == null || target == null || !target.isInRange(actor, actor.getAggroRange()))
			return false;

		// если НПС может атаковать цель
		if(actor.checkTarget(target))
		{
			// если цель не игрок
			if(!target.isPlayer())
			{
				// добавляем агр поинт
				actor.addAggro(target, 1, true);
				return true;
			}
			// если цель не ГМ
			else if(!target.getPlayer().isGM())
			{
				// добавляем агр поинт
				actor.addAggro(target, 1, true);
				return true;
			}
		}

		return false;
	}

	/**
	 * @return пул использованных задач.
	 */
	public FoldablePool<Task> getTaskPool()
	{
		return taskPool;
	}

	@Override
	public void clearTaskList()
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		// если пуст, выходим
		if(taskList.isEmpty())
			return;

		// получаем пул использованных задач
		FoldablePool<Task> taskPool = getTaskPool();

		taskList.writeLock();
		try
		{
			Task[] array = taskList.array();

			// вносим старые задачи в пул
			for(int i = 0, length = taskList.size(); i < length; i++)
				taskPool.put(array[i]);

			// очищаем таск лист
			taskList.clear();
		}
		finally
		{
			taskList.writeUnlock();
		}
	}

	/**
	 * Форс очищение списка заданий.
	 */
	protected void forceClearTaskList()
	{
		taskList.clear();
	}

	@Override
	public boolean doTask(T actor, long currentTime, LocalObjects local)
	{
		// если актор мертв, выходим
		if(actor.isDead())
			return false;

		// получаем список задач
		Array<Task> taskList = getTaskList();

		// если заданий нет, выходим
		if(taskList.isEmpty())
			return true;

		// получаем следующее задание
		Task currentTask = taskList.poll();

		// тип задания
		TaskType type = currentTask.getType();

		// если типа нету о_О
		if(type == null)
		{
			//  уведомляемd
			log.warning(this, "not found type for task " + currentTask);
			// очищаем список заданий
			forceClearTaskList();
			// выходим
			return false;
		}

		// определяем метод исполнения
		switch(type)
		{
			// придти в указаную точку с установкой направления в соответсвии точки
			case MOVE_UPDATE_HEADING:
			{
				// если перемещаться запрещено, удаляем задание и выходим
				if(actor.isMovementDisabled())
					return maybeNextTask(currentTask, false, true);

				// определям дистанцию до целевой точки
				float distance = actor.getDistance(currentTask.getX(), currentTask.getY(), currentTask.getZ());

				// если она меньше 30, то удаляем задание и выходим
				if(distance < 30F)
					return maybeNextTask(currentTask, false, true);

				// рассчитываем направлении в соответсвии с целевой точкой
				actor.setHeading(actor.calcHeading(currentTask.getX(), currentTask.getY()));

				// если есть сообщение
				if(currentTask.getMessage() != Strings.EMPTY)
					// выдаем его
					actor.sayMessage(currentTask.getMessage());

				// указываем идти туда
				startMove(actor.getHeading(), MoveType.RUN, currentTask.getX(), currentTask.getY(), currentTask.getZ(), true, false);

				// удаляем задание и выходим
				return maybeNextTask(currentTask, false, true);
			}
			//придти в указаную точку без рассчета направления
			case MOVE_NOT_UPDATE_HEADING:
			{
				//если перемещение запрещено, удаляем задание и выходим
				if(actor.isMovementDisabled())
					return maybeNextTask(currentTask, false, true);

				//определяем дистанцию до целевой точки
				float distance = actor.getDistance(currentTask.getX(), currentTask.getY(), currentTask.getZ());

				//если дистанция меньше 30, удаляем задание и выходим
				if(distance < 30F)
					return maybeNextTask(currentTask, false, true);

				//указываем идти туда
				startMove(actor.getHeading(), MoveType.RUN, currentTask.getX(), currentTask.getY(), currentTask.getZ(), true, false);

				//удаляем задание и выходим
				return maybeNextTask(currentTask, false, true);
			}
			// сфокусироваться на указанном персонаже
			case NOTICE_FAST:
			case NOTICE:
			{
				// определяем цель фокуса
				Character target = currentTask.getTarget();

				if(target == actor)
				{
					// очищаем задания
					forceClearTaskList();
					// выходим
					return false;
				}

				// определяем тип разворота
				boolean fast = type == TaskType.NOTICE_FAST;

				// если цель не перед лицом
				if(target != null && !actor.isInFront(target))
				{
					// если быстрый режим
					if(fast)
						// мгновенно разворачиваем
						actor.setHeading(actor.calcHeading(target.getX(), target.getY()));
					else
						// иначе ставим плавный разворот
						actor.nextTurn(actor.calcHeading(target.getX(), target.getY()));
				}

				// обновляем боевую стойку
				actor.startBattleStance(target);

				// обновляем фокус таргет
				setTarget(target);

				// если есть сообщение
				if(currentTask.getMessage() != Strings.EMPTY)
					// выдаем его
					actor.sayMessage(currentTask.getMessage());

				// удаляем задание и выходим
				return maybeNextTask(currentTask, false, true);
			}
			// скастовать скил на цель
			case CAST_ON_HEADING:
			case CAST:
			{
				// получаем цель скила
				Character target = currentTask.getTarget();

				// получаем скил, который будем кастить
				Skill skill = currentTask.getSkill();

				// если чего-то нет, удаляем таск
				if(target == null || skill == null)
				{
					// очищаем задания
					clearTaskList();
					// возвращаем необоходимость создания новых
					return false;
				}

				// нацелен ли скил на себя же
				boolean self = actor == target || skill.getTargetType() == TargetType.TARGET_SELF;

				// получаем зону воздействия скила
				int maxDist = skill.getCastMaxRange();
				int minDist = skill.getCastMinRange();

				// определяем текущее расстояние до цели
				int currentDistance = self? 0 : (int) (target.getDistance(actor.getX(), actor.getY(), target.getZ()));

				// если цель не входит в зону действия скила
				if(!self && (currentDistance > maxDist || currentDistance < minDist))
				{
					// получаем счетчик попыток догона
					int followCounter = getFollowCounter();

					// TODO вынести в параметр если превысили лимит
					if(followCounter > 3)
					{
						// зануляем
						setFollowCounter(0);
						// зануляем направление
						setAttackHeading(null);
						// выходим
						return maybeNextTask(currentTask, false, true);
					}

					// получаем рекамендуемое кастовое расстояние
					int newDist = target.isRangeClass()? maxDist / 2 : (int) (maxDist - actor.getGeomRadius()) * 2 / 3;

					// делаем поправку на минимально допустимое расстояние
					newDist = Math.max(newDist, (int) (actor.getGeomRadius() + target.getGeomRadius()));
					newDist = Math.min(maxDist, newDist);

					// получаем направление атаки
					Integer attackHeading = getAttackHeading();

					// если направления атаки нет
					if(attackHeading == null)
					{
						// получаем новое
						attackHeading = getNextAttackHeading();
						// запоминаем его
						setAttackHeading(attackHeading);
					}

					// расчитываем направление подхода к цели
					int heading = target.calcHeading(actor.getX(), actor.getY()) + attackHeading.intValue();

					// рассчитываем целевую точку
					float newX = Coords.calcX(target.getX(), newDist, heading);
					float newY = Coords.calcY(target.getY(), newDist, heading);

					// получаем менеджера геодаты
					GeoManager geoManager = GeoManager.getInstance();

					// определяем высоту в той точке
					float newZ = geoManager.getHeight(actor.getContinentId(), newX, newY, target.getZ());

					// добавляем задание доганять
					addMoveTask(newX, newY, newZ, true);
					// ставим его после задания догнать
					addTask(currentTask);

					// увеличиваем счетчик приследований
					setFollowCounter(followCounter + 1);

					// выходим
					return doTask(actor, currentTime, local);
				}

				// запоминаем цель
				actor.setTarget(getTarget());

				// зануяем направление атаки
				setAttackHeading(null);

				// если есть сообщение
				if(currentTask.getMessage() != Strings.EMPTY)
					// выдаем его
					actor.sayMessage(currentTask.getMessage());

				// получаем точку удара в плоскости
				float targetX = target.getX();
				float targetY = target.getY();

				// получаем скорость скила
				int speed = skill.getSpeed();

				// если цель не актор и она в движении
				if(target != actor && target.isMoving())
				{
					// получаем формулы
					Formulas formulas = Formulas.getInstance();

					// время удара до цели
					float time = 0F;

					// если скил стреляющий
					if(speed > 1)
						// расчитываемся время полета
						time += ((float) currentDistance / speed);


					// расчитываем время каста до удара
					time += (formulas.castTime(skill.getDelay(), actor) / 1000F);

					// рассчитываем расстояние смещение цели за время полета скила
					int newDist = (int) (target.getRunSpeed() * time) + skill.getRadius();

					// получаем направление цели
					float radians = Angles.headingToRadians(target.getHeading());

					// перерасчитываем точку удара в плоскости
					targetX = Coords.calcX(targetX, newDist, radians);
					targetY = Coords.calcY(targetY, newDist, radians);
				}

				// определяем выоту удара
				float targetZ = target.getZ() + (target.getGeomHeight() / 2);

				// указываем исполнить каст
				if(type == TaskType.CAST_ON_HEADING)
					startCast(skill, currentTask.getHeading(), targetX, targetY, targetZ);
				else
					startCast(skill, actor.calcHeading(targetX, targetY), targetX, targetY, targetZ);

				// удаляем задание и выходим
				return maybeNextTask(currentTask, false, true);
			}
			default:
			{
				log.warning(this, "not supported task type " + type);
				break;
			}
		}

		return true;
	}

	/**
	 * @param followCounter счетчик попыток догнать цель.
	 */
	public void setFollowCounter(int followCounter)
	{
		this.followCounter = followCounter;
	}

	/**
	 * @return счетчик попыток догнать цель.
	 */
	public int getFollowCounter()
	{
		return followCounter;
	}

	@Override
	public void removeTask(Task task)
	{
		taskList.slowRemove(task);
	}

	@Override
	public int getAttackedCount()
	{
		return attackedCount;
	}

	@Override
	public long getClearAggro()
	{
		return clearAggro;
	}

	@Override
	public TaskFactory getCurrentFactory()
	{
		return config.getFactory(currentState);
	}

	@Override
	public NpcAIState getCurrentState()
	{
		return currentState;
	}

	@Override
	public long getLastAttacked()
	{
		return lastAttacked;
	}

	@Override
	public long getLastNotifyIcon()
	{
		return lastNotifyIcon;
	}

	@Override
	public final long getNextRandomWalk()
	{
		return nextRandomWalk;
	}

	@Override
	public Character getTarget()
	{
		return target;
	}

	@Override
	public boolean isGlobalAI()
	{
		return config.isGlobal();
	}

	@Override
	public boolean isWaitingTask()
	{
		return !taskList.isEmpty();
	}

	/**
	 * Определить нужно ли новое задание.
	 *
	 * @param task предыдущее задание.
	 * @return нужно ли определить новое задание.
	 */
	protected boolean maybeNextTask(Task task)
	{
		return maybeNextTask(task, true, true);
	}

	/**
	 * Определить нужно ли новое задание.
	 *
	 * @param task предыдущее задание.
	 * @param remove удалять ли его из списка.
	 * @return нужно ли определить новое задание.
	 */
	protected boolean maybeNextTask(Task task, boolean remove, boolean pool)
	{
		// получаем список задач
		Array<Task> taskList = getTaskList();

		// если таск есть
		if(task != null)
		{
			taskList.writeLock();
			try
			{
				if(remove)
					taskList.slowRemove(task);

				if(pool)
					taskPool.put(task);
			}
			finally
			{
				taskList.writeUnlock();
			}
		}

		// есть ли еще задания
		return taskList.isEmpty();
	}

	@Override
	public void notifyAttacked(Character attacker, Skill skill, int damage)
	{
		// получаем НПС
		T actor = getActor();

		// если НПС нет, выходим
		if(actor == null)
			return;

		// если АИ почему-то выключен
		if(running < 1)
			// запускаем
			startAITask();

		// получаем текущее состояние
		NpcAIState currentState = getCurrentState();

		// если НПС в ожидании
		if(currentState == NpcAIState.WAIT || currentState == NpcAIState.PATROL)
		{
			// если НПС в движении
			if(actor.isMoving())
				// останавливаем
				actor.stopMove();
		}

		// если мертв, выходим
		if(attacker == null)
			return;

		// добавляем дмг в аггро
		actor.addAggro(attacker, damage, true);

		if(skill != null)
			// добавляем хейт в аггро
			actor.addAggro(attacker, skill.getAggroPoint(), false);

		// увеличиваем счетчик
		setAttackedCount(attackedCount + 1);

		// запоминаем время последней атаки
		setLastAttacked(System.currentTimeMillis());

		// получаем радиус фракции
		int range = actor.getFractionRange();

		// если радиус есть
		if(range > 0)
		{
			// получаем название фракции
			String fraction = actor.getFraction();

			// если название есть
			if(fraction != Strings.EMPTY)
			{
				LocalObjects local = LocalObjects.get();

				// перебераем окружающих нпс
				Array<Npc> npcs = World.getAround(Npc.class, local.getNextNpcList(), actor, range);

				if(!npcs.isEmpty())
				{
					// получаем массив нпс
					Npc[] array = npcs.array();

					// перебираем окружающих нпс
					for(int i = 0, length = npcs.size(); i < length; i++)
					{
						// получаем нпс
						Npc npc = array[i];

						// если нпс не подходит, пропускаем
						if(npc == null || npc == actor || !fraction.equals(npc.getFraction()))
							continue;

						// уведомляем нпс о нападении
						npc.getAI().notifyClanAttacked(actor, attacker, damage);
					}
				}
			}
		}

		// получаем лидера группы
		MinionLeader leader = actor.getMinionLeader();

		// если это группа НПС
		if(leader != null)
		{
			// получаем минионов
			Array<Minion> minions = leader.getMinions();

			// если они есть
			if(!minions.isEmpty())
			{
				minions.readLock();
				try
				{
					// получаем массив минионов
					Npc[] array = minions.array();

					// перебираем доступных минионов
					for(int i = 0, length = minions.size(); i < length; i++)
					{
						// получаем миниона
						Npc minion = array[i];

						// если это атакуемый, пропускаем
						if(minion == null || minion == actor)
							continue;

						// уведомляем о нападении
						minion.getAI().notifyPartyAttacked(actor, attacker, damage);
					}
				}
				finally
				{
					minions.readUnlock();
				}
			}

			// если это не лидера атаковали
			if(leader != actor)
				// уведомляем его об этом
				leader.getAI().notifyPartyAttacked(actor, attacker, damage);
		}
	}

	@Override
	public void notifyClanAttacked(Character attackedMember, Character attacker, int damage)
	{
		// получаем НПС
		T actor = getActor();

		// если нпс нету или мертв, выходим
		if(actor == null || actor.isDead())
			return;

		// добавляем дмг в аггро
		actor.addAggro(attacker, damage, true);
	}

	@Override
	public void notifyPartyAttacked(Character attackedMember, Character attacker, int damage)
	{
		// получаем НПС
		T actor = getActor();

		// если нпс нету или мертв, выходим
		if(actor == null || actor.isDead())
			return;

		// добавляем дмг в аггро
		actor.addAggro(attacker, damage, true);
	}

	@Override
	public void run()
	{
		// получаем актора
		T actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем текущее время
		long currentTime = System.currentTimeMillis();

		try
		{
			// думаем свое след. действие
			config.getThink(currentState).think(this, actor, local, config, currentTime);
		}
		catch(Exception exc)
		{
			log.warning(this, exc);
		}
	}

	@Override
	public void setAttackedCount(int count)
	{
		this.attackedCount = count;
	}

	@Override
	public void setClearAggro(long clearAggro)
	{
		this.clearAggro = clearAggro;
	}

	/**
	 * @param config конфиг АИ.
	 */
	public void setConfig(ConfigAI config)
	{
		this.config = config;
	}

	/**
	 * @param currentState текущее состояние АИ.
	 */
	protected void setCurrentState(NpcAIState currentState)
	{
		this.currentState = currentState;
	}

	@Override
	public void setLastAttacked(long lastAttacked)
	{
		this.lastAttacked = lastAttacked;
	}

	@Override
	public void setLastNotifyIcon(long lastNotifyIcon)
	{
		this.lastNotifyIcon = lastNotifyIcon;
	}

	@Override
	public void setNewState(NpcAIState state)
	{
		// если новое состояние уже применено, выходим
		if(currentState == state)
			return;

		synchronized(this)
		{
			// если новое состояние уже применено, выходим
			if(currentState == state)
				return;

			// если АИ активно
			if(schedule != null)
				// отменяем предыдущий таск
				schedule.cancel(false);

			// изменяем состояние
			setCurrentState(state);

			// подготавливаем новое состояние
			config.getThink(currentState).prepareState(this, actor, LocalObjects.get(), config, System.currentTimeMillis());

			// получаем интервал новой работы
			int interval = config.getInterval(state);

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// запускаем новый таск
			schedule = executor.scheduleAiAtFixedRate(this, interval, interval);
		}
	}

	@Override
	public final void setNextRandomWalk(long nextRandomWalk)
	{
		this.nextRandomWalk = nextRandomWalk;
	}

	@Override
	public void setTarget(Character target)
	{
		this.target = target;
	}

	@Override
	public synchronized void startAITask()
	{
		if(!config.isRunnable())
			return;

		// если уже работает, выходим
		if(running > 0)
			return;

		// ставим флаг что работает
		running += 1;

		// получаем интервал работы
		int interval = config.getInterval(currentState);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем АИ
		schedule = executor.scheduleAiAtFixedRate(this, interval, interval);
	}

	@Override
	public synchronized void stopAITask()
	{
		if(!config.isRunnable())
			return;

		// если уже остановлен, выходим
		if(running < 1)
			return;

		running -= 1;

		// если есть ссылка на таск, отменяем
		if(schedule != null)
			schedule.cancel(false);

		// ставим стадию ожидания
		setCurrentState(NpcAIState.WAIT);

		// получаем управленци
		T actor = getActor();

		// если он есть
		if(actor != null)
			// очищаем агр лист
			actor.clearAggroList();

		// очищаем задачи
		clearTaskList();

		// зануляем
		schedule = null;
	}

	@Override
	public Integer getAttackHeading()
	{
		return attackHeading;
	}

	@Override
	public void setAttackHeading(Integer attackHeading)
	{
		this.attackHeading = attackHeading;
	}

	/**
	 * @return следующее направление для атаки.
	 */
	protected Integer getNextAttackHeading()
	{
		return ATTACK_HEADINGS[Rnd.nextInt(0, ATTACK_HEADINGS.length - 1)];
	}

	@Override
	public void notifySpawn(){}

	@Override
	public void notifyStartDialog(Player player){}

	@Override
	public void notifyStopDialog(Player player){}

	@Override
	public void notifyArrived(){}

	@Override
	public void notifyAgression(Character attacker, long aggro){}

	@Override
	public void notifyAttack(Character attacked, Skill skill, int damage){}

	@Override
	public void notifyDead(Character killer){}

	@Override
	public void notifyAppliedEffect(Effect effect){}

	@Override
	public void notifyArrivedBlocked(){}

	@Override
	public void notifyArrivedTarget(TObject target){}

	@Override
	public void notifyCollectResourse(ResourseInstance resourse){}

	@Override
	public void notifyFinishCasting(Skill skill){}

	@Override
	public void notifyPickUpItem(ItemInstance item){}

	@Override
	public void notifyStartCasting(Skill skill){}

	@Override
	public boolean isActiveDialog()
	{
		return false;
	}
}
