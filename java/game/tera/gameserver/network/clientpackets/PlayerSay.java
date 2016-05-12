package tera.gameserver.network.clientpackets;

import rlib.logging.GameLogger;
import rlib.logging.GameLoggers;
import tera.Config;
import tera.gameserver.manager.CommandManager;
import tera.gameserver.model.SayType;
import tera.gameserver.model.playable.Player;
import tera.remotecontrol.handlers.LoadChatHandler;

/**
 * Клиентский пакет с сообщением в чат от игрока.
 *
 * @author Ronn
 */
public class PlayerSay extends ClientPacket
{
	/** логгер чата */
	private static final GameLogger log = GameLoggers.getLogger("Chat");

	/** игрок */
	private Player player;

	/** текст сообщения */
	private String text;

	/** тип сообщения */
	private SayType type;

	@Override
	public void finalyze()
	{
		player = null;
		text = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		readShort();

		type = SayType.valueOf(readInt());  // тейп
		text = readString();  // читаем текст

		player = owner.getOwner();
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

	//	System.out.println(text);

		String message = player.getName() + ": " + text;

		if(Config.DIST_CONTROL_ENABLED)
			LoadChatHandler.add(message);

		log.write(message);

		if(text.indexOf("--") == 6)// проверяем на команду
		{
			text = text.substring(8, text.length() - 7);// утрезаем лишнее

			String[] commands = text.split(" ", 2);

			String command = commands[0];
			String values = null;

			if(commands.length > 1)
				values = commands[1];

			// получаем менеджер команд
			CommandManager commandManager = CommandManager.getInstance();

			if(!commandManager.execute(player, command, values))
				player.getAI().startSay(text, type);

			return;
		}

		player.getAI().startSay(text, type);
	}
}