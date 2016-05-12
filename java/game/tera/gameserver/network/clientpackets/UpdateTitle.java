package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.playable.Player;

/**
 * Обновление титула игрока.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class UpdateTitle extends ClientPacket
{
	/** новый титул */
	private String title;
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
		title = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		title = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		if(player == null || title.length() > 15)
			return;

		String old = player.getTitle();

		player.setTitle(title);

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		if(!dbManager.updatePlayerTitle(player))
			player.setTitle(old);
	}
}
