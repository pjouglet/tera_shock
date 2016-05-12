package tera.gameserver.model.inventory;

import rlib.util.Synchronized;
import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;

/**
 * Интерфейс для реализации инвенторя.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public interface Inventory extends Foldable, Synchronized
{
	/** итем ид денег */
	public static final int MONEY_ITEM_ID = 20000000;
	
	/**
	 * Создает и добавляет итем в инвентарь.
	 * 
	 * @param itemId темплейт ид итема.
	 * @param count кол-во создаваемых итемов.
	 * @param autor автор итемов.
	 * @return успешно ли все итемы созданы.
	 */
	public boolean addItem(int itemId, long count, String autor);
	
	/**
	 * Увеличивает уровень инвенторя на 1.
	 */
	public void addLevel();
	
	/**
	 * Добавляем денег инвентарю.
	 * 
	 * @param count кол-во добавляемых денег.
	 */
	public void addMoney(long count);
	
	/**
	 * Проверка на содержание нужного кол-во нужных итемов в инвенторе.
	 * 
	 * @param itemId ид итема.
	 * @param itemCount кол-во итемов.
	 * @return содержаться ли.
	 */
	public boolean containsItems(int itemId, int itemCount);
	
	/**
	 * Сохранение в пуле инвенторя.
	 */
	public void fold();
	
	/**
	 * Создает и добавляет итем в инвентарь с учетом резервных ячеяк.
	 * 
	 * @param itemId темплейт ид итема.
	 * @param count кол-во создаваемых итемов.
	 * @param autor автор итемов.
	 * @return успешно ли все итемы созданы.
	 */
	public boolean forceAddItem(int itemId, long count, String autor);
	
	/**
	 * @return получаем макс ячеек доступных + заштрихованых.
	 */
	public int getAllMaxCells();
	
	/**
	 * @return базовый уровень инвенторя.
	 */
	public int getBaseLevel();
	
	/**
	 * Получаем ячейку с указанным индексом.
	 * 
	 * @param index индекс ячейки.
	 * @return ячейка инвенторя.
	 */
	public Cell getCell(int index);
	
	/**
	 * Получить ячейку с итемом с указанным уник ид.
	 * 
	 * @param objectId уник ид итема.
	 * @return ячейка инвенторя.
	 */
	public Cell getCellForObjectId(int objectId);
	
	/**
	 * @return все ячейки инвенторя.
	 */
	public Cell[] getCells();
	
	/**
	 * @return кол-во занятых ячеяк.
	 */
	public int getEngagedCells();
	
	/**
	 * @return кол-во свободных ячеяк.
	 */
	public int getFreeCells();
	
	/**
	 * @return ячейка с голдом.
	 */
	public Cell getGold();
	
	/**
	 * Подсчет кол-во итемов с указанным итем ид.
	 * 
	 * @param itemId темплейт ид итема.
	 * @return кол-во итемов с таким итем ид.
	 */
	public int getItemCount(int itemId);
	
	/**
	 * Возвращает первый попавшийся итем с указанным идом
	 * 
	 * @param itemId темплейт ид итема.
	 * @return итем с таким темплейт ид.
	 */
	public ItemInstance getItemForItemId(int itemId);
	
	/**
	 * Возвращает итем с указанным обджект ид
	 * 
	 * @param objectId обджект ид итема.
	 * @return итем с таким обджект ид.
	 */
	public ItemInstance getItemForObjectId(int objectId);
	
	/**
	 * @return индекс последней занятой ячейки.
	 */
	public int getLastIndex();
	
	/**
	 * @return уровень инвенторя.
	 */
	public int getLevel();
	
	/**
	 * @return кол-во дополнительных ячеяк за уровень.
	 */
	public int getLevelBonus();
	
	/**
	 * @return максимальный размер доступных ячеек.
	 */
	public int getMaxCells();
	
	/**
	 * @return возвращает кол-во денег в инвенторе.
	 */
	public long getMoney();
	
	/**
	 * @return владелец инвенторя.
	 */
	public Character getOwner();
	
	/**
	 * Переместить итем с исходного инвенторя в этот.
	 * 
	 * @param item перемещаемый итем.
	 * @param source исходный инвентарь.
	 * @return перемещен ли итем.
	 */
	public boolean moveItem(ItemInstance item, Inventory source);
	
	/**
	 * Положить итем в инвентарь.
	 * 
	 * @param item нужный итем.
	 * @return был ли положен итем в инвентарь.
	 */
	public boolean putItem(ItemInstance item);
	
	/**
	 * @param itemId темплейт ид удаляемого итема.
	 * @return все ли итемы были удалены.
	 */
	public boolean removeItem(int itemId);
	
	/**
	 * @param itemId темплейт ид удаляемого итема.
	 * @param count кол-во удаляемых итемов.
	 * @return было ли удалено указанное кол-во итемов.
	 */
	public boolean removeItem(int itemId, long count);
	
	/**
	 * @param item итем.
	 * @return был ли итем удален.
	 */
	public boolean removeItem(ItemInstance item);

	/**
	 * @param count кол-во удаляемых итемов.
	 * @param index индекс ячейки.
	 * @return было ли удалено указанное кол-во итемов.
	 */
	public boolean removeItemFromIndex(long count, int index);
	
	/**
	 * @param cells набор ячеяк инвенторя.
	 */
	public void setCells(Cell[] cells);
	
	/**
	 * @param gold ячейка с голдом.
	 */
	public void setGold(Cell gold);
	
	/**
	 * Принудительная установка итема в указанную ячейку, -1 ячейка денег
	 * 
	 * @param item устанавливаемый итем.
	 * @param index индекс ячейки.
	 * @return был ли итем установлен.
	 */
	public boolean setItem(ItemInstance item, int index);
	
	/**
	 * @param owner владелец.
	 */
	public void setOwner(Character owenr);
	
	/**
	 * Сортирует ячейки.
	 * 
	 * @return нужно ли обновлять инвентарь игроку.
	 */
	public boolean sort();
	
	/**
	 * Уменьшает уровень инвенторя на 1
	 */
	public void subLevel();
	
	/**
	 * Уменьшает кол-во денег в инвенторе на указанное кол-во.
	 * 
	 * @param count кол-во денег.
	 */
	public void subMoney(long count);
}
