package tera.gameserver.model.territory;

import rlib.util.array.Array;
import tera.gameserver.model.TObject;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.listeners.TerritoryListener;

/**
 * Интерфейс для реализации территорий.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public interface Territory
{
	/**
	 * @param listener добавляемый слушатель.
	 */
	public void addListener(TerritoryListener listener);

	/**
	 * Добавляет точку в полигон.
	 *
	 * @param x координата грани территории.
	 * @param y координата грани территории.
	 */
	public void addPoint(int x, int y);

	/**
	 * @param x координата объекта.
	 * @param y координата объекта.
	 * @return находится ли в территории.
	 */
	public boolean contains(float x, float y);

	/**
	 * @param x координата объекта.
	 * @param y координата объекта.
	 * @param z координата объекта.
	 * @return находится ли в территории.
	 */
	public boolean contains(float x, float y, float z);

	/**
	 * @return ид континента.
	 */
	public int getContinentId();

	/**
	 * @return ид территории.
	 */
	public int getId();

	/**
	 * @return максимальная х координата.
	 */
	public int getMaximumX();

	/**
	 * @return максимальная у координата.
	 */
	public int getMaximumY();

	/**
	 * @return максимальная z координата.
	 */
	public int getMaximumZ();

	/**
	 * @return минимальная x координата.
	 */
	public int getMinimumX();

	/**
	 * @return минимальная y координата.
	 */
	public int getMinimumY();

	/**
	 * @return минимальная z  координата.
	 */
	public int getMinimumZ();

	/**
	 * @return название территории.
	 */
	public String getName();

	/**
	 * @return список объектов на территории.
	 */
	public Array<TObject> getObjects();

	/**
	 * @return список связаных регионов.
	 */
	public WorldRegion[] getRegions();

	/**
	 * @return тип территории.
	 */
	public TerritoryType getType();

	/**
	 * Обработка входа объекта в зону.
	 *
	 * @param object входимый объект.
	 */
	public void onEnter(TObject object);

	/**
	 * Обработка выхода объекта из зоны.
	 *
	 * @param object выходимый объект.
	 */
	public void onExit(TObject object);

	/**
	 * @param listener удаляемый слушатель.
	 */
	public void removeListener(TerritoryListener listener);

	/**
	 * @param regions список связаных регионов.
	 */
	public void setRegions(WorldRegion[] regions);
}
