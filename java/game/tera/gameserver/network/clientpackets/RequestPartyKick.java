package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, который запрашивает исключение игрока из группы.
 *
 * @author Ronn
 */
public class RequestPartyKick extends ClientPacket
{
	/** лидер группы */
	private Player player;

	/** ид игрока */
	private int objectId;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();

		objectId = readInt();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		// получаем группу игрока
		Party party = player.getParty();

		// если ее нет, выходим
		if(party == null)
			return;

		// исключаем игрока из группы
		party.kickPlayer(player, objectId);
	}
}
