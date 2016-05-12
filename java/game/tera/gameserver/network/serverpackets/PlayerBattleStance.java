package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, обновляющий боевую стойку и персонажа
 * 
 * @author Ronn
 */
public class PlayerBattleStance extends ServerPacket
{
	/** находится в боевой стойке */
	public static final int STANCE_ON = 1;
	/** не находится в боевой стойке */
	public static final int STANCE_OFF = 0;
	
	private static final ServerPacket instance = new PlayerBattleStance();
	
	public static PlayerBattleStance getInstance(Character character, int stance)
	{
		PlayerBattleStance packet = (PlayerBattleStance) instance.newInstance();
		
		packet.character = character;
		packet.stance = stance;
		
		return packet;
	}
	
	/** персонаж */
	private Character character;

	/** находится ли в боевой стойки */
	private int stance;
	
	@Override
	public void finalyze()
	{
		character = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHAR_BATTLE_STATE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(character.getObjectId());// 81 0B B9 03
		writeInt(character.getSubId());// 00 80 00 0A
		writeInt(stance);// 01 00 00 00 1 в битве, 0спокоен
		writeByte(0);
	}
}
