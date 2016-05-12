package tera.gameserver.model;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Nameable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.GuildManager;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.GuildBank;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.util.Identified;

/**
 * Модель клана в Тера
 *
 * @author Ronn
 */
public final class Guild implements Nameable, Identified
{
	private static final Logger log = Loggers.getLogger(Guild.class);

	 /** таблица рангов */
	 private final Table<IntKey, GuildRank> ranks;

	 /** список всех участников клана */
	 private final Array<GuildMember> members;
	 /** онлаин игроки клана */
	 private final Array<Player> online;
	 /** набор логов гильдии */
	 private final Array<GuildLog> logs;

	 /** банк гильдии */
	 private final Bank bank;

	/** название клана */
	 private String name;
	 /** титул клана */
	 private String title;
	 /** сообщение гильдии */
	 private String message;

	 /** иконка гильдии  */
	 private GuildIcon icon;

	 /** лидер гильдии */
	 private GuildMember guildLeader;

	 /** ид клана */
	 private int id;
	 /** уровень клана */
	 private int level;

	 /**
	  * @param name название клана.
	  * @param title титул гильдии.
	  * @param message сообщение.
	  * @param id ид клана.
	  * @param level уровень клана.
	  * @param icon иконка клана.
	  */
	public Guild(String name, String title, String message, int id, int level, GuildIcon icon)
	{
		this.name = name;
		this.title = title;
		this.message = message;
		this.id = id;
		this.level = level;
		this.icon = icon;

		this.members = Arrays.toConcurrentArray(GuildMember.class);
		this.online = Arrays.toConcurrentArray(Player.class);
		this.logs = Arrays.toConcurrentArray(GuildLog.class);
		this.ranks = Tables.newIntegerTable();
		this.bank = GuildBank.newInstance(this);
	}

	/**
	 * Добавление нового мембера клана.
	 *
	 * @param member новый мембер.
	 */
	public void addMember(GuildMember member)
	{
		members.add(member);

		if(member.getRankId() == GuildRank.GUILD_MASTER)
			guildLeader = member;
	}

	/**
	 * Добавление игрока в онлаин.
	 *
	 * @param player добавляемый игрок.
	 */
	public void addOnline(Player player)
	{
		online.add(player);
	}

	/**
	 * Добавление ранга в таблицу.
	 *
	 * @param rank новый ранг.
	 */
	public void addRank(GuildRank rank)
	{
		// получаем таблицу рангов
		Table<IntKey, GuildRank> ranks = getRanks();

		// проверяем на дублирование
		if(ranks.containsKey(rank.getIndex()))
		{
			log.warning("found duplicate " + rank + " for guild " + name);
			return;
		}

		// вносим новый ранг
		ranks.put(rank.getIndex(), rank);
	}

	/**
	 * Изменение заметки о игроке в гилд листе.
	 *
	 * @param player игрок.
	 * @param newNote новая пометка.
	 */
	public synchronized void changeMemberNote(Player player, String newNote)
	{
		// получаем члена гильдии
		GuildMember member = getMember(player.getObjectId());

		// если такого нашли
		if(member != null)
		{
			// получаем его старую заметку
			String old = player.getGuildNote();

			// вносим новюу заметку
			player.setGuildNote(newNote);

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// если обновление не вышло
			if(!dbManager.updatePlayerGuildNote(player))
				// возвращаем старую
				player.setGuildNote(old);
			else
			{
				// применяем новую
				member.setNote(newNote);

				// обновляем гильдию
				player.updateGuild();
			}
		}
	}

	/**
	 * Изменить набор прав у ранга.
	 *
	 * @param player изменяющий игрок.
	 * @param index индекс ранга.
	 * @param law набор прав.
	 */
	public synchronized void changeRank(Player player, int index, String name, GuildRankLaw law)
	{
		if(law == null || index == GuildRank.GUILD_MEMBER || index == GuildRank.GUILD_MASTER)
			return;

		// получаем целевой ранг
		GuildRank rank = ranks.get(index);

		// если такой есть
		if(rank != null)
		{
			// меняем ему права
			rank.setLaw(law);

			// обновляем имя
			rank.setName(name);

			// обновление флагов
			rank.prepare();

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// обновляем ранг в БД
			dbManager.updateGuildRank(this, rank);

			// обнолвяем окно гильдии
			player.updateGuild();

			// отправляем сообщение
			player.sendMessage("Вы изменили ранг, переоткройте окно гильдии.");
		}
	}

	/**
	 * Создание нового ранга.
	 *
	 * @param player создающий ранг.
	 * @param name название ранга.
	 */
	public synchronized void createRank(Player player, String name)
	{
		// получаем таблицу рангов
		Table<IntKey, GuildRank> ranks = getRanks();

		// если привышен лимит по рангам или некорректное название ранга, выходим
		if(ranks.size() > 10 || !Config.checkName(name))
			return;

		// проверяем на наличие дубля
		for(GuildRank rank : ranks)
			if(name.equalsIgnoreCase(rank.getName()))
				return;

		// определяем стартовый индекс ранга
		int index = 4;

		// находим первый свободный
		while(ranks.containsKey(index))
			index++;

		// если индекс привышил лимит, выходим
		if(index > 128)
			return;

		// создаем новый ранг
		GuildRank rank = GuildRank.newInstance(name, GuildRankLaw.MEMBER, index);

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// создаем запись в БД
		dbManager.createGuildRank(this, rank);

		// вносим ранг в таблицу
		ranks.put(index, rank);

		// обновляем окно гильдии
		player.updateGuild();

		// сообщаем о создании ранга
		player.sendMessage("Вы создали новый ранг, переоткройте окно гильдии.");
	}

	/**
	 * Обработка входа в игру члена клана.
	 *
	 * @param player вошедший игрок.
	 */
	public void enterInGame(Player player)
	{
		// получаем список членов ГИ
		Array<GuildMember> members = getMembers();

		// получаем индекс в списке игрока
		int index = members.indexOf(player);

		// если такого в нем нет, выходим
		if(index < 0)
			return;

		// получаем инфу о члене ГИ
		GuildMember target = members.get(index);

		// если она есть
		if(target != null)
			// ставим флаг онлайна
			target.setOnline(true);

		// добавляем в список онлаин членов ГИ
		online.add(player);
	}

	/**
	 * Исключить участника с указанным именем.
	 *
	 * @param excluder исключающий.
	 * @param name имя исключаемого.
	 */
	public synchronized void exclude(Player excluder, String name)
	{
		// получчаем члена ГИ
		GuildMember member = getMember(name);

		// если его не нашли, делать тут нечего
		if(member == null)
			return;

		// получаем ранг члена ГИ
		GuildRank rank = member.getRank();

		// гильд мастера нельзя исключить
		if(rank.isGuildMaster())
			return;

		// зануляем ранг мемберу
		member.setRank(null);

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// записываем изменения в БД
		dbManager.updatePlayerGuild(null, member);

		// удаляем члена из списка
		members.fastRemove(member);

		// полуаем онлаин игрока
		Player player = getPlayer(member.getObjectId());

		// члена ложим в пул
		member.fold();

		// если игрок онлаин
		if(player != null)
		{
			// зануляем ему гильдию
			player.setGuild(null);

			// зануляем ему ранг
			player.setGuildRank(null);

			// обновляем для окружающих
			player.updateOtherInfo();

			// удаляем из онлаин игроков
			online.fastRemove(player);

			// удаляем надпись гильдии
			player.updateGuild();
		}

		// обновляем гильдию
		excluder.updateGuild();
	}

	/**
	 * Обработка выхода из игры члена клана.
	 *
	 * @param player вышедший игрок.
	 */
	public void exitOutGame(Player player)
	{
		// получаем члена ГИ
		GuildMember target = getMember(player.getObjectId());

		// если такой есть
		if(target != null)
		{
			// убераем флаг онлайна
			target.setOnline(false);
			// обновляем дату последнего онлайна
			target.setLastOnline((int) (System.currentTimeMillis() / 1000));
		}

		// удаляем из списка онлаин игроков
		online.fastRemove(player);
	}

	/**
	 * @return банк гильдии.
	 */
	public final Bank getBank()
	{
		return bank;
	}

	/**
	 * @return иконка гильдии.
	 */
	public final GuildIcon getIcon()
	{
		return icon;
	}

	/**
	 * @return ид клана.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return лидер клана.
	 */
	public GuildMember getLeader()
	{
		return guildLeader;
	}

	/**
	 * @return уровень гильдии.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * @return события гильдии.
	 */
	public Array<GuildLog> getLogs()
	{
		return logs;
	}

	/**
	 * Получение гильд мембера по нику.
	 *
	 * @param objectId ид игрока.
	 * @return гилд мембер.
	 */
	public GuildMember getMember(int objectId)
	{
		// получаем членов ГИ
		Array<GuildMember> members = getMembers();

		members.readLock();
		try
		{
			// получаем их список
			GuildMember[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена ГИ
				GuildMember member = array[i];

				// если это искомый, возвращаем его
				if(member != null && member.getObjectId() == objectId)
					return member;
			}

			return null;
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Получение гильд мембера по нику.
	 *
	 * @param name имя игрока.
	 * @return гилд мембер.
	 */
	public GuildMember getMember(String name)
	{
		// получаем членов ГИ
		Array<GuildMember> members = getMembers();

		members.readLock();
		try
		{
			// получаем их список
			GuildMember[] array = members.array();

			// перебираем членов ГИ
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена ГИ
				GuildMember member = array[i];

				// если это искомый, возвращаем его
				if(member != null && name.equals(member.getName()))
					return member;
			}

			return null;
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * @return список участников гильдии.
	 */
	public final Array<GuildMember> getMembers()
	{
		return members;
	}

	/**
	 * @return сообщение гильдии.
	 */
	public String getMessage()
	{
		return message;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getObjectId()
	{
		return id;
	}

	/**
	 * @return списое онлаин игроков ГИ.
	 */
	public Array<Player> getOnline()
	{
		return online;
	}

	/**
	 * Получение онлаин игрока по нику.
	 *
	 * @param objectId ид игрока.
	 * @return игрок.
	 */
	public Player getPlayer(int objectId)
	{
		// получаем список онлаин игроков
		Array<Player> online = getOnline();

		online.readLock();
		try
		{
			// получаем массив игроков
			Player[] array = online.array();

			// перебираем игроков
			for(int i = 0, length = online.size(); i < length; i++)
			{
				// получаем игрока
				Player player = array[i];

				// если это искомый игрок, возвращаем его
				if(player != null && player.getObjectId() == objectId)
					return player;
			}

			return null;
		}
		finally
		{
			online.readUnlock();
		}
	}

	/**
	 * Получение онлаин игрока по нику.
	 *
	 * @param name имя игрока.
	 * @return игрок.
	 */
	public Player getPlayer(String name)
	{
		// получаем список онлаин игроков
		Array<Player> online = getOnline();

		online.readLock();
		try
		{
			// получаем массив игроков
			Player[] array = online.array();

			// перебираем игроков
			for(int i = 0, length = online.size(); i < length; i++)
			{
				// получаем игрока
				Player player = array[i];

				// если это искомый игрок, возвращаем его
				if(player != null && name.equals(player.getName()))
					return player;
			}

			return null;
		}
		finally
		{
			online.readUnlock();
		}
	}

	/**
	 * Получение ранга по индексу.
	 *
	 * @param index индекс ранга.
	 * @return нужный ранг.
	 */
	public GuildRank getRank(int index)
	{
		// получаем таблицу рангов
		Table<IntKey, GuildRank> ranks = getRanks();

		// получаем искомый ранг
		GuildRank rank = ranks.get(index);

		// если нужного ранга нет
		if(rank == null)
			// получаем ранг обычного члена
			rank = ranks.get(GuildRank.GUILD_MEMBER);

		// если всетаки ранга не нашли, уведомляем
		if(rank == null)
			log.warning("found incorrect rank table for guild " + name);

		return rank;
	}

	/**
	 * @return набор рангов гильдии.
	 */
	public Table<IntKey, GuildRank> getRanks()
	{
		return ranks;
	}

	/**
	 * @return титул гильдии.
	 */
	public final String getTitle()
	{
		return title;
	}

	/**
	 * Обработка присоединения игрока в клан.
	 *
	 * @param player присоединяемый игрок.
	 */
	public synchronized void joinMember(Player player)
	{
		GuildMember member = GuildMember.newInstance();

		// устанавливаем гильдию
		player.setGuild(this);
		// ставим базовый ранг
		player.setGuildRank(getRank(GuildRank.GUILD_MEMBER));

		// применяем параметры игрока мемберу
		member.setClassId(player.getClassId());
		member.setLevel(player.getLevel());
		member.setName(player.getName());
		member.setNote(player.getGuildNote());
		member.setObjectId(player.getObjectId());
		member.setOnline(true);
		member.setRaceId(player.getRaceId());
		member.setRank(player.getGuildRank());
		member.setSex(player.getSexId());

		// обнолвяем для окружающих.
		player.updateOtherInfo();

		// добавляем в онлаин игроки
		online.add(player);

		// добавляем мембера
		addMember(member);

		// отображаем гильдию
		player.updateGuild();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// записываем в БД
		dbManager.updatePlayerGuild(player);
	}

	/**
	 * Обработка исключения из клана игрока.
	 *
	 * @param player исключаемый игрок.
	 */
	public synchronized void leaveMember(Player player)
	{
		// гилд мастер ливнуть не можеты
		if(player.getGuildRankId() == GuildRank.GUILD_MASTER)
			return;

		// ид игрока
		int objectId = player.getObjectId();

		// получаем мембера этого игрока
		GuildMember member = getMember(objectId);

		if(member == null)
			return;

		// удаляем мембера
		members.fastRemove(member);

		// удаляем из онлаин игроков
		online.fastRemove(player);

		// мембера ложим в пул
		member.fold();

		// зануляем гильдию
		player.setGuild(null);

		// занулям ранг
		player.setGuildRank(null);

		// обнолвяем для окружающих.
		player.updateOtherInfo();

		// пишем сообщение о выходе
		player.sendMessage("Вы вышли из гильдии.");

		// убераем надпись гильдии
		player.updateGuild();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// удаляем гильдию в БД
		dbManager.updatePlayerGuild(player);
	}

	/**
	 * Передача мастера гильдии.
	 *
	 * @param player тот, кто передает.
	 * @param name имя передоваемому.
	 */
	public synchronized void makeGuildMaster(Player player, String name)
	{
		// получаем текущий ранг игрока
		GuildRank current = player.getGuildRank();

		// если он не ГМ, выходим
		if(!current.isGuildMaster())
			return;

		// получаем члена ГИ игрока
		GuildMember playerMember = getMember(player.getObjectId());

		// получаем целевого члена ГИ
		GuildMember targetMember = getMember(name);

		// если кого-то из них не нашли, выходим
		if(playerMember == null || targetMember == null)
			return;

		// получаем ранг обычного члена ГИ
		GuildRank def = getRank(GuildRank.GUILD_MEMBER);

		// получаем ранг ГМ
		GuildRank master = getRank(GuildRank.GUILD_MASTER);

		// если что-то не нашли, выходим
		if(def == null || master == null)
			return;

		// сменяем ранги
		playerMember.setRank(def);
		targetMember.setRank(master);

		// обновляем лидера
		setGuildLeader(targetMember);

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляем их в БД
		dbManager.updatePlayerGuild(this, playerMember);
		dbManager.updatePlayerGuild(this, targetMember);

		// обновляем ранг у игрока
		player.setGuildRank(def);

		// обновляем окно ГИ
		player.updateGuild();

		// получаем целевого игрока
		Player target = getPlayer(targetMember.getObjectId());

		// если он в игре
		if(target != null)
		{
			// обновляем ему ранг
			target.setGuildRank(master);

			// обновляем окно ГИ
			target.updateGuild();
		}
	}

	/**
	 * Обновить ранк игрока с указанным ид
	 *
	 * @param player обновляющий ранк.
	 * @param rankId ид ранка.
	 */
	public synchronized void removeRank(Player player, int rankId)
	{
		// получаем удаляемый ранг
		GuildRank rank = getRank(rankId);

		// получаем ранг обычного члена ГИ
		GuildRank def = getRank(GuildRank.GUILD_MEMBER);

		// если удаляемый ранг нельзя удалить, выходим
		if(rank == null || rank.getIndex() == GuildRank.GUILD_MASTER || rank.getIndex() == GuildRank.GUILD_MEMBER)
			return;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// удаляем ранг из БД
		dbManager.removeGuildRank(this, rank);

		// удаляем ранг и членов ГИ
		dbManager.removeGuildRankForPlayer(this, def, rank);

		// удаляем ранг из таблицы рангов
		ranks.remove(rankId);

		// получаем всех членов ГИ
		Array<GuildMember> members = getMembers();

		members.readLock();
		try
		{
			// получаем массив всех членов
			GuildMember[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена ГИ
				GuildMember member = array[i];

				// если у него удаляемый ранг
				if(member.getRank() == rank)
					// устанавливаем ранг обычного члена ГИ
					member.setRank(def);
			}
		}
		finally
		{
			members.readUnlock();
		}

		// получаем список онлаин игроков
		Array<Player> online = getOnline();

		online.readLock();
		try
		{
			// получаем массив игроков
			Player[] array = online.array();

			// перебираем игроков
			for(int i = 0, length = online.size(); i < length; i++)
			{
				// получаем игрока ГИ
				Player member = array[i];

				// если у него удаляемый ранг
				if(member.getGuildRank() == rank)
				{
					// устанавливаем обычный
					member.setGuildRank(def);

					// обновляем окно ГИ
					member.updateGuild();
				}
			}
		}
		finally
		{
			online.readUnlock();
		}
	}

	/**
	 * Отправка пати сообщения в чат.
	 *
	 * @param player член группы.
	 * @param message сообщение.
	 */
	public void sendMessage(Player player, String message)
	{
		sendPacket(player, CharSay.getInstance(player.getName(), message, SayType.GUILD_CHAT, player.getObjectId(), player.getSubId()));
	}

	/**
	 * Отправка пакета всем членам гильдии.
	 *
	 * @param player член гильдии.
	 * @param packet отправляемый пакет..
	 */
	public void sendPacket(Player player, ServerPacket packet)
	{
		// получаем список онлаин игроков
		Array<Player> online = getOnline();

		online.readLock();
		try
		{
			if(online.isEmpty())
				return;

			// получаем список онлаин
			Player[] array = online.array();

			// устанавливаем кол-во отправок
			packet.increaseSends(online.size());

			// рассылаем пакет
			for(int i = 0, length = online.size(); i < length; i++)
				array[i].sendPacket(packet, false);
		}
		finally
		{
			online.readUnlock();
		}
	}

	/**
	 * @param guildLeader лидер гильдии.
	 */
	public void setGuildLeader(GuildMember guildLeader)
	{
		this.guildLeader = guildLeader;
	}

	/**
	 * @param message сообщение гильдии.
	 */
	public synchronized void setMessage(String message)
	{
		this.message = message;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляем сообщение ГИ в БД
		dbManager.updateGuildMessage(this);
	}

	/**
	 * @param title титул гильдии.
	 */
	public synchronized void setTitle(String title)
	{
		this.title = title;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляем титул ГИ в БД
		dbManager.updateGuildTitle(this);
	}

	/**
	 * @return размер гильдии.
	 */
	public final int size()
	{
		return members.size();
	}

	/**
	 * Обновить иконку гильдии.
	 */
	public void updateIcon()
	{
		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// обновляем иконку в БД
		dbManager.updateGuildIcon(this);

		// получаем менеджер гильдий
		GuildManager guildManager = GuildManager.getInstance();

		// обновляем в нем иконку
		guildManager.putIcon(icon);
	}

	/**
	 * Обновление инфы об члене игрока.
	 *
	 * @param player игрок.
	 */
	public void updateMember(Player player)
	{
		// получаем уникальнвй ид игрока
		int objectId = player.getObjectId();

		GuildMember target = null;

		// получаем членов ГИ
		Array<GuildMember> members = getMembers();

		members.readLock();
		try
		{
			// получаем их список
			GuildMember[] array = members.array();

			// перебираем
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена ГИ
				GuildMember member = array[i];

				// если это искомый
				if(member.getObjectId() == objectId)
				{
					// запоминаем его и выходим из цикла
					target = member;
					break;
				}
			}
		}
		finally
		{
			members.readUnlock();
		}

		// если нашли члена ГИ
		if(target != null)
		{
			target.setLevel(player.getLevel());
			target.setNote(player.getTitle());
			target.setName(player.getName());
			target.setZoneId(player.getZoneId());
		}
	}

	/**
	 * Обновить ранк игрока с указанным ид
	 *
	 * @param player обновляющий ранк.
	 * @param objectId ид игрока.
	 * @param rankId ид ранка.
	 */
	public synchronized void updateRank(Player player, int objectId, int rankId)
	{
		// получаем искомый ранг
		GuildRank rank = getRank(rankId);

		// если ранга не нашли либо это ГМ, выходим
		if(rank == null || rank.isGuildMaster())
			return;

		// получаем члена ГИ
		GuildMember member = getMember(objectId);

		// если мы его нашли
		if(member != null)
		{
			// если он ГМ, выходим
			if(member.getRankId() == GuildRank.GUILD_MASTER)
				return;

			// устанавливаем новый ранг
			member.setRank(rank);

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// обновляем ранг в БД
			dbManager.updatePlayerGuild(this, member);
		}

		// получаем онлаин игрока
		Player online = getPlayer(objectId);

		// если он есть
		if(online != null)
		{
			// обновляем и ему ранг
			online.setGuildRank(rank);

			// обновляем окно ГИ
			online.updateGuild();
		}

		// обновляем окно ГИ
		player.updateGuild();
	}

	@Override
	public String toString()
	{
		return name;
	}
}
