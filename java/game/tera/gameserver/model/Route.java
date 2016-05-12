package tera.gameserver.model;

/**
 * Модель маршрута полета на пегасе.
 *
 * @author Ronn
 * @created 26.02.2012
 */
public final class Route
{
	/** целевой город */
	private TownInfo target;
	
	/** код маршрута */
	private int index;
	/** цена маршрута */
	private int price;
	
	/** короткий ли перелет */
	private boolean local;

	/**
	 * @param index номер маршрута.
	 * @param price цена маршрута.
	 * @param target целевой город.
	 * @param local является ли локальным.
	 */
	public Route(int index, int price, TownInfo target, boolean local)
	{
		this.index = index;
		this.price = price;
		this.target = target;
		this.local = local;
	}

	/**
	 * @return индекс маршрута.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return цена маршрута.
	 */
	public int getPrice()
	{
		return price;
	}

	/**
	 * @return целевой город.
	 */
	public TownInfo getTarget()
	{
		return target;
	}

	/**
	 * @return короткий ли перелет.
	 */
	public final boolean isLocal()
	{
		return local;
	}

	@Override
	public String toString()
	{
		return "Route index = " + index + ", price = " + price + ", target = " + target;
	}
}
