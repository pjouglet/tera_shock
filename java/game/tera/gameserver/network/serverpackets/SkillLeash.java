package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;

/**
 * Пакет с результатом использования удочки танка.
 * 
 * @author Ronn
 */
public class SkillLeash extends ServerPacket
{
	private static final ServerPacket instance = new SkillLeash();
	
	public static SkillLeash getInstance(int casterId, int casterSubId, int targetId, int targetSubId, boolean resut)
	{
		SkillLeash packet = (SkillLeash) instance.newInstance();
		
		packet.casterId = casterId;
		packet.casterSubId = casterSubId;
		packet.targetId = targetId;
		packet.targetSubId = targetSubId;
		packet.resut = resut;
		
		return packet;
	}
	
	/** тот кто кинул */
	private int casterId;
	private int casterSubId;
	/** на кого кинул */
	private int targetId;
	private int targetSubId;
	
	/** удачно ли */
	private boolean resut;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SKILL_LEASH;
	}
	
	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, casterId);//7C 65 0D 00 
		writeInt(buffer, casterSubId);//00 80 00 01 
		writeInt(buffer, targetId);///38 96 0C 00   
		writeInt(buffer, targetSubId);//00 80 0B 00 
		writeShort(buffer, resut? 1 : 0);//01 00
	}
}
