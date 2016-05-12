package tera.gameserver.model.npc.interaction.dialogs;

import rlib.util.pools.Foldable;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации диалога с НПС.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface Dialog extends Foldable
{
	/**
	 * Применить изминения в окне.
	 * 
	 * @return произошли ли изминения.
	 */
	public boolean apply();
	
	/**
	 * Закрыть окно.
	 * 
	 * @return произошли ли изминения.
	 */
	public boolean close();
	
	/**
	 * @return нпс у которого было начат диалог.
	 */
	public Npc getNpc();
	
	/**
	 * @return игрок, который говорит с нпс.
	 */
	public Player getPlayer();
	
	/**
	 * @return тип диалогового окна
	 */
	public DialogType getType();
	
	/**
	 * Инициализация окна.
	 * 
	 * @return успешно ли инициализировалось.
	 */
	public boolean init();
}
