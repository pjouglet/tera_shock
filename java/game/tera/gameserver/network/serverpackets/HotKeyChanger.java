package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Пакет изменения раскладки.
 * 
 * @author Ronn
 */
public class HotKeyChanger extends ServerPacket
{
	public static enum ChangeType
	{
		NONE,
		REPLACE,
	}
	
	private static final ServerPacket instance = new HotKeyChanger();
	
	public static final HotKeyChanger getInstance(ChangeType type, int... vals)
	{
		HotKeyChanger packet = (HotKeyChanger) instance.newInstance();
		
		packet.type = type;
		packet.vals = vals;
		
		return packet;
	}
	
	/** тип изменения */
	private ChangeType type;
	
	/** значения изменений */
	private int[] vals;
    
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.HOT_KEY_CHANGED;
	}

	@Override
	protected final void writeImpl()
	{
        writeOpcode();
        
        writeShort(type.ordinal()); 
        
        for(int i = 0, length = vals.length; i < length; i++)
        	writeInt(vals[i]);
	}
}