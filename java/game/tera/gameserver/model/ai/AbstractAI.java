package tera.gameserver.model.ai;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.Character;

/**
 * Базовая модель АИ.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public abstract class AbstractAI<E extends Character> implements AI
{
	protected static final Logger log = Loggers.getLogger(AI.class);

	/** тот, кем АИ управляет */
	protected E actor;

	/**
	 * @param actor управляемый АИ.
	 */
	public AbstractAI(E actor)
	{
		this.actor = actor;
	}

	/**
	 * @return управляемый АИ.
	 */
	public E getActor()
	{
		return actor;
	}

	/**
	 * Удалить управляемого.
	 */
	public void removeActor()
	{
		actor = null;
	}

	/**
	 * @param actor управляемый АИ.
	 */
	public void setActor(E actor)
	{
		this.actor = actor;
	}
}
