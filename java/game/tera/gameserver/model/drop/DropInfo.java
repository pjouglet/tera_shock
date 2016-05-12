package tera.gameserver.model.drop;

import tera.gameserver.templates.ItemTemplate;

/**
 * Модель данных о дропе.
 * 
 * @author Ronn
 */
public final class DropInfo
{
	/** темплейт итема */
	private ItemTemplate item;
	
	/** минимальное кол-во выпадших итемов */
	private int minCount;
	/** максимальное кол-во выпадших итемов */
	private int maxCount;
	/** шанс выпасти итем */
	private int chance;
	
	/**
	 * @param item выпадающий итем.
	 * @param minCount минимальное кол-во.
	 * @param maxCount максимальное кол-во.
	 * @param chance шанс дропа.
	 */
	public DropInfo(ItemTemplate item, int minCount, int maxCount, int chance)
	{
		super();
		this.item = item;
		this.minCount = minCount;
		this.maxCount = maxCount;
		this.chance = chance;
	}

	/**
	 * @return шанс дропа.
	 */
	public final int getChance()
	{
		return chance;
	}
	
	/**
	 * @return выпадающий итем.
	 */
	public final ItemTemplate getItem()
	{
		return item;
	}

	/**
	 * @return ид выпадающего итема.
	 */
	public final int getItemId()
	{
		return item.getItemId();
	}

	/**
	 * @return максимальное кол-во выпадающих итемов.
	 */
	public final int getMaxCount()
	{
		return maxCount;
	}

	/**
	 * @return минимальное кол-во выпадающих итемов.
	 */
	public final int getMinCount()
	{
		return minCount;
	}
}
