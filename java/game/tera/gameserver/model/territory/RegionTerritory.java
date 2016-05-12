package tera.gameserver.model.territory;

import org.w3c.dom.Node;

import rlib.util.array.Array;
import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionState;
import tera.gameserver.model.Guild;
import tera.gameserver.model.TObject;
import tera.gameserver.model.WorldRegion;
import tera.gameserver.model.playable.Player;

/**
 * Модель региональной территории.
 *
 * @author Ronn
 */
public class RegionTerritory extends AbstractTerritory
{
	/** контролирующий регион */
	private Region region;

	public RegionTerritory(Node node, TerritoryType type)
	{
		super(node, type);
	}

	/**
	 * Получение всех нужных объектов в рамках территории.
	 *
	 * @param container контейнер объектов.
	 * @return список объектов.
	 */
	public <T extends TObject> Array<T> getObjects(Array<T> container, Class<T> type)
	{
		WorldRegion[] regions = getRegions();

		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addObjects(container, type);

		return container;
	}

	/**
	 * @return владеющий регион.
	 */
	public Region getRegion()
	{
		return region;
	}

	/**
	 * @param region контролирующий регион.
	 */
	public void setRegion(Region region)
	{
		this.region = region;
	}

	@Override
	public void onEnter(TObject object)
	{
		// если объект не игрок, выходим
		if(!object.isPlayer())
			return;

		// получаем игрока
		Player player = object.getPlayer();

		// получаем регион
		Region region = getRegion();

		// если региона нет или он в неподходящем состоянии, выходим
		if(region == null || region.getState() != RegionState.PREPARE_START_WAR)
			return;

		// получаем владельца региона
		Guild owner = region.getOwner();

		// если его нет
		if(owner == null)
			player.sendMessage("You entered in neutral region.");
		// если игрок относится к владельцам
		else if(owner == player.getGuild())
		{
			player.sendMessage("You're in your region.");
			region.addFuncsTo(player);
		}
		// елси игрок чужак
		else
		{
			player.sendMessage("You're in a region dominated by \"" + owner.getName() + "\".");
			region.addFuncsTo(player);
		}
	}

	@Override
	public void onExit(TObject object)
	{
		// если объект не игрок, выходим
		if(!object.isPlayer())
			return;

		// получаем игрока
		Player player = object.getPlayer();

		// получаем регион
		Region region = getRegion();

		// если региона нет или он в неподходящем состоянии, выходим
		if(region == null || region.getState() != RegionState.PREPARE_START_WAR)
			return;

		// получаем владельца региона
		Guild owner = region.getOwner();

		// если его нет
		if(owner == null)
			player.sendMessage("You came out the neutral region.");
		// если игрок относится к владельцам
		else if(owner == player.getGuild())
		{
			player.sendMessage("You went out of your region.");
			region.removeFuncsTo(player);
		}
		// елси игрок чужак
		else
		{
			player.sendMessage("You came out the region dominated by \"" + owner.getName() + "\".");
			region.removeFuncsTo(player);
		}
	}
}
