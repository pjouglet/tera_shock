package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет показывает информацию игроку об другом игроке
 * 
 * @author Ronn
 */
public class PlayerPvPOff extends ServerPacket
{
	private static final ServerPacket instance = new PlayerPvPOff();
	
	public static PlayerPvPOff getInstance(Player player)
	{
		PlayerPvPOff packet = (PlayerPvPOff) instance.newInstance();
		
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
		return ServerPacketType.PLAYER_PVP_OFF;
	}

	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(id);//Обжект ид
		writeInt(subId);//саб ид
	}
}