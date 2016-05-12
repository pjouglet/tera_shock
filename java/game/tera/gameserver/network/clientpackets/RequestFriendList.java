package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.FriendListInfo;
import tera.gameserver.network.serverpackets.FriendListState;

/**
 * Клиентский пакет с запросом информации о друзьях.
 *
 * @author Ronn
 */
public class RequestFriendList extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();
    }

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		player.sendPacket(FriendListInfo.getInstance(player), true);
		player.sendPacket(FriendListState.getInstance(player), true);
	}
}