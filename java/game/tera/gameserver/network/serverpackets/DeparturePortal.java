package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет обрабатывающий вылет из портала
 * 
 * @author Ronn
 */
public class DeparturePortal extends ServerPacket
{
	private static final ServerPacket instance = new DeparturePortal();
	
	public static DeparturePortal getInstance(Character actor)
	{
		DeparturePortal packet = (DeparturePortal) instance.newInstance();
		
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
		return ServerPacketType.DEPARTURE_PORTAL;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId());//обжект айди наш
		writeInt(actor.getSubId()); //саб айди нашь
		writeInt(9);
		writeInt(2);
		writeInt(0);
	}
}
