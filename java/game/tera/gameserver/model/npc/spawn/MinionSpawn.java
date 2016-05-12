package tera.gameserver.model.npc.spawn;

import rlib.geom.Coords;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Minion;
import tera.gameserver.model.npc.MinionLeader;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Модель информации о минионах.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public final class MinionSpawn implements Spawn
{
	private static final Logger log = Loggers.getLogger(MinionSpawn.class);

	/** пул минионов */
	private final FoldablePool<Minion> pool;
	/** шаблон нпс */
	private final NpcTemplate template;

	/** параметры для АИ */
	private ConfigAI config;

	/** конструктор для АИ */
	private NpcAIClass aiClass;

	/** кол-во отспавниваемых минионов. */
	private int count;
	/** радиус спавна от точки */
	private int radius;

	/**
	 * @param template темплейт нпс.
	 */
	public MinionSpawn(NpcTemplate template)
	{
		this.template = template;
		this.pool = Pools.newConcurrentFoldablePool(Minion.class);
	}

	@Override
	public void doDie(Npc npc)
	{
		pool.put((Minion) npc);
	}

	/**
	 * @return кол-во отспавниваемых минионов.
	 */
	public final int getCount()
	{
		return count;
	}

	@Override
	public Location getLocation()
	{
		throw new IllegalArgumentException("unsupported method.");
	}

	/**
	 * @return радиус спавна.
	 */
	public int getRadius()
	{
		return radius;
	}

	/**
	 * @return темплейт нпс.
	 */
	public NpcTemplate getTemplate()
	{
		return template;
	}

	@Override
	public int getTemplateId()
	{
		return template.getTemplateId();
	}

	@Override
	public int getTemplateType()
	{
		return template.getTemplateType();
	}

	/**
	 * @param count кол-во отспавниваемых минионов.
	 */
	public final void setCount(int count)
	{
		this.count = count;
	}

	@Override
	public void setLocation(Location location)
	{
		throw new IllegalArgumentException("unsupported method.");
	}

	/**
	 * @param radius радиус спавна минионов от лидера.
	 */
	public final void setRadius(int radius)
	{
		this.radius = radius;
	}

	@Override
	public void start()
	{
		log.warning(new Exception("unsupported method"));
	}

	/**
	 * Спавн минионов.
	 *
	 * @param leader лидер минионов.
	 * @param array список минионов.
	 */
	public void start(MinionLeader leader, Array<Minion> array)
	{
		// получаем координаты лидера
		float x = leader.getX();
		float y = leader.getY();
		float z = leader.getZ();

		// получаем ид континента
		int continentId = leader.getContinentId();

		// получаем пул инстансов
		FoldablePool<Minion> pool = getPool();

		// получаем шаблон миниона
		NpcTemplate template = getTemplate();

		// спавним нужное кол-во минионов
		for(int i = 0, length = getCount(); i < length; i++)
		{
			// извлекаем миниона из пула
			Minion newNpc = pool.take();

			// если его нету
			if(newNpc == null)
			{
				// создаем нового
				newNpc = (Minion) template.newInstance();
				// запоминаем спавн
				newNpc.setSpawn(this);

				// ссылка на точку спавна
				Location spawnLoc = null;

				// если радиус есть
				if(radius > 0)
					// рассчитываем рандомную точку
					spawnLoc = Coords.randomCoords(new Location(), x, y, z, 0, radius);
				else
					// иначе делаем статичную
					spawnLoc = new Location(x, y, z, Rnd.nextInt(32000));

				// вносим ид континента
				spawnLoc.setContinentId(continentId);

				// создаем и вносим АИ
				newNpc.setAi(aiClass.newInstance(newNpc, getConfig()));

				// спавним
				newNpc.spawnMe(spawnLoc, leader);

				// добавляем в список
				array.add((Minion) newNpc);
			}
			else
			{
				// получаем прошлую точку спавна
				Location spawnLoc = newNpc.getSpawnLoc();

				// если радиус есть
				if(radius > 0)
					// рассчитываем новую
					spawnLoc = Coords.randomCoords(spawnLoc, x, y, z, 0, radius);
				else
					// иначе спавним в статичной
					spawnLoc.setXYZH(x, y, z, Rnd.nextInt(32000));

				// вносим ид континента
				spawnLoc.setContinentId(continentId);

				// спавним
				newNpc.spawnMe(spawnLoc, leader);

				// добавляем в список
				array.add(newNpc);
			}
		}
	}

	@Override
	public void stop()
	{
		log.warning(new Exception("unsupported method"));
	}

	/**
	 * @return пул минионов.
	 */
	public FoldablePool<Minion> getPool()
	{
		return pool;
	}

	/**
	 * @param aiClass класс АИ.
	 */
	public void setAiClass(NpcAIClass aiClass)
	{
		this.aiClass = aiClass;
	}

	/**
	 * @param config конфиг АИ.
	 */
	public void setConfig(ConfigAI config)
	{
		this.config = config;
	}

	/**
	 * @return класс АИ.
	 */
	public NpcAIClass getAiClass()
	{
		return aiClass;
	}

	/**
	 * @return конфиг АИ.
	 */
	public ConfigAI getConfig()
	{
		return config;
	}

	@Override
	public Location[] getRoute()
	{
		return null;
	}
}
