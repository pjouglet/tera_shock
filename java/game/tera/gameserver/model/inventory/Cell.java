package tera.gameserver.model.inventory;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;

/**
 * Модель ячейки инвенторя.
 *
 * @author Ronn
 */
public final class Cell implements Comparable<Cell>
{
	/** итем ячейки */
	private ItemInstance item;

	/** местоположение ячейки */
	private ItemLocation location;

	/** номер ячейки */
	private int index;

	/**
	 * @param index номер ячейки.
	 * @param location местоположение ячейки.
	 */
	public Cell(int index, ItemLocation location)
	{
		this.index = index;
		this.location = location;
	}

	@Override
	public int compareTo(Cell cell)
	{
		return (item == null? 1 : 0) - (cell.getItem() == null? 1 : 0);
	}

	/**
	 * @return номер ячейки.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return итем в ячейке.
	 */
	public ItemInstance getItem()
	{
		return item;
	}

	/**
	 * @return кол-во итемов в ячейке.
	 */
	public long getItemCount()
	{
		return item == null? 0 : item.getItemCount();
	}

	/**
	 * @return ид темплейта итема.
	 */
	public int getItemId()
	{
		return item == null? 0 : item.getItemId();
	}

	/**
	 * @return пустая ли ячейка.
	 */
	public boolean isEmpty()
	{
		return item == null;
	}

	/**
	 * @param index номер ячейки.
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * @param item итем.
	 */
	public void setItem(ItemInstance item)
	{
		this.item = item;

		if(item != null)
		{
			item.setLocation(location);
			item.setIndex(index);
		}
	}

	@Override
	public String toString()
	{
		return "index = " + index + ", item = " + item;
	}
}
