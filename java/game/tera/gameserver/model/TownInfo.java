package tera.gameserver.model;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.util.Location;

/**
 * Модель описывающая город.
 *
 * @author Ronn
 */
public final class TownInfo
{
	/** название города */
	private final String name;

	/** информация для системы пегасов */
	private final PegasInfo pegasInfo;

	/** центр города */
	private final Location center;

	/** ид города */
	private final int id;
	/** ид зоны, в которой ноходится город */
	private final int zone;

	public TownInfo(Node node)
	{
		// созаем твблизу атрибутов
		VarTable set = VarTable.newInstance(node);

		// применяем параметры
		this.name = set.getString("name");
		this.id = set.getInteger("id");
		this.zone = set.getInteger("zone");

		int continentId = set.getInteger("continentId", 0);

		// инфа для пегасов
		PegasInfo pegasInfo = null;

		// центр города
		Location center = null;

		for(Node nd = node.getFirstChild(); nd != null; nd = nd.getNextSibling())
		{
			if("pegas".equals(nd.getNodeName()))
				pegasInfo = new PegasInfo(nd, continentId);
			else if("center".equals(nd.getNodeName()))
			{
				// парсим атрибуты
				set.parse(nd);

				float x = set.getFloat("x");
				float y = set.getFloat("y");
				float z = set.getFloat("z");

				center = new Location(x, y, z, 0, continentId);
			}
		}

		this.pegasInfo = pegasInfo;
		this.center = center;
	}

	/**
	 * @return координаты центра города.
	 */
	public Location getCenter()
	{
		return center;
	}

	/**
	 * @return ид города.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return точка посадки.
	 */
	public final Location getLanding()
	{
		return pegasInfo.getLanding();
	}

	/**
	 * @return время локального палета.
	 */
	public final int getLocal()
	{
		return pegasInfo.getLocal();
	}

	/**
	 * @return название города.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return информацияд ля пегасов.
	 */
	public PegasInfo getPegasInfo()
	{
		return pegasInfo;
	}

	/**
	 * @return точка портала.
	 */
	public final Location getPortal()
	{
		return pegasInfo.getPortal();
	}

	/**
	 * @return время полета к порталу.
	 */
	public final int getToLanding()
	{
		return pegasInfo.getToLanding();
	}

	/**
	 * @return время полета к порталу.
	 */
	public final int getToPortal()
	{
		return pegasInfo.getToPortal();
	}

	/**
	 * @return ид зоны города.
	 */
	public int getZone()
	{
		return zone;
	}

	@Override
	public String toString()
	{
		return "TownInfo  name = " + name + ", id = " + id + ", zone = " + zone;
	}
}
