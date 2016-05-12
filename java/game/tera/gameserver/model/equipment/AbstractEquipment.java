package tera.gameserver.model.equipment;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.CrystalInstance;
import tera.gameserver.model.items.CrystalList;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;

/**
 * Фундаментальная модель экиперовки.
 *
 * @author Ronn
 */
public abstract class AbstractEquipment implements Equipment
{
	/** пустой массив слотов */
	protected static final Slot[] EMPTY_SLOTS = new Slot[0];

	/** блокировщик */
	protected final Lock lock;

	/** массив слотов */
	private Slot[] slots;

	/** владелец экиперовки */
	protected Character owner;

	/**
	 * @param owner владелец экиперовки.
	 */
	public AbstractEquipment(Character owner)
	{
		this.lock = Locks.newLock();
		this.owner = owner;

		prepare();
	}

	@Override
	public boolean dressItem(Inventory inventory, Cell cell)
	{
		if(cell == null)
			return false;

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем все слоты
		Slot[] slots = getSlots();

		// получаем владельца экиперовки
		Character owner = getOwner();

		inventory.lock();
		try
		{
			lock();
			try
			{
				// если ячейка пуста, выходим
				if(cell.isEmpty())
					return false;

				// получаем итем из ячейки
				ItemInstance item = cell.getItem();

				// если итем кристал
				if(item.isCrystal())
				{
					CrystalInstance crystal = item.getCrystal();

					// подготавливаем целевой итем
					ItemInstance target = null;

					// список кристалов итема
					CrystalList list = null;

					// перебираем слоты
					for(int i = 0, length = slots.length; i < length; i++)
					{
						// получаем слот
						Slot slot = slots[i];

						// пустой пропускаем
						if(slot.isEmpty())
							continue;

						// получаем итем из слота
						item = slot.getItem();

						// получаем его список кристалов
						list = item.getCrystals();

						// пропускаем итемы, без списка кристалов
						if(list == null)
							continue;

						// если уже такого тип кристала вставлен
						if(crystal.isNoStack() && list.containsCrystal(crystal.getStackType()))
							return false;

						// запоминаем подходящий итем
						if(target == null && item.checkCrystal(crystal))
							target = item;
					}

					// если не нашли подходящий итем
					if(target == null)
						owner.sendMessage(MessageType.ALL_CRYSTAL_SLOTS_ARE_FULL);
					else
					{
						// получаем его список критсалов
						list = target.getCrystals();

						// вставляем туда кристал
						list.put(crystal, cell, owner);

						// обновляем инвентарь
						eventManager.notifyInventoryChanged(owner);

						// обновляем статы
						eventManager.notifyStatChanged(owner);

						return true;
					}

					return false;
				}

				// если итем нельзя экиперовать, выходим
				if(item.getSlotType() == SlotType.NONE)
					return false;

				// если нельзя сейчас его экиперовать, тоже выходим
				if(!equiped(item))
					return false;

				// заготавливаем пустой слот
				Slot empty = null;

				// заготавливаем занятый однотипный слот
				Slot emplayed = null;

				// перебираем все слоты
				for(int i = 0, length = slots.length; i < length; i++)
				{
					// получаем слот
					Slot slot = slots[i];

					// если слот подходит по типу
					if(slot.getType() == item.getSlotType())
					{
						// если он занят, запоминаем
						if(!slot.isEmpty())
							emplayed = slot;
						else
						{
							// если свободен, выходим и запоминаем
							empty = slot;
							break;
						}
					}
				}

				// если есть свободный слот
				if(empty != null)
				{
					// добавляем туда итем
					empty.setItem(item);

					// опусташаем ячейку
					cell.setItem(null);

					// добавляем бонусы
					item.addFuncsTo(owner);

					// обновляем итем в БД
					dbManager.updateLocationItem(item);

					// обновляем экиперовку
					eventManager.notifyEquipmentChanged(owner);

					// обновляем статы
					eventManager.notifyStatChanged(owner);

					return true;
				}
				// если найден занятый однотипный
				else if(emplayed != null)
				{
					// получаем уже экиперованный итем
					ItemInstance old = emplayed.getItem();

					// удаляем его бонусы
					old.removeFuncsTo(owner);

					// ложим его в ячейку
					cell.setItem(old);

					// экиперуем новый итем
					emplayed.setItem(item);

					// добавляем его бонусы
					item.addFuncsTo(owner);

					// обновляем снятый итем в БД
					dbManager.updateLocationItem(old);

					// обновляем одетый итем в БД
					dbManager.updateLocationItem(item);

					// обновляем экиперовку
					eventManager.notifyEquipmentChanged(owner);

					// обновляем статы
					eventManager.notifyStatChanged(owner);

					return true;
				}

				return false;
			}
			finally
			{
				unlock();
			}
		}
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public void finalyze()
	{
		// получаем все слоты
		Slot[] slots = getSlots();

		// получаем владельца экиперови
		Character owner = getOwner();

		// перебираем слоты
		for(int i = 0, length = slots.length; i < length; i++)
		{
			// получаем слот
			Slot slot = slots[i];

			// пустой пропускаем
			if(slot.isEmpty())
				continue;

			// получаем итем в слоте
			ItemInstance item = slot.getItem();

			// удаляем функции итема
			item.removeFuncsTo(owner);

			// зануляем уникальный ид
			item.setObjectId(-1);

			// удаляем итем
			item.deleteMe();

			// зануляем слот
			slot.setItem(null);
		}

		// зануляем владельца
		setOwner(null);
	}

	@Override
	public int getCount(SlotType type)
	{
		// объявляем счетчик
		int counter = 0;

		// получаем слоты
		Slot[] slots = getSlots();

		lock();
		try
		{
			// перебираем слоты
			for(int i = 0, length = slots.length; i < length; i++)
			{
				// получаем слот
				Slot slot = slots[i];


				//если слот с указанны индексом, увеличиваем счетчик
				if(!slot.isEmpty() && slot.getType() == type)
					counter++;
			}

			return counter;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public int getEngagedSlots()
	{
		// объявляем счетчик
		int counter = 0;

		// получаем слоты
		Slot[] slots = getSlots();

		lock();
		try
		{
			// подсчет занятых слотов
			for(int i = 0, length = slots.length; i < length; i++)
				if(!slots[i].isEmpty())
					counter++;

			return counter;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public ItemInstance getItem(int index)
	{
		// получаем слоты
		Slot[] slots = getSlots();

		// если индекс не корректный, выходим
		if(index < 0 || index >= slots.length)
			return null;

		// получаем нужный слот
		Slot slot = slots[index];

		// возвращаем его итем
		return slot == null? null : slot.getItem();
	}

	@Override
	public ItemInstance getItem(SlotType type)
	{
		// получаем слоты
		Slot[] slots = getSlots();

		lock();
		try
		{
			// перебираем слоты
			for(int i = 0, length = slots.length; i < length; i++)
			{
				// получаем слот
				Slot slot = slots[i];

				// если это искомый слот
				if(slot.getType() == type && !slot.isEmpty())
					// возвращаем его итем
					return slot.getItem();
			}

			return null;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public int getItemId(SlotType type)
	{
		// получаем итем с нужным слотом
		ItemInstance item = getItem(type);

		// возврааем его ид
		return item == null? 0 : item.getItemId();
	}

	@Override
	public Character getOwner()
	{
		return owner;
	}

	@Override
	public Slot getSlotForObjectId(int objectId)
	{
		// получаем слоты
		Slot[] slots = getSlots();

		lock();
		try
		{
			// перебираем слоты
			for(int i = 0, length = slots.length; i < length; i++)
			{
				// получаем слот
				Slot slot = slots[i];

				// пустой пропускаем
				if(slot.isEmpty())
					continue;

				// получаем итем в слоте
				ItemInstance item = slot.getItem();

				// если это искомый итем, возвращаем слот
				if(item.getObjectId() == objectId)
					return slot;
			}

			return null;
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public Slot[] getSlots()
	{
		return slots;
	}

	@Override
	public void lock()
	{
		lock.lock();
	}

	/**
	 * Создание набора слотов.
	 */
	protected abstract void prepare();

	@Override
	public void recreateSlots(SlotType... slotTypes)
	{
		lock();
		try
		{
			slots = new Slot[slotTypes.length];

			for(int i = 0; i < slots.length; i++)
				slots[i] = new Slot(slotTypes[i], i);
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void reinit(){}

	@Override
	public boolean setItem(ItemInstance item, int index)
	{
		if(item == null)
			return false;

		// устанавливаем итем в слот
		slots[index].setItem(item);

		// выдаем функции итема владельцу
		item.addFuncsTo(owner);

		return true;
	}

	@Override
	public Equipment setOwner(Character owner)
	{
		this.owner = owner;

		return this;
	}

	@Override
	public void setSlots(Slot[] slots)
	{
		this.slots = slots;
	}

	@Override
	public boolean shootItem(Inventory inventory, int index, int itemId)
	{
		// получаем слоты
		Slot[] slots = getSlots();

		// если индекс не корректен, выходим
		if(index < 0 || index >= slots.length)
			return false;

		// получаем владельца экиперовки
		Character owner = getOwner();

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		inventory.lock();
		try
		{
			lock();
			try
			{
				// получаем целевой слот
				Slot slot = slots[index];

				// получаем итем в слоте
				ItemInstance item = slot.getItem();

				// если итема там нет, выходим
				if(item == null)
					return false;

				// если ид с итемом не совпадает, скорее всего это кристал
				if(item.getItemId() != itemId)
				{
					// получаем список кристалов итема
					CrystalList crystals = item.getCrystals();

					// если кристалов у итема быть не может, выходим
					if(crystals == null)
						return false;

					// получаем кристал с нужным ид
					CrystalInstance target = crystals.getCrystal(itemId);

					// если такого нет или его сейчас нельзя снять, выходим
					if(target == null || !unequiped(target))
						return false;

					// если положить итем в инвентарь не удалось
					if(!inventory.putItem(target))
						// сообщаем об этом
						owner.sendMessage(MessageType.INVENTORY_IS_FULL);
					else
					{
						// удаляем кристал из списка кристалов
						crystals.remove(target);

						// удаляем функции кристала у владельца
						target.removeFuncsTo(owner);

						// если этот кристал просто приплюсовался в инвенторе, значит экземпляр нужно удалить
						if(target.getLocation() == ItemLocation.CRYSTAL)
						{
							// зануляем владельца
							target.setOwnerId(0);

							// обновляем в БД
							dbManager.updateLocationItem(target);

							// удаляем кристал из мира
							target.deleteMe();
						}
					}

					// обновляем экиперовку
					eventManager.notifyEquipmentChanged(owner);

					// обновляем статы
					eventManager.notifyStatChanged(owner);
				}
				else
				{
					// если итем снять нельзя, выходим
					if(!unequiped(item))
						return false;

					// если не получилось положить в инвентарь
					if(!inventory.putItem(item))
						// сообщаем
						owner.sendMessage(MessageType.INVENTORY_IS_FULL);
					else
					{
						// опусташаем слот
						slot.setItem(null);

						// удаляем его бонусы
						item.removeFuncsTo(owner);

						// обновляем экиперовку
						eventManager.notifyEquipmentChanged(owner);

						// обновляем статы
						eventManager.notifyStatChanged(owner);

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
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public int size()
	{
		return slots.length;
	}

	@Override
	public String toString()
	{
		return "Equipment  " + (slots != null ? "slots = " + Arrays.toString(slots) + ", " : "");
	}

	@Override
	public void unlock()
	{
		lock.unlock();
	}
}
