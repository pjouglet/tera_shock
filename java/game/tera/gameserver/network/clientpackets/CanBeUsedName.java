package tera.gameserver.network.clientpackets;

import tera.Config;
import tera.gameserver.network.serverpackets.ResultCheckName;

/**
 * Запрос клиента на проверку корректности имени.
 *
 * @author Ronn
 */
public class CanBeUsedName extends ClientPacket
{
	public static final int CREATE_PLAYER = 1;
	public static final int CREATE_GUILD = 2;
	public static final int INPUT_GUILD_TITLE = 13;
	public static final int INPUT_GUILD_MESSAGE = 12;
	public static final int INPUT_PLAYER_TITLE = 11;
	public static final int INPUT_RANG_NAME = 17;
	public static final int CHANGE_RANG_NAME = 6;

	/** имя, которое будет проверятся */
	private String name;

	/** тип */
	private int type;

	@Override
	public void finalyze()
	{
		name = null;
	}

	protected void readImpl()
	{
		readInt();// 01 00 0C 00
		readInt();// 0A 00 00 00
		readInt();// 0C 00 00 00
		readShort();// 16 00
		type = readInt();// 02 00 00 00 1 для перса, 2 гильдия
		name = readString();
	}

	@Override
	protected void runImpl()
	{
		switch(type)
		{
			case CHANGE_RANG_NAME:
			case CREATE_GUILD:
			case CREATE_PLAYER:
			case INPUT_GUILD_TITLE:
			case INPUT_PLAYER_TITLE:
			case INPUT_RANG_NAME:
			{
				if(!Config.checkName(name))
					return;
			}
		}

		// ложим на отправку
		owner.sendPacket(ResultCheckName.getInstance(name, type), true);
	}
}
