package tera.gameserver.network.clientpackets;

import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.PlayerNameResult;

/**
 * Клиентский пакет, запрашивающий проверку на доступность нового имени
 *
 * @author  Ronn
 */
public class NameChange extends ClientPacket
{
	/** новое имя */
	private String name;

	@Override
	public void finalyze()
	{
		name = null;
	}

	@Override
	public void readImpl()
	{
		readShort();

		name = readString();
    }

	@Override
	public void runImpl()
	{
		// получаем менеджер БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем клиент игрока
		UserClient owner = getOwner();

		if(dbManager.getAccountSize(owner.getAccount().getName()) >= 8)
			owner.sendPacket(PlayerNameResult.getInstance(PlayerNameResult.FAILED), true);
		else if(!Config.checkName(name))
			owner.sendPacket(PlayerNameResult.getInstance(PlayerNameResult.FAILED), true);
		else if(!dbManager.isFreeName(name))
			owner.sendPacket(PlayerNameResult.getInstance(PlayerNameResult.FAILED), true);
		else
			owner.sendPacket(PlayerNameResult.getInstance(PlayerNameResult.SUCCESSFUL), true);
	}
}