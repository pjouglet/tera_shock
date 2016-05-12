package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, который запрашивает выход из пати.
 *
 * @author Ronn
 */
public class RequestPartyLeave extends ClientPacket
{
	/** ливающий игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Party party = player.getParty();

		if(party == null)
			return;

		party.removePlayer(player);
	}
}
