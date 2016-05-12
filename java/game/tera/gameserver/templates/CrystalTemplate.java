package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.model.items.CrystalType;
import tera.gameserver.model.items.StackType;

/**
 * Модель шаблона кристала.
 *
 * @author Ronn
 */
public class CrystalTemplate extends ItemTemplate
{
	/** тип стыковки кристалов */
	private StackType stackType;

	/** запрещено ли больше 1 однотипного кристала */
	private boolean noStack;

	public CrystalTemplate(CrystalType type, VarTable vars)
	{
		super(type, vars);

		try
		{
			stackType = StackType.valueOfXml(vars.getString("stackType", "none"));
			noStack = vars.getBoolean("noStack", true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int getClassIdItemSkill()
	{
		return -10;
	}

	/**
	 * @return тип стыковки.
	 */
	public StackType getStackType()
	{
		return stackType;
	}

	@Override
	public CrystalType getType()
	{
		return (CrystalType) type;
	}

	/**
	 * @return запрещено ли больше 1 однотипного кристала.
	 */
	public final boolean isNoStack()
	{
		return noStack;
	}
}
