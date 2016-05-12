package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет показывает информацию игроку об другом игроке
 * 
 * @author Ronn
 */
public class PlayerPvPOn extends ServerPacket
{
	private static final ServerPacket instance = new PlayerPvPOn();
	
	public static PlayerPvPOn getInstance(Player player)
	{
		PlayerPvPOn packet = (PlayerPvPOn) instance.newInstance();
		
		packet.id = player.getObjectId();
		packet.subId = player.getSubId();
		
		return packet;
	}
	
	/** ид игрока */
	private int id;
	/** саб ид игрока */
	private int subId;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_PVP_ON;
	}

	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(id);//Обжект ид
		writeInt(subId);//саб ид
	}
}