package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, показывающий блокировку скила щитом.
 * 
 * @author Ronn
 */
public class CharShieldBlock extends ServerPacket
{
	private static final ServerPacket instance = new CharShieldBlock();
	
	public static CharShieldBlock getInstance(Character attacked)
	{
		CharShieldBlock packet = (CharShieldBlock) instance.newInstance();
		
		packet.objectId = attacked.getObjectId();
		packet.subId = attacked.getSubId();
		
		return packet;
	}
	
	/** обджект ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;
    
    @Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.SHIELD_BLOCK;
	}

	@Override
    protected final void writeImpl()
    {
        writeOpcode();
        writeInt(objectId);
        writeInt(subId);
        writeInt(0x440D044D);
        writeInt(0x00000119);            
    }
}