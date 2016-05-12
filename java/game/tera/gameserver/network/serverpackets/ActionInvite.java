package tera.gameserver.network.serverpackets;

import rlib.util.Strings;
import tera.gameserver.network.ServerPacketType;

/**
 * Приглашение в акшен игроку
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class ActionInvite extends ServerPacket
{
	private static final ServerPacket instance = new ActionInvite();
	
	/**
	 * @param actorName имя инициатора.
	 * @param enemyName имя опонента.
	 * @param id ид акшена.
	 * @param objectId уникальный ид акшена.
	 * @return новый пакет.
	 */
	public static ActionInvite getInstance(String actorName, String enemyName, int id, int objectId)
	{
		ActionInvite packet = (ActionInvite) instance.newInstance();
		
		packet.actorName = actorName;
		packet.enemyName = enemyName;
		packet.id = id;
		packet.objectId = objectId;
		
		return packet;
	}
	
	/** имя инициатора */
	private String actorName;
	/** имя опонента */
	private String enemyName;
	
	/** ид акшена */
	private int id;
	/** уникальный ид акшена */
	private int objectId;
	
	@Override
	public void finalyze()
	{
		actorName = null;
		enemyName = null;
	}
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_ACTION_INVITE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeShort(22);//начало первого ника
		writeShort(22 + Strings.length(actorName));  //начало 2го ника
		writeShort(Strings.length(actorName));  //длинна первого ника +2
		writeInt(id);
		writeInt(objectId);
		writeByte(0x26);
		writeShort(0x46);
		writeByte(0);
		writeString(actorName);
		writeString(enemyName);
		writeByte(0);
	}
}
