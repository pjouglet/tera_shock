package tera.gameserver.model.listeners;

import tera.gameserver.model.TObject;
import tera.gameserver.model.territory.Territory;

/**
 * Прослушиватель входа/выхода объектов на территорию.
 * 
 * @author Ronn
 */
public interface TerritoryListener
{
	/**
	 * Событие входа объекта на территорию.
	 * 
	 * @param territory территория.
	 * @param object вошедший объект.
	 */
	public void onEnter(Territory territory, TObject object);
	
	/**
	 * Событие выхода из территории объекта.
	 * 
	 * @param territory территория.
	 * @param object вышедший объект с территории.
	 */
	public void onExit(Territory territory, TObject object);
}
