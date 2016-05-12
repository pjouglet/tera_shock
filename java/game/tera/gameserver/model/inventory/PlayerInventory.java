package tera.gameserver.model.inventory;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;

/**
 * Модель инвенторя игрока.
 *
 * @author Ronn
 */
public final class PlayerInventory extends AbstractInventory
{
	/** пул инвенторей */
	private static final FoldablePool<Inventory> inventoryPool = Pools.newConcurrentFoldablePool(Inventory.class);

	/** пул массивов ячеяк инвенторей */
	private static final Array<Cell[]>[] cellPool = Arrays.create(Array.class, 200);

	// инициализация пула ячеяк
	static
	{
		for(int i = 0; i < cellPool.length; i++)
		{
			Array<Cell[]> cells = Arrays.toConcurrentArray(Cell[].class);
			cellPool[i] = cells;
		}
	}

	/**
	 * @return новый инвентарь.
	 */
	public static Inventory newInstance(Character owner)
	{
		Inventory inventory = newInstance(owner, 1);

		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// создаем запись о инвенторе в БД
		dbManager.createInventory(owner, inventory);

		return inventory;
	}

	/**
	 * Создание нового инвенторя.
	 *
	 * @param level уровень инвенторя.
	 * @return новый инвентарь.
	 */
	public static final Inventory newInstance(Character owner, int level)
	{
		// получаем старый инвентарь из пула
		AbstractInventory inventory = (AbstractInventory) inventoryPool.take();

		// если его нету
		if(inventory == null)
			// создаем новый
			inventory = new PlayerInventory(owner, level);
		// иначе переинициализируем старый
		else
		{
			// применяем уровень
			inventory.level = level;
			// применяем владельца
			inventory.owner = owner;

			// получаем его размер
			int size = inventory.getAllMaxCells();

			// получаем с пула масив ячеяк для инвенторя
			Cell[] cells = cellPool[size].pop();

			// если больше нет в пуле их
			if(cells == null)
			{
				// создаем новые
				cells = new Cell[size];

				for(int i = 0; i < size; i++)
					cells[i] = new Cell(i, ItemLocation.INVENTORY);
			}

			// применяем массив ячеяк к инвенторю
			inventory.cells = cells;
		}

		// готовый инвентарь для нового владельца
		return inventory;
	}

	/**
	 * @param level уровень инвенторя.
	 */
	public PlayerInventory(Character owner, int level)
	{
		super(level);

		this.owner = owner;
	}

	@Override
	public void addLevel()
	{
		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// старый набор ячеяк
			Cell[] old = cells;

			super.addLevel();

			// создаем новый набор ячеяк
			cells = new Cell[getAllMaxCells()];

			// заполняем новый массив ячейками
			for(int i = 0, length = cells.length; i < length; i++)
				cells[i] = new Cell(i, ItemLocation.INVENTORY);

			// переносим в новый набор итемы
			for(int i = 0, length = old.length; i < length; i++)
				cells[i].setItem(old[i].getItem());

			// очищаем старый набор
			for(int i = 0, length = old.length; i < length; i++)
				old[i].setItem(null);

			// вносим в пул старый набор
			cellPool[old.length].add(old);

			dbManager.updateInventory(owner, this);
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void finalyze()
	{
		// размер инвенторя
		int size = cells.length;

		//перебирем ячейки
		for(int i = 0; i < size; i++)
		{
			Cell cell = cells[i];

			// получаем инвентарь с ячейки
			ItemInstance item = cell.getItem();

			if(item == null)
				continue;

			// удаляем итем с мира
			item.deleteMe();
			// очищаем ячейку
			cell.setItem(null);
		}

		// ложим с пул массив ячеяк
		cellPool[size].add(cells);
		// забываем ячейки этого инвенторя
		cells = null;

		// очищаем ячейку с деньгами
		if(!gold.isEmpty())
		{
			ItemInstance item = gold.getItem();

			// удаляем из мира
			item.deleteMe();
			// зануляем ячейку
			gold.setItem(null);
		}

		owner = null;
	}

	@Override
	public void fold()
	{
		// ложим в пул инвентарь
		inventoryPool.put(this);
	}

	@Override
	public int getAllMaxCells()
	{
		// масимальное клл-во доступных ячеяк в массиве
		return getMaxCells() + 8;
	}

	@Override
	public int getBaseLevel()
	{
		return 1;
	}

	@Override
	public int getLevelBonus()
	{
		return 8;
	}

	@Override
	public int getMaxCells()
	{
		return 48 + level * getLevelBonus();
	}

	@Override
	public void reinit(){}

	@Override
	public void subLevel()
	{
		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		lock();
		try
		{
			// старый набор ячеяк
			Cell[] old = cells;

			super.subLevel();

			// создаем новый набор ячеяк
			cells = new Cell[getAllMaxCells()];

			// заполняем новый массив ячейками
			for(int i = 0, length = cells.length; i < length; i++)
				cells[i] = new Cell(i, ItemLocation.INVENTORY);

			// переносим в новый набор итемы
			for(int i = 0, length = cells.length; i < length; i++)
				cells[i].setItem(old[i].getItem());

			// очищаем старый набор
			for(int i = 0, length = old.length; i < length; i++)
			{
				ItemInstance item = old[i].getItem();

				if(item != null)
					item.deleteMe();

				old[i].setItem(null);
			}

			// вносим в пул старый набор
			cellPool[old.length].add(old);

			dbManager.updateInventory(owner, this);
		}
		finally
		{
			unlock();
		}
	}
}