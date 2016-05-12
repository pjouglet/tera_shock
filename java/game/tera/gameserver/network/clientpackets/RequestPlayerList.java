package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Account;
import tera.gameserver.network.model.UserClient;

/**
 * Клиентский пакет, запрашивает список игроков на аккаунте.
 *
 * @author Ronn
 */
public class RequestPlayerList extends ClientPacket
{
	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl(){}

	@Override
	public void runImpl()
	{
		// получаем клиент
		UserClient client = getOwner();

		// если его нет, выходим
		if(client == null)
		{
			log.warning(this, "not found client.");
			return;
		}

		// получаем аккаунт
		Account account = client.getAccount();

		// если аккаунта нет, выходим
		if(account == null)
		{
			log.warning(this, "not found account.");
			return;
		}

		PacketManager.showPlayerList(client, account);
	}
}