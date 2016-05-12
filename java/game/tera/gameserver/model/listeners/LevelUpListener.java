package tera.gameserver.model.listeners;

import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации слушателя лвлапов тгоков.
 * 
 * @author Ronn
 */
public interface LevelUpListener
{
	/**
	 * Прослушка повышения уровней.
	 * 
	 * @param player игрок, который	повысил вой уровень.
	 */
	public void onLevelUp(Player player);
}
