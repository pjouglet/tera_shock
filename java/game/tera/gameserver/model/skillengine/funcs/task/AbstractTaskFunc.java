package tera.gameserver.model.skillengine.funcs.task;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Loggers;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель периодической функции.
 *
 * @author Ronn
 */
public abstract class AbstractTaskFunc implements TaskFunc
{
	/** список обрабатываемых персонажей */
	protected final Array<Character> characters;

	/** ссылка на задачу */
	protected volatile ScheduledFuture<AbstractTaskFunc> schedule;

	/** кол-во оставшихся выполнений */
	protected volatile int currentCount;

	public AbstractTaskFunc(VarTable vars)
	{
		this.characters = Arrays.toConcurrentArray(Player.class);
	}

	@Override
	public final void addFuncTo(Character owner)
	{
		// получаем персонажи
		Array<Character> characters = getCharacters();

		characters.writeLock();
		try
		{
			// вносим нового
			characters.add(owner);

			// устанавливаем новый лимит
			currentCount = getLimit();

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			if(schedule == null)
				schedule = executor.scheduleAiAtFixedRate(this, getInterval(), getInterval());
		}
		finally
		{
			characters.writeUnlock();
		}
	}

	/**
	 * Приминение функции на персонажа.
	 */
	public abstract void applyFunc();

	/**
	 * @return обрабатываемые персонажи.
	 */
	public final Array<Character> getCharacters()
	{
		return characters;
	}

	@Override
	public final void removeFuncTo(Character owner)
	{
		// получаем персонажи
		Array<Character> characters = getCharacters();

		characters.writeLock();
		try
		{
			characters.fastRemove(owner);

			if(characters.isEmpty() && schedule != null)
			{
				schedule.cancel(true);
				schedule = null;
			}
		}
		finally
		{
			characters.writeUnlock();
		}
	}

	@Override
	public final void run()
	{
		if(currentCount > -2)
		{
			if(currentCount < 1)
				return;

			currentCount--;
		}

		try
		{
			applyFunc();
		}
		catch(Exception e)
		{
			Loggers.warning(this, e);
		}
	}
}
