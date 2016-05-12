package tera.gameserver.manager;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.Config;
import tera.gameserver.document.DocumentAnnounce;
import tera.gameserver.model.SayType;
import tera.gameserver.model.listeners.PlayerSelectListener;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.tasks.AnnounceTask;

/**
 * Менеджер анонсов сервера.
 *
 * @author Ronn
 * @created 29.03.2012
 */
public final class AnnounceManager implements PlayerSelectListener
{
	private static final Logger log = Loggers.getLogger(AnnounceManager.class);

	private static AnnounceManager instance;

	public static AnnounceManager getInstance()
	{
		if(instance == null)
			instance = new AnnounceManager();

		return instance;
	}

	/** интервальные анонсы */
	private Array<AnnounceTask> runingAnnouncs;

	/** стартовые анонсы */
	private Array<String> startAnnouncs;

	/** документ, работающий с хмл */
	private DocumentAnnounce document;

	private AnnounceManager()
	{
		// создаем список периодических аннонсов
		runingAnnouncs = Arrays.toArray(AnnounceTask.class);

		// создаем список стартовых аннонсов
		startAnnouncs = Arrays.toArray(String.class);

		// создаем парсер аннонсов с файла
		document = new DocumentAnnounce(new File(Config.SERVER_DIR + "/data/announces.xml"));

		// устанавливаем контейнеры аннонсов
		document.setRuningAnnouncs(runingAnnouncs);
		document.setStartAnnouncs(startAnnouncs);

		//парсим
		document.parse();

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// добавляемся на прослушку
		eventManager.addPlayerSelectListener(this);

		log.info("loaded " + startAnnouncs.size() + " start announces and " + runingAnnouncs.size() + " running announces.");
	}

	/**
	 * @return список периодических аннонсов.
	 */
	public final Array<AnnounceTask> getRuningAnnouncs()
	{
		return runingAnnouncs;
	}

	/**
	 * @return список стартовых аннонсов.
	 */
	public final Array<String> getStartAnnouncs()
	{
		return startAnnouncs;
	}

	/**
	 * Сохранение изминений.
	 */
	public synchronized final void save()
	{
		document.save();
	}

	/**
	 * Отправка стартовых аннонсов игроку.
	 *
	 * @param player игрок, которому нужно отправить стартовый аннонс.
	 */
	public final void showStartAnnounce(Player player)
	{
		// получаем список стартовых аннонсов
		Array<String> startAnnouncs = getStartAnnouncs();

		// получаем их массив
		String[] array = startAnnouncs.array();

		// отправляем по очереди стартовые аннонсы
		for(int i = 0, length = startAnnouncs.size(); i < length; i++)
			player.sendPacket(CharSay.getInstance(Strings.EMPTY, array[i], SayType.NOTICE_CHAT, 0, 0), true);
	}

	@Override
	public void onSelect(Player player)
	{
		showStartAnnounce(player);
	}
}
