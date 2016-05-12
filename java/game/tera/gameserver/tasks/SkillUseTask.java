package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель обработки приминения скила.
 *
 * @author Ronn
 */
public final class SkillUseTask extends SafeTask
{
	/** блокировщик */
	private final Lock lock;
	/** кастующий скилл */
	private final Character caster;

	/** кастующийся скилл */
	private Skill skill;

	/** целевая координата */
	private float targetX;
	/** целевая координата */
	private float targetY;
	/** целевая координата */
	private float targetZ;

	/** кол-во приминений */
	private int count;

	/** ссылка на таск */
	private volatile ScheduledFuture<SkillUseTask> schedule;

	/**
	 * @param caster кастующий персонаж.
	 */
	public SkillUseTask(Character caster)
	{
		this.lock = Locks.newLock();
		this.caster = caster;
	}

	/**
	 * Отмена приминения скила.
	 *
	 * @param force принудительная ли отмена.
	 */
	public void cancel(boolean force)
	{
		if(schedule != null)
		{
			lock.lock();
			try
			{
				if(schedule != null)
				{
					schedule.cancel(false);
					schedule = null;
				}
			}
			finally
			{
				lock.unlock();
			}
		}
	}

	/**
	 * @return обрабатываемый скил.
	 */
	public Skill getSkill()
	{
		return skill;
	}

	/**
	 * @param skill применяемый скил.
	 */
	public void nextUse(Skill skill)
	{
		cancel(true);

		Formulas formulas = Formulas.getInstance();

		lock.lock();
		try
		{
			this.skill = skill;
			this.count = skill.getCastCount();

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			this.schedule = executor.scheduleSkillCast(this, skill.isStaticCast()? skill.getDelay() : formulas.castTime(skill.getDelay(), caster));
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @param skill применяемый скил.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void nextUse(Skill skill, float targetX, float targetY, float targetZ)
	{
		cancel(true);

		Formulas formulas = Formulas.getInstance();

		lock.lock();
		try
		{
			this.skill = skill;
			this.count = skill.getCastCount();
			this.targetX = targetX;
			this.targetY = targetY;
			this.targetZ = targetZ;

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			this.schedule = executor.scheduleSkillUse(this, skill.isStaticCast()? skill.getDelay() : formulas.castTime(skill.getDelay(), caster));
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	protected void runImpl()
	{
		count -= 1;

		Skill skill = getSkill();

		if(skill == null)
			return;

		// добавляем смещение направления каста
		caster.setHeading(caster.getHeading() + skill.getCastHeading());

		// активируем приминение скила
		skill.useSkill(caster, targetX, targetY, targetZ);

		if(count > 0)
		{
			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			Formulas formulas = Formulas.getInstance();

			schedule = executor.scheduleSkillCast(this, skill.isStaticInterval()? skill.getInterval() : formulas.castTime(skill.getInterval(), caster));
		}
	}

	/**
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void setTarget(float targetX, float targetY, float targetZ)
	{
		cancel(true);

		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
	}
}
