package tera.gameserver.model.actions.dialogs;

import rlib.util.pools.Foldable;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации диалоговых окон акшенов.
 * 
 * @author Ronn
 */
public interface ActionDialog extends Foldable
{
	/**
	 * Приминение действий с окна.
	 * 
	 * @return успешно ли применилось.
	 */
	public boolean apply();
	
	/**
	 * Закрытие окна и отмена всех действий.
	 */
	public void cancel(Player player);
	
	/**
	 * @return инициатор.
	 */
	public Player getActor();
	
	/**
	 * @return опонент.
	 */
	public Player getEnemy();
	
	/**
	 * Получение опонента указанного игрока.
	 * 
	 * @param player игрок.
	 * @return опонент указанного игрока.
	 */
	public Player getEnemy(Player player);
	
	/**
	 * @return тип диалога.
	 */
	public ActionDialogType getType();
	
	/**
	 * Инициализация окна.
	 * 
	 * @return успешно ли инициализировалось.
	 */
	public boolean init();
}
