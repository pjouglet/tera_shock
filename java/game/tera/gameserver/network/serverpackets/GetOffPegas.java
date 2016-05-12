package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакт для того, чтобы персонаж мог слезть с пегаса
 * 
 * @author Ronn
 *
 */
public class GetOffPegas extends ServerPacket
{
	private static final ServerPacket instance = new GetOffPegas();
	
	public static GetOffPegas getInstance(Character actor)
	{
		GetOffPegas packet = (GetOffPegas) instance.newInstance();
		
		packet.actor = actor;
		
		return packet;
	}
	
	/** персонаж */
	private Character actor;
	
	@Override
	public void finalyze()
	{
		actor = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.GET_OFF_PEGAS;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId());//обжект айди наш
		writeInt(actor.getSubId()); // саб айди нашь	
	}
}
