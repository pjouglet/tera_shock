package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.MoveSkill;
import tera.gameserver.network.serverpackets.SkillEnd;

/**
 * Модель обрбаотки движения скила.
 *
 * @author Ronn
 * @created 12.03.2012
 */
public final class SkillMoveTask extends SafeTask
{
	/** блокировщик */
	private final Lock lock;
	/** кастер */
	private final Character caster;

	/** список возможных помех */
	private final Array<Character> barriers;

	/** скил, который перемещает */
	private Skill skill;

	/** кол-во смещений */
	private int counter;

	/** полная дистанция */
	private float alldist;
	/** выполненый */
	private float done;
	/** шаг */
	private float step;

	/** точка старта */
	private float startX;
	/** точка старта */
	private float startY;
	/** точка старта */
	private float startZ;
	/** точка конца */
	private float targetX;
	/** точка конца */
	private float targetY;
	/** точка конца */
	private float targetZ;
	/** направление */
	private float radians;

	/** игнорирует ли препядствия скил */
	private boolean ignore;

	/** ссылка на таск */
	private volatile ScheduledFuture<SkillMoveTask> schedule;

	/**
	 * @param caster кастующий скил персонаж.
	 */
	public SkillMoveTask(Character caster)
	{
		this.caster = caster;
		this.lock = Locks.newLock();
		this.barriers = Arrays.toArray(Character.class);
	}

	/**
	 * Применить скил.
	 */
	public void applySkill()
	{
		lock.lock();
		try
		{
			// получаем текущий активный скил
			Skill skill = getSkill();

			// если его нет, выходим
			if(skill == null)
				return;

			// активируем его
			caster.nextUse(skill);

			// зануляем скил
			setSkill(null);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Прерывание таска.
	 */
	public void cancel(boolean force)
	{
		lock.lock();
		try
		{
			// зануляем текущий активный скил
			setSkill(null);

			// отменяем таск обработки
			if(schedule != null)
			{
				schedule.cancel(false);
				schedule = null;
			}

			// убераем флаг перемещения при касте скила
			caster.setSkillMoved(false);

			// если есть препядствия
			if(!barriers.isEmpty())
				// очищаем от них
				barriers.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Завершение таска.
	 */
	public void done()
	{
		lock.lock();
		try
		{
			// получаем текущий активный скил
			Skill skill = getSkill();

			// если он есть и активируется при сталкновении
			if(skill != null && skill.isCastToMove())
				// применяем
				applySkill();

			// убераем флаг перемещения скилом
			caster.setSkillMoved(false);

			// отменяем таск
			if(schedule != null)
			{
				schedule.cancel(false);
				schedule = null;
			}

			// если есть препядствия
			if(!barriers.isEmpty())
				// очищаем от них
				barriers.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return alldist
	 */
	public final float getAlldist()
	{
		return alldist;
	}

	/**
	 * @return done
	 */
	public final float getDone()
	{
		return done;
	}

	/**
	 * @return radians
	 */
	public final float getRadians()
	{
		return radians;
	}

	/**
	 * @return скил таска.
	 */
	public Skill getSkill()
	{
		return skill;
	}

	/**
	 * @return startX
	 */
	public final float getStartX()
	{
		return startX;
	}

	/**
	 * @return startY
	 */
	public final float getStartY()
	{
		return startY;
	}

	/**
	 * @return startZ
	 */
	public final float getStartZ()
	{
		return startZ;
	}

	/**
	 * @return targetX
	 */
	public final float getTargetX()
	{
		return targetX;
	}

	/**
	 * @return targetY
	 */
	public final float getTargetY()
	{
		return targetY;
	}

	/**
	 * @return targetZ
	 */
	public final float getTargetZ()
	{
		return targetZ;
	}

	/**
	 * Запустить обработку скила.
	 *
	 * @param skill скил, который нужно обработать.
	 */
	public void nextTask(Skill skill, float targetX, float targetY, float targetZ)
	{
		// отменяем предыдущий скил
		cancel(false);

		Formulas formulas = Formulas.getInstance();

		// определяем время перемещения
		int time = skill.isStaticCast()? skill.getMoveTime() : formulas.castTime(skill.getMoveTime(), caster);

		// определяем дистанцию
		int moveDistance = skill.getMoveDistance();

		// если дистанции нет, отменяем
		if(moveDistance == 0 && !skill.isRush())
			return;

		lock.lock();
		try
		{
			// обновляем скил
			setSkill(skill);

			// зануляем пройденное расстояние
			setDone(0);

			// запоминаем точку старта
			setStartX(caster.getX());
			setStartY(caster.getY());
			setStartZ(caster.getZ());

			// получаем менеджера геодаты
			GeoManager geoManager = GeoManager.getInstance();

			// если скил раш
			if(skill.isRush())
			{
				// рассчитываем дистанцию
				setAlldist(caster.getDistance(targetX, targetY, targetZ));

				// если она больше допустимой
				if(getAlldist() > moveDistance)
				{
					// рассчитываем направление
					setRadians(Angles.headingToRadians(caster.getHeading() + skill.getMoveHeading()));
					// меняем дистанцию
					setAlldist(moveDistance);

					// перерассчитываем целевую точку
					setTargetX(Coords.calcX(startX, moveDistance, radians));
					setTargetY(Coords.calcY(startY, moveDistance, radians));
					setTargetZ(geoManager.getHeight(caster.getContinentId(), getTargetX(), getTargetY(), getStartZ()));
				}
				// иначе
				else
				{
					// применяем целевую точку
					setTargetX(targetX);
					setTargetY(targetY);
					setTargetZ(targetZ);
					// рассчитываем направление
					setRadians(Angles.headingToRadians(caster.calcHeading(targetX, targetY) + skill.getMoveHeading()));
				}
			}
			// иначе
			else
			{
				// рассчитываем направление
				setRadians(Angles.headingToRadians(caster.getHeading() + skill.getMoveHeading()));
				// применяем дистанцию
				setAlldist(moveDistance);

				// рассчитываем целевюу точку
				setTargetX(Coords.calcX(getStartX(), moveDistance, getRadians()));
				setTargetY(Coords.calcY(getStartY(), moveDistance, getRadians()));
				setTargetZ(geoManager.getHeight(caster.getContinentId(), getTargetX(), getTargetY(), getStartZ()));
			}

			// определяем, игнорирует ли барьеры
			ignore = skill.isIgnoreBarrier();
			// определяем кол-во перемещений
			counter = Math.round(Math.max(1, (int) Math.sqrt(Math.abs(alldist))));
			// определяем размер шага
			step = alldist / counter;

			// ставим флаг движения во время скила
			caster.setSkillMoved(true);

			// если идет учет препядствий, получаем их список
			if(!ignore || (skill != null && skill.isCastToMove()))
				World.getAroundBarriers(barriers, caster, skill.getMoveDistance() * 3);

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// запускаем обработку
			schedule = executor.scheduleSkillMoveAtFixedRate(this, skill.isStaticCast()? skill.getMoveDelay() : formulas.castTime(skill.getMoveDelay(), caster), time / counter);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return кастующий персонаж.
	 */
	public Character getCaster()
	{
		return caster;
	}

	@Override
	protected void runImpl()
	{
		// если закончили перемещаться, выходим
		if(counter < 1)
		{
			done();
			return;
		}

		// получаем кастера
		Character caster = getCaster();

		// уменьшаем счетчик
		counter -= 1;

		// получаем текущий обрабатываемый скил
    	Skill skill = getSkill();

		// если перемещение не игнорирует барьеры, проверяем на помехи
		if((!ignore || (skill != null && skill.isCastToMove())) && !caster.checkBarriers(barriers, (int) step + 1, radians))
		{
			// если есть скил
			if(skill != null)
			{
				// если скил срабатывает при колизии
				if(skill.isCastToMove())
					// активируем
					applySkill();

				// если скил раш
				if(skill.isRush())
					// отправляем пакет об столкновении
					caster.broadcastPacket(MoveSkill.getInstance(caster, caster));

				// если кастер нпс
				if(caster.isBroadcastEndSkillForCollision())
					// обрываем анимацию перемещения
					caster.broadcastPacket(SkillEnd.getInstance(caster, 0, skill.getIconId()));

				// если скил раш или кастер нпс
				if(skill.isRush() || caster.isBroadcastEndSkillForCollision())
					// завершаем работу таска
					done();
			}

			return;
		}

		// увеличиваем пройденное расстояние
		done += step;

		// смотрит результат
		float result = done / alldist;

		// если все прошли
		if(result >= 1F)
		{
			// завершаем работу таска
			done();
			return;
		}

		// рассчитываем новые координаты
		float newX = startX + ((targetX - startX) * result);
		float newY = startY + ((targetY - startY) * result);
		float newZ = startZ + ((targetZ - startZ) * result);

		// смещаем
		caster.setXYZ(newX, newY, newZ);
	}

	/**
	 * @param alldist задаваемое alldist
	 */
	public final void setAlldist(float alldist)
	{
		this.alldist = alldist;
	}

	/**
	 * @param done задаваемое done
	 */
	public final void setDone(float done)
	{
		this.done = done;
	}

	/**
	 * @param radians задаваемое radians
	 */
	public final void setRadians(float radians)
	{
		this.radians = radians;
	}

	/**
	 * @param skill скил таска.
	 */
	public void setSkill(Skill skill)
	{
		this.skill = skill;
	}

	/**
	 * @param startX задаваемое startX
	 */
	public final void setStartX(float startX)
	{
		this.startX = startX;
	}

	/**
	 * @param startY задаваемое startY
	 */
	public final void setStartY(float startY)
	{
		this.startY = startY;
	}

	/**
	 * @param startZ задаваемое startZ
	 */
	public final void setStartZ(float startZ)
	{
		this.startZ = startZ;
	}

	/**
	 * @param targetX задаваемое targetX
	 */
	public final void setTargetX(float targetX)
	{
		this.targetX = targetX;
	}

	/**
	 * @param targetY задаваемое targetY
	 */
	public final void setTargetY(float targetY)
	{
		this.targetY = targetY;
	}

	/**
	 * @param targetZ задаваемое targetZ
	 */
	public final void setTargetZ(float targetZ)
	{
		this.targetZ = targetZ;
	}
}
