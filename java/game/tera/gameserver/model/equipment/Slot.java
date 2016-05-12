package tera.gameserver.model.equipment;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;

/**
 * Модель слота экиперовки.
 * 
 * @author Ronn
 */
public final class Slot
{
	/** тип слота */
	private SlotType type;
	
	/** итем */
	private ItemInstance item;
	
	/** индекс */
	private int index;
	
	/**
	 * @param type тип слота.
	 * @param index индекс слота.
	 */
	public Slot(SlotType type, int index)
	{
		this.type = type;
		this.index = index;
	}

	/**
	 * @return индекс слота.
	 */
	public int getIndex()
	{
		return index;
	}
	
	/**
	 * @return итем в слоте.
	 */
	public ItemInstance getItem()
	{
		return item;
	}
	
	/**
	 * @return тип слота.
	 */
	public SlotType getType()
	{
		return type;
	}
	
	/**
	 * @return свободен ли слот.
	 */
	public boolean isEmpty()
	{
		return item == null;
	}
	
	/**
	 * @param index индекс слота.
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * @param newItem итем.
	 */
	public void setItem(ItemInstance newItem)
	{
		item = newItem;
		
		if(item != null)
		{
			item.setLocation(ItemLocation.EQUIPMENT);
			item.setIndex(index);
		}
	}

	/**
	 * @param type тип слота.
	 */
	public void setType(SlotType type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return "Slot  " + (type != null ? "type = " + type + ", " : "") + (item != null ? "item = " + item + ", " : "") + "index = " + index;
	}
}
