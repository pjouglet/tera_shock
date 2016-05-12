package tera.gameserver.model.listeners;

import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для прослушки спавна игроков.
 * 
 * @author Ronn
 */
public interface PlayerSpawnListener
{
	/**
	 * @param player отспавнившийся игрок.
	 */
	public void onSpawn(Player player);
}
