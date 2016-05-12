package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель обработки каст скила.
 *
 * @author Ronn
 */
public final class SkillCastTask extends SafeTask
{
	private static final Logger log = Loggers.getLogger(SkillCastTask.class);

	/** кастующийся скилл */
	private Skill skill;

	/** кастер скила */
	private Character caster;

	/** координата точки атаки скила */
	private float targetX;
	/** координата точки атаки скила */
	private float targetY;
	/** координата точки атаки скила */
	private float targetZ;

	/** ссылка на таск */
	private volatile ScheduledFuture<SkillCastTask> schedule;

	/**
	 * @param caster кастующий персонаж.
	 */
	public SkillCastTask(Character caster)
	{
		this.caster = caster;
	}

	/**
	 * Отмена каста скила.
	 */
	public synchronized void cancel(boolean force)
	{
		// получаем кастуемый скил
		Skill skill = getSkill();;

		synchronized(this)
		{
			// если работает таск
			if(schedule != null)
			{
				// отстанавливаем
				schedule.cancel(false);
				// зануляем
				schedule = null;
			}

			// если есть скил
			if(skill != null)
			{
				// зануляем скил
				setSkill(null);

				// занулям кастующийся скил
				caster.setCastingSkill(null);
			}
		}

		if(skill != null)
		{
			// выполняем завершение каста
			skill.endSkill(caster, targetX, targetY, targetZ, force);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляй о завершении каста скила
			eventManager.notifyFinishCasting(caster, skill);
		}
	}

	/**
	 * @return кастуемый скил.
	 */
	private final Skill getSkill()
	{
		return skill;
	}

	/**
	 * @return запущен ли.
	 */
	public boolean isRunning()
	{
		return schedule != null;
	}

	/**
	 * @param skill кастуемый скилл.
	 * @param targetX координата точки атаки скила.
	 * @param targetY координата точки атаки скила.
	 * @param targetZ координата точки атаки скила.
	 */
	public void nextTask(Skill skill, float targetX, float targetY, float targetZ)
	{
		// отменяем предыдущий таск
		cancel(true);

		synchronized(this)
		{
			this.caster.setCastingSkill(skill);
			this.skill = skill;
			this.targetX = targetX;
			this.targetY = targetY;
			this.targetZ = targetZ;

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			Formulas formulas = Formulas.getInstance();

			// запускаем новый
			this.schedule = executor.scheduleSkillCast(this, skill.isStaticCast()? skill.getHitTime() : formulas.castTime(skill.getHitTime(), caster));
		}
	}

	@Override
	protected void runImpl()
	{
		// получаем кастуемый скил
		Skill skill = getSkill();

		// если скила нет, выходим
		if(skill == null)
		{
			log.warning(this, new Exception("not found skill"));
			return;
		}

		synchronized(this)
		{
			// зануляем
			schedule = null;

			// зануляем его
			setSkill(null);

			// зануляем кастуемый скил
			caster.setCastingSkill(null);
		}

		// запусаем завершение скила
		skill.endSkill(caster, targetX, targetY, targetZ, false);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о том, что завершили каст скила
		eventManager.notifyFinishCasting(caster, skill);
	}

	/**
	 * @param skill кастуемый скил.
	 */
	private final void setSkill(Skill skill)
	{
		this.skill = skill;
	}
}
