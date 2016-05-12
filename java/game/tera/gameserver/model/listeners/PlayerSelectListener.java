package tera.gameserver.model.listeners;

import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации слушателя выбора игрока для входа.
 *
 * @author Ronn
 */
public interface PlayerSelectListener
{
	/**
	 * Обработка выбора игрока для входа в игру.
	 *
	 * @param player выбранный игрок для входа.
	 */
	public void onSelect(Player player);
}
