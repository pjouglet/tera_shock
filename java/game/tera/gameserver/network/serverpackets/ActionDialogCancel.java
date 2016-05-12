package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет закрытия диалога акшена.
 * 
 * @author Ronn
 */
public class ActionDialogCancel extends ServerPacket
{
	private static final ServerPacket instance = new ActionDialogCancel();
	
	/**
	 * @param actor инициатор диалога.
	 * @param enemy опонент инициатора.
	 * @param id ид диалога.
	 * @param objectId обджект ид диалога.
	 */
	public static ActionDialogCancel getInstance(Player actor, Player enemy, int id, int objectId)
	{
		ActionDialogCancel packet = (ActionDialogCancel) instance.newInstance();
		
		packet.actorId = actor.getObjectId();
		packet.actorSubId = actor.getSubId();
		packet.enemyId = enemy.getObjectId();
		packet.enemySubId = enemy.getSubId();
		packet.id = id;
		packet.objectId = objectId;
		
		return packet;
	}
	
	/** ид инициатора */
	private int actorId;
	/** саб ид инициатора */
	private int actorSubId;
	/** ид опонента */
	private int enemyId;
	/** саб ид опонента */
	private int enemySubId;
	/** ид акшена */
	private int id;
	/** обджект ид акшена */
	private int objectId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CLOSE_TRADE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actorId);//BD 07 00 10 обжект ид 1го
		writeInt(actorSubId);//00 80 00 13 саб ид
		writeInt(enemyId);//C7 07 00 10 обжект ид 2го
		writeInt(enemySubId);//00 80 00 13 саб ид
		writeInt(id);//03 00 00 00 ид экшена
		writeInt(objectId);//93 EE 06 00 обжект ид экшена
	}
}
