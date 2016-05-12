package tera.gameserver.model.npc.interaction;

/**
 * Перечесление типов ссылок в диалогах.
 * 
 * @author Ronn
 */
public enum LinkType
{
	QUEST(24),
	DIALOG(0),
	TRADE(26),
	SHOP(28),
	FLY_PEGAS(26),
	;
	
	/** ид типа ссылки для пакета */
	private int id;

	private LinkType(int id)
	{
		this.id = id;
	}

	/**
	 * @return ид ссылки.
	 */
	public final int getId()
	{
		return id;
	}
}
