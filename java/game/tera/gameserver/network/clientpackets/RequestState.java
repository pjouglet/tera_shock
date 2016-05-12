package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.StateAllowed;

/**
 * Клиентский пакет старта мили скила.
 *
 * @author Ronn
 */
public class RequestState extends ClientPacket
{
	/** игрок */
	private Player player;

	/** уник ид игрока */
	private int objectId;
	/** саб ид игрока */
	private int subId;
	/** ид состояния */
	private int stateId;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		objectId = readInt();//обжект ид  B4 4D 0D 00
		subId = readInt();//саб ид  00 80 00 00
		stateId = readInt();//1E 00 00 00
	}

	@Override
	public void runImpl()
	{
		if(player == null || player.getObjectId() != objectId || player.getSubId() != subId)
			return;

		player.sendPacket(StateAllowed.getInstance(player, stateId), true);
	}
}