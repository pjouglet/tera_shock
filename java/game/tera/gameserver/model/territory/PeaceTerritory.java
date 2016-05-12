package tera.gameserver.model.territory;

import org.w3c.dom.Node;

import tera.gameserver.model.TObject;


/**
 * Модель мирной территории.
 *
 * @author Ronn
 * @created 26.03.2012
 */
public class PeaceTerritory extends AbstractTerritory
{
	public PeaceTerritory(Node node, TerritoryType type)
	{
		super(node, type);
	}

	@Override
	public void onEnter(TObject object)
	{
		super.onEnter(object);
		
		//если объект игрок, то уведовляем его о входе
		if(object.isPlayer())
			object.getPlayer().sendMessage("You're now in a peaceful zone.");
	}

	@Override
	public void onExit(TObject object)
	{
		super.onExit(object);
		
		//если объект игрок, то уведовляем его о выходе
		if(object.isPlayer())
			object.getPlayer().sendMessage("You came out of the peaceful zone.");
	}
}
