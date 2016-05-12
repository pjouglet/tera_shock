package tera.gameserver.model.skillengine.shots;

import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;

/**
 * Интерфейс для реализации выстрелов.
 * 
 * @author Ronn
 */
public interface Shot extends Foldable, Runnable
{
	/**
	 * @return уникальный ид выстрела.
	 */
	public int getObjectId();
	
	/**
	 * @return саб ид выстрела.
	 */
	public int getSubId();
	
	/**
	 * @return цель выстрела.
	 */
	public Character getTarget();
	
	/**
	 * @return целевая координата.
	 */
	public float getTargetX();
	
	/**
	 * @return целевая координата.
	 */
	public float getTargetY();
	
	/**
	 * @return целевая координата.
	 */
	public float getTargetZ();
	
	/**
	 * @return тип выстрела.
	 */
	public ShotType getType();
	
	/**
	 * @return является ли автонаводкой.
	 */
	public boolean isAuto();
	
	/**
	 * Запуск выстрела.
	 */
	public void start();
	
	/**
	 * Остановка выстрела.
	 */
	public void stop();
}
