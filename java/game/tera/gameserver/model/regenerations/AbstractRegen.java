package tera.gameserver.model.regenerations;

import tera.gameserver.model.Character;

/**
 * Базовая модель регена.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public abstract class AbstractRegen<E extends Character> implements Regen
{
	/** регенерируемый персонаж */
	protected E actor;
	
	public AbstractRegen(E actor)
	{
		this.actor = actor;
	}
	
	/**
	 * @return регенирируемый персонаж.
	 */
	protected final E getActor()
	{
		return actor;
	}
}
