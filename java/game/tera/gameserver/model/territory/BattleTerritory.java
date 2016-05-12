package tera.gameserver.model.territory;

import org.w3c.dom.Node;

/**
 * Модель боевой территории.
 * 
 * @author Ronn
 */
public class BattleTerritory extends AbstractTerritory
{
	public BattleTerritory(Node node, TerritoryType type)
	{
		super(node, type);
	}
}
