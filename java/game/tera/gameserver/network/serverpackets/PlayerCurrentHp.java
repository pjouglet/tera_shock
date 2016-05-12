package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, обновляющий инфу об изменении текущего состояния хп игрока
 * 
 * @author Ronn
 */
public class PlayerCurrentHp extends ServerPacket
{
	/** просто прибавка */
	public static final int INCREASE = 0;
	/** прибавка с плюсом */
	public static final int INCREASE_PLUS = 1;

	private static final ServerPacket instance = new PlayerCurrentHp();
	
	public static PlayerCurrentHp getInstance(Character player, Character attacked, int countChange, int type)
	{
		PlayerCurrentHp packet = (PlayerCurrentHp) instance.newInstance();
		
		packet.player = player;
		packet.countChange = countChange;
		packet.type = type;
		packet.attacked = attacked;
		
		return packet;
	}
	
	/** игрок */
	private Character player;
	/** тот кто влиял */
	private Character attacked;

	/** насколько хп изменилось */
	private int countChange;
	/** тип изменения */
	private int type;
	
	@Override
	public void finalyze()
	{
		player = null;
		attacked = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_CURRENT_HP;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(player.getCurrentHp()); // сколько сейчас
		writeInt(player.getMaxHp()); // сколько размер полоски
		writeInt(countChange); // на сколько увеличилось хп
		writeInt(type);// если 01 будет зелёный плюсик
		writeInt(player.getObjectId()); // наш ид
		writeInt(player.getSubId()); // наш сабид

		if(attacked != null)
		{
			writeInt(attacked.getObjectId()); // ИД того кто вас хильнул
			writeInt(attacked.getSubId()); // САБ ИД того кто вас хильнул
		}
		else
		{
			writeInt(0); // ИД того кто вас хильнул
			writeInt(0); // САБ ИД того кто вас хильнул
		}
	}
}