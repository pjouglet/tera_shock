package tera.gameserver.model.skillengine.shots;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Базовая модель новодяхчихся выстрелов.
 *
 * @author Ronn
 */
public abstract class AbstractAutoShot implements Shot
{
	protected static final Logger log = Loggers.getLogger(Shot.class);

	/** кастер */
	protected Character caster;
	/** цель */
	protected Character target;
	/** скил */
	protected Skill skill;

	/** скорость */
	protected int speed;
	/** радиус */
	protected int radius;

	/** время вылета */
	protected long startTime;

	/** стартовая точка */
	protected float startX;
	protected float startY;
	protected float startZ;

	/** таск выстрела */
	protected ScheduledFuture<?> task;

	@Override
	public void finalyze()
	{
		caster = null;
		target = null;
		skill = null;
	}

	/**
	 * @return the caster
	 */
	protected final Character getCaster()
	{
		return caster;
	}

	@Override
	public int getObjectId()
	{
		return 0;
	}

	/**
	 * @return the radius
	 */
	protected final int getRadius()
	{
		return radius;
	}

	/**
	 * @return скил выстрела.
	 */
	public Skill getSkill()
	{
		return skill;
	}

	/**
	 * @return the speed
	 */
	protected final int getSpeed()
	{
		return speed;
	}

	/**
	 * @return the startTime
	 */
	protected final long getStartTime()
	{
		return startTime;
	}

	/**
	 * @return the startX
	 */
	protected final float getStartX()
	{
		return startX;
	}

	/**
	 * @return the startY
	 */
	protected final float getStartY()
	{
		return startY;
	}

	/**
	 * @return the startZ
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
		return target;
	}

	@Override
	public float getTargetX()
	{
		return target == null? 0 : target.getX();
	}

	@Override
	public float getTargetY()
	{
		return target == null? 0 : target.getY();
	}

	@Override
	public float getTargetZ()
	{
		return target == null? 0 : target.getZ();
	}

	/**
	 * @return the task
	 */
	protected final ScheduledFuture<?> getTask()
	{
		return task;
	}

	@Override
	public boolean isAuto()
	{
		return true;
	}

	@Override
	public void reinit(){}

	@Override
	public synchronized void run()
	{
		try
		{
			// текущая цель
			Character target = getTarget();
			// текущий скил
			Skill skill = getSkill();

			// если что-то пошло не так, прекращаем обработку
			if(target == null || skill == null)
			{
				log.warning(this, new Exception("not found target or skill"));
				stop();
				return;
			}

			// текущее время
			long now = System.currentTimeMillis();

			// пройденное расстояние
			float donedist = (now - getStartTime()) * getSpeed() / 1000F;

			float startX = getStartX();
			float startY = getStartY();
			float startZ = getStartZ();

			// расстояние от места выстрела до цели
			float alldist = target.getDistance(startX, startY, startZ);

			// получаем радиус
			float radius = getRadius();

			// если выстрел догнал цель
			if(target.getDistance(startX, startY, startZ) <= donedist + radius)
			{
				// и цель может быть подвержена удару
				if(!target.isDead() && !target.isInvul() && !target.isEvasioned() && caster.checkTarget(target))
					// ударяем
					skill.applySkill(caster, target);

				// завершаем работу выстрела
				stop();

				return;
			}

			float done = donedist / alldist;

			// если путь выстрел весь прошел и почему-то не ударил, всеравно завершаем
			if(done >= 1F)
				stop();
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	/**
	 * @param caster the caster to set
	 */
	protected final void setCaster(Character caster)
	{
		this.caster = caster;
	}

	/**
	 * @param radius the radius to set
	 */
	protected final void setRadius(int radius)
	{
		this.radius = radius;
	}

	/**
	 * @param skill the skill to set
	 */
	protected final void setSkill(Skill skill)
	{
		this.skill = skill;
	}

	/**
	 * @param speed the speed to set
	 */
	protected final void setSpeed(int speed)
	{
		this.speed = speed;
	}

	/**
	 * @param startTime the startTime to set
	 */
	protected final void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @param startX the startX to set
	 */
	protected final void setStartX(float startX)
	{
		this.startX = startX;
	}

	/**
	 * @param startY the startY to set
	 */
	protected final void setStartY(float startY)
	{
		this.startY = startY;
	}

	/**
	 * @param startZ the startZ to set
	 */
	protected final void setStartZ(float startZ)
	{
		this.startZ = startZ;
	}

	/**
	 * @param target the target to set
	 */
	protected final void setTarget(Character target)
	{
		this.target = target;
	}

	/**
	 * @param task the task to set
	 */
	protected final void setTask(ScheduledFuture<?> task)
	{
		this.task = task;
	}

	@Override
	public synchronized void start()
	{
		// получаем стреляющего
		Character caster = getCaster();

		// если его нет, выходим
		if(caster == null)
		{
			log.warning(this, new Exception("not found caster"));
			return;
		}

		// вносим стартовые координаты
		setStartX(caster.getX());
		setStartY(caster.getY());
		setStartZ(caster.getZ() + caster.getGeom().getHeight() - 5F);

		// получаем скил
		Skill skill = getSkill();

		// если его нет, выходим
		if(skill == null)
		{
			log.warning(this, new Exception("not found skill"));
			return;
		}

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		setSpeed(skill.getSpeed());
		setRadius(skill.getRadius());
		setStartTime( System.currentTimeMillis());
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
