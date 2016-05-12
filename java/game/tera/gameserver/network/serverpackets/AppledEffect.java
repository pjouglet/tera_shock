package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет наложения эффекта.
 * 
 * @author Ronn
 */
public class AppledEffect extends ServerPacket
{
	private static final ServerPacket instance = new AppledEffect();
	
	public static ServerPacket getInstance(Character effector, Character effected, Effect effect)
	{
		AppledEffect packet = (AppledEffect) instance.newInstance();
		
		packet.effector = effector;
		packet.effected = effected;
		packet.effectId = effect.getEffectId();
		packet.time = effect.getTimeForPacket();
		
		return packet;
	}
	
	public static ServerPacket getInstance(Character effector, Character effected, int effectId, int time)
	{
		AppledEffect packet = (AppledEffect) instance.newInstance();
		
		packet.effector = effector;
		packet.effected = effected;
		packet.effectId = effectId;
		packet.time = time;
		
		return packet;
	}
	
	/** тот кто наложил эффект */
	private Character effector;
	/** тот, на кого наложили эффект */
	private Character effected;
	
	/** ид эффекта */
	private int effectId;
	/** время эффекта */
	private int time;

	@Override
	public void finalyze()
	{
		effector = null;
		effected = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.APPLED_BUFF_PACKET;
	}

	@Override
	protected void writeImpl()
	{	
		if(effected == null || effector == null)
			return;
		
		writeOpcode();
		writeInt(effected.getObjectId()); // обжект ид кто бафнут
		writeInt(effected.getSubId());    // саб ид кто бафнут
		writeInt(effector.getObjectId()); // обжект ид кто бафнул
		writeInt(effector.getSubId());    // саб ид кто бафнул
		writeInt(effectId);               // ид бафа
		writeInt(time);
		writeInt(1);                      // лвл или колчиество можно пока статиком
	}
}
