package tera.gameserver.events;

import tera.gameserver.model.playable.Player;

/**
 *  Интерфейс для регистрируемых ивентов
 *
 * @author Ronn
 * @created 11.04.2012
 */
public interface Registered
{
	/**
	 * Регистрация на участие игрока.
	 * 
	 * @param player желающий участвовать игрок.
	 * @return принят ли на ивент.
	 */
	public boolean registerPlayer(Player player);
	
	/**
	 * Отмена регистрации на участие игрока.
	 * 
	 * @param player игрок отказавшийся учавствовать.
	 * @return отрегистрирован ли он.
	 */
	public boolean unregisterPlayer(Player player);
}
