package tera.gameserver.model.territory;

import org.w3c.dom.Node;

import rlib.util.VarTable;

/**
 * Модель региона, в которои надо залазить куда-то.
 *
 * @author Ronn
 */
public class ClimbTerritory extends AbstractTerritory
{
	/** целевая точка спуска */
	private float targetX;
	private float targetY;
	private float targetZ;

	public ClimbTerritory(Node node, TerritoryType type)
	{
		super(node, type);

		VarTable vars = VarTable.newInstance(node);

		this.targetX = vars.getFloat("targetX");
		this.targetY = vars.getFloat("targetY");
		this.targetZ = vars.getFloat("targetZ");
	}

	/**
	 * @return целевая точка.
	 */
	public final float getTargetX()
	{
		return targetX;
	}

	/**
	 * @return целевая точка.
	 */
	public final float getTargetY()
	{
		return targetY;
	}

	/**
	 * @return целевая точка.
	 */
	public final float getTargetZ()
	{
		return targetZ;
	}
}
