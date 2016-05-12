package tera.gameserver.scripts.commands;

import java.util.Arrays;

/**
 * Фундамент для реализации обработчика команд.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public abstract class AbstractCommand implements Command
{
	/** список команд, которые выполняются этим обработчиком */
	private String[] commands;

	/** уровень прав доступа игрока, для выполнения команды */
	private int access;

	/**
	 * @param access минимальный кровень доступа.
	 * @param commands список обрабатываемых команд.
	 */
	public AbstractCommand(int access, String[] commands)
	{
		this.commands = commands;
		this.access = access;
	}

	@Override
	public int getAccess()
	{
		return access;
	}

	@Override
	public String[] getCommands()
	{
		return commands;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "  " + (commands != null ? "commands = " + Arrays.toString(commands) + ", " : "") + "access = " + access;
	}
}
