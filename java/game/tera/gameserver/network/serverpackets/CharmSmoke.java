package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.worldobject.BonfireObject;
import tera.gameserver.network.ServerPacketType;


/**
 * Пакет дыма от чарма над костром.
 * 
 * @author Ronn
 */
public class CharmSmoke extends ServerPacket
{
	private static final ServerPacket instance = new CharmSmoke();
	
	public static CharmSmoke getInstance(BonfireObject bonfire)
	{
		CharmSmoke packet = (CharmSmoke) instance.newInstance();
		
		packet.objectId = bonfire.getObjectId();
		packet.subId = bonfire.getSubId();
		
		return packet;
	}
	
	/** обджект ид объекта */
	private int objectId;
	/** саю ид объекта */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHARM_SMOKE;
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
		writeInt(buffer, objectId);//обжект ид костра
		writeInt(buffer, subId);//саб ид костра
		writeInt(buffer, 0x00000006);//02 00 00 00 не
		writeInt(buffer, 0x0000ccb0);//14 CD 00 00 ид анимации 52400,5325 разницы не вижу
	}
}
