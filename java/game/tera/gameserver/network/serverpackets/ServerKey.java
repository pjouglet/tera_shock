package tera.gameserver.network.serverpackets;

import rlib.util.Rnd;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с серверным ключем для криптора
 * 
 * @author Ronn
 */
public class ServerKey extends ServerPacket
{
	private static final ServerPacket instance = new ServerKey();
	
	public static ServerKey getInstance()
	{
		return (ServerKey) instance.newInstance();
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SERVER_KEY;
	}

	@Override
	protected void writeImpl()
	{
		buffer.put(Rnd.byteArray(128));
	}
}