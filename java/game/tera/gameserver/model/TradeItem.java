package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.items.ItemInstance;

/**
 * Обертка передаваемого итема.
 * 
 * @author Ronn
 */
public final class TradeItem implements Foldable
{
	private static final FoldablePool<TradeItem> pool = Pools.newConcurrentFoldablePool(TradeItem.class);
	
	public static final TradeItem newInstance(ItemInstance item, long count)
	{
		TradeItem tradeItem = pool.take();
		
		if(tradeItem == null)
			tradeItem = new TradeItem();
		
		tradeItem.item = item;
		tradeItem.count = count;
		
		return tradeItem;
	}
	
	/** передаваемый предмет */
	private ItemInstance item;
	
	/** кол-во передаваемых вещей */
	private long count;
	
	private TradeItem()
	{
		super();
	}
	
	/**
	 * Добавление кол-во передаваемых итемов.
	 * 
	 * @param count кол-во передаваемых итемов.
	 */
	public void addCount(long count)
	{
		this.count += count;
	}
	
	@Override
	public boolean equals(Object object)
	{
		return item == object;
	}

	@Override
	public void finalyze()
	{
		item = null;
		count = 0;
	}
	
	public void fold()
	{
		pool.put(this);
	}
	
	/**
	 * @return кол-во передаваемых итемов.
	 */
	public long getCount()
	{
		return count;
	}
	
	/**
	 * @return передаваемый итем.
	 */
	public ItemInstance getItem()
	{
		return item;
	}

	/**
	 * @return итем ид итема.
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
	 * @return стакуемый ли итем.
	 */
	public boolean isStackable()
	{
		return item.isStackable();
	}

	@Override
	public void reinit(){}
	
	/**
	 * Отнимание кол-во передаваемых итемов.
	 * 
	 * @param count кол-во передаваемых итемов.
	 */
	public void subCount(long count)
	{
		this.count -= count;
	}
}
