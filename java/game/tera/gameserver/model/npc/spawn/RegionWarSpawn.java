package tera.gameserver.model.npc.spawn;

import org.w3c.dom.Node;

import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.Strings;
import rlib.util.VarTable;

import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWarNpc;
import tera.gameserver.model.Guild;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Модель спавна контрольных точек в битве регионов.
 *
 * @author Ronn
 */
public class RegionWarSpawn extends NpcSpawn
{
	/** владеющая гильдия */
	private Guild owner;

	/** регион, в котром учавствует НПС */
	private Region region;

	/** название точки */
	private String name;

	/** позиция НПС для чата */
	private String chatLoc;

	public RegionWarSpawn(Node node, VarTable vars, NpcTemplate template, Location location, int respawn, int random, int minRadius, int maxRadius, ConfigAI config, NpcAIClass aiClass)
	{
		super(node, vars, template, location, respawn, random, minRadius, maxRadius, config, aiClass);

		this.name = vars.getString("name", Strings.EMPTY);

		String regionId = vars.getString("regionId", Strings.EMPTY);

		this.chatLoc = getPointMessage(name, regionId, location.getX(), location.getY(), location.getZ());
	}

	/**
	 * @param owner владеющая гильдия.
	 */
	public void setOwner(Guild owner)
	{
		this.owner = owner;
	}

	/**
	 * @param region регион.
	 */
	public void setRegion(Region region)
	{
		this.region = region;
	}

	/**
	 * @return владеющая гильдия.
	 */
	public Guild getOwner()
	{
		return owner;
	}

	/**
	 * @return регион.
	 */
	public Region getRegion()
	{
		return region;
	}

	/**
	 * Спавн моба.
	 */
	public synchronized void doSpawn()
	{
		// если респ остановлен, выходим
		if(isStoped())
			return;

		// если уже отспавненный есть, выходим
		if(spawned != null)
		{
			log.warning(this, new Exception("found duplicate spawn"));
			return;
		}

		// если есть ссылка на таск респавна
		if(schedule != null)
		{
			// выключаем
			schedule.cancel(false);
			// зануляем
			schedule = null;
		}

		// получаем мертвого нпс
		Npc newNpc = getDead();

		// получаем интерфейс для взаимодействия
		RegionWarNpc regionNpc = (RegionWarNpc) newNpc;

		// получаем локацию спавна
		Location location = getLocation();

		// если мертвого нпс нету
		if(newNpc == null)
		{
			// создаем нового
			newNpc = template.newInstance();

			// получаем из него интерфейс
			regionNpc = (RegionWarNpc) newNpc;

			// запоминаем спавн
			newNpc.setSpawn(this);

			// создаем АИ ему
			newNpc.setAi(aiClass.newInstance(newNpc, config));

			// установка владельца гильдии
			regionNpc.setGuildOwner(owner);

			// устанавливаем регион
			regionNpc.setRegion(region);

			// точка спавна
			Location spawnLoc = null;

			// если рандоминизированная
			if(maxRadius > 0)
				// генерируем точку
				spawnLoc = Coords.randomCoords(new Location(), location.getX(), location.getY(), location.getZ(), location.getHeading() == -1? Rnd.nextInt(35000) : location.getHeading(), minRadius, maxRadius);
			else
				// иначе делаем статичную
				spawnLoc = new Location(location.getX(), location.getY(), location.getZ(), location.getHeading() == -1? Rnd.nextInt(0, 65000) : location.getHeading());

			// вносим ид континента
			spawnLoc.setContinentId(location.getContinentId());

			// спавним в мир
			newNpc.spawnMe(spawnLoc);
		}
		else
		{
			// зануляем мертвого нпс
			setDead(null);

			// переинициализиуем старого
			newNpc.reinit();

			// установка владельца гильдии
			regionNpc.setGuildOwner(owner);

			// устанавливаем регион
			regionNpc.setRegion(region);

			// рассчитываем точку спавна
			Location spawnLoc = null;

			// если точка рандомная
			if(maxRadius > 0)
				// рассчитываем новую
				spawnLoc = Coords.randomCoords(newNpc.getSpawnLoc(), location.getX(), location.getY(), location.getZ(), location.getHeading() == -1? Rnd.nextInt(35000) : location.getHeading(), minRadius, maxRadius);
			else
				// берем старую
				spawnLoc = newNpc.getSpawnLoc();

			// вносим ид континента
			spawnLoc.setContinentId(location.getContinentId());

			// спавним в мир
			newNpc.spawnMe(spawnLoc);
		}

		// сохраняем отспавненное нпс
		setSpawned(newNpc);
	}

	/**
	 * @return название точки.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Формирование сообщение с точкой.
	 *
	 * @param message сообщение.
	 * @param x координата точки.
	 * @param y координата точки.
	 * @param z координата точки.
	 */
	private String getPointMessage(String name, String regionId, float x, float y, float z)
	{
		// формируем строку с точкой и сообщением
		StringBuilder builder = new StringBuilder("<FONT FACE=\"$ChatFont\" SIZE=\"18\" COLOR=\"#FF0000\" KERNING=\"0\"> <A HREF=\"asfunction:chatLinkAction,3#####");

		// добавляем ид региона
		builder.append(regionId);

		// добавляем координаты
		builder.append('@').append(x).append(',').append(y).append(',').append(z);

		// добавляем разделение
		builder.append("\">&lt;");

		// добавляем сообщение
		builder.append(name);

		// добавляем окончание
		builder.append("&gt;</A></FONT>");

		return builder.toString();
	}

	/**
	 * @return позиция для чата.
	 */
	public String getChatLoc()
	{
		return chatLoc;
	}
}
