package tera.gameserver.model.actions;

import rlib.util.pools.Foldable;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации акшенов.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public interface Action extends Foldable
{
	/**
	 * Подтверждение согласия на участие в акшене.
	 */
	public void assent(Player player);
	
	/**
	 * Отмена акшена.
	 */
	public void cancel(Player player);
	
	/**
	 * @return инициатор акшена.
	 */
	public Player getActor();
	
	/**
	 * @return ид акшена.
	 */
	public int getId();
	
	/**
	 * @return уникальный ид акшена.
	 */
	public int getObjectId();
	
	/**
	 * @return цель акшена.
	 */
	public Object getTarget();
	
	/**
	 * @return тип акшена.
	 */
	public ActionType getType();
	
	/**
	 * Инициализация акшена.
	 */
	public void init(Player actor, String name);
	
	/**
	 * Приглашение на участие в акшене.
	 */
	public void invite();
	
	/**
	 * @param actor инициатор акшена.
	 */
	public void setActor(Player actor);
	
	/**
	 * @param target цель акшена.
	 */
	public void setTarget(Object target);
	
	/**
	 * @return все ли выполняет условиям акшена.
	 */
	public boolean test();
}
