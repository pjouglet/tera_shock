package tera.gameserver.model.territory;

import java.awt.Polygon;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.TObject;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.listeners.TerritoryListener;

/**
 * Базовая модель територии.
 *
 * @author Ronn
 */
public abstract class AbstractTerritory implements Territory
{
	protected static final Logger log = Loggers.getLogger(Territory.class);

	/** ид территории */
	protected final int id;

	/** ид континента */
	protected final int continentId;

	/** ширина территории по х */
	protected int minimumX;
	protected int maximumX;
	/** ширина территории по у */
	protected int minimumY;
	protected int maximumY;
	/** высота территории по z */
	protected int minimumZ;
	protected int maximumZ;

	/** название территории */
	protected final String name;

	/** полигон территории, описывающий ее границы */
	protected final Polygon territory;
	/** тип территории */
	protected final TerritoryType type;

	/** список слушателей */
	protected final Array<TerritoryListener> listeners;
	/** список объектов на территории */
	protected final Array<TObject> objects;

	/** список связанных регионов */
	protected WorldRegion[] regions;

	public AbstractTerritory(Node node, TerritoryType type)
	{
		try
		{
			// созаем твблизу атрибутов
			VarTable vars = VarTable.newInstance(node);

			this.id = vars.getInteger("id");
			this.continentId = vars.getInteger("continentId", 0);
			this.maximumZ = vars.getInteger("maxZ");
			this.minimumZ = vars.getInteger("minZ");
			this.name = vars.getString("name");

			this.type = type;

			this.minimumX = Integer.MAX_VALUE;
			this.maximumX = Integer.MIN_VALUE;
			this.minimumY = Integer.MAX_VALUE;
			this.maximumY = Integer.MIN_VALUE;

			this.territory = new Polygon();

			this.listeners = Arrays.toConcurrentArray(TerritoryListener.class);
			this.objects = Arrays.toConcurrentArray(TObject.class);

			// парсим точки территории
			for(Node point = node.getFirstChild(); point != null; point = point.getNextSibling())
				if("point".equals(point.getNodeName()))
				{
					// парсим атрибуты точки
					vars.parse(point);
					// добавляем точку
					addPoint((int) vars.getFloat("x"), (int) vars.getFloat("y"));
				}
		}
		catch(Exception e)
		{
			log.warning(e);
			throw e;
		}
	}

	@Override
	public final void addListener(TerritoryListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public final void addPoint(int x, int y)
	{
		// обновляем мин/макс х
		minimumX = Math.min(minimumX, x);
		maximumX = Math.max(maximumX, x);

		// обновляем мин/макс у
		minimumY = Math.min(minimumY, y);
		maximumY = Math.max(maximumY, y);

		// добавляем новую точку
		territory.addPoint(x, y);
	}

	@Override
	public final boolean contains(float x, float y)
	{
		return territory.contains(x, y);
	}

	@Override
	public final boolean contains(float x, float y, float z)
	{
		if(z > maximumZ || z < minimumZ)
			return false;

		return contains(x, y);
	}

	@Override
	public int getContinentId()
	{
		return continentId;
	}

	@Override
	public final int getId()
	{
		return id;
	}

	@Override
	public final int getMaximumX()
	{
		return maximumX;
	}

	@Override
	public final int getMaximumY()
	{
		return maximumY;
	}

	@Override
	public final int getMaximumZ()
	{
		return maximumZ;
	}

	@Override
	public final int getMinimumX()
	{
		return minimumX;
	}

	@Override
	public final int getMinimumY()
	{
		return minimumY;
	}

	@Override
	public final int getMinimumZ()
	{
		return minimumZ;
	}

	@Override
	public final String getName()
	{
		return name;
	}

	@Override
	public Array<TObject> getObjects()
	{
		return objects;
	}

	@Override
	public WorldRegion[] getRegions()
	{
		return regions;
	}

	@Override
	public final TerritoryType getType()
	{
		return type;
	}

	@Override
	public final int hashCode()
	{
		return id;
	}

	@Override
	public void onEnter(TObject object)
	{
		// добавляем в список объектов
		objects.add(object);

		// если слушателей нет, выходим
		if(listeners.isEmpty())
			return;

		listeners.readLock();
		try
		{
			TerritoryListener[] array = listeners.array();

			// перебираем слушателей
			for(int i = 0, length = listeners.size(); i < length; i++)
				array[i].onEnter(this, object);
		}
		finally
		{
			listeners.readUnlock();
		}
	}

	@Override
	public void onExit(TObject object)
	{
		// удаление объекта из списка объектов на территории
		objects.fastRemove(object);

		// если слушателей нет, выходим
		if(listeners.isEmpty())
			return;

		listeners.readLock();
		try
		{
			TerritoryListener[] array = listeners.array();

			// перебираем слушателей
			for(int i = 0, length = listeners.size(); i < length; i++)
				array[i].onExit(this, object);
		}
		finally
		{
			listeners.readUnlock();
		}
	}

	@Override
	public final void removeListener(TerritoryListener listener)
	{
		listeners.fastRemove(listener);
	}

	@Override
	public void setRegions(WorldRegion[] regions)
	{
		this.regions = regions;
	}

	@Override
	public String toString()
	{
		return "Territory id = " + id + ", " + "type = " + type;
	}
}
