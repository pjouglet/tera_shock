package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Окно выхода на выбор перса.
 * 
 * @author Ronn
 */
public class RestartWindow extends ServerPacket
{
	private static final ServerPacket instance = new RestartWindow();
	
	public static RestartWindow getInstance(int seconds)
	{
		RestartWindow packet = (RestartWindow) instance.newInstance();
		
		packet.seconds = seconds;
		
		return packet;
	}

	/** кол-во ожидания секунд */
	private int seconds;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_RESTART;
	}

	@Override
	protected final void writeImpl()
	{
		switch(seconds)
		{
			case 1:
			{
				writeOpcode();
				writeInt(10); // 10 seconds before logout
			}
			default :
			{
				writeOpcode();
			}
		}
	}
}
