package tera.gameserver.model;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель покупаемого итема.
 * 
 * @author Ronn
 */
public final class BuyableItem implements Foldable
{
	/** пул не используемых итемов */
	private static final FoldablePool<BuyableItem> pool = Pools.newConcurrentFoldablePool(BuyableItem.class);
	
	/**
	 * Создание нового экземпляра оболочки продаваемого итема.
	 * 
	 * @param item продаваемый итем.
	 * @param count кол-во продаваемых итемов.
	 * @return новая оболочка.
	 */
	public static final BuyableItem newInstance(ItemTemplate item, long count)
	{
		BuyableItem buy = pool.take();
		
		if(buy == null)
			buy = new BuyableItem();

		buy.item = item;
		buy.count = count;
		
		return buy;
	}
	
	/** покупаемый итем */
	private ItemTemplate item;
	
	/** кол-во */
	private long count;

	/**
	 * @param count добавленное кол-во итемов.
	 */
	public void addCount(long count)
	{
		this.count += count;
	}
	
	@Override
	public void finalyze()
	{
		item = null;
	}

	/**
	 * Положить в пул.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return итоговая цена на итемы
	 */
	public long getBuyPrice()
	{
		return (long) (count * item.getBuyPrice() * Config.WORLD_SHOP_PRICE_MOD);
	}

	/**
	 * @return кол-во покупаемых итемов.
	 */
	public long getCount()
	{
		return count;
	}
	
	/**
	 * @return покупаемый итем.
	 */
	public ItemTemplate getItem()
	{
		return item;
	}
	
	/**
	 * @return ид покупаемого итема.
	 */
	public int getItemId()
	{
		return item.getItemId();
	}
	
	@Override
	public void reinit(){}
	
	/**
	 * @param count кол-во покупаемых итемов.
	 */
	public void setCount(int count)
	{
		this.count = count;
	}

	/**
	 * @param item покупаемый итем.
	 */
	public void setItem(ItemTemplate item)
	{
		this.item = item;
	}

	/**
	 * @param count кол-во на которое нужно уменьшить покупаемых итемов.
	 */
	public void subCount(int count)
	{
		this.count -= count;
	}
}
