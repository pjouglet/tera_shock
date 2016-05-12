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
public class SetAccountHandler implements PacketHandler
{
	public static final SetAccountHandler instance = new SetAccountHandler();

	@Override
	public Packet processing(Packet packet)
	{
		String login = packet.nextString();

		AccountManager accountManager = AccountManager.getInstance();

		Account account = accountManager.getAccount(login.toLowerCase());
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		boolean inDB = false;

		if(account == null)
		{
			account = dbManager.restoreAccount(login);
			inDB = true;
		}

		if(account == null)
			return new Packet(PacketType.RESPONSE, false);

		account.setEmail(packet.nextString());
		account.setLastIP(packet.nextString());
		account.setAllowIPs(packet.nextString());
		account.setComments(packet.nextString());
		account.setEndBlock(packet.nextLong());
		account.setEndPay(packet.nextLong());
		account.setAccessLevel(packet.nextInt());

		dbManager.updateFullAccount(account);

		if(inDB)
			accountManager.removeAccount(account);

		return new Packet(PacketType.RESPONSE, true);
	}
}
