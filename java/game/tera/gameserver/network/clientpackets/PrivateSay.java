package tera.gameserver.network.clientpackets;

import tera.gameserver.model.MessageType;
import tera.gameserver.model.SayType;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharSay;

/**
 * Клиентский пакет с приват сообщением.
 *
 * @author Ronn
 */
public class PrivateSay extends ClientPacket
{
	/** отправитель */
	private Player player;

	/** ник игрока, которому отписали */
	private String name;
	/** содержание сообщения */
	private String text;

	@Override
	public void finalyze()
	{
		name = null;
		text = null;
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		readByte();
		readByte();
		name = readString();//кому Ник
		text = readString(); //текст
	}

	@Override
	protected void runImpl()
	{
		if(player == null || player.getName().equals(name))
			return;

		Player enemy = World.getAroundByName(Player.class, player, name);

		if(enemy == null)
			enemy = World.getPlayer(name);

		if(enemy == null || !enemy.isConnected())
		{
			player.sendMessage(MessageType.THAT_CHARACTER_ISNT_ONLINE);
			return;
		}

		player.sendPacket(CharSay.getInstance(player.getName(), text, SayType.PRIVATE_CHAT, player.getObjectId(), player.getSubId()), true);
		enemy.sendPacket(CharSay.getInstance(player.getName(), text, SayType.WHISHPER_CHAT, player.getObjectId(), player.getSubId()), true);
	}
}
