package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.model.items.CommonType;
import tera.gameserver.tables.SkillTable;

/**
 * Модель шаблона различных итемов.
 *
 * @author Ronn
 */
public final class CommonTemplate extends ItemTemplate
{
	/** активный скил */
	protected SkillTemplate activeSkill;

	public CommonTemplate(CommonType type, VarTable vars)
	{
		super(type, vars);

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		// получаем активный скил
		activeSkill = skillTable.getSkill(getClassIdItemSkill(), vars.getInteger("activeSkill", 0));

		// получаем тип слота
		slotType = type.getSlot();
	}

	@Override
	public final SkillTemplate getActiveSkill()
	{
		return activeSkill;
	}

	@Override
	public CommonType getType()
	{
		return (CommonType) type;
	}

	@Override
	public String toString()
	{
		return super.toString() + " activeSkill = " + (activeSkill != null? activeSkill.getId() : "null");
	}
}
