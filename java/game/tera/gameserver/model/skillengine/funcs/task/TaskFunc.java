package tera.gameserver.model.skillengine.funcs.task;

import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Интерфейс для реализации тасковой функции.
 * 
 * @author Ronn
 */
public interface TaskFunc extends Func, Runnable
{
	/**
	 * @return интервал.
	 */
	public int getInterval();
	
	/**
	 * @return кол-во выполнений.
	 */
	public int getLimit();
}
