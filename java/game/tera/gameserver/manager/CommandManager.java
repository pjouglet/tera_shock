package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.gameserver.model.playable.Player;
import tera.gameserver.scripts.commands.Command;
import tera.gameserver.scripts.commands.CommandType;

/**
 * Менеджер обработки команд из чата.
 *
 * @author Ronn
 */
public final class CommandManager
{
	private static final Logger log = Loggers.getLogger(CommandManager.class);

	private static CommandManager instance;

	public static CommandManager getInstance()
	{
		if(instance == null)
			instance = new CommandManager();

		return instance;
	}

	/** таюлица обработчиков команд */
	private Table<String, Command> commands;

	private CommandManager()
	{
		// создаем таблицу команд и обработчиков
		commands = Tables.newObjectTable();

		// регистрируем доступные команды
		for(CommandType cmds : CommandType.values())
			registerCommands(cmds.newInstance());

		log.info("loaded " + commands.size() + " commands.");
	}

	/**
	 * Обработка команды.
	 *
	 * @param player игрок, который ввел команду.
	 * @param command название команды.
	 * @param values параметры команды.
	 * @return обработана ли команда.
	 */
	public final boolean execute(Player player, String cmd, String values)
	{
		// если команды нет, выходим
		if(cmd == null)
			return false;

		// получаем обработчика команды
		Command command = commands.get(cmd);

		// если обработчика нет, выходим
		if(command == null)
			return false;

		// если игрок не имеет доступа к обработчику, выходим
		if(player.getAccessLevel() < command.getAccess())
			return false;

		try
		{
			// обрабатываем
			command.execution(cmd, player, values);
		}
		catch(Exception e)
		{
			log.warning(e);
		}

		return true;
	}

	/**
	 * Регистрация обработчика команды.
	 *
	 * @param command обработчик команды.
	 */
	public final void registerCommands(Command command)
	{
		String[] cmmds = command.getCommands();

		for(String cmd : cmmds)
			if(commands.containsKey(cmd))
				log.warning("found a duplicate command " + cmd + ".");
			else
				commands.put(cmd, command);
	}

	/**
	 * @return кол-во обрабатываемых команд.
	 */
	public final int size()
	{
		return commands.size();
	}
}