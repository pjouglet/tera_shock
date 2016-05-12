package tera.remotecontrol.handlers;

import tera.gameserver.manager.AccountManager;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.Account;
import tera.remotecontrol.Packet;
import tera.remotecontrol.PacketHandler;
import tera.remotecontrol.PacketType;

/**
 * Сборщик диномичной инфы о сервере
 *
 * @author Ronn
 * @created 25.04.2012
 */
public class UpdateAccountHandler implements PacketHandler
{
	public static final UpdateAccountHandler instance = new UpdateAccountHandler();

	@Override
	public Packet processing(Packet packet)
	{
		String login = packet.nextString();

		AccountManager accountManager = AccountManager.getInstance();
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		Account account = accountManager.getAccount(login.toLowerCase());

		boolean inDB = false;

		if(account == null)
		{
			account = dbManager.restoreAccount(login);
			inDB = true;
		}

		if(account == null)
			return new Packet(PacketType.RESPONSE, false);

		Packet response = new Packet(PacketType.RESPONSE, true, account.getEmail(), account.getLastIP(), account.getAllowIPs(), account.getComments(), account.getEndBlock(), Math.max(System.currentTimeMillis(), account.getEndPay()), account.getAccessLevel());

		if(inDB)
			accountManager.removeAccount(account);

		return response;
	}
}
