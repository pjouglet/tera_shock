package tera.gameserver.manager;

import rlib.logging.GameLogger;
import rlib.logging.GameLoggers;
import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Менеджер логирования игровых событий.
 *
 * @author Ronn
 */
public final class GameLogManager
{
	private static final Logger log = Loggers.getLogger(GameLogManager.class);

	private static GameLogManager instance;

	public static GameLogManager getInstance()
	{
		if(instance == null)
			instance = new GameLogManager();

		return instance;
	}

	/** логер событий связанных с итемами игроков */
	private GameLogger itemLog;
	/** логер событий о выполнении квестов игроками */
	private GameLogger questLog;
	/** логгер событий о получении опыта игроками */
	private GameLogger expLog;

	private GameLogManager()
	{
		itemLog = GameLoggers.getLogger("Item Log");
		questLog = GameLoggers.getLogger("Quest Log");
		expLog = GameLoggers.getLogger("Exp Log");

		log.info("initialized.");
	}

	/**
	 * Запись в лог опыта.
	 */
	public void writeExpLog(String log)
	{
		expLog.write(log);
	}

	/**
	 * Запись в лог итемов.
	 */
	public void writeItemLog(String log)
	{
		itemLog.write(log);
	}

	/**
	 * Запись в лог квестов.
	 */
	public void writeQuestLog(String log)
	{
		questLog.write(log);
	}
}
