package tera.gameserver.network.serverpackets;

import rlib.util.Strings;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, описывающий принятие акшена.
 * 
 * @author Ronn
 * @created 07.03.2012
 */
public class AppledAction extends ServerPacket
{
	private static final ServerPacket instance = new AppledAction();
	
	/**
	 * @param player игрок.
	 * @param enemy опонент.
	 * @param type тип акшена.
	 * @param objectId обджект ид акшена.
	 * @return новый пакет.
	 */
	public static AppledAction newInstance(Player player, Player enemy, int type, int objectId)
	{
		AppledAction packet = (AppledAction) instance.newInstance();
		
		packet.player = player.getName();
		packet.enemy = enemy == null? Strings.EMPTY : enemy.getName();
		packet.actorId = player.getObjectId();
		packet.actorSubId = player.getSubId();
		packet.enemyId = enemy == null? 0 : enemy.getObjectId();
		packet.enemySubId = enemy == null? 0 : enemy.getSubId();
		packet.type = type;
		packet.objectId = objectId;
		
		return packet;
	}
	
	/** игрок */
	private String player;
	/** опонент */
	private String enemy;
	
	/** ид инициатора */
	private int actorId;
	/** саб ид инициатора */
	private int actorSubId;
	/** ид опонента */
	private int enemyId;
	/** саб ид опонента */
	private int enemySubId;
	
	/** тип акшена */
	private int type;
	/** обджект ид акшена */
	private int objectId;
	
	@Override
	public void finalyze()
	{
		player = null;
		enemy = null;
	}
 
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_WAITING_ACTION;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		
		int n = 44;
		
		writeShort(n);   // 2C 00 44
		writeShort(n += Strings.length(player));  // 3A 00 56
		writeShort(n);   // 58
		writeShort(Strings.length(enemy));
		writeInt(actorId);
		writeInt(actorSubId);
		writeInt(enemyId);
		writeInt(enemySubId);
		writeInt(type);//
		writeInt(objectId);
		writeInt(0);
		writeByte(48);
		writeShort(165);
		writeByte(0);
		writeString(player);
		writeString(enemy);
		writeByte(0);
	}
}
