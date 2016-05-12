package tera.gameserver.model;

import tera.gameserver.model.territory.LocalTerritory;

/**
 * Модель региона, куда можно телепортнуться от НПС.
 * 
 * @author Ronn
 */
public final class TeleportRegion
{
	/** регион */
	private LocalTerritory region;
	
	/** цена телепортации */
	private int price;
	/** индекс точки */
	private int index;

	public TeleportRegion(LocalTerritory region, int price, int index)
	{
		this.region = region;
		this.price = price;
		this.index = index;
	}

	/**
	 * @return индекс точки.
	 */
	public final int getIndex()
	{
		return index;
	}

	/**
	 * @return цена телепорта.
	 */
	public final int getPrice()
	{
		return price;
	}

	/**
	 * @return регион для телепорта.
	 */
	public final LocalTerritory getRegion()
	{
		return region;
	}

	/**
	 * @param index индекс точки.
	 */
	public final void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * @param price цена телепорта.
	 */
	public final void setPrice(int price)
	{
		this.price = price;
	}

	/**
	 * @param region регион для телепорта.
	 */
	public final void setRegion(LocalTerritory region)
	{
		this.region = region;
	}
}
