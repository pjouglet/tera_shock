package tera.gameserver.model.skillengine.shots;

import java.util.concurrent.ScheduledFuture;

import rlib.geom.Geometry;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.Skill;

/**
 * Базовая модель выстрела.
 *
 * @author Ronn
 */
public abstract class AbstractShot implements Shot
{
	protected static final Logger log = Loggers.getLogger(Shot.class);

	/** возможные цели */
	protected final Array<Character> targets;

	/** тип выстрела */
	protected ShotType type;
	/** кастующий персонаж */
	protected Character caster;
	/** кастуемый скил */
	protected Skill skill;

	/** скорость */
	protected int speed;
	/** радиус */
	protected int radius;
	/** кол-во поражаемых целей */
	protected int count;

	/** начало полета */
	protected long startTime;

	/** стартовая точка */
	protected float startX;
	protected float startY;
	protected float startZ;

	/** целевые координаты */
	protected float targetX;
	protected float targetY;
	protected float targetZ;

	/** всего путь */
	protected float alldist;

	/** таск выстрела */
	protected volatile ScheduledFuture<?> task;

	public AbstractShot()
	{
		this.targets = Arrays.toArray(Character.class);
	}

	@Override
	public void finalyze()
	{
		targets.clear();

		setCaster(null);
		setSkill(null);
	}

	/**
	 * @return вся дистанция выстрела.
	 */
	protected final float getAlldist()
	{
		return alldist;
	}

	/**
	 * @return стреляющий персонаж.
	 */
	protected final Character getCaster()
	{
		return caster;
	}

	/**
	 * @return кол-во атакованных целей.
	 */
	protected final int getCount()
	{
		return count;
	}

	@Override
	public int getObjectId()
	{
		return 0;
	}

	/**
	 * @return радиус выстрела.
	 */
	protected final int getRadius()
	{
		return radius;
	}

	/**
	 * @return стреляющий скил.
	 */
	protected final Skill getSkill()
	{
		return skill;
	}

	/**
	 * @return скорость выстрела.
	 */
	protected final int getSpeed()
	{
		return speed;
	}

	/**
	 * @return стартовое время выстрела.
	 */
	protected final long getStartTime()
	{
		return startTime;
	}

	/**
	 * @return стартовая координата выстрела.
	 */
	protected final float getStartX()
	{
		return startX;
	}

	/**
	 * @return стартовая координата выстрела.
	 */
	protected final float getStartY()
	{
		return startY;
	}

	/**
	 * @return стартовая координата выстрела.
	 */
	protected final float getStartZ()
	{
		return startZ;
	}

	@Override
	public int getSubId()
	{
		return Config.SERVER_SHOT_SUB_ID;
	}

	@Override
	public Character getTarget()
	{
		return null;
	}

	/**
	 * @return список целей.
	 */
	protected final Array<Character> getTargets()
	{
		return targets;
	}

	@Override
	public float getTargetX()
	{
		return targetX;
	}

	@Override
	public float getTargetY()
	{
		return targetY;
	}

	@Override
	public float getTargetZ()
	{
		return targetZ;
	}

	/**
	 * @return ссылка на задачу полета.
	 */
	protected final ScheduledFuture<?> getTask()
	{
		return task;
	}

	@Override
	public ShotType getType()
	{
		return type;
	}

	@Override
	public boolean isAuto()
	{
		return false;
	}

	/**
	 * Реинициализация выстрела.
	 *
	 * @param caster стрелок.
	 * @param skill стреляющий скил.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	protected void prepare(Character caster, Skill skill, float targetX, float targetY, float targetZ)
	{
		// получаем стартовые координаты
		float startX = caster.getX();
		float startY = caster.getY();
		float startZ = caster.getZ() + caster.getGeomHeight() - 5F;

		// получаем дистанцию до цели
		float alldist = Geometry.getDistance(startX, startY, startZ, targetX, targetY, targetZ);

		// получаем максимальную дистанцию
		float range = skill.getRange();

		// получаем % от максимальной дистанции
		float diff = range / alldist;

		// если целевая координата не является предельной
		if(diff > 1F)
		{
			targetX = startX + ((targetX - startX) * diff);
			targetY = startY + ((targetY - startY) * diff);
			targetZ = startZ + ((targetZ - startZ) * diff);
		}

		// считаем всю дистанцию
		setAlldist(range);

		if(Config.DEVELOPER_MAIN_DEBUG)
			caster.sendMessage("Shot: dist " + getAlldist());

		// вносим стартовые координаты
		setStartX(startX);
		setStartY(startY);
		setStartZ(startZ);

		// вносим конечные координаты
		setTargetX(targetX);
		setTargetY(targetY);
		setTargetZ(targetZ);

		// получаем список целей
		Array<Character> targets = getTargets();

		// собираем потенциальные цели
		World.getAround(Character.class, targets, caster, skill.getRange() * 2);

		// если есть потенциальные цели
		if(!targets.isEmpty())
		{
			// получаем массив
			Character[] array = targets.array();

			// перебираем цели
			for(int i = 0, length = targets.size(); i < length; i++)
			{
				// получаем цель
				Character target = array[i];

				// если кастующий не может ударить его
				if(!caster.checkTarget(target))
				{
					// удаляем из списка
					targets.fastRemove(i--);
					length--;
					continue;
				}
			}
		}

		setCaster(caster);
		setSkill(skill);
		setSpeed(skill.getSpeed());
		setRadius(skill.getRadius());
	}

	@Override
	public void reinit()
	{
		count = 0;
	}

	@Override
	public synchronized void run()
	{
		// получаем текущее время
		long now = System.currentTimeMillis();

		// получаем пройденное расстояние
		float donedist = (now - getStartTime()) * getSpeed() / 1000F;

		// получаем список целей
		Array<Character> targets = getTargets();

		// получаем стартовые координаты
		float startX = getStartX();
		float startY = getStartY();
		float startZ = getStartZ();

		// получаем конечные координаты
		float targetX = getTargetX();
		float targetY = getTargetY();
		float targetZ = getTargetZ();

		if(!targets.isEmpty())
		{
			// получаем массив целей
			Character[] array = targets.array();

			// перебираем
			for(int i = 0, length = targets.size(); i < length; i++)
			{
				// получаем цель
				Character target = array[i];

				// если цели нет либо она дальше пройденного расстояния, пропускаем
				if(target == null || target.getDistance(startX, startY, startZ) > donedist + 100)
					continue;

				// удаляем цель из списка
				targets.fastRemove(i--);
				length--;

				// если цель в уклоне/неуязвимости либо мертва, попускаем
				if(target.isDead() || target.isInvul() || target.isEvasioned())
					continue;

				// определяем попадает ли выстрел в цель
				if(target.isHit(startX, startY, startZ, targetX, targetY, targetZ, radius))
				{
					// получаем скил
					Skill skill = getSkill();

					// если скила нет, останавливаем выстрел
					if(skill == null)
					{
						stop();
						return;
					}

					// применяем скил
					AttackInfo info = skill.applySkill(getCaster(), target);

					// увеличиваем счетчик
					count++;

					// если выстрел был заблокирован или он набрал лимит поражений, останавливаем
					if(info.isBlocked() || count >= skill.getMaxTargets())
					{
						stop();
						return;
					}
				}
			}
		}

		// если пройдено все расстояние, останавливаемся
		if(donedist >= getAlldist())
			stop();
	}

	/**
	 * @param alldist вся дистанция полета.
	 */
	protected final void setAlldist(float alldist)
	{
		this.alldist = alldist;
	}

	/**
	 * @param caster кастующицй персонаж.
	 */
	protected final void setCaster(Character caster)
	{
		this.caster = caster;
	}

	/**
	 * @param count кол-во атакованнвых целей.
	 */
	protected final void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * @param radius радиус выстрела.
	 */
	protected final void setRadius(int radius)
	{
		this.radius = radius;
	}

	/**
	 * @param skill стреляющий скил.
	 */
	protected final void setSkill(Skill skill)
	{
		this.skill = skill;
	}

	/**
	 * @param speed скорость выстрела.
	 */
	protected final void setSpeed(int speed)
	{
		this.speed = speed;
	}

	/**
	 * @param startTime время старта выстрела.
	 */
	protected final void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @param startX стартовая координата.
	 */
	protected final void setStartX(float startX)
	{
		this.startX = startX;
	}

	/**
	 * @param startY стартовая координата.
	 */
	protected final void setStartY(float startY)
	{
		this.startY = startY;
	}

	/**
	 * @param startZ стартовая координата.
	 */
	protected final void setStartZ(float startZ)
	{
		this.startZ = startZ;
	}

	/**
	 * @param targetX целевая координата.
	 */
	protected final void setTargetX(float targetX)
	{
		this.targetX = targetX;
	}

	/**
	 * @param targetY целевая координата.
	 */
	protected final void setTargetY(float targetY)
	{
		this.targetY = targetY;
	}

	/**
	 * @param targetZ целевая координата.
	 */
	protected final void setTargetZ(float targetZ)
	{
		this.targetZ = targetZ;
	}

	/**
	 * @param task ссылка на задачу полета.
	 */
	protected final void setTask(ScheduledFuture<?> task)
	{
		this.task = task;
	}

	/**
	 * @param typeтип выстрела.
	 */
	protected final void setType(ShotType type)
	{
		this.type = type;
	}

	@Override
	public synchronized void start()
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запоминаем время старта
		setStartTime(System.currentTimeMillis());

		// запускаем таск полета
		setTask(executor.scheduleMoveAtFixedRate(this, 0, 100));
	}

	@Override
	public synchronized void stop()
	{
		if(task != null)
		{
			task.cancel(false);
			task = null;
		}
	}
}
