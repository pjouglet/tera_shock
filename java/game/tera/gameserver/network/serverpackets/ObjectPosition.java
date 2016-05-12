package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * @author Ronn
 */
public class ObjectPosition extends ServerPacket
{
	private static final ServerPacket instance = new ObjectPosition();
	
	public static ObjectPosition getInstance(Character attacker, Character target)
	{
		ObjectPosition packet = (ObjectPosition) instance.newInstance();
		
		packet.attackedId = attacker.getObjectId();
		packet.attackerSubId = attacker.getSubId();
		packet.targetId = target.getObjectId();
		packet.targetSubId = target.getSubId();
		packet.heading = target.getHeading();
		packet.x = target.getX();
		packet.y = target.getY();
		packet.z = target.getZ();
		
		return packet;
	}
	
	/** ид атакующего */
	private int attackedId;
	private int attackerSubId;
	
	/** ид цели */
	private int targetId;
	private int targetSubId;
	
	private int heading;
	
	/** координаты */
	private float x;
	private float y;
	private float z;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.OBJECT_POSITION;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(attackedId);//6A 65 0D 00 
		writeInt(attackerSubId);//00 80 00 01 
		writeInt(targetId);//CF B9 07 00 
		writeInt(targetSubId);//00 80 0B 00 
		writeFloat(x);//4F D8 96 C6 
		writeFloat(y);//B5 98 49 47 
		writeFloat(z);//00 60 14 45  
		writeShort(heading);//96 81 
		writeShort(1);//01 00 а я ебу что за 1н
	}
}
