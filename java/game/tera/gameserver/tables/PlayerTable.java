package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.document.DocumentPlayer;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.templates.PlayerTemplate;

/**
 * Таблица шаблонов игроков.
 *
 * @author Ronn
 */
public final class PlayerTable
{
	private static final Logger log = Loggers.getLogger(PlayerTable.class);

	private static PlayerTable instance;

	public static PlayerTable getInstance()
	{
		if(instance == null)
			instance = new PlayerTable();

		return instance;
	}

	/** таблица всех темплейтов */
	private PlayerTemplate[][][] templates;

	private PlayerTable()
	{
		templates = new PlayerTemplate[Sex.SIZE][Race.SIZE][PlayerClass.length];

		int counter = 0;

		Array<PlayerTemplate> parsed = new DocumentPlayer(new File(Config.SERVER_DIR + "/data/player_templates.xml")).parse();

		for(PlayerTemplate temp : parsed)
			templates[temp.getSex().ordinal()][temp.getRace().ordinal()][temp.getPlayerClass().ordinal()] = temp;

		for(PlayerTemplate[][] matrix : templates)
			for(PlayerTemplate[] array : matrix)
				for(PlayerTemplate template : array)
					if(template != null)
						counter++;

		log.info("loaded " + counter + " player templates.");
	}

	/**
	 * Получение темплейта игрока.
	 *
	 * @param playerClass класс игрока.
	 * @param race раса игрока.
	 * @param sex пол игрока.
	 * @return соответствующий темплейт.
	 */
	public final PlayerTemplate getTemplate(PlayerClass playerClass, Race race, Sex sex)
	{
		return templates[sex.ordinal()][race.ordinal()][playerClass.getId()];
	}
}
