package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для удаление хп персонажа над головой.
 * 
 * @author Ronn
 */
public class CancelTargetHp extends ServerPacket
{
	private static final ServerPacket instance = new CancelTargetHp();
	
	public static CancelTargetHp getInstance(Character target)
	{
		CancelTargetHp packet = (CancelTargetHp) instance.newInstance();
		
		packet.objectId = target.getObjectId();
		packet.subId = target.getSubId();
		
		return packet;
	}
	
	/** ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CANCEL_TARGET_HP;
	}

	@Override
	protected void writeImpl()
	{
		 writeOpcode();
		 writeInt(objectId);//обжект ид
		 writeInt(subId);//саб ид
	}
}
