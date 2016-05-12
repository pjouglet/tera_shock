package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.network.ServerPacketType;


/**
 * Серверный пакет наложения эффекта.
 * 
 * @author Ronn
 */
public class AppledCharmEffect extends ServerPacket
{
	private static final ServerPacket instance = new AppledCharmEffect();
	
	public static ServerPacket getInstance(Character effected, Effect effect)
	{
		AppledCharmEffect packet = (AppledCharmEffect) instance.newInstance();
		
		packet.effected = effected;
		packet.effectId = effect.getEffectId();
		packet.time = effect.getTimeForPacket();
		
		return packet;
	}
	
	public static ServerPacket getInstance(Character effected, int id, int time)
	{
		AppledCharmEffect packet = (AppledCharmEffect) instance.newInstance();
		
		packet.effected = effected;
		packet.effectId = id;
		packet.time = time;
		
		return packet;
	}

	
	/** тот, на кого наложили эффект */
	private Character effected;
	
	/** ид эффекта */
	private int effectId;
	/** время эффекта */
	private int time;

	@Override
	public void finalyze()
	{
		effected = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.APPLED_CHARM_PACKET;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}
	
	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		if(effected == null)
			return;
		
		writeOpcode(buffer);
		writeInt(buffer, effected.getObjectId());
		writeInt(buffer, effected.getSubId());
		writeInt(buffer, effectId);
		writeByte(buffer, 1);
		writeInt(buffer, time);
	}
}
