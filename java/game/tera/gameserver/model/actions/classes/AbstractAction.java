package tera.gameserver.model.actions.classes;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель акшена.
 *
 * @author Ronn
 * @created 06.03.2012
 */
public abstract class AbstractAction<T> extends SafeTask implements Action, Foldable
{
	protected static final Logger log = Loggers.getLogger(AbstractAction.class);

	/** инициатор акшена */
	protected Player actor;
	/** цель акшена */
	protected T target;

	/** ссылка на таск */
	protected ScheduledFuture<?> schedule;

	/** уникальный ид акшена */
	protected int objectId;

	public AbstractAction()
	{
		// получаем фабрику ид
		IdFactory idFactory = IdFactory.getInstance();

		// получаем ид нового акшена
		this.objectId = idFactory.getNextActionId();
	}

	@Override
	public synchronized void assent(Player player)
	{
		// получаем ссылку на таск
		ScheduledFuture<?> schedule = getSchedule();

		// если она есть
		if(schedule != null)
			// отменяем
			schedule.cancel(false);

		// очищаем акшен
		clear();
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем ссылку на таск
		ScheduledFuture<?> schedule = getSchedule();

		// если она есть
		if(schedule != null)
			// отменяем
			schedule.cancel(false);

		// очищаем акшен
		clear();
	}

	/**
	 * Очистка акшена.
	 */
	protected synchronized void clear()
	{
		// получаем инициатора
		Player actor = getActor();

		// зануляем акшен
		if(actor != null)
			actor.setLastAction(null);

		// сохраняем в пул
		getPool().put(this);
	}

	@Override
	public void finalyze()
	{
		actor = null;
		target = null;
		schedule = null;
	}

	@Override
	public final Player getActor()
	{
		return actor;
	}

	@Override
	public final int getId()
	{
		return getType().ordinal();
	}

	@Override
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return пул этого акшена.
	 */
	protected final FoldablePool<Action> getPool()
	{
		return getType().getPool();
	}

	/**
	 * @return ссылка на таск.
	 */
	protected final ScheduledFuture<?> getSchedule()
	{
		return schedule;
	}

	@Override
	public final T getTarget()
	{
		return target;
	}

	@Override
	public abstract ActionType getType();

	@Override
	public synchronized void invite()
	{
		// получаем инициатора
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
			return;

		// запоминаем акшен за инициатором
		actor.setLastAction(this);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем новуй отложенный таск
		setSchedule(executor.scheduleGeneral(this, 20000));
	}

	@Override
	public void reinit(){}

	@Override
	protected void runImpl()
	{
		cancel(actor);
	}

	@Override
	public final void setActor(Player actor)
	{
		this.actor = actor;
	}

	/**
	 * @param ссылка на таск.
	 */
	protected final void setSchedule(ScheduledFuture<?> schedule)
	{
		this.schedule = schedule;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void setTarget(Object target)
	{
		this.target = (T) target;
	}

	@Override
	public final boolean test()
	{
		return test(actor, target);
	}

	/**
	 * Проверить выполнение условий.
	 *
	 * @param actor инициатор.
	 * @param target цель акшена.
	 * @return результат проверки.
	 */
	protected abstract boolean test(Player actor, T target);
}
