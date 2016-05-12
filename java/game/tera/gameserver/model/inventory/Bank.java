package tera.gameserver.model.inventory;

import rlib.util.Synchronized;
import rlib.util.pools.Foldable;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;

/**
 * Интерфейс для реализации банка.
 *
 * @author Ronn
 */
public interface Bank extends Foldable, Synchronized
{
	/**
	 * Добавление новых итемов в банк.
	 *
	 * @param itemId ид итема.
	 * @param itemCount кол-во итемов.
	 * @return добавлены ли итемы.
	 */
	public boolean addItem(int itemId, int itemCount);

	/**
	 * Добавляем денег в банк.
	 *
	 * @param count кол-во добавляемых денег.
	 */
	public void addMoney(long count);

	/**
	 * Складировать банк в пул.
	 */
	public void fold();

	/**
	 *
	 * @return ячейка по указанному индексу.
	 */
	public Cell getCell(int index);

	/**
	 * @return список ячеяк банка.
	 */
	public Cell[] getCells();

	/**
	 * Полкчение итема по его обджект ид.
	 *
	 * @param objectId ид итема.
	 * @return искомый итем.
	 */
	public ItemInstance getItemForObjectId(int objectId);

	/**
	 * @return индекс последней занятой ячейки.
	 */
	public int getLastIndex();

	/**
	 * @return размещение банка.
	 */
	public ItemLocation getLocation();

	/**
	 * @return максимальный рамзер банка.
	 */
	public int getMaxSize();

	/**
	 * @return кол-во денег в банке.
	 */
	public long getMoney();

	/**
	 * @return владелец банка.
	 */
	public Object getOwner();

	/**
	 * @return кол-во ячеяк в табе.
	 */
	public int getTabSize();

	/**
	 * @return кол-во используемых ячеяк.
	 */
	public int getUsedCount();

	/**
	 * Добавление нового итема в банк.
	 *
	 * @param item добавляемый итем.
	 * @return успешно ли итем добавлен.
	 */
	public boolean putItem(ItemInstance item);

	/**
	 * Удаление итемов из банка указанного ид и кол-ва.
	 *
	 * @param itemId ид итема.
	 * @param itemCount кол-во итемов.
	 * @return успешно ли удалены.
	 */
	public boolean removeItem(int itemId, int itemCount);

	/**
	 * Удаление указанного итема из банка.
	 *
	 * @param item удаляемый итем.
	 * @return успешно ли удален.
	 */
	public boolean removeItem(ItemInstance item);

	/**
	 * Установка итема в указанную ячейку.
	 *
	 * @param index индекс ячейки.
	 * @param item итем.
	 * @return успешн ли установлен.
	 */
	public boolean setItem(int index, ItemInstance item);

	/**
	 * @param owner владелец банка.
	 */
	public void setOwner(Object owner);

	/**
	 * @return были ли перемещены итемы.
	 */
	public boolean sort();

	/**
	 * Уменьшает кол-во денег в банке на указанное кол-во.
	 *
	 * @param count кол-во денег.
	 */
	public void subMoney(long count);
}
