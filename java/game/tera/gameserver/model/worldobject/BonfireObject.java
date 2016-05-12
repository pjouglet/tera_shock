package tera.gameserver.model.worldobject;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Bonfire;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.StatFunc;
import tera.util.LocalObjects;

/**
 * Модель переносного костра.
 *
 * @author Ronn
 */
public class BonfireObject extends WorldObject implements Foldable, Bonfire
{
	/** пул костров */
	private static final FoldablePool<BonfireObject> bonfirePool = Pools.newConcurrentFoldablePool(BonfireObject.class);

	/**
	 * Запуск нового костра.
	 *
	 * @param regenPower сила регена хп.
	 * @param lifeTime время жизни.
	 * @param x координата старта.
	 * @param y координата старта.
	 * @param z координата старта.
	 */
	public static void startBonfire(float regenPower, int lifeTime, int continentId, float x, float y, float z)
	{
		BonfireObject bonfire = bonfirePool.take();

		if(bonfire != null)
			bonfire.reinit(regenPower, lifeTime);
		else
		{
			// получаем фабрику ИД
			IdFactory idFactory = IdFactory.getInstance();

			bonfire = new BonfireObject(idFactory.getNextObjectId(), regenPower, lifeTime);
		}

		// устанавливаем ид континента
		bonfire.setContinentId(continentId);

		// спавним в нуждной точке
		bonfire.spawnMe(x, y, z, 0);
	}

	/** функция регена костра */
	private final StatFunc func;

	/** таск жизни окстра */
	private Runnable lifeTask;
	/** таск регена костра */
	private Runnable regenTask;

	/** ссылка на таск жизни окстра */
	private ScheduledFuture<Runnable> lifeSchedule;
	/** ссылка на таск регена костра */
	private ScheduledFuture<Runnable> regenSchedule;

	/** список игроков в зоне действия костра */
	private final Array<Player> players;

	/** мощность регена костра */
	private float regenPower;

	/** время жизни костра */
	private int lifeTime;

	/**
	 * @param objectId уникальный ид костра.
	 * @param regenPower мощность регена костра.
	 * @param lifeTime время жизни костра.
	 */
	public BonfireObject(int objectId, float regenPower, int lifeTime)
	{
		super(objectId);

		this.regenPower = regenPower;
		this.lifeTime = lifeTime;
		this.players = Arrays.toArray(Player.class);

		// описываем бонус крегену хп у игроков
		this.func = new StatFunc()
		{

			@Override
			public void addFuncTo(Character owner)
			{
				owner.addStatFunc(this);
			}

			@Override
			public float calc(Character attacker, Character attacked, Skill skill, float val)
			{
				return val *= getRegenPower();
			}

			@Override
			public int compareTo(StatFunc func)
			{
				return 0x30 - func.getOrder();
			}

			@Override
			public int getOrder()
			{
				return 0x30;
			}

			@Override
			public StatType getStat()
			{
				return StatType.REGEN_HP;
			}

			@Override
			public void removeFuncTo(Character owner)
			{
				owner.removeStatFunc(this);
			}
		};

		// описываем таск жизни костра
		this.lifeTask = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				if(isDeleted())
					return;

				deleteMe();
			}
		};

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем таск жизни костра
		this.lifeSchedule = executor.scheduleGeneral(lifeTask, lifeTime);

		// описываем таск регена игроков
		this.regenTask = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();
				// получаем текущий набор игроков
				Array<Player> players = getPlayers();
				// получаем окружающих игроков
				Array<Player> around = World.getAround(Player.class, local.getNextPlayerList(), BonfireObject.this, 200F);

				// получаем функцию бонуса регена хп
				StatFunc func = getFunc();

				// получаем костер
				BonfireObject bonfire = getBonfire();

				// если окружющие есть
				if(!around.isEmpty())
				{
					// получаем их массив
					Player[] array = around.array();

					// перебираем
					for(int i = 0, length = around.size(); i < length; i++)
					{
						// получаем игрока
						Player player = array[i];

						// если его нет в уже обрабатываемых и он не обрабатывается каким-то костром
						if(!players.contains(player) && player.addBonfire(bonfire))
						{
							// добавляем бонус регена хп
							func.addFuncTo(player);
							// добавляем в обрабатывааемые
							players.add(player);

						}
					}
				}

				// если есть обрабатываемые игроки
				if(!players.isEmpty())
				{
					// получаем их массив
					Player[] array = players.array();

					// перебираем
					for(int i = 0, length = players.size(); i < length; i++)
					{
						// получаем игрока
						Player player = array[i];

						// если его нет, пропускаем
						if(player == null)
							continue;

						// если игрок вышел из зоны костра
						if(!player.isInRangeZ(bonfire, 200) || player.isDead())
						{
							// удаляемся из обрабатывающихся
							player.removeBonfire(bonfire);
							// удаляем у него бонус к хп
							func.removeFuncTo(player);
							// удаляем его из списка
							players.fastRemove(i--);
							// уменьшаем длинну массива
							length--;
							// идем дальше
							continue;
						}

						// увеличиваем значение стамины
						player.addStamina();
					}
				}
			}
		};

		this.regenSchedule = executor.scheduleGeneralAtFixedRate(regenTask, 3000, 3000);
	}

	@Override
	public void deleteMe()
	{
		try
		{
			// удаляем из мира
			super.deleteMe();

			// отрубаем таск жизни
			if(lifeSchedule != null)
			{
				lifeSchedule.cancel(false);
				lifeSchedule = null;
			}

			// отрубаем таск регена
			if(regenSchedule != null)
			{
				regenSchedule.cancel(false);
				regenSchedule = null;
			}

			bonfirePool.put(this);
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void finalyze()
	{
		// получаем список обрабатываемых игроков
		Array<Player> players = getPlayers();

		// если он не пуст
		if(!players.isEmpty())
		{
			// получаем масив
			Player[] array = players.array();

			// перебираем
			for(int i = 0, length = players.size(); i < length; i++)
			{
				// получаем игрока
				Player player = array[i];

				// если он есть
				if(player != null)
				{
					// удаляем у него бонус
					func.removeFuncTo(player);
					// удаляемся у него
					player.removeBonfire(this);
				}
			}
		}

		// очищаем список
		players.clear();
	}

	/**
	 * @return сам себя.
	 */
	protected BonfireObject getBonfire()
	{
		return this;
	}

	/**
	 * @return функция бонуса регена хп.
	 */
	protected final StatFunc getFunc()
	{
		return func;
	}

	/**
	 * @return кол-во оставшихся секунд.
	 */
	public long getLifeTime()
	{
		// если шедул есть, возвращаем его время
		if(lifeSchedule != null)
			return lifeSchedule.getDelay(TimeUnit.SECONDS);

		return 0;
	}

	/**
	 * @return игроки на обработке костра.
	 */
	protected final Array<Player> getPlayers()
	{
		return players;
	}

	/**
	 * @return сила регена.
	 */
	protected final float getRegenPower()
	{
		return regenPower;
	}

	@Override
	public void reinit(){}

	/**
	 * Реинициализация костра.
	 *
	 * @param regenPower сила регена хп.
	 * @param lifeTime время жизни.
	 */
	public synchronized void reinit(float regenPower, int lifeTime)
	{
		// запоминаем новые характеристики
		this.regenPower = regenPower;
		this.lifeTime = lifeTime;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// пересоздаем таски
		this.lifeSchedule = executor.scheduleGeneral(lifeTask, lifeTime);
		this.regenSchedule = executor.scheduleGeneralAtFixedRate(regenTask, 3000, 3000);
	}

	/**
	 * Перезапуск костра.
	 */
	public synchronized void restart()
	{
		if(lifeSchedule == null)
			return;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		lifeSchedule.cancel(false);
		lifeSchedule = executor.scheduleGeneral(lifeTask, this.lifeTime);
	}

	@Override
	public String toString()
	{
		return "BonfireObject [" + getLifeTime() + "]";
	}
}
