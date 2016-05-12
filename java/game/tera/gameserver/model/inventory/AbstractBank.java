package tera.gameserver.model.inventory;

import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Nameable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.Identified;
import tera.util.LocalObjects;

/**
 * Базовая модель реализации банка.
 *
 * @author Ronn
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBank<T extends Nameable & Identified> implements Bank
{
	protected static final Logger log = Loggers.getLogger(Bank.class);

	/** блокировщик */
	protected final Lock lock;

	/** владелец банка */
	protected T owner;

	/** массив ячеяк банка */
	protected Cell[] cells;
	/** ячейка для денег */
	protected Cell gold;

	public AbstractBank()
	{
		this.lock = Locks.newLock();
		this.cells = new Cell[getMaxSize()];

		ItemLocation location = getLocation();

		for(int i = 0, length = cells.length; i < length; i++)
			cells[i] = new Cell(i, location);

		this.gold = new Cell(-1, location);
	}

	@Override
	public boolean addItem(int id, int count)
	{
		if(count < 1)
			return false;

		// проверяем отношение итема к донату
		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, id))
		{
			log.warning(this, new Exception("not create donate item for id " + id));
			return false;
		}

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// если это деньги
			if(id == Inventory.MONEY_ITEM_ID)
			{
				// добавляем деньги и выходим
				addMoney(count);
				return true;
			}

			// получаем ячейки
			Cell[] cells = getCells();

			// пустая ячейка
			Cell empty = null;

			// однотипный итем
			ItemInstance sametype = null;

			// перебираем доступные ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
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
				if(old.isStackable() && old.getItemId() == id)
				{
					sametype = old;
					break;
				}
			}

			// если есть однотипный стакуемый итем, добавляем к нему
			if(sametype != null)
			{
				// ставим автора
				sametype.setAutor(owner.getName());
				// добавляем итемов в стопку
				sametype.addItemCount(count);

				// обновляем в базе итем
				dbManager.updateDataItem(sametype);

				return true;
			}

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			// получаем темплейт итема
			ItemTemplate template = itemTable.getItem(id);

			// если вариантов нет, выходим
			if(template == null || empty == null)
				return false;

			// создаем новый итем
			ItemInstance item = template.newInstance();

			// если создать не вышло, выходим
			if(item == null)
				return false;

			// если стакауемый
			if(template.isStackable())
				// указываем нужное кол-во
				item.setItemCount(count);

			// добавляем владельца
			item.setOwnerId(getOwnerId());
			// обновляем автора
			item.setAutor(owner.getName());
			// устанавливаем итем в ячейку
			empty.setItem(item);

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
	public void addMoney(long count)
	{
		if(count < 1)
			return;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// получаем текущие деньги
			ItemInstance old = gold.getItem();

			// если деньги есть
			if(old != null)
				// просто увеличиваем их кол-во
				old.addItemCount(count);
			else
			{
				// получаем таблицу итемов
				ItemTable itemTable = ItemTable.getInstance();

				// создаем экземпляр денег
				old = itemTable.getItem(Inventory.MONEY_ITEM_ID).newInstance();

				// если не удалось создать итем, выходим
				if(old == null)
				{
					log.warning(this, new Exception("not created money item."));
					return;
				}

				// устанавливаем нужное кол-во
				old.setItemCount(count);
				// обновляем владельца итема
				old.setOwnerId(getOwnerId());

				// вставляем в ячейку
				gold.setItem(old);
			}

			// обновляем в базе итем
			dbManager.updateItem(old);
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void finalyze()
	{
		// олучаем ячейки банка
		Cell[] cells = getCells();

		// перебираем ячейки банка
		for(int i = 0, length = cells.length; i < length; i++)
		{
			// получаем ячейку
			Cell cell = cells[i];

			// получаем итем с ячейки
			ItemInstance item = cell.getItem();

			// если итем есть
			if(item != null)
				// удаляем итем
				item.deleteMe();

			// обнуляем ячейку
			cell.setItem(null);
		}

		// получаем деньги
		ItemInstance item = gold.getItem();

		// если деньги есть
		if(item != null)
			// удаляем их
			item.deleteMe();

		// обнуляем ячейку
		gold.setItem(null);

		// обнуляем владельца
		setOwner(null);
	}

	@Override
	public Cell getCell(int index)
	{
		if(index < 0 || index >= cells.length)
			return null;

		return cells[index];
	}

	@Override
	public Cell[] getCells()
	{
		return cells;
	}

	@Override
	public ItemInstance getItemForObjectId(int objectId)
	{
		lock();
		try
		{
			// получаем набор ячеяк банка
			Cell[] cells = getCells();

			// перебираем их
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// есди она пустая, пропускаем
				if(cell.isEmpty())
					continue;

				// получаем итем в ячейке
				ItemInstance item = cell.getItem();

				// если это искомый итем
				if(item.getObjectId() == objectId)
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
	public int getLastIndex()
	{
		lock();
		try
		{
			// получаем ячейки банка
			Cell[] cells = getCells();

			// получаем индекс последней ячейки
			int last = cells.length - 1;

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
	public long getMoney()
	{
		return gold.getItemCount();
	}

	@Override
	public T getOwner()
	{
		return owner;
	}

	/**
	 * @return ид владельца итемов в этом банке.
	 */
	protected int getOwnerId()
	{
		return 0;
	}

	@Override
	public int getTabSize()
	{
		return 72;
	}

	@Override
	public int getUsedCount()
	{
		lock();
		try
		{
			// создаем счетчик
			int counter = 0;

			// получаем ячейки банка
			Cell[] cells = getCells();

			// мчитаем занятые ячейки
			for(int i = 0, length = cells.length; i < length; i++)
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
	public void lock()
	{
		lock.lock();
	}

	@Override
	public boolean putItem(ItemInstance item)
	{
		if(item == null)
			return false;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// получаем доступные ячейки
			Cell[] cells = getCells();

			//если нужна будет свободная ячейка
			Cell empty = null;

			// если итем стопкуем
			if(item.isStackable())
			{
				// перебираем ячейки
				for(int i = 0, length = cells.length; i < length; i++)
				{
					// получаем ячейку
					Cell cell = cells[i];

					// если ячейка свободная
					if(cell.isEmpty())
					{
						// и свободной у нас еще нет
						if(empty == null)
							// запоминаем ее
							empty = cell;

						continue;
					}

					// получаем иттем в ячейке
					ItemInstance old = cell.getItem();

					// если нужный ид у итема
					if(old.getItemId() == item.getItemId())
					{
						// добавляем в стопку новых итемов
						old.addItemCount(item.getItemCount());

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
				for(int i = 0, length = cells.length; i < length; i++)
				{
					// получаем ячейку
					Cell cell = cells[i];

					// если нашли пустую
					if(cell.isEmpty() && empty == null)
					{
						// запоминаем
						empty = cell;
						break;
					}
				}
			}

			// если есть пустая ячейка
			if(empty != null)
			{
				// указываем владельца итема.
				item.setOwnerId(getOwnerId());

				// вставляем в ячейку
				empty.setItem(item);

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
	public void reinit(){}

	@Override
	public boolean removeItem(int itemId, int itemCount)
	{
		if(itemCount < 1)
			return false;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// получаем доступные ячейки
			Cell[] cells = getCells();

			// перебираем ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// если пустая, пррпускаем
				if(cell.isEmpty())
					continue;

				// достаем итем из ячейки
				ItemInstance item = cell.getItem();

				// если это искомый
				if(item.getItemId() == itemId)
				{
					// и есть нужное кол-во для удаления
					if(item.getItemCount() > itemCount)
					{
						// отнимаем
						item.subItemCount(itemCount);

						// обновляем в БД
						dbManager.updateDataItem(item);

						return true;
					}
					// есди в стопке ровно столько итемов, сколько надо удалить
					else if(item.getItemCount() == itemCount)
					{
						// удаляем итем с ячейки
						cell.setItem(null);

						// зануляем владельца
						item.setOwnerId(0);
						// зануляем счетчик
						item.setItemCount(0);

						// обновляем в БД
						dbManager.updateItem(item);

						// удаляем с мира
						item.deleteMe();

						return true;
					}

					return false;
				}
			}

			return false;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean removeItem(ItemInstance item)
	{
		if(item == null)
			return false;

		lock();
		try
		{
			// получаем доступные ячейки
			Cell[] cells = getCells();

			// перебираекм все ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// если ячейка пустаяЮ пррпускаем
				if(cell.isEmpty())
					continue;

				// вынимаем итем из ячейки
				ItemInstance old = cell.getItem();

				// если это искомый итем
				if(old == item)
				{
					// итем удаляем и выходим
					cell.setItem(null);

					return true;
				}
			}

			return false;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public boolean setItem(int index, ItemInstance item)
	{
		// если индекс некорректен, выходим
		if(index < -1 || index >= cells.length)
			return false;

		lock();
		try
		{
			Cell cell = null;

			// получаем ячейку, куда хотим вставить
			if(index == -1)
				cell = gold;
			else
				cell = cells[index];

			// если она заняты, выходим
			if(!cell.isEmpty())
				return false;

			// применяем итем в ячейку
			cell.setItem(item);

			return true;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void setOwner(Object owner)
	{
		this.owner = (T) owner;
	}

	@Override
	public boolean sort()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// получаем ячейки банка
			Cell[] cells = getCells();

			// получаем индекс последней занятой ячейки
			int last = getLastIndex();

			// если банк пуст ,выходим
			if(last < 1)
				return false;

			// флаг отсортированности
			boolean sorted = true;

			// проверяем, есть ли пустые ячейки между итемами
			for(int i = last - 1; i >= 0; i--)
				if(cells[i].isEmpty())
				{
					sorted = false;
					break;
				}

			// если нету, выходим
			if(sorted)
				return false;

			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем буферный список итемов
			Array<ItemInstance> items = local.getNextItemList();

			// перебираем ячейки
			for(int i = 0, length = cells.length; i < length; i++)
			{
				// получаем ячейку
				Cell cell = cells[i];

				// если она пуста, пропускаем
				if(cell.isEmpty())
					continue;

				// ложим итем с ячейки в список
				items.add(cell.getItem());

				// опусташаем ячейку
				cell.setItem(null);
			}

			// получаем массив итемов
			ItemInstance[] array = items.array();

			// перебираем итемы
			for(int i = 0, g = 0, length = items.size(); i < length; i++)
			{
				// получаем итем
				ItemInstance item = array[i];

				// ложим его в ячейку
				cells[g++].setItem(item);

				// обновляем место итема в БД
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
	public void subMoney(long count)
	{
		if(count < 1)
			return;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// получаем итем с деньгами
			ItemInstance item = gold.getItem();

			// если его нет, выходим
			if(item == null)
				return;

			// уменьшаем кол-во итемов
			item.subItemCount(Math.min(count, item.getItemCount()));

			// обновляем в БД
			dbManager.updateDataItem(gold.getItem());
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void unlock()
	{
		lock.unlock();
	}
}
