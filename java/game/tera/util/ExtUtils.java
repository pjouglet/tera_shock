package tera.util;

import rlib.util.table.FuncValue;
import rlib.util.wraps.Wrap;

import tera.gameserver.model.skillengine.Skill;

/**
 * Набор утилит для этого проекта.
 *
 * @author Ronn
 */
public final class ExtUtils
{
	private ExtUtils()
	{
		throw new IllegalArgumentException();
	}

	public static final FuncValue<Wrap> FOLD_WRAP_TABLE_FUNC = new FuncValue<Wrap>()
	{
		@Override
		public void apply(Wrap value)
		{
			value.fold();
		}
	};

	public static final FuncValue<Skill> FOLD_SKILL_TABLE_FUNC = new FuncValue<Skill>()
	{
		@Override
		public void apply(Skill value)
		{
			value.fold();
		}
	};
}
