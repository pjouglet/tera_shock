package tera.gameserver.model.items;

import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.inventory.Cell;

/**
 * Модель списка вставленных кристалов в итем.
 *
 * @author Ronn
 */
public final class CrystalList
{
	/** список вставленных кристалов */
	private Array<CrystalInstance> crystals;

	/** ид итема, чьи кристалы */
	private int objectId;

	/** максимальное кол-во криталов */
	private int max;

	/**
	 * @param max максимальное кол-во вставляемых кристалов.
	 * @param objectId ид итема, чьи кристалы.
	 */
	public CrystalList(int max, int objectId)
	{
		this.max = max;
		this.objectId = objectId;
		this.crystals = Arrays.toConcurrentArray(CrystalInstance.class, max);
	}

	/**
	 * Добавление функций персонажу.
	 *
	 * @param owner персонаж.
	 */
	public void addFuncs(Character owner)
	{
		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		crystals.readLock();
		try
		{
			// получаем список критсалов
			CrystalInstance[] array = crystals.array();

			// применяем функции кристалов к персонажу
			for(int i = 0, length = crystals.size(); i < length; i++)
				array[i].addFuncsTo(owner);
		}
		finally
		{
			crystals.readUnlock();
		}
	}

	/**
	 * Проверка на содержание в итеме кристала с указаннам стэк типом.
	 *
	 * @param stackType стэк тип.
	 * @return содержит ли итем такой кристал.
	 */
	public boolean containsCrystal(StackType stackType)
	{
		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		// если вставленных кристалов нет, выходим
		if(crystals.isEmpty())
			return false;

		crystals.readLock();
		try
		{
			// список вставленных кристалов
			CrystalInstance[] array = crystals.array();

			// ищем с одинаковым типом
			for(int i = 0, length = crystals.size(); i < length; i++)
				if(array[i].getStackType() == stackType)
					return true;

			return false;
		}
		finally
		{
			crystals.readUnlock();
		}
	}

	/**
	 * @return расчет разрушения кристалов.
	 */
	public boolean destruction(Character owner)
	{
		// кол-во разрушенных итемов
		int counter = 0;

		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		crystals.writeLock();
		try
		{
			// перебираем кристалы
			for(int i = 0, length = crystals.size(); i < length; i++)
				// если шанс разрушения срабатывает
				if(Rnd.chance(Config.WORLD_CHANCE_DELETE_CRYSTAL)) //owner.isPK() ||
				{
					// получаем разрушаемый кристал
					CrystalInstance crystal = crystals.get(i);

					// удаляем его из мписка.
					crystals.fastRemove(i--);

					// удаляем его функции с персонажа
					crystal.removeFuncsTo(owner);

					// обнуляем владельца
					crystal.setOwnerId(0);

					// обновляем в БД
					dbManager.updateLocationItem(crystal);

					// отображаем удаление итема
					PacketManager.showDeleteItem(owner, crystal);

					// уменьшаем список
					length--;

					//увеличиваем счетчик
					counter++;
				}

			return counter > 0;
		}
		finally
		{
			crystals.writeUnlock();
		}
	}

	/**
	 * Финализация списка.
	 */
	public void finalyze()
	{
		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		// если кристалов нет, выходим
		if(crystals.isEmpty())
			return;

		crystals.readLock();
		try
		{
			// получаем масив кристалов
			CrystalInstance[] array = crystals.array();

			// удаляем с мира вставленные кристалы
			for(int i = 0, length = crystals.size(); i < length; i++)
				array[i].deleteMe();
		}
		finally
		{
			crystals.readUnlock();
		}

		// очищаем список
		crystals.clear();
	}

	/**
	 * Получение кристала по ид.
	 *
	 * @param itemId итем ид кристала.
	 * @return кристал.
	 */
	public CrystalInstance getCrystal(int itemId)
	{
		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		// если список пуст, выходим
		if(crystals.isEmpty())
			return null;

		crystals.readLock();
		try
		{
			// получаем массив кристалов
			CrystalInstance[] array = crystals.array();

			//перебираем кристалы
			for(int i = 0, length = crystals.size(); i < length; i++)
			{
				CrystalInstance crystal = array[i];

				// если кристал с нужным темплейт ид
				if(crystal.getItemId() == itemId)
					return crystal;
			}

			return null;
		}
		finally
		{
			crystals.readUnlock();
		}
	}

	/**
	 * @return массив кристалов.
	 */
	public CrystalInstance[] getArray()
	{
		return crystals.array();
	}

	/**
	 * @return список кристалов.
	 */
	public Array<CrystalInstance> getCrystals()
	{
		return crystals;
	}

	/**
	 * @return есть ли доступный слот.
	 */
	public boolean hasEmptySlot()
	{
		return crystals.size() < max;
	}

	/**
	 * @return пустой ли список.
	 */
	public boolean isEmpty()
	{
		return crystals.isEmpty();
	}

	/**
	 * Вставка кристала в итем.
	 *
	 * @param crystal вставляемый кристал.
	 * @param cell ячейка, в которой лежал кристал.
	 * @param owner персонаж владелец итема.
	 */
	public void put(CrystalInstance crystal, Cell cell, Character owner)
	{
		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		crystals.writeLock();
		try
		{
			// если есть место под кристал
			if(crystals.size() < max)
			{
				CrystalInstance target = null;

				// если в ячейке кристалов много
				if(crystal.getItemCount() < 2)
				{
					target = crystal;

					if(cell != null)
						cell.setItem(null);
				}
				else
				{
					// создаем новый экземпляр 1 кристала
					target = (CrystalInstance) crystal.getTemplate().newInstance();

					if(target == null)
						return;

					// отнимаем 1 итем у старого
					crystal.subItemCount(1);

					// обновляем старый кристал.
					dbManager.updateDataItem(crystal);
				}

				// устанавливаем нового владельца
				target.setOwnerId(objectId);

				// обновляем позицию
				target.setLocation(ItemLocation.CRYSTAL);

				target.setIndex(0);

				if(owner != null)
					target.addFuncsTo(owner);

				// обновляем кристал в БД
				dbManager.updateLocationItem(target);

				// одабавляем в список
				crystals.add(target);
			}
		}
		finally
		{
			crystals.writeUnlock();
		}
	}

	/**
	 * @param crystal удаление кристала.
	 */
	public void remove(CrystalInstance crystal)
	{
		crystals.fastRemove(crystal);
	}

	/**
	 * Удаление функций персонажу.
	 *
	 * @param owner персонаж.
	 */
	public void removeFuncs(Character owner)
	{
		// получаем список кристалов
		Array<CrystalInstance> crystals = getCrystals();

		crystals.readLock();
		try
		{
			CrystalInstance[] array = crystals.array();

			// удаляем функции для указанного персонажа
			for(int i = 0, length = crystals.size(); i < length; i++)
				array[i].removeFuncsTo(owner);
		}
		finally
		{
			crystals.readUnlock();
		}
	}

	/**
	 * @param objectId обджект ид владельца списка.
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @return кол-во вставленных кристалов.
	 */
	public int size()
	{
		return crystals.size();
	}
}
