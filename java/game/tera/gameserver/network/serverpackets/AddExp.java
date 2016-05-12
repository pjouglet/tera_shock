package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Пакет выдачи опыта игроку.
 * 
 * @author Ronn
 */
public class AddExp extends ServerPacket
{
	private static final ServerPacket instance = new AddExp();
	
	public static final AddExp getInstance(int exp, int added, int next, int npcId, int npcSubId)
	{
		AddExp packet = (AddExp) instance.newInstance();
		
		packet.exp = exp;
		packet.added = added;
		packet.next = next;
		packet.npcId = npcId;
		packet.npcSubId = npcSubId;
		
		return packet;
	}
	
    /** кол-во экспы игрока */
    private int exp;
    /** сколько получили */
    private int added;
    /** уровень игрока */
    private int next;
    
    /** ид нпс */
    private int npcId;
    private int npcSubId;
    
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_EXP;
	}

	@Override
	protected final void writeImpl()
	{
        writeOpcode();
        writeInt(added);   //кол-во экспы
        writeInt(0);
        writeLong(exp);
        writeLong(exp);
        writeInt(next);
        writeInt(0);
        writeInt(npcId);
        writeInt(npcSubId);
        writeInt(0);
        writeInt(0);
        writeInt(0);
        writeInt(0x0000210B);
        writeInt(0x3F800000);
        writeInt(0);
	}
}