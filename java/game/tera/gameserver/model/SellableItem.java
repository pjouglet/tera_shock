package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;

/**
 * Модель покупаемого итема.
 * 
 * @author Ronn
 */
public final class SellableItem implements Foldable
{
	/** пул не используемых итемов */
	private static final FoldablePool<SellableItem> pool = Pools.newConcurrentFoldablePool(SellableItem.class);
	
	/**
	 * Создание нового экземпляра оболочки продаваемого итема.
	 * 
	 * @param item продаваемый итем.
	 * @param inventory инвентапь.
	 * @param count кол-во продаваемых итемов.
	 * @return новая оболочка.
	 */
	public static final SellableItem newInstance(ItemInstance item, Inventory inventory, long count)
	{
		SellableItem sell = pool.take();
		
		if(sell == null)
			sell = new SellableItem(item, inventory, count);
		else
		{
			sell.item = item;
			sell.inventory = inventory;
			sell.count = count;
		}
		
		return sell;
	}
	
	/** покупаемый итем */
	private ItemInstance item;
	/** инвентарь */
	private Inventory inventory;
	/** кол-во */
	private long count;

	/**
	 * @param item покупаемый итем.
	 * @param inventory инвентарь.
	 * @param count кол-во покупаемого итема.
	 */
	private SellableItem(ItemInstance item, Inventory inventory, long count)
	{
		this.item = item;
		this.inventory = inventory;
		this.count = count;
	}

	/**
	 * @param count добавленное кол-во итемов.
	 */
	public void addCount(long count)
	{
		this.count += count;
	}
	
	/**
	 * @return все ли хорошо с этим итемом.
	 */
	public boolean check()
	{
		return inventory != null && inventory.getCellForObjectId(item.getObjectId()) != null;
	}
	
	/**
	 * @return удаляет продаваемые итемы
	 */
	public void deleteItem()
	{
		inventory.lock();
		try
		{
			Cell cell = inventory.getCellForObjectId(item.getObjectId());
			
			inventory.removeItemFromIndex(count, cell.getIndex());
		}
		finally
		{
			inventory.unlock();
		}
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(this == object || item == object)
			return true;
		
		return false;
	}

	@Override
	public void finalyze()
	{
		inventory = null;
		item = null;
		count = 0;
	}

	/**
	 * Положить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}
	
	/**
	 * @return кол-во продаваемых итемов.
	 */
	public long getCount()
	{
		return count;
	}

	/**
	 * @return продаваемый итем.
	 */
	public ItemInstance getItem()
	{
		return item;
	}
	
	/**
	 * @return ид продаваемого итема.
	 */
	public int getItemId()
	{
		return item.getItemId();
	}
	
	/**
	 * @return уникальный ид итема.
	 */
	public int getObjectId()
	{
		return item.getObjectId();
	}

	/**
	 * @return итоговая цена на итемы
	 */
	public long getSellPrice()
	{
		return (long) (count * item.getSellPrice() * Config.WORLD_SHOP_PRICE_MOD);
	}
	
	@Override
	public void reinit(){}

	/**
	 * @param count кол-во продаваемых итемов.
	 */
	public void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * @param item продаваемый итем.
	 */
	public void setItem(ItemInstance item)
	{
		this.item = item;
	}

	/**
	 * @param count кол-во на которое нужно уменьшить продаваемых итемов.
	 */
	public void subCount(int count)
	{
		this.count -= count;
	}
}
