package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, который запрашивает роспуск группы.
 *
 * @author Ronn
 */
public class RequestPartyDisband extends ClientPacket
{
	/** лидер группы */
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

		// получаем группу игрока
		Party party = player.getParty();

		// если группы нет, выходим
		if(party == null)
			return;

		// распускаем группу
		party.disband(player);
	}
}
