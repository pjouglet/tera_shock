package tera.gameserver.network.serverpackets;

import tera.gameserver.model.actions.ActionType;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с подтверждением попытка запустить акшен.
 * 
 * @author Ronn
 */
public class ActionStart extends ServerPacket
{
	private static final ServerPacket instance = new ActionStart();
	
	/**
	 * Получение нового пакета под указанный тип акшена.
	 * 
	 * @param actionType тип акшена.
	 * @return новый пакет.
	 */
	public static ActionStart getInstance(ActionType actionType)
	{
		ActionStart packet = (ActionStart) instance.newInstance();
		
		packet.actionType = actionType;
		
		return packet;
	}
	
	/** тип акшена */
	private ActionType actionType;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ACTION_START;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(actionType.ordinal());
	}
}
