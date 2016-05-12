package tera.gameserver.model;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.util.Location;

/**
 * Модель информации для системы пигасов, необходимых для ее работы.
 *
 * @author Ronn
 */
public final class PegasInfo
{
	/** координаты портала */
	private final Location portal;
	/** координаты точки посадки */
	private final Location landing;

	/** время полета от портала до посадки */
	private final int toLanding;
	/** время полета от взлета до портала */
	private final int toPortal;
	/** время локального перелета */
	private final int local;

	public PegasInfo(Node node, int continentId)
	{
		this.landing = new Location();
		this.portal = new Location();

		VarTable vars = VarTable.newInstance(node);

		this.toLanding = vars.getInteger("landing");
		this.toPortal = vars.getInteger("portal");
		this.local = vars.getInteger("local");

		for(Node nd = node.getFirstChild(); nd != null; nd = nd.getNextSibling())
			if(nd.getNodeType() == Node.ELEMENT_NODE)
			{
				vars.parse(nd);

				float x = vars.getFloat("x");
				float y = vars.getFloat("y");
				float z = vars.getFloat("z");

				if("portal".equals(nd.getNodeName()))
				{
					// обновляем координаты
					portal.setXYZ(x, y, z);
					// устанавливаем ид континент а
					portal.setContinentId(continentId);
				}
				else if("landing".equals(nd.getNodeName()))
				{
					// обновляем координаты
					landing.setXYZ(x, y, z);
					// устанавливаем ид континент а
					landing.setContinentId(continentId);
				}
			}
	}

	/**
	 * @return точка посадки.
	 */
	public final Location getLanding()
	{
		return landing;
	}

	/**
	 * @return время локального палета.
	 */
	public final int getLocal()
	{
		return local;
	}

	/**
	 * @return точка портала.
	 */
	public final Location getPortal()
	{
		return portal;
	}

	/**
	 * @return время полета из портала.
	 */
	public final int getToLanding()
	{
		return toLanding;
	}

	/**
	 * @return время полета к порталу.
	 */
	public final int getToPortal()
	{
		return toPortal;
	}
}
