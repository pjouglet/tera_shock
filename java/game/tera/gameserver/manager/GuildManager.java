package tera.gameserver.manager;

import java.util.Iterator;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Strings;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.gameserver.IdFactory;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildIcon;
import tera.gameserver.model.GuildMember;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.GuildRankLaw;
import tera.gameserver.model.playable.Player;

/**
 * Менеджер для работы с гильдиями.
 *
 * @author Ronn
 */
public final class GuildManager
{
	private static final Logger log = Loggers.getLogger(GuildManager.class);

	private static GuildManager instance;

	public static GuildManager getInstance()
	{
		if(instance == null)
			instance = new GuildManager();

		return instance;
	}

	/** таблица кланов */
	private Table<IntKey, Guild> guilds;

	/** таблица иконок */
	private Table<String, GuildIcon> icons;

	private GuildManager()
	{
		// создаем таблицу гильдий
		guilds = Tables.newConcurrentIntegerTable();

		// созадем таблицу лого гильдий
		icons = Tables.newConcurrentObjectTable();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// загружаем гильдии
		dbManager.restoreGuilds(guilds);

		// счетчик удаленных гильдий
		int remove = 0;

		// перебираем загруженные гильдии
		for(Iterator<Guild> iterator = guilds.iterator(); iterator.hasNext();)
		{
			// получаем гильдию
			Guild guild = iterator.next();

			// загружаем ранги гильдии
			dbManager.restoreGuildRanks(guild);

			// загружаем итемы в банке гильдии
			dbManager.restoreGuildBankItems(guild);

			// загружаем членов гильдии
			dbManager.restoreGuildMembers(guild);

			// вносим иконку гильдии
			putIcon(guild.getIcon());

			// если гильдия живая, пропускаем
			if(guild.size() > 1 && guild.getLeader() != null)
				continue;

			// удаляем гильдию из бд
			dbManager.removeGuild(guild);

			// удаляем всех членов гильдии
			dbManager.removeGuildMembers(guild);

			// удаляем из таблицы гильдию
			iterator.remove();

			remove++;
		}

		if(remove > 0)
			log.info("remove " + remove + " guilds.");

		log.info("loaded " + guilds.size() + " guilds.");
	}

	/**
	 * Создание нового кана.
	 *
	 * @param name название клана.
	 * @param leader лидер клана.
	 * @return новый клан.
	 */
	public synchronized Guild createNewGuild(String name, Player leader)
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// если название гильдии не подходит, выходим
		if(!dbManager.checkGuildName(name))
			return null;

		// получаем фабрику ИД гильдий
		IdFactory idFactory = IdFactory.getInstance();

		// созадем новую гильдию
		Guild guild = new Guild(name, Strings.EMPTY, Strings.EMPTY, idFactory.getNextGuildId(), 1, new GuildIcon(Strings.EMPTY, new byte[0]));

		// создаем ранг ГМ
		GuildRank rank = GuildRank.newInstance("GuildMaster", GuildRankLaw.GUILD_MASTER, GuildRank.GUILD_MASTER);

		// если не удалось создать ранг, выходим
		if(!dbManager.createGuildRank(guild, rank))
			return null;

		// вносим ранг в ГИ
		guild.addRank(rank);

		// создаем ранг обычного члена группы
		rank = GuildRank.newInstance("Member", GuildRankLaw.MEMBER, GuildRank.GUILD_MEMBER);

		// если не удалось его создать, выходим
		if(!dbManager.createGuildRank(guild, rank))
			return null;

		// вносим ранг в ГИ
		guild.addRank(rank);

		// если не удалось создать ГИ в БД, выходим
		if(!dbManager.insertGuild(guild))
			return null;

		// вносим ГИ лидеру
		leader.setGuild(guild);
		// устанавлимваем ранг в ГИ
		leader.setGuildRank(guild.getRank(GuildRank.GUILD_MASTER));

		// создаем члена ГИ
		GuildMember member = GuildMember.newInstance();

		member.setClassId(leader.getClassId());
		member.setLevel(leader.getLevel());
		member.setName(leader.getName());
		member.setObjectId(leader.getObjectId());
		member.setOnline(true);
		member.setRaceId(leader.getRaceId());
		member.setRank(guild.getRank(leader.getGuildRankId()));
		member.setSex(leader.getSexId());

		// вносим в ГИ
		guild.addMember(member);
		guild.addOnline(leader);

		// обновляем игрока в БД
		dbManager.updatePlayerGuild(leader);

		// вносим гильдию в таблицу гильдий
		guilds.put(guild.getId(), guild);

		return guild;
	}

	/**
	 * Получение клана по ид.
	 *
	 * @param id ид клана.
	 * @return клан.
	 */
	public Guild getGuild(int id)
	{
		return guilds.get(id);
	}

	/**
	 * @param name название иконки.
	 * @return иконка.
	 */
	public GuildIcon getIcon(String name)
	{
		return icons.get(name);
	}

	/**
	 * @param icon новая иконка.
	 */
	public void putIcon(GuildIcon icon)
	{
		if(icon.hasIcon())
			icons.put(icon.getName(), icon);
	}
}
