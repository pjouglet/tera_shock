package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, отвечающий на запрос о смене ника
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class PlayerNameResult extends ServerPacket
{
	/** удално сменен */
	public static final int SUCCESSFUL = 1;
	/** не удачно сменен */
	public static final int FAILED = 0;
	
	private static final ServerPacket instance = new PlayerNameResult();
	
	public static PlayerNameResult getInstance(int result)
	{
		PlayerNameResult packet = (PlayerNameResult) instance.newInstance();
		
		packet.result = result;
		
		return packet;
	}
	
	/** результат */
	private int result = 0;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_NAME_RESULT;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeByte(result);
	}
}