package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с уведомление о принятии решения насчет акшена.
 *
 * @author Ronn
 * @created 07.03.2012
 */
public class ActionDoned extends ServerPacket
{
	private static final ServerPacket instance = new ActionDoned();
	
	/**
	 * @param actorId ид инициатора.
	 * @param actorSubId под ид инициатора.
	 * @param enemyId ид опонента.
	 * @param enemySubId под ид опонента.
	 * @param id ид акшена.
	 * @param objectId уникальный ид акшена.
	 * @return новый пакет.
	 */
	public static ActionDoned getInstance(int actorId, int actorSubId, int enemyId, int enemySubId, int id, int objectId)
	{
		ActionDoned packet = (ActionDoned) instance.newInstance();
		
		packet.actorId = actorId;
		packet.actorSubId = actorSubId;
		packet.enemyId = enemyId;
		packet.enemySubId = enemySubId;
		packet.id = id;
		packet.objectId = objectId;
		
		return packet;
	}
	
	/** ид создателя акшена */
	private int actorId;
	private int actorSubId;
	/** ид приглашаемого в акшен */
	private int enemyId;
	private int enemySubId;
	/** ид акшена */
	private int id;
	/** уникальный ид акшена */
	private int objectId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_ACTION_DONED;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actorId);
		writeInt(actorSubId);
		writeInt(enemyId);
		writeInt(enemySubId);
		writeInt(id);
		writeInt(objectId);
	}
}
