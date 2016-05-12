package tera.gameserver.model.listeners;

import tera.gameserver.model.Character;

/**
 * Прослушиватель смерти.
 * 
 * @author Ronn
 */
public interface DieListener
{
	/**
	 * Прослушка убийства.
	 * 
	 * @param killer убийца.
	 * @param killed убитый.
	 */
	public void onDie(Character killer, Character killed);
}
