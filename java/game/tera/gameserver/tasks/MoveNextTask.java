package tera.gameserver.tasks;

import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.util.array.Array;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MoveType;
import tera.gameserver.taskmanager.MoveTaskManager;

/**
 * Модель задачи перемещения персонажа.
 *
 * @author Ronn
 */
public final class MoveNextTask
{
	public static final int INTERVAL = 100;

	/** блокировщик */
	private final Lock lock;

	/** персонаж */
	private final Character owner;

	/** текущий тип движения */
	private MoveType type;

	/** контейнер задач */
	private volatile Array<MoveNextTask> container;

	/** полная дистанция */
	private float alldist;
	/** модификатор скорости */
	private float mod;

	/** стартовая позиция */
	private float startX;
	private float startY;
	private float startZ;

	/** конечная позиция */
	private float targetX;
	private float targetY;
	private float targetZ;

	/** последняя скорость */
	private int lastSpeed;

	/** получаем время прошлой обработки */
	private long startTime;

	/**
	 * @param character персонаж.
	 */
	public MoveNextTask(Character character)
	{
		this.owner = character;
		this.lock = Locks.newLock();
	}

	/**
	 * Завершение выполнения перемещения.
	 */
	public void done()
	{
		// получаем владельца
		Character owner = getOwner();

		lock.lock();
		try
		{
			// ставим конечные координаты
			owner.setXYZ(targetX, targetY, targetZ);

			// убераем флаг движения
			owner.setMoving(false);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляет о прибытии в место назначения
			eventManager.notifyArrived(owner);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return контейнер задач.
	 */
	public Array<MoveNextTask> getContainer()
	{
		return container;
	}

	/**
	 * @return последняя рассчетная скорость.
	 */
	private final int getLastSpeed()
	{
		return lastSpeed;
	}

	/**
	 * @return модификатор скорости.
	 */
	public float getMod()
	{
		return mod;
	}

	/**
	 * @return владелец задачи.
	 */
	public Character getOwner()
	{
		return owner;
	}

	/**
	 * @return время последнего запуска.
	 */
	public long getStartTime()
	{
		return startTime;
	}

	/**
	 * @return текущий тип движения.
	 */
	public MoveType getType()
	{
		return type;
	}

	/**
	 * Обновление позиции персонажа.
	 *
	 * @param owner владелец задачи.
	 * @param currentTime
	 * @param currentSpeed скорость его.
	 * @return завершено ли перемещение.
	 */
	private boolean move(Character owner, long currentTime, int currentSpeed)
	{
		// рассчитываем пройденное расстояние
		float donedist = (currentTime - getStartTime()) * currentSpeed / 1000F * getMod();

		// рассчитываем коэффициент
		float done = donedist / alldist;

		if((!owner.isPlayer() || type.isFall()) && done >= 1)
			return true;

		// рассчитываем новую точку текущей позиции
		float newX = startX + ((targetX - startX) * done);
		float newY = startY + ((targetY - startY) * done);
		float newZ = startZ + ((targetZ - startZ) * done);

		// передвигаем на новую позицию
		owner.setXYZ(newX, newY, newZ);

		return false;
	}

	/**
	 * Запуск нового перемещения.
	 *
	 * @param startX стартовая координта.
	 * @param startY стартовая координта
	 * @param startZ стартовая координта
	 * @param type тип перемещения.
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @param targetZ целевая координата.
	 */
	public void nextTask(float startX, float startY, float startZ, MoveType type, float targetX, float targetY, float targetZ)
	{
		// получаем владельца задчи
		Character owner = getOwner();

		// получаем текущую скорость
		int currentSpeed = owner.getRunSpeed();

		lock.lock();
		try
		{
			// запоминаем тип движения
			setType(type);

			// запоминаем текущую скорость
			setLastSpeed(currentSpeed);

			// если она отсутсвует, выходим
			if(currentSpeed < 1)
				return;

			// расстояние рассинхронизации
			float dist = 0;

			// если это завершения движения
			if(type == MoveType.STOP)
			{
				// получаем расстояние до конечной точки
				dist = owner.getDistance(targetX, targetY, targetZ);

				// если это в пределах погрешности
				if(dist < currentSpeed / 3)
					// заносим корректирующие координаты
					owner.setXYZ(targetX, targetY, targetZ);

				// ставим флаг остановки
				owner.setMoving(false);

				// выходим
				return;
			}

			// рассчитываем модификатор скорости
			float modiff = 1;

			if(!owner.isPlayer())
				modiff = 1F;
			else if(type.isFall())
				modiff = 2F;

			// запоминаем модификатор
			setMod(modiff);

			// запоминаем стартовые координаты
			this.startX = startX;
			this.startY = startY;
			this.startZ = startZ;

			// запоминаем целевые координаты
			this.targetX = targetX;
			this.targetY = targetY;
			this.targetZ = targetZ;

			// определяем разницу
			float dx = startX - targetX;
			float dy = startY - targetY;
			float dz = startZ - targetZ;

			// рассчитываем всю дистанцию
			float alldist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

			// если они минимальна, выходим
			if(alldist < 0.001F)
				return;

			// запоминаем время начала нового движения
			setStartTime(System.currentTimeMillis());

			// запоминаем перемещаемое расстояние
			setAlldist(alldist);

			// ставим флаг движения
			owner.setMoving(true);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @param alldist итоговая дистанция перемещения.
	 */
	public void setAlldist(float alldist)
	{
		this.alldist = alldist;
	}

	/**
	 * @param container контейнер задач.
	 */
	public void setContainer(Array<MoveNextTask> container)
	{
		this.container = container;
	}

	/**
	 * @param last последняя рассчетная скорость.
	 */
	private final void setLastSpeed(int last)
	{
		this.lastSpeed = last;
	}

	/**
	 * @param mod модификатор скорости.
	 */
	public void setMod(float mod)
	{
		this.mod = mod;
	}

	/**
	 * @param startTime время начала задачи.
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @param type тип движения.
	 */
	public void setType(MoveType type)
	{
		this.type = type;
	}

	/**
	 * Запуск задачи.
	 */
	public void startTask()
	{
		// если уже стоит на обработке, выходим
		if(container != null)
			return;

		lock.lock();
		try
		{
			// если уже стоит на обработке, выходим
			if(container != null)
				return;

			// получаем менеджера по орбаботке задач движения
			MoveTaskManager taskManager = MoveTaskManager.getInstance();

			// вносим туда эту задачу
			taskManager.addMoveTask(this);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Остановка перемещения.
	 */
	public void stopMove()
	{
		// получаем владельца
		Character owner = getOwner();

		lock.lock();
		try
		{
			// убераем флаг движения
			owner.setMoving(false);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляет о прибытии в место назначения
			eventManager.notifyArrived(owner);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Останвка задачи.
	 */
	public void stopTask()
	{
		if(container == null)
			return;

		// получаем менеджера по орбаботке задач движения
		MoveTaskManager taskManager = MoveTaskManager.getInstance();

		// удаляем оттуда эту задачу
		taskManager.removeTask(this);
	}

	/**
	 * Обновление пакета о перемещении.
	 */
	public void update()
	{
		// получаем владельца задачи
		Character owner = getOwner();

		// получаем тип движения
		MoveType type = getType();

		// если персонаж движется
		if(owner.isMoving() && type != null)
			// обновляем его движение для окружающих
			owner.broadcastMove(owner.getX(), owner.getY(), owner.getZ(), owner.getHeading(), type, targetX, targetY, targetZ, false);
	}

	/**
	 * Обновление пакета о перемещении для указанного персонажа.
	 */
	public void update(Character target)
	{
		// получаем владельца задачи
		Character owner = getOwner();

		// получаем тип движения
		MoveType type = getType();

		if(owner.isMoving() && type != null)
			target.sendPacket(owner.getMovePacket(type, targetX, targetY, targetZ), true);
	}

	/**
	 * @param currentTime текущее время.
	 */
	public void update(long currentTime)
	{
		// получаем владельца задачи
		Character owner = getOwner();

		// если персонаж не в движении, выходим
		if(!owner.isMoving())
			return;

		// результат выполнение задачи
		int result = 0;

		lock.lock();
		try
		{
			// получаем текущую скорость
			int currentSpeed = owner.getRunSpeed();

			// если ее нету
			if(currentSpeed < 1)
				// ставим результат остановки движения
				result = 1;
			else
			{
				// если она не ровняется последней
				if(getLastSpeed() != currentSpeed)
				{
					// обновляем последнюю
					setLastSpeed(currentSpeed);

					// ставим результат обновления движения
					result = 2;
				}

				// если это последний такт перемещения
				if(move(owner, currentTime, currentSpeed))
					// ставим результат завершения движения
					result = 3;
			}
		}
		finally
		{
			lock.unlock();
		}

		switch(result)
		{
			case 1: owner.stopMove(); break;
			case 2: update(); break;
			case 3: done();
		}
	}
}
