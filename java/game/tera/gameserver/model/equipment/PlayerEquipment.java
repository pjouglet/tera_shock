package tera.gameserver.model.equipment;

import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.Character;
import tera.gameserver.model.items.ItemInstance;

/**
 * Модель экиперовки игроков.
 *
 * @author Ronn
 */
public final class PlayerEquipment extends AbstractEquipment
{
	/** пул экиперовок */
	private static FoldablePool<Equipment> pool = Pools.newConcurrentFoldablePool(Equipment.class);

	/** структура экиперовки игрока */
	private static final SlotType[] STRUCTURE =
	{
		SlotType.SLOT_WEAPON,
		SlotType.SLOT_SHIRT,
		SlotType.SLOT_ARMOR,
		SlotType.SLOT_GLOVES,
		SlotType.SLOT_BOOTS,

		SlotType.SLOT_EARRING,
		SlotType.SLOT_EARRING,
		SlotType.SLOT_RING,
		SlotType.SLOT_RING,
		SlotType.SLOT_NECKLACE,
		SlotType.SLOT_SHIRT,
		SlotType.SLOT_HAT,
		SlotType.SLOT_MASK,
	};

	/**
	 * @param owner владелец экиперовки.
	 * @return новая экиперовка игрока.
	 */
	public static final Equipment newInstance(Character owner)
	{
		Equipment equipment = pool.take();

		if(equipment == null)
			equipment = new PlayerEquipment(owner);

		equipment.setOwner(owner);

		return equipment;
	}

	public PlayerEquipment(Character owner)
	{
		super(owner);
	}

	@Override
	public boolean equiped(ItemInstance item)
	{
		if(item == null || owner == null)
			return false;

		if(owner.isBattleStanced())
		{
			owner.sendMessage("Нельзя одевать вещь в боевой стойке.");
			return false;
		}

		return item.equipmentd(owner, true);
	}

	@Override
	public void fold()
	{
		pool.put(this);
	}

	@Override
	public void prepare()
	{
		recreateSlots(STRUCTURE);
	}

	@Override
	public PlayerEquipment setOwner(Character owner)
	{
		super.setOwner(owner);

		return this;
	}

	@Override
	public boolean unequiped(ItemInstance item)
	{
		if(item == null || owner == null)
			return false;

		if(owner.isBattleStanced())
		{
			owner.sendMessage("Нельзя снимать вещь в боевой стойке.");
			return false;
		}

		return true;
	}
}
