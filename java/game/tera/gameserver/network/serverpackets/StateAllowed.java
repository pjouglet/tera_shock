package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для продолжения полета после вылета из портала 
 * 
 * @author Ronn
 */
public class StateAllowed extends ServerPacket
{
	private static final ServerPacket instance = new StateAllowed();
	
	public static StateAllowed getInstance(Character actor, int stateId)
	{
		StateAllowed packet = (StateAllowed) instance.newInstance();
		
		packet.actor = actor;
		packet.stateId = stateId;
		
		return packet;
	}
	
	/** персонаж */
	private Character actor;
	
	/** ид состояния */
	private int stateId;
	
	@Override
	public void finalyze()
	{
		actor = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.STATE_ALLOWED;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId());//обжект айди наш
		writeInt(actor.getSubId()); //саб айди нашь
		writeInt(stateId);
	}
}
