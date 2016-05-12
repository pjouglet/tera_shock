package tera.gameserver.model.playable;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.base.Experience;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.model.territory.TerritoryType;
import tera.gameserver.tables.TerritoryTable;
import tera.gameserver.templates.CharTemplate;

/**
 * Модель играбельных персонажей.
 *
 * @author Ronn
 */
public abstract class Playable extends Character
{
	/** текущие территории */
	protected final Array<Territory> territories;

	/** экиперовка */
	protected Equipment equipment;
	/** инвентарь */
	protected Inventory inventory;
	/** банк */
	protected Bank bank;

	/** текущий лвл */
	protected int level;
	/** текущая зона игрока */
	protected int zoneId;
	/** ид фракции */
	protected int fractionId;

	/** кол-во набраннойэкспы */
	protected volatile int exp;

	/**
	 * @param objectId
	 * @param template
	 */
	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);

		this.level = 1;
		this.exp = Experience.LEVEL[1];
		this.territories = Arrays.toConcurrentArray(Territory.class, 2);
	}

	@Override
	public void deleteMe()
	{
		if(isDeleted())
			return;

		// получаем таблицу территорий
		TerritoryTable territoryTable = TerritoryTable.getInstance();

		// выходим из всех территорий
		territoryTable.onExitWorld(this);

		// очищаем список територий
		territories.clear();

		super.deleteMe();

		// складиуем эффект лист
		effectList.fold();
	}

	@Override
	public Bank getBank()
	{
		return bank;
	}

	@Override
	public final Equipment getEquipment()
	{
		return equipment;
	}

	/**
	 * @return ид фракции.
	 */
	public final int getFractionId()
	{
		return fractionId;
	}

	@Override
	public final Inventory getInventory()
	{
		return inventory;
	}

	@Override
	public final Array<Territory> getTerritories()
	{
		return territories;
	}

	/**
	 * Получение территории нужного типа, в которой находится игрок.
	 *
	 * @param type тип территории.
	 * @return искомая территория.
	 */
	public <T extends Territory> T getTerritory(Class<T> type)
	{
		Array<Territory> territories = getTerritories();

		if(territories.isEmpty())
			return null;

		territories.readLock();
		try
		{
			Territory[] array = territories.array();

			for(int i = 0, length = territories.size(); i < length; i++)
			{
				Territory territory = array[i];

				if(type.isInstance(territory))
					return type.cast(territory);
			}

			return null;
		}
		finally
		{
			territories.readUnlock();
		}
	}

	/**
	 * @return the zoneId
	 */
	public final int getZoneId()
	{
		return zoneId;
	}

	/**
	 * @return имеет ли игрок премиум аккаунт.
	 */
	public boolean hasPremium()
	{
		return false;
	}

	@Override
	public boolean isInBattleTerritory()
	{
		if(territories == null || territories.isEmpty())
			return false;

		territories.readLock();
		try
		{
			Territory[] array = territories.array();

			for(int i = 0, length = territories.size(); i < length; i++)
				if(array[i].getType() == TerritoryType.BATTLE_TERRITORY)
					return true;

			return false;
		}
		finally
		{
			territories.readUnlock();
		}
	}

	@Override
	public boolean isInBonfireTerritory()
	{
		if(territories == null || territories.isEmpty())
			return false;

		territories.readLock();
		try
		{
			Territory[] array = territories.array();

			for(int i = 0, length = territories.size(); i < length; i++)
				if(array[i].getType() == TerritoryType.CAMP_TERRITORY)
					return true;

			return false;
		}
		finally
		{
			territories.readUnlock();
		}
	}

	@Override
	public boolean isInPeaceTerritory()
	{
		if(territories == null || territories.isEmpty())
			return false;

		territories.readLock();
		try
		{
			Territory[] array = territories.array();

			for(int i = 0, length = territories.size(); i < length; i++)
				if(array[i].getType() == TerritoryType.PEACE_TERRITORY)
					return true;

			return false;
		}
		finally
		{
			territories.readUnlock();
		}
	}

	/**
	 * @param bank личный банк.
	 */
	public void setBank(Bank bank)
	{
		this.bank = bank;
	}

	@Override
	public final void setEquipment(Equipment equipment)
	{
		this.equipment = equipment;
	}

	/**
	 * @param fractionId ид фракции.
	 */
	public final void setFractionId(int fractionId)
	{
		this.fractionId = fractionId;
	}

	@Override
	public final void setInventory(Inventory inventory)
	{
		this.inventory = inventory;
	}

	@Override
	public final void setTerritories(Array<Territory> territories)
	{
		this.territories.addAll(territories);
	}

	/**
	 * @param zoneId the zoneId to set
	 */
	public final void setZoneId(int zoneId)
	{
		this.zoneId = zoneId;
	}

	@Override
	public void updateTerritories()
	{
		territories.writeLock();
		try
		{
			//если в территориях есть территория
			if(!territories.isEmpty())
			{
				Territory[] array = territories.array();

				//запускаем перебор территорий
				for(int i = 0, length = territories.size(); i < length; i++)
				{
					Territory territory = array[i];

					//если территоря нулл или я в ней нахожусь, то далее
					if(territory.contains(x, y, z))
						continue;

					//если я не нахожусь в территории, то выходим из нее
					territory.onExit(this);

					//удаляем территорию
					territories.fastRemove(i--);
					length--;
				}
			}

			//если текущий регион нулл, то выходим
			if(currentRegion == null)
				return;

			//получаем список возможных территррий
			Territory[] news = currentRegion.getTerritories();

			//если список не пуст
			if(news != null && news.length > 0)
			{
				//Перебераем территории
				for(int i = 0, length = news.length; i < length; i++)
				{
					Territory territory = news[i];

					//если территория уже у нас есть, то далее
					if(territories.contains(territory))
						continue;

					//если в территорию мы не входим, то далее
					if(!territory.contains(x, y, z))
						continue;

					//входим
					territory.onEnter(this);
					//добавляем
					territories.add(territory);
				}
			}
		}
		finally
		{
			territories.writeUnlock();
		}
	}

	@Override
	public void updateZoneId()
	{
		new Exception("update zone id").printStackTrace();

		// получаем текущий регион
		WorldRegion region = getCurrentRegion();

		// если такого нету
		if(region == null)
			// ищем его по координатам
			region = World.getRegion(this);

		// получаем ид локации
		int newZoneId = region.getZoneId(this);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// если ид определн и он новый
		if(newZoneId != -1 && newZoneId != zoneId)
		{
			// применяем
			setZoneId(newZoneId);

			// обновляем
			eventManager.notifyChangedZoneId(this);
		}
	}
}
