package tera.gameserver.scripts.items;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для создания кастомной обработки активных итемов.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public interface ItemExecutor
{
	/**
	 * Обработка использования итема.
	 * 
	 * @param item используемый итем.
	 * @param player игрок, использующий итем.
	 */
	public void execution(ItemInstance item, Player player);
	
	/**
	 * @return минимальный уровень доступа.
	 */
	public int getAccess();
	
	/**
	 * @return массив итем ид, на которых распространяется обработчик.
	 */
	public int[] getItemIds();
}
