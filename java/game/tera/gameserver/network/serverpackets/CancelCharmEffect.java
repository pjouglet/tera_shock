package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.network.ServerPacketType;


/**
 * Пакет завершения эффекта на персонаже.
 * 
 * @author Ronn
 */
public class CancelCharmEffect extends ServerPacket
{
	private static final ServerPacket instance = new CancelCharmEffect();
	
	public static CancelCharmEffect getInstance(int effectId)
	{
		CancelCharmEffect packet = (CancelCharmEffect) instance.newInstance();
		
		packet.effectId = effectId;
		
		return packet;
	}
	
	/** эффект, который спадает */
	private int effectId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CANCEL_CHARM_EFFECT;
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
		writeInt(buffer, effectId);//ид бафа который спал только что
	}
}

