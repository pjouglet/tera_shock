package tera.gameserver.model.npc;

import java.util.concurrent.ScheduledFuture;

import rlib.idfactory.IdGenerator;
import rlib.idfactory.IdGenerators;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MinionData;
import tera.gameserver.model.World;
import tera.gameserver.templates.NpcTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Модель лидера минионов.
 *
 * @author Ronn
 * @created 14.03.2012
 */
public class MinionLeader extends Monster
{
	private static final IdGenerator ID_FACTORY = IdGenerators.newSimpleIdGenerator(800001, 1000000);

	/** отспавненые минионы */
	private final Array<Minion> minions;

	/** таск респа минионов */
	private final SafeTask task;

	/** контейнер минионов */
	private final MinionData data;

	/** таск респавна минионов */
	private volatile ScheduledFuture<SafeTask> schedule;

	/**
	 * @param objectId уникальный ид.
	 * @param template темплейт нпс.
	 */
	public MinionLeader(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		this.minions = Arrays.toConcurrentArray(Minion.class);
		this.data = getTemplate().getMinions();
		this.task = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				respawnMinions();
			}
		};
	}

	@Override
	public void doDie(Character attacker)
	{
		synchronized(this)
		{
			// получаем минионы лидера
			Array<Minion> minions = getMinions();

			// если они есть
			if(!minions.isEmpty())
			{
				minions.readLock();
				try
				{
					// получаем массив минионов
					Minion[] array = minions.array();

					// перебираем
					for(int i = 0, length = minions.size(); i < length; i++)
						// зануляем лидера
						array[i].setLeader(null);
				}
				finally
				{
					minions.readUnlock();
				}

				// очищаем список минионов
				minions.clear();
			}

			// получаем ссылку на задачу
			ScheduledFuture<SafeTask> schedule = getSchedule();

			// если есть задача
			if(schedule != null)
			{
				// останавливаем ее
				schedule.cancel(false);
				// зануляем ссылку
				setSchedule(null);
			}
		}

		super.doDie(attacker);
	}

	/**
	 * @return инфа об минионах этого боса.
	 */
	protected final MinionData getData()
	{
		return data;
	}

	@Override
	public MinionLeader getMinionLeader()
	{
		return this;
	}

	/**
	 * @return список живых минионов.
	 */
	public final Array<Minion> getMinions()
	{
		return minions;
	}

	/**
	 * @return имеются ли живые минионы.
	 */
	public boolean hasMinions()
	{
		return minions.size() > 0;
	}

	@Override
	public boolean isMinionLeader()
	{
		return true;
	}

	@Override
	public int nextCastId()
	{
		return ID_FACTORY.getNextId();
	}

	/**
	 * Обработка убитого миниона.
	 *
	 * @param minion убитый минион.
	 */
	public void onDie(Minion minion)
	{
		// получаем список минионов
		Array<Minion> minions = getMinions();

		// получаем инфу о минионах
		MinionData data = getData();

		// если минионов нет, выходим
		if(data == null || minions.isEmpty())
			return;

		// удаляем миниона из списка
		minions.fastRemove(minion);

		// если минионы кончились
		if(minions.isEmpty())
		{
			// получаем ссылку на задачу респа
			ScheduledFuture<SafeTask> schedule = getSchedule();

			// если ссылки нету
			if(schedule == null)
			{
				synchronized(this)
				{
					// повторяем получение
					schedule = getSchedule();

					// если ее точно нет
					if(schedule == null)
					{
						// получаем исполнительного менеджера
						ExecutorManager executor = ExecutorManager.getInstance();

						// создаем задачу
						setSchedule(executor.scheduleGeneral(task, data.getRespawnDelay() * 1000));
					}
				}
			}
		}
	}

	/**
	 * Респавн убитых минионов.
	 */
	public final void respawnMinions()
	{
		// инфа об минионах
		MinionData data = getData();

		// если ее нету, выходим
		if(data == null)
			return;

		// получаем список минионов
		Array<Minion> minions = getMinions();

		// спавним минионов с добавлением в список
		data.spawnMinions(this, minions);

		// обновляем агро минионов
		updateMinionAggro();

		// зануляем таск на респ минионов
		setSchedule(null);
	}

	@Override
	public void spawnMe(Location loc)
	{
		// спавнимся
		super.spawnMe(loc);

		// получаем инфу об минионах
		MinionData data = getData();

		// если ее нет, выходим
		if(data == null)
			return;

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем окружающих минионов
		Array<Minion> around = World.getAround(local.getNextMinionList(), Minion.class, this);

		// получаем список минионов боса
		Array<Minion> minions = getMinions();

		// подхват живых ничейных минионов
		if(!around.isEmpty())
		{
			// получаем массив минионов
			Minion[] array = around.array();

			// перебираем минионы
			for(int i = 0, length = around.size(); i < length; i++)
			{
				// получаем миниона
				Minion minion = array[i];

				// если минион имеет лидера либо он не походит этому нпс, пропускаем
				if(minion.getMinionLeader() != null || !data.containsMinion(minion.getSpawn(), minions))
					continue;

				// запоминаем лидера
				minion.setLeader(this);
				// обновляем точку спавна миниона
				minion.getSpawnLoc().set(spawnLoc);
				// добавляем в список
				minions.add(minion);
			}
		}

		// если минионов еще нету, создаем новых
		if(minions.isEmpty())
			data.spawnMinions(this, minions);
		else
			// обновляем агро минионов
			updateMinionAggro();
	}

	/**
	 * Обновление агро поинтов у минионов.
	 */
	protected void updateMinionAggro()
	{
		// получаем агро лист
		Array<AggroInfo> aggroList = getAggroList();

		// если агро лист не пуст
		if(!aggroList.isEmpty())
		{
			// получаем список минионов
			Array<Minion> minions = getMinions();

			aggroList.readLock();
			try
			{
				// получаем массив агрессоров
				AggroInfo[] aggro = aggroList.array();

				minions.readLock();
				try
				{
					// получаем массив минионов
					Minion[] array = minions.array();

					// перебираем агресоров
					for(int g = 0, size = aggroList.size(); g < size; g++)
					{
						// получаем инфу об агрессоре
						AggroInfo info = aggro[g];

						for(int i = 0, length = minions.size(); i < length; i++)
							array[i].addAggro(info.getAggressor(), info.getAggro(), false);
					}
				}
				finally
				{
					minions.readUnlock();
				}
			}
			finally
			{
				aggroList.readUnlock();
			}
		}
	}

	/**
	 * @return ссылка на задачу респавна минионов.
	 */
	public ScheduledFuture<SafeTask> getSchedule()
	{
		return schedule;
	}

	/**
	 * @param schedule ссылка на задачу респавна минионов.
	 */
	public void setSchedule(ScheduledFuture<SafeTask> schedule)
	{
		this.schedule = schedule;
	}
}
