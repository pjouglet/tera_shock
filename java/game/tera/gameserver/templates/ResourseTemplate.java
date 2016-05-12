package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.IdFactory;
import tera.gameserver.model.drop.ResourseDrop;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.resourse.ResourseType;

/**
 * Модель темплейта ресурса.
 *
 * @author Ronn
 */
public final class ResourseTemplate
{
	/** дроп с ресурса */
	private ResourseDrop drop;

	/** тип ресурса */
	private ResourseType type;

	/** ид ресурса */
	private int id;
	/** уровень ресурса */
	private int level;
	/** уровень навыка для сбора */
	private int req;
	/** получаемый опыт */
	private int exp;

	public ResourseTemplate(VarTable vars)
	{
		this.id = vars.getInteger("id");
		this.level = vars.getInteger("level");
		this.req = vars.getInteger("req");
		this.exp = vars.getInteger("exp", 0);

		this.type = vars.getEnum("type", ResourseType.class);
	}

	/**
	 * @return дроп с ресурсов.
	 */
	public final ResourseDrop getDrop()
	{
		return drop;
	}

	/**
	 * @return кол-во опыта за ресурс.
	 */
	public int getExp()
	{
		return exp;
	}

	/**
	 * @return ид темплейта.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return уровень ресурса.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * @return необходимый навык для сбора ресурса.
	 */
	public final int getReq()
	{
		return req;
	}

	/**
	 * @return тип ресурса.
	 */
	public ResourseType getType()
	{
		return type;
	}

	/**
	 * Получение нового экземпляра ресурсов.
	 *
	 * @return экземпляр ресурса.
	 */
	public ResourseInstance newInstance()
	{
		IdFactory idFactory = IdFactory.getInstance();

		return newInstance(idFactory.getNextResourseId());
	}

	/**
	 * Получение нового экземпляра ресурсов с указанным ид.
	 *
	 * @param objectId уникальный ид.
	 * @return экземпляр ресурса.
	 */
	public ResourseInstance newInstance(int objectId)
	{
		// создаем новый инстанс
		ResourseInstance resourse = type.newInstance(this);

		// запоминаем уникальный ид
		resourse.setObjectId(objectId);

		// возвращаем экземпляр
		return resourse;
	}

	/**
	 * @param drop дроп с ресурсов.
	 */
	public final void setDrop(ResourseDrop drop)
	{
		this.drop = drop;
	}
}
