package tera.gameserver.model.equipment;

import rlib.util.Synchronized;
import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;

/**
 * Интерфейс для реализации экиперовки персонажа.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface Equipment extends Foldable, Synchronized
{
	/**
	 * Перенос итема с ячейки инвенторя в экиперовку.
	 *
	 * @param inventory инвентарь.
	 * @param cell ячейка инвенторя.
	 * @return успешно ли перенеслось.
	 */
	public boolean dressItem(Inventory inventory, Cell cell);

	/**
	 * Проверка на возможность одеть итем.
	 *
	 * @param item проверяемый итем.
	 * @return можно ли одеть.
	 */
	public boolean equiped(ItemInstance item);

	/**
	 * Положить в пул.
	 */
	public void fold();

	/**
	 * Подсчет одетых итемов с указанным слотом.
	 *
	 * @param type тип слота.
	 * @return кол-во итемов одетых.
	 */
	public int getCount(SlotType type);

	/**
	 * @return кол-во занятых слотов.
	 */
	public int getEngagedSlots();

	/**
	 * Получение экиперованного итема по индексу.
	 *
	 * @param index индекс слота.
	 * @return экиперованный итем.
	 */
	public ItemInstance getItem(int index);

	/**
	 * @param type тип слота.
	 * @return первый попавшийся итем по указаному слоту.
	 */
	public ItemInstance getItem(SlotType type);

	/**
	 * @param type тип слота.
	 * @return ид первого попавшегося итема с указанным типом слота.
	 */
	public int getItemId(SlotType type);

	/**
	 * @return владелец экиперовки.
	 */
	public Character getOwner();

	/**
	 * Поиск слота содержащего итем с указанным уникальным ид.
	 *
	 * @param objectId уникальный ид итема.
	 * @return слот, содержащий нужный итем.
	 */
	public Slot getSlotForObjectId(int objectId);

	/**
	 * @return список всех слотов.
	 */
	public Slot[] getSlots();

	/**
	 * Создание структуры экиперовки.
	 *
	 * @param slotTypes набор слотов.
	 */
	public void recreateSlots(SlotType... slotTypes);

	/**
	 * Установка итема в указанный слот по индексу.
	 *
	 * @param item устанавливаемый итем.
	 * @param index индекс слота.
	 * @return успешно ли установлен.
	 */
	public boolean setItem(ItemInstance item, int index);

	/**
	 * @param owner владелец экиперовки.
	 */
	public Equipment setOwner(Character owner);

	/**
	 * @param slots набор слотов.
	 */
	public void setSlots(Slot[] slots);

	/**
	 * Положит итем с указанного слота в указанный инвентарь.
	 *
	 * @param inventory инвентарь.
	 * @param index слот, содержащий нужный итем.
	 * @param itemId ид итема.
	 * @return перемещен ли итем.
	 */
	public boolean shootItem(Inventory inventory, int index, int itemId);

	/**
	 * @return кол-во слотов.
	 */
	public int size();

	/**
	 * Проверка на возможность снятия указанного итема.
	 *
	 * @param item проверяемый итем.
	 * @return можно ли его снять.
	 */
	public boolean unequiped(ItemInstance item);
}
