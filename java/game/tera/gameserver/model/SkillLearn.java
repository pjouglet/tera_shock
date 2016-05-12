package tera.gameserver.model;

import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель изучанемого скила.
 *
 * @author Ronn
 */
public final class SkillLearn
{
	/** ид изучаемого скила */
	private int id;
	/** ид для игры */
	private int useId;
	/** цена изучения */
	private int price;
	/** какой скил будет заменять */
	private int replaceId;
	/** какой скил будет заменять */
	private int replaceUseId;
	/** класс ид */
	private int classId;
	/** минимальный уровень для изучения */
	private int minLevel;

	/** пассивный ли скил */
	private boolean passive;
	/** реализован ли скил */
	private boolean implemented;

	public SkillLearn(int id, int price, int replaceId, int minLevel, int classId, boolean passive)
	{
		this.id = id;
		this.price = price;
		this.replaceId = replaceId;
		this.minLevel = minLevel;
		this.classId = classId;
		this.useId = id;
		this.replaceUseId = replaceId;
		this.passive = passive;

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		if(skillTable.getSkill(classId, id) == null)
			useId += 67108864;

		if(skillTable.getSkill(classId, replaceUseId) == null)
			replaceUseId += 67108864;

		SkillTemplate template = skillTable.getSkill(classId, id);

		this.implemented = template != null && template.isImplemented();
	}

	/**
	 * @return ид класса скила.
	 */
	public int getClassId()
	{
		return classId;
	}

	/**
	 * @return ид скила.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return минимальный уровень для изучения.
	 */
	public int getMinLevel()
	{
		return minLevel;
	}

	/**
	 * @return цена изучения скила.
	 */
	public int getPrice()
	{
		return price;
	}

	/**
	 * @return ид заменяемого скила.
	 */
	public int getReplaceId()
	{
		return replaceId;
	}

	/**
	 * @return используемый ид заменяемого скила.
	 */
	public int getReplaceUseId()
	{
		return replaceUseId;
	}

	/**
	 * @return используемый ид скила.
	 */
	public int getUseId()
	{
		return useId;
	}

	/**
	 * @return реализован ли скил.
	 */
	public final boolean isImplemented()
	{
		return implemented;
	}

	/**
	 * @return является ли скил паассивным.
	 */
	public final boolean isPassive()
	{
		return passive;
	}

	@Override
	public String toString()
	{
		return "SkillLearn  id = " + id + ", price = " + price + ", replaceId = " + replaceId + ", classId = " + classId + ", minLevel = " + minLevel;
	}
}
