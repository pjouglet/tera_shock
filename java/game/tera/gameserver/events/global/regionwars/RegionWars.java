package tera.gameserver.events.global.regionwars;

import java.io.File;

import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.Config;
import tera.gameserver.document.DocumentRegionWar;
import tera.gameserver.events.EventType;
import tera.gameserver.events.global.AbstractGlobalEvent;
import tera.gameserver.model.Guild;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Модель ивента битв за территорию.
 *
 * @author Ronn
 */
public class RegionWars extends AbstractGlobalEvent
{
	public static final String BATTLE_POINT = "region_war_battle_point";

	public static final int REWARD_INTERVAL = 300000;

	private static final String EVENT_NANE = "Region Wars";

	/** все зарегестрированные гильдии */
	private final Array<Guild> registerGuilds;

	/** все гильдии владельцы */
	private final Array<Guild> ownerGuilds;

	/** список учавствующих регионов */
	private Region[] regions;

	public RegionWars()
	{
		this.registerGuilds = Arrays.toConcurrentArraySet(Guild.class);
		this.ownerGuilds = Arrays.toConcurrentArraySet(Guild.class);
	}

	@Override
	public void addLinks(Array<Link> links, Npc npc, Player player)
	{
		// получаем все зарегестрированные региона
		Region[] regions = getRegions();

		// спрашиваем у всех доступные ссылки
		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].addLinks(links, npc, player);
	}

	/**
	 * Добавления гильдии в список владеющих регионом.
	 *
	 * @param guild владелющая регионом гильдия.
	 */
	public void addOwnerGuild(Guild guild)
	{
		ownerGuilds.add(guild);
	}

	/**
	 * Добавление в зарегестрированные гильдии на битву за регион.
	 *
	 * @param guild регистрируемая гильдия.
	 */
	public void addRegisterGuild(Guild guild)
	{
		// получаем список зарегестрированных гильдий
		Array<Guild> registerGuilds = getRegisterGuilds();

		// если такая гильдия еще не внесена
		if(!registerGuilds.contains(guild))
			// вносим
			registerGuilds.add(guild);
	}

	/**
	 * @return список зарегестрированных гильдий.
	 */
	public Array<Guild> getRegisterGuilds()
	{
		return registerGuilds;
	}

	@Override
	public String getName()
	{
		return EVENT_NANE;
	}

	public Region[] getRegions()
	{
		return regions;
	}

	@Override
	public EventType getType()
	{
		return EventType.REGION_WARS;
	}

	/**
	 * @return являетсял и гильдия владельцем какого-нибудь региона.
	 */
	public boolean isOwnerGuild(Guild guild)
	{
		return ownerGuilds.contains(guild);
	}

	/**
	 * @return зарегестрированна ли уже эта гильдия на какую-нибудь битву.
	 */
	public boolean isRegisterGuild(Guild guild)
	{
		return registerGuilds.contains(guild);
	}

	@Override
	public boolean onLoad()
	{
		// создаем список регионов
		Array<Region> regions = Arrays.toArray(Region.class);

		// парсим регионы
		regions.addAll(new DocumentRegionWar(new File(Config.SERVER_DIR + "/data/events/region_wars/region_wars.xml"), this).parse());

		// сжимаем список
		regions.trimToSize();

		// вносим в ивент
		setRegions(regions.array());

		// подготавливаем регионы
		for(Region region : regions)
			region.prepare();

		return true;
	}

	/**
	 * Удаление из списка владеющих регионами гильдии.
	 *
	 * @param guild удаляемая гильдия.
	 */
	public void removeOwnerGuild(Guild guild)
	{
		ownerGuilds.fastRemove(guild);
	}

	/**
	 * Удаление из списка харегестрированных на битву гильдий.
	 *
	 * @param guild удаляемая гильдия.
	 */
	public void removeRegisterGuild(Guild guild)
	{
		registerGuilds.fastRemove(guild);
	}

	/**
	 * @param regions список действующих регионов.
	 */
	private void setRegions(Region[] regions)
	{
		this.regions = regions;
	}
}
