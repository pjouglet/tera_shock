package tera.gameserver.model.territory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Перечисление видов территорий.
 *
 * @author Ronn
 * @created 09.03.2012
 */
public enum TerritoryType
{
	CAMP_TERRITORY(BonfireTerritory.class),
	PEACE_TERRITORY(PeaceTerritory.class),
	BATTLE_TERRITORY(BattleTerritory.class),
	LOCAL_TERRITORY(LocalTerritory.class),
	CLIMB_TERRITORY(ClimbTerritory.class),
	REGION_TERRITORY(RegionTerritory.class),
	;

	private static final Logger log = Loggers.getLogger(TerritoryType.class);

	/** конструктор территории */
	private Constructor<? extends Territory> constructor;

	private TerritoryType(Class<? extends Territory> type)
	{
		try
		{
			constructor = type.getConstructor(Node.class, TerritoryType.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
		}
	}

	/**
	 * Создание экземпляра территории.
	 *
	 * @param node хмл описание территории.
	 * @return новая территория.
	 */
	public Territory newInstance(Node node)
	{
		try
		{
			return constructor.newInstance(node, this);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			log.warning(e);
		}

		throw new IllegalArgumentException("Incorrect creating " + this);
	}
}
