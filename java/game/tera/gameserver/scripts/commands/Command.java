package tera.gameserver.scripts.commands;

import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации обработчика команд.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public interface Command
{
	/**
	 * Процесс использования команды игроком.
	 * 
	 * @param command введенная команда.
	 * @param player игрок, который использует команду.
	 * @param values надор параметров для команды.
	 */
	public void execution(String command, Player player, String values);
	
	/**
	 * @return минимальный уровень доступа для обработчика.
	 */
	public int getAccess();
	
	/**
	 * @return массив команд, обрабатываемых этим обработчиком.
	 */
	public String[] getCommands();
}
