package tera.gameserver.model.inventory;

import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.LocalObjects;

/**
 * Базовая модель реализации инвенторя.
 *
 * @author Ronn
 */
public abstract class AbstractInventory implements Inventory
{
	protected static final Logger log = Loggers.getLogger(Inventory.class);

	/** блокировщик */
	protected final Lock lock;

	/** владелец сумки */
	protected Character owner;

	/** набор ячеяк */
	protected Cell[] cells;
	/** ячейка, в которой хранятся деньги */
	protected Cell gold;

	/** левел сумки */
	protected int level;

	/**
	 * @param level уровень сумки.
	 */
	public AbstractInventory(int level)
	{
		// сохраняем уровень
		this.level = level;

		// создаем блокировщика
		lock = Locks.newLock();

		// создаем массив ячеяк
		cells = new Cell[getAllMaxCells()];

		// заполняем массив
		for(int i = 0; i < cells.length; i++)
			cells[i] = new Cell(i, ItemLocation.INVENTORY);

		// слот для голда
		gold = new Cell(-1, ItemLocation.INVENTORY);
	}

	@Override
	public boolean addItem(int itemId, long count, String autor)
	{
		return addItem(itemId, count, autor, getMaxCells());
	}

	/**
	 *
	 * @param itemId темплейт ид итема.
	 * @param count кол-во создаваемых итемов.
	 * @param autor автор итемов.
	 * @param max максимальное кол-во обрабатываемых ячеяк.
	 * @return был ли создан и добавлен итем.
	 */
	private boolean addItem(int itemId, long count, String autor, int max)
	{
		if(count < 1)
			return false;

		// проверяем отношение итема к донату
    	if(!"wait_items".equals(autor) && Arrays.contains(Config.WORLD_DONATE_ITEMS, itemId))
    	{
    		log.warning(this, new Exception("not create donate item for id " + itemId));
    		return false;
    	}

    	// получаем владельца инвенторя
    	Character owner = getOwner();

    	// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// если это деньги
			if(itemId == Inventory.MONEY_ITEM_ID)
			{
				// добавляем деньги
				addMoney(count);
				return true;
			}

			// получаем ячейки
			Cell[] cells = getCells();

			// пустая ячейка
			Cell empty = null;

			// однотипный итем
			ItemInstance sametype = null;

			// перебираем ячейки
			for(int i = 0; i < max; i++)
			{
				Cell cell = cells[i];

				// если ячейка пустая
				if(cell.isEmpty())
				{
					// запоминаем ее как свободную
					if(empty == null)
						empty = cell;

					continue;
				}

				// лежачий итем в текущей ячейки
				ItemInstance old = cell.getItem();

				// если итем стакуем и с тем же темплейт ид, то запоминаем
				if(old.isStackable() && old.getItemId() == itemId)
				{
					sametype = old;
					break;
				}
			}

			// если есть однотипный стакуемый итем, добавляем к нему
			if(sametype != null)
			{
				// ставим автора
				sametype.setAutor(autor);

				// добавляем итемов в стопку
				sametype.addItemCount(count);

				// уведомляем об добалвении итема
				eventManager.notifyInventoryAddItem(owner, sametype);

				// обновляем в базе итем
				dbManager.updateDataItem(sametype);

				return true;
			}

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			// получаем темплейт итема
			ItemTemplate template = itemTable.getItem(itemId);

			// если вариантов нет, выходим
			if(template == null || empty == null)
				return false;

			// создаем новый итем
			ItemInstance item = template.newInstance();

			// если создать не получилось, выходим
			if(item == null)
				return false;

			// если стакуем
			if(template.isStackable())
				// ставим нужное кол-во
				item.setItemCount(count);

			// добавляем владельца
			item.setOwnerId(owner.getObjectId());

			// обновляем автора
			item.setAutor(autor);

			// устанавливаем итем в ячейку
			empty.setItem(item);

			// уведомляем об добалвении итема
			eventManager.notifyInventoryAddItem(owner, item);

			// обновляем в базе итем
			dbManager.updateItem(item);

			return true;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public synchronized void addLevel()
	{
		level++;
	}

	@Override
	public void addMoney(long count)
	{
		// если денег новых нет, выходим
		if(count < 1)
			return;

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

    	// получаем таблицу итемов
    	ItemTable itemTable = ItemTable.getInstance();

    	// получаем владельца инвенторя
    	Character owner = getOwner();

		lock();
		try
		{
			// текущий итем
			ItemInstance old = gold.getItem();

			// если текущий есть
			if(old != null)
				// добавляем в стопку голда
				old.addItemCount(count);
			else
			{
				// создаем экземпляр голда
				ItemInstance item = itemTable.getItem(MONEY_ITEM_ID).newInstance();

				// если не удалось создать итем, выходим
				if(item == null)
				{
					log.warning(this, new Exception("not created money item"));
					return;
				}

				// устанавливаем нужное кол-во
				item.setItemCount(count);

				// обноавляем владельца итема
				item.setOwnerId(owner.getObjectId());

				// вставляем в ячейку
				gold.setItem(item);
			}

			// получаем итем денег
			ItemInstance item = gold.getItem();

			// уведомляем об добалвении итема
			eventManager.notifyInventoryAddItem(owner, item);

			// обновляем в базе итем
			dbManager.updateItem(item);
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean containsItems(int itemId, int itemCount)
	{
		lock();
		try
		{
			// если ид денег
			if(itemId == Inventory.MONEY_ITEM_ID)
			{
				// получаем деньги
				ItemInstance item = gold.getItem();

				// если нет их, выходим
				if(item == null)
					return false;

				// сравниваем с кол-вом денег
				return item.getItemCount() >= itemCount;
			}

			// создаем счетчик
			int counter = 0;

			// получаем все ячейки
			Cell[] cells = getCells();

			// перебираем доступные ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем итем в ячейке
				ItemInstance item = cells[i].getItem();

				// если итема нет, пропускаем
				if(item == null)
					continue;

				// если итем с нужным ид
				if(item.getItemId() == itemId)
					// увеличиваем счетчик на кол-во итемов
					counter += item.getItemCount();

				// если нашли нужное кол-во
				if(counter >= itemCount)
					return true;
			}

			// нажли ли нужное кол-во
			return counter >= itemCount;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean forceAddItem(int itemId, long count, String autor)
	{
		return addItem(itemId, count, autor, getAllMaxCells());
	}

	@Override
	public Cell getCell(int index)
	{
		// проверка индекса
		if(index < 0 || index >= getMaxCells())
			return null;

		// извлекаем ячейку
		return cells[index];
	}

	@Override
	public Cell getCellForObjectId(int objectId)
	{
		lock();
		try
		{
			// получаем все ячейки
			Cell[] cells = getCells();

			// перебор ячеяк
			for(int i = 0, length = getMaxCells(); i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// пустые пропускаем
				if(cell.isEmpty())
					continue;

				// получаем итем в ячейке
				ItemInstance item = cell.getItem();

				// если это искомый
				if(item.getObjectId() == objectId)
					return cell;
			}

			// получем деньги
			ItemInstance item = gold.getItem();

			// если деньги есть и они нам нужны
			if(item != null && item.getObjectId() == objectId)
				return gold;

			return null;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public Cell[] getCells()
	{
		return cells;
	}

	@Override
	public int getEngagedCells()
	{
		lock();
		try
		{
			int counter = 0;

			// получаем достувпные ячейки
			Cell[] cells = getCells();

			// перебераем ячейки
			for(int i = 0, length = getMaxCells(); i < length; i++)
				// считаем все занятые
				if(!cells[i].isEmpty())
					counter++;

			return counter;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public int getFreeCells()
	{
		lock();
		try
		{
			// создаем счетчик
			int counter = 0;

			// получаем доступные ячейки
			Cell[] cells = getCells();

			// перебераем
			for(int i = 0, length = getMaxCells(); i < length; i++)
				// считаем все свободные
				if(cells[i].isEmpty())
					counter++;

			return counter;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public Cell getGold()
	{
		return gold;
	}

	@Override
	public int getItemCount(int itemId)
	{
		lock();
		try
		{
			// создаем счетчик
			int counter = 0;

			// получаем доступные ячейки
			Cell[] cells = getCells();

			// перебираем итемы
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем итем
				ItemInstance item = cells[i].getItem();

				// если итема нет либо не искомый, пропускаем
				if(item == null || item.getItemId() != itemId)
					continue;

				// плюсуем к счетчику
				counter += item.getItemCount();
			}

			return counter;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public ItemInstance getItemForItemId(int itemId)
	{
		lock();
		try
		{
			// получаем массив ячеяк
			Cell[] cells = getCells();

			// перебираем итемы
			for(int i = 0, length = getMaxCells(); i < length; i++)
			{
				// получаем итем в ячейке
				ItemInstance item = cells[i].getItem();

				// если итема нет либо он не подходящий, пропускаем
				if(item == null || item.getItemId() != itemId)
					continue;

				// возвращаем искомый
				return item;
			}

			return null;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public ItemInstance getItemForObjectId(int objectId)
	{
		lock();
		try
		{
			// получаепм ячейку с итемом
			Cell cell = getCellForObjectId(objectId);

			// если ячейка есть, возвращаем ее итем
			return cell == null? null : cell.getItem();
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public int getLastIndex()
	{
		lock();
		try
		{
			// определяем последний индекс
			int last = getMaxCells() - 1;

			// получаем доступные ячейки
			Cell[] cells = getCells();

			// перебираем с конца ячейки
			for(int i = last; i >= 0; i--)
			{
				// если ячейка занята, выходим
				if(!cells[i].isEmpty())
					break;

				last--;
			}

			return last;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public int getLevel()
	{
		return level;
	}

	@Override
	public long getMoney()
	{
		return gold.getItemCount();
	}

	@Override
	public Character getOwner()
	{
		return owner;
	}

	@Override
	public void lock()
	{
		lock.lock();
	}

	@Override
	public boolean moveItem(ItemInstance item, Inventory source)
	{
		if(item == null)
			return false;

		// получаем владельца инвенторя
		Character owner = getOwner();

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем доступные ячейки
		Cell[] cells = getCells();

		// кол-во доступных ячеяк
		int max = getMaxCells();

		// если нужна будет свободная ячейка
		Cell empty = null;

		// если итем стопкуем
		if(item.isStackable())
		{
			// перебираем ячейки
			for(int i = 0; i < max; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// свободную ячейку запоминаем
				if(cell.isEmpty())
				{
					// если еще не апоминали
					if(empty == null)
						// запоминаем
						empty = cell;

					continue;
				}

				// итем, лежачий в текущей ячейки
				ItemInstance old = cell.getItem();

				// если темплейт ид одинаковый
				if(old.getItemId() == item.getItemId())
				{
					// удаляем из старого инвенторя итем.
					source.removeItem(item);

					// удаляем владельца итема
					item.setOwnerId(0);

					// обновляем его положение в базе
					dbManager.updateLocationItem(item);

					// добавляем в стопку новых итемов
					old.addItemCount(item.getItemCount());

					// обновляем в базе итем
					dbManager.updateDataItem(old);

					// удаляем итем
					item.deleteMe();

					return true;
				}
			}
		}
		else
		// иначе ищем свободную ячейку
		{
			// перебираем ячейки
			for(int i = 0; i < max; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// если она пустая
				if(cell.isEmpty() && empty == null)
				{
					// запоминаем и выходим
					empty = cell;
					break;
				}
			}
		}

		// если есть пустая ячеяка
		if(empty != null)
		{
			// удаляем из старого инвенторя итем.
			source.removeItem(item);

			// указываем владельца итема.
			item.setOwnerId(owner.getObjectId());

			// вставляем в ячейку
			empty.setItem(item);

			// обновляем в базе итем
			dbManager.updateLocationItem(item);

			return true;
		}

		return false;
	}

	@Override
	public boolean putItem(ItemInstance item)
	{
		if(item == null)
			return false;

		// получаем владельца инвенторя
		Character owner = getOwner();

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// если итем является деньгами
			if(item.getItemId() == MONEY_ITEM_ID)
			{
				// получаем текущий итем денег
				ItemInstance old = gold.getItem();

				// если денег в инвенторе небыло
				if(old == null)
				{
					// добавляем владельца
					item.setOwnerId(owner.getObjectId());

					// устанавливаем итем в инвентарь
					gold.setItem(item);

					// обновляем в базе итем
					dbManager.updateLocationItem(item);
				}
				else
				{
					// добавляем денег
					old.addItemCount(item.getItemCount());

					// обновляем в базе итем
					dbManager.updateDataItem(old);
				}

				// уведомляем об добалвении итема
				eventManager.notifyInventoryAddItem(owner, item);

				return true;
			}

			// получаем доступные ячейки
			Cell[] cells = getCells();

			// кол-во доступных итемов
			int max = getMaxCells();

			//если нужна будет свободная ячейка
			Cell empty = null;

			// если итем стопкуем
			if(item.isStackable())
			{
				// перебираем ячейки
				for(int i = 0; i < max; i++)
				{
					// получаем ячейку
					Cell cell = cells[i];

					// свободную ячейку запоминаем
					if(cell.isEmpty())
					{
						// если еще не запоминали
						if(empty == null)
							// запоминаем
							empty = cell;

						continue;
					}

					// итем, лежачий в текущей ячейки
					ItemInstance old = cell.getItem();

					// если темплейт ид одинаковый
					if(old.getItemId() == item.getItemId())
					{
						// добавляем в стопку новых итемов
						old.addItemCount(item.getItemCount());

						// уведомляем об добалвении итема
						eventManager.notifyInventoryAddItem(owner, item);

						// обновляем в базе итем
						dbManager.updateDataItem(old);

						return true;
					}
				}
			}
			else
			// иначе ищем свободную ячейку
			{
				// перебираем ячейки
				for(int i = 0; i < max; i++)
				{
					// получаем ячейку
					Cell cell = cells[i];

					// есди ячейка пустая
					if(cell.isEmpty() && empty == null)
					{
						// запоминаем и выходим
						empty = cell;
						break;
					}
				}
			}

			// если нашли пустую ячейку
			if(empty != null)
			{
				// указываем владельца итема.
				item.setOwnerId(owner.getObjectId());

				// вставляем в ячейку
				empty.setItem(item);

				// уведомляем об добалвении итема
				eventManager.notifyInventoryAddItem(owner, item);

				// обновляем в базе итем
				dbManager.updateLocationItem(item);

				return true;
			}

			return false;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean removeItem(int itemId)
	{
		// получаем владельца инвенторя
		Character owner = getOwner();

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// счетчик удаленных итемов
			int counter = 0;

			// получаем список ячеяк
			Cell[] cells = getCells();

			// перебираем ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// итем, лежащий в ячейке
				ItemInstance item = cell.getItem();

				// если не подходит, пропускаем
				if(item == null || item.getItemId() != itemId)
					continue;

				// обнуляем в чейке
				cell.setItem(null);

				// обнуляем владельца
				item.setOwnerId(0);

				// уведомляем об удалении итема
				eventManager.notifyInventoryRemoveItem(owner, item);

				// обновляем в БД
				dbManager.updateLocationItem(item);

				// удаляем с мира
				item.deleteMe();

				counter++;
			}

			return counter > 0;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean removeItem(int itemId, long count)
	{
		// получаем владельца инвенторя
		Character owner = getOwner();

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// получаем список ячеяк
			Cell[] cells = getCells();

			// перебираем ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// получаем итем из ячейки
				ItemInstance item = cell.getItem();

				// если не подходит, пропускаем
				if(item == null || item.getItemId() != itemId)
					continue;

				// если кол-во итема в стопке больше нужного
				if(item.getItemCount() > count)
				{
					// уменьшаем стопку на нужное
					item.subItemCount(count);

					// уведомляем об удалении итема
					eventManager.notifyInventoryRemoveItem(owner, item);

					// обновляем в БД
					dbManager.updateDataItem(item);

					return true;
				}
				// есди в стопке ровно столько итемов, сколько надо удалить
				else if(item.getItemCount() == count)
				{
					// удаляем итем с ячейки
					cell.setItem(null);

					//зануляем владельца
					item.setOwnerId(0);

					// уведомляем об удалении итема
					eventManager.notifyInventoryRemoveItem(owner, item);

					// обновляем в БД
					dbManager.updateLocationItem(item);

					// удаляем с мира
					item.deleteMe();

					return true;
				}
				// если меньше в стопке, чем надо
				else
				{
					// удаляем итем с ячейки
					cell.setItem(null);
					//зануляем владельца
					item.setOwnerId(0);

					// уведомляем об удалении итема
					eventManager.notifyInventoryRemoveItem(owner, item);

					// обновляем в БД
					dbManager.updateLocationItem(item);

					// удаляем с мира
					item.deleteMe();

					// уменьшаем необходимое кол-во удаленных итемов на кол-во итемов в дулаенной стопке
					count -= item.getItemCount();
				}
			}

			// выполнен ли план удаления полностью
			return count < 1;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean removeItem(ItemInstance item)
	{
		// получаем владельца инвенторя
		Character owner = getOwner();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// получаем список доступных ячеяк
			Cell[] cells = getCells();

			// перебираем ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// пропускаем пустые
				if(cell.isEmpty())
					continue;

				// итем, лежащий в ячейке
				ItemInstance old = cell.getItem();

				// если это искомый итем
				if(old == item)
				{
					// зануляем ячейку
					cell.setItem(null);

					// уведомляем об удалении итема
					eventManager.notifyInventoryRemoveItem(owner, item);

					return true;
				}
			}

			// получаем итем с деньгами
			ItemInstance old = gold.getItem();

			// если это искомый итем
			if(!gold.isEmpty() && old == item)
			{
				// зануляем ячейку
				gold.setItem(null);

				// уведомляем об удалении итема
				eventManager.notifyInventoryRemoveItem(owner, item);

				return true;
			}

			return false;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean removeItemFromIndex(long count, int index)
	{
		if(index < 0 || index >= cells.length)
			return false;

		// получаем владельца инвенторя
		Character owner = getOwner();

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// искомая ячейка
			Cell cell = cells[index];

			// итем лежащий в ячейке
			ItemInstance item = cell.getItem();

			// если итема нет, выходим
			if(item == null)
				return false;

			// если кол-во итема в стопке больше нужного
			if(item.getItemCount() > count)
			{
				// уменьшаем стопку на нужное
				item.subItemCount(count);

				// уведомляем об удалении итема
				eventManager.notifyInventoryRemoveItem(owner, item);

				// обновляем в БД
				dbManager.updateDataItem(item);

				return true;
			}
			// есди в стопке ровно или меньше итемов, сколько надо удалить
			else
			{
				// удаляем итем с ячейки
				cell.setItem(null);

				//зануляем владельца
				item.setOwnerId(0);

				// уведомляем об удалении итема
				eventManager.notifyInventoryRemoveItem(owner, item);

				// обновляем в БД
				dbManager.updateLocationItem(item);

				// удаляем с мира
				item.deleteMe();

				return true;
			}
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void setCells(Cell[] cells)
	{
		this.cells = cells;
	}

	@Override
	public void setGold(Cell gold)
	{
		this.gold = gold;
	}

	@Override
	public boolean setItem(ItemInstance item, int index)
	{
		// если итема нет, выходим
		if(item == null)
			return false;

		lock();
		try
		{
			// если это деньги
			if(index == -1)
			{
				// устанавливаем итем в ячейку денег
				gold.setItem(item);
				return true;
			}

			// проверка входимости в инвентарь
			if(index < 0 || index >= cells.length)
				return false;

			// устанавливаем итем в ячейку
			cells[index].setItem(item);

			return true;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void setOwner(Character owner)
	{
		this.owner = owner;
	}

	@Override
	public boolean sort()
	{
		lock();
		try
		{
			// получаем индекс последней занятой ячейки
			int last = getLastIndex();

			// если инвентарь пуст, выходим
			if(last < 1)
				return false;

			// получаем список всех ячеяк
			Cell[] cells = getCells();

			// определяем флаг отсортированности
			boolean sorted = true;

			// перебираем ячейки с конца
			for(int i = last - 1; i >= 0; i--)
				// если между занятыми ячейками обнаруживается пустая
				if(cells[i].isEmpty())
				{
					// ставим флаг не отсортированности выходим из цикла
					sorted = false;
					break;
				}

			// если инвентарь отсортирован, выходим
			if(sorted)
				return false;

			// получаем менеджера БД
	    	DataBaseManager dbManager = DataBaseManager.getInstance();

	    	// получаем локальные объекты
	    	LocalObjects local = LocalObjects.get();

	    	// получаем список итемов
			Array<ItemInstance> items = local.getNextItemList();

			// перебираем все ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// пустую пропускаем
				if(cell.isEmpty())
					continue;

				// вносим итем из ячейки
				items.add(cell.getItem());

				// опустошаем ячейку
				cell.setItem(null);
			}

			// получаем массив всех итемов инвенторя
			ItemInstance[] array = items.array();

			// перебираем итемы
			for(int i = 0, g = 0, length = items.size(); i < length; i++)
			{
				// получаем итем
				ItemInstance item = array[i];

				// упорядочено заполняем ячейки
				cells[g++].setItem(item);

				// обновляем положение итема в БД
				dbManager.updateLocationItem(item);
			}

			return true;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void subLevel()
	{
		level--;
	}

	@Override
	public void subMoney(long count)
	{
		if(count < 1)
			return;

		// получаем менеджера БД
    	DataBaseManager dbManager = DataBaseManager.getInstance();

    	// получаем менеджер событий
    	ObjectEventManager eventManager = ObjectEventManager.getInstance();

		lock();
		try
		{
			// итем с деньгами
			ItemInstance item = gold.getItem();

			// если тиема нет, выходим
			if(item == null)
				return;

			// уменьшаем кол-во итемов
			item.subItemCount(Math.min(count, item.getItemCount()));

			// уведомляем об удалении итема
			eventManager.notifyInventoryRemoveItem(owner, item);

			// обновляем в БД
			dbManager.updateDataItem(gold.getItem());
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public String toString()
	{
		return "owner = " + owner.getName() + ", cells = " + Arrays.toString(cells) + ", gold = " + gold + ", level = " + level;
	}

	@Override
	public void unlock()
	{
		lock.unlock();
	}
}
