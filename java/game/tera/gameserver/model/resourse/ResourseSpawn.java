package tera.gameserver.model.resourse;

import java.util.concurrent.ScheduledFuture;

import rlib.geom.Coords;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Rnd;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.templates.ResourseTemplate;
import tera.util.Location;

/**
 * Модель спавна ресурсов.
 *
 * @author Ronn
 */
public final class ResourseSpawn extends SafeTask
{
	protected static final Logger log = Loggers.getLogger(ResourseSpawn.class);

	/** темплейт ресурса */
	private ResourseTemplate template;

	/** точка спавна */
	private Location loc;

	/** отспавненный ресурс */
	private ResourseInstance spawned;
	/** ожидающий спавна ресурс */
	private ResourseInstance waited;

	/** ссылка на таск респавна */
	private ScheduledFuture<ResourseSpawn> schedule;

	/** время респа */
	private int respawn;
	/** рандоминайзер времени респа */
	private int randomRespawn;

	/** радиус спавна от точки */
	private int minRadius;
	private int maxRadius;

	/** остановка спавна */
	private boolean stoped;

	public ResourseSpawn(ResourseTemplate template, Location loc, int respawn, int randomRespawn, int minRadius, int maxRadius)
	{
		this.template = template;
		this.loc = loc;
		this.respawn = respawn;
		this.randomRespawn = randomRespawn;
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		this.stoped = true;
	}

	/**
	 * Запустить респавн.
	 */
	protected synchronized void doRespawn()
	{
		if(isStoped())
			return;

		// если уже таск запущен, выходим
		if(schedule != null)
		{
			log.warning(this, new Exception("found duplicate respawn"));
			return;
		}

		// получаем время респа
		int delay = respawn;

		// если есть рандомное
		if(randomRespawn > 0)
			// расчитываем прибавку
			delay += Rnd.nextInt(0, randomRespawn);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем таск
		schedule = executor.scheduleGeneral(this, delay * 1000);
	}

	/**
	 * Спавн ресурса.
	 */
	protected synchronized void doSpawn()
	{
		if(isStoped())
			return;

		// зануляем
		schedule = null;

		// получаем ожидающий ресурс
		ResourseInstance resourse = getWaited();

		// если его нет
		if(resourse == null)
			// создаем новый
			resourse = template.newInstance();

		// запоминаем
		setSpawned(resourse);

		// забываем
		setWaited(null);

		// запоминаем спавн
		resourse.setSpawn(this);

		// если точка спавна рандоминизирована
		if(maxRadius > 0)
			// спавним в рандомной точке
			resourse.spawnMe(Coords.randomCoords(new Location(), loc.getX(), loc.getY(), loc.getZ(), minRadius, maxRadius));
		else
			// иначе спавним в статичной
			resourse.spawnMe(loc);
	}

	/**
	 * @return точка спавна.
	 */
	public final Location getLoc()
	{
		return loc;
	}

	/**
	 * @return максимальный радиус спавна от точки.
	 */
	protected final int getMaxRadius()
	{
		return maxRadius;
	}

	/**
	 * @return минимальный радиус спавна от точки.
	 */
	protected final int getMinRadius()
	{
		return minRadius;
	}

	/**
	 * @return рандоминизированное время респа.
	 */
	protected final int getRandomRespawn()
	{
		return randomRespawn;
	}

	/**
	 * @return базовое время респа.
	 */
	protected final int getRespawn()
	{
		return respawn;
	}

	/**
	 * @return отспавненный инстанс.
	 */
	protected final ResourseInstance getSpawned()
	{
		return spawned;
	}

	/**
	 * @return темплейт ресурса.
	 */
	public final ResourseTemplate getTemplate()
	{
		return template;
	}

	/**
	 * @return ид темплейта спавненого ресурса.
	 */
	public int getTemplateId()
	{
		return template.getId();
	}

	/**
	 * @return ожидающий спавна инстанс.
	 */
	protected final ResourseInstance getWaited()
	{
		return waited;
	}

	/**
	 * @return флаг остановки спавна.
	 */
	protected final boolean isStoped()
	{
		return stoped;
	}

	/**
	 * Обработка собранного ресурса.
	 *
	 * @param resourse собранный ресурс.
	 */
	public synchronized void onCollected(ResourseInstance resourse)
	{
		if(isStoped())
			return;

		// зануляемзаспавненный
		setSpawned(null);

		// заносим в ожидающий спавна
		setWaited(waited);

		// запускаем респавн
		doRespawn();
	}

	@Override
	protected void runImpl()
	{
		doSpawn();
	}

	/**
	 * @param loc точка спавна.
	 */
	protected final void setLoc(Location loc)
	{
		this.loc = loc;
	}

	/**
	 * @param maxRadius максимальный радиус спавна от точки.
	 */
	protected final void setMaxRadius(int maxRadius)
	{
		this.maxRadius = maxRadius;
	}

	/**
	 * @param minRadius минимальный радиус спавна от точки.
	 */
	protected final void setMinRadius(int minRadius)
	{
		this.minRadius = minRadius;
	}

	/**
	 * @param randomRespawn рандоминизированное время респа.
	 */
	protected final void setRandomRespawn(int randomRespawn)
	{
		this.randomRespawn = randomRespawn;
	}

	/**
	 * @param respawn базовое время респа.
	 */
	protected final void setRespawn(int respawn)
	{
		this.respawn = respawn;
	}

	/**
	 * @param spawned отспавненный инстанс.
	 */
	protected final void setSpawned(ResourseInstance spawned)
	{
		this.spawned = spawned;
	}

	/**
	 * @param stoped флаг остановки спавна.
	 */
	protected final void setStoped(boolean stoped)
	{
		this.stoped = stoped;
	}

	/**
	 * @param template темплейт ресурса.
	 */
	protected final void setTemplate(ResourseTemplate template)
	{
		this.template = template;
	}

	/**
	 * @param waited ожидающий спавна инстанс.
	 */
	protected final void setWaited(ResourseInstance waited)
	{
		this.waited = waited;
	}

	/**
	 * Запуск спавна.
	 */
	public synchronized void start()
	{
		// если уже активирован, выходим
		if(!isStoped())
			return;

		// ставим флаг активности
		setStoped(false);

		// запускаем спавн
		doSpawn();
	}

	/**
	 * Остановка спавна.
	 */
	public synchronized void stop()
	{
		// если уже остановлено, выходим
		if(isStoped())
			return;

		// ставим флаг остановлиности
		setStoped(true);

		// получаем отспавненный ресурс
		ResourseInstance resourse = getSpawned();

		// если отспавненный есть
		if(resourse != null)
		{
			// удаляем из мира
			resourse.deleteMe();
			// запоминаем в ожидающие
			setWaited(resourse);
		}
	}
}
