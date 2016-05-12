package tera.gameserver.model.ai.npc;

import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;

/**
 * Модель задания АИ.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public final class Task implements Foldable
{
	/** цели задания */
	private Character target;
	/** скил для задания */
	private Skill skill;

	/** тип задания */
	private TaskType type;

	/** сообщение при выполнении */
	private String message;

	/** целевые координаты задания */
	private float x;
	private float y;
	private float z;

	/** разворот в заданиях */
	private int heading;

	public Task()
	{
		super();
	}

	/**
	 * @param type тип задания.
	 * @param target цель задания.
	 */
	public Task(TaskType type, Character target)
	{
		this.type = type;
		this.target = target;
	}

	/**
	 * @param type тип задания.
	 * @param target цель задания.
	 * @param skill скил для задания.
	 */
	public Task(TaskType type, Character target, Skill skill)
	{
		this.type = type;
		this.target = target;
		this.skill = skill;
	}

	/**
	 * @param type тип задания.
	 * @param target цель задания.
	 * @param skill скил для задания.
	 * @param heading разворот.
	 */
	public Task(TaskType type, Character target, Skill skill, int heading)
	{
		this(type, target, skill);

		this.heading = heading;
	}


	/**
	 * @param type тип задания.
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 */
	public Task(TaskType type, float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}


	/**
	 * @param type тип задания.
	 * @param x целевая координата.
	 * @param y целевая координата.
	 * @param z целевая координата.
	 * @param skill скил для задания.
	 * @param target цель задания.
	 */
	public Task(TaskType type, float x, float y, float z, Skill skill, Character target)
	{
		this(type, x, y, z);

		this.skill = skill;
		this.target = target;
	}

	@Override
	public void finalyze()
	{
		type = null;
		target = null;
		skill = null;
	}

	/**
	 * @return разворот.
	 */
	public int getHeading()
	{
		return heading;
	}

	/**
	 * @return сообщение при выполнении.
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @return скилл задания.
	 */
	public Skill getSkill()
	{
		return skill;
	}

	/**
	 * @return цель задания.
	 */
	public Character getTarget()
	{
		return target;
	}

	/**
	 * @return тип задания.
	 */
	public TaskType getType()
	{
		return type;
	}

	/**
	 * @return целевая координата.
	 */
	public float getX()
	{
		return x;
	}

	/**
	 * @return целевая координата.
	 */
	public float getY()
	{
		return y;
	}

	/**
	 * @return целевая координата.
	 */
	public float getZ()
	{
		return z;
	}

	@Override
	public void reinit(){}

	/**
	 * @param heading разворот.
	 */
	public Task setHeading(int heading)
	{
		this.heading = heading;

		return this;
	}

	/**
	 * @param message сообщение при выполнении.
	 */
	public Task setMessage(String message)
	{
		this.message = message;

		return this;
	}

	/**
	 * @param skill скилл задания.
	 */
	public Task setSkill(Skill skill)
	{
		this.skill = skill;

		return this;
	}

	/**
	 * @param target цель задания.
	 */
	public Task setTarget(Character target)
	{
		this.target = target;

		return this;
	}

	/**
	 * @param type тип задания.
	 */
	public Task setType(TaskType type)
	{
		this.type = type;

		if(type == null)
			Thread.dumpStack();

		return this;
	}

	/**
	 * @param x целевая координата.
	 */
	public Task setX(float x)
	{
		this.x = x;

		return this;
	}

	/**
	 * @param y целевая координата.
	 */
	public Task setY(float y)
	{
		this.y = y;

		return this;
	}

	/**
	 * @param z целевая координата.
	 */
	public Task setZ(float z)
	{
		this.z = z;

		return this;
	}

	@Override
	public String toString()
	{
		return "Task [target=" + target + ", skill=" + skill + ", type=" + type + ", message=" + message + ", x=" + x + ", y=" + y + ", z=" + z + ", heading=" + heading + "]";
	}
}
