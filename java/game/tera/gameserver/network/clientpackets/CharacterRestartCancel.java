package tera.gameserver.network.clientpackets;

import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.CharacterRestartCancelOk;

/**
 * Запрос на выход на выбор персов.
 * 
 * @author Ronn
 */
public class CharacterRestartCancel extends ClientPacket
{
	private UserClient client;
	
	@Override
	public void readImpl()
	{
		client = getClient();
	}

	@Override
	public void runImpl()
	{
		if(client == null)
		{
			log.warning(this, new Exception("not found client"));
			return;
		}
		
		// ложим на отправку
		client.sendPacket(CharacterRestartCancelOk.getInstance(), true);
	}
}