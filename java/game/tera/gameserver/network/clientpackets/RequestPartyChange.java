package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Party;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, который запрашивает изменения настроек группы.
 *
 * @author Ronn
 */
public class RequestPartyChange extends ClientPacket
{
	/** лидер группы */
	private Player player;

	/** можно ли поднимать лут в бою */
	private boolean lootIsCombat;
	/** рандомный ли подбор лута */
	private boolean roundLoot;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		roundLoot = readInt() == 1;

		readInt();
		readInt();
		readInt();
		readShort();

		lootIsCombat = readByte() == 0;
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

		// применяем новые настройки группы
		party.change(lootIsCombat, roundLoot);
	}
}
