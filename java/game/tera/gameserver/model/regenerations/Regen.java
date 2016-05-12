package tera.gameserver.model.regenerations;

/**
 * Интерфейс для реализации регена у персонажей.
 *
 * @author Ronn
 * @created 14.04.2012
 */
public interface Regen
{
	/**
	 * @return нужно ли выполнить реген.
	 */
	public boolean checkCondition();
	
	/**
	 * Выполнение регена.
	 */
	public void doRegen();
}
