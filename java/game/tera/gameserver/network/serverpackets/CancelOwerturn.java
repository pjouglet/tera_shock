package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для отмены состояния опрокидывания.
 * 
 * @author Ronn
 */
public class CancelOwerturn extends ServerPacket
{
	private static final ServerPacket instance = new CancelOwerturn();
	
	public static CancelOwerturn getInstance(Character actor)
	{
		CancelOwerturn packet = (CancelOwerturn) instance.newInstance();
		
		packet.actor = actor;
		
		return packet;
	}
	
	/** игрок, которому нужно отменить */
	private Character actor;
	
	@Override
	public void finalyze()
	{
		actor = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CANCEL_OWERTURN;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actor.getObjectId());
		writeInt(actor.getSubId());
        writeFloat(actor.getX());
        writeFloat(actor.getY());
        writeFloat(actor.getZ());
        writeShort(actor.getHeading());
        writeInt(actor.getModelId());
      
        writeInt(actor.getOwerturnId());
       
        writeInt(0);
        writeInt(0);
	}
}
