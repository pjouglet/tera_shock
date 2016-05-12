package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет об удалении выстрела с мира.
 * 
 * @author Ronn
 */
public class DeleteShot extends ServerPacket
{
	private static final ServerPacket instance = new DeleteShot();
	
	public static DeleteShot getInstance(int objectId, int subId)
	{
		DeleteShot packet = (DeleteShot) instance.newInstance();
		
		packet.objectId = objectId;
		packet.subId = subId;
		
		return packet;
	}
	
	/** уникальный ид выстрела */
	private int objectId;
	/** тип объекта */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_DELETE_SHOT;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(objectId);//41 1F B5 03 Обжект ид шарика который удаляем
		writeInt(subId);//08 80 00 0B Саб ид шарика
		writeByte(1);//01
	}
}
