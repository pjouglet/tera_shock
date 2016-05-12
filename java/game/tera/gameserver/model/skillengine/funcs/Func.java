package tera.gameserver.model.skillengine.funcs;

import tera.gameserver.model.Character;

/**
 * Интерфейс для реализации функции рассчета параметров.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface Func
{
	/**
	 * Добавление функции персонажу.
	 *
	 * @param owner персонаж.
	 */
	public void addFuncTo(Character owner);

	/**
	 * Удаление функции персонажу
	 *
	 * @param owner персонаж.
	 */
	public void removeFuncTo(Character owner);
}
