package tera.util;

import rlib.geom.GamePoint;
import rlib.logging.Loggers;

/**
 * Класс описывающий точку в пространстве в мире Tera
 *
 * @author Ronn
 */
public final class Location implements GamePoint
{
	protected static final float HEADINGS_IN_PI = 10430.378350470452724949566316381F;

	/**
	 * Парсит строку в виде "1,2,3" в локацию
	 *
	 * @param string строковое представление позиции.
	 * @return новая позиция.
	 */
	public static Location valueOf(String string)
	{
		if(string == null)
			return new Location();

		String[] coords = string.split(",");

		if(coords.length < 3)
			return null;

		try
		{
			Location newLoc = new Location(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]), Float.parseFloat(coords[2]));

			if(coords.length > 3)
				newLoc.setHeading(Integer.parseInt(coords[3]));

			return newLoc;
		}
		catch(NumberFormatException e)
		{
			Loggers.warning("Location", e);
		}

		return null;
	}
	/** координата х */
	private float x;
	/** координата у */
	private float y;

	/** координата z */
	private float z;

	/** разворот */
	private int heading;

	/** ид континента */
	private int continentId;

	public Location()
	{
		this(0f, 0f, 0f, 0);
	}

	public Location(float x, float y, float z)
	{
		this(x, y, z, 0);
	}

	public Location(float x, float y, float z, int heading)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}

	public Location(float x, float y, float z, int heading, int continentId)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.continentId = continentId;
	}

	public Location(float[] points, int heading)
	{
		x = points[0];
		y = points[1];
		z = points[2];

		this.heading = heading;
	}

	public Location(Location loc)
	{
		this(loc.x, loc.y, loc.z, loc.heading);
	}

	/**
	 * Расчет разворота в указанные координаты.
	 *
	 * @param targetX целевая координата.
	 * @param targetY целевая координата.
	 * @return нужный разворот.
	 */
	public final int calcHeading(float targetX, float targetY)
	{
		return (int) (Math.atan2(y - targetY, x - targetX) * HEADINGS_IN_PI) + 32768;
	}

	/**
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @return соответствуют ли координаты позиции.
	 */
	public boolean equals(float x, float y, float z)
	{
		return this.x == x && this.y == y && this.z == z;
	}

	/**
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param h целевой разворот.
	 * @return соответствуют ли координаты позиции.
	 */
	public boolean equals(float x, float y, float z, int heading)
	{
		return this.x == x && this.y == y && this.z == z && this.heading == heading;
	}

	/**
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @return соответствуют ли координаты позиции.
	 */
	public boolean equals(int x, int y, int z)
	{
		return this.x == x && this.y == y && this.z == z;
	}

	/**
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param h целевой разворот.
	 * @return соответствуют ли координаты позиции.
	 */
	public boolean equals(int x, int y, int z, int heading)
	{
		return this.x == x && this.y == y && this.z == z && this.heading == heading;
	}

	/**
	 * @param loc целевая позиция.
	 * @return соотвествует ли эта позиция целевой.
	 */
	public boolean equals(Location loc)
	{
		return loc.x == x && loc.y == y && loc.z == z;
	}

	/**
	 * @return ид континента.
	 */
	public int getContinentId()
	{
		return continentId;
	}

	@Override
	public int getHeading()
	{
		return heading;
	}

	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public float getZ()
	{
		return z;
	}

	/**
	 * @return пустая ли позиция.
	 */
	public boolean isNull()
	{
		return x == 0f || y == 0f || z == 0f;
	}

	/**
	 * Установка новой позиции.
	 *
	 * @param locaction новая позиция.
	 */
	public void set(Location loc)
	{
		x = loc.x;
		y = loc.y;
		z = loc.z;

		heading = loc.heading;
	}

	/**
	 * @param continentId ид континента.
	 */
	public void setContinentId(int continentId)
	{
		this.continentId = continentId;
	}

	@Override
	public Location setHeading(int heading)
	{
		this.heading = heading;

		return this;
	}

	@Override
	public Location setX(float x)
	{
		this.x = x;

		return this;
	}

	@Override
	public Location setXYZ(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}

	@Override
	public Location setXYZH(float x, float y, float z, int heading)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;

		return this;
	}

	public Location setXYZHC(float x, float y, float z, int heading, int continentId)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.continentId = continentId;

		return this;
	}

	@Override
	public Location setY(float y)
	{
		this.y = y;

		return this;
	}

	@Override
	public Location setZ(float z)
	{
		this.z = z;

		return this;
	}

	@Override
	public final String toString()
	{
		return "x = " + x + ", y = " + y + ", z = " + z + ", continentId = " + continentId + ", heading = " + heading;
	}
}