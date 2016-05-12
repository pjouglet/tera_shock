package tera.gameserver.manager;

import java.io.File;

import rlib.geoengine.GeoConfig;
import rlib.geoengine.GeoMap;
import rlib.geoengine.GeoMap3D;
import rlib.geoengine.GeoQuard;
import rlib.logging.ByteGameLogger;
import rlib.logging.GameLoggers;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.Config;

/**
 * Менеджер геодаты.
 *
 * @author Ronn
 */
public final class GeoManager
{
	private static final Logger log = Loggers.getLogger(GeoManager.class);

	private static final ByteGameLogger geoLog = GameLoggers.getByteLogger("GeoManager");

	private static GeoManager instance;

	public static GeoManager getInstance()
	{
		if(instance == null)
			instance = new GeoManager();

		return instance;
	}

	/**
	 * Запись в лог координат.
	 *
	 * @param continentId ид континента.
	 * @param x координата точки.
	 * @param y координата точки.
	 * @param z координата точки.
	 */
	public static void write(int continentId, float x, float y, float z)
	{
		if(!Config.DEVELOPER_GEO_LOGING)
			return;

		geoLog.lock();
		try
		{
			geoLog.writeByte(0);
			geoLog.writeByte(continentId);
			geoLog.writeFloat(x);
			geoLog.writeFloat(y);
			geoLog.writeFloat(z);
		}
		finally
		{
			geoLog.unlock();
		}
	}

	/** геоданные */
	private GeoMap[] geodata;

	private GeoManager()
	{
		geodata = new GeoMap[Config.WORLD_CONTINENT_COUNT];

		// начинаем заполнение структуры
		for(int i = 0, length = geodata.length; i < length; i++)
		{
			// создаем реализацию гео карты
			GeoMap3D geoImpl = new GeoMap3D(Config.GEO_CONFIG);

			// вносим его в структуру гео
			geodata[i] = geoImpl;

			// пробуем получить файл с необходимыми данными
			File file = new File(Config.SERVER_DIR + "/data/geodata_" + (i + 1) + ".dat");

			// если файла нет, пропускаем
			if(!file.exists())
				continue;

			// импортируем файл
			geoImpl.importTo(file);
		}

		log.info("initialized.");
	}

	/**
	 * Получаем высоту в нужных нам координатах.
	 *
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @return высота в этой точке.
	 */
	public float getHeight(int continentId, float x, float y, float z)
	{
		// получаем гео нужного континента
		GeoMap geo = geodata[continentId];

		// получаем квадрат геодаты в этих координатах
		GeoQuard quard = geo.getGeoQuard(x, y, z);

		// возвращаем высоту
		return quard == null? z : quard.getHeight();
	}

	/**
	 * Получение столба квадратов геодаты.
	 *
	 * @param continentId ид континента.
	 * @param x координата.
	 * @param y координата.
	 * @return столб гео квадратов.
	 */
	public GeoQuard[] getQuards(int continentId, float x, float y)
	{
		// получаем гео нужного континента
		GeoMap geo = geodata[continentId];

		// получаем гео конфиг
		GeoConfig config = Config.GEO_CONFIG;

		// рассчитываем координаты
		int i = (int) (x / config.getQuardSize() + config.getOffsetX());
		int j = (int) (y / config.getQuardSize() + config.getOffsetY());

		// возвращаем
		return ((GeoMap3D) geo).getQuards(i, j);
	}
}
