package tera.gameserver.model;

import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.FriendListInfo;
import tera.gameserver.network.serverpackets.FriendListState;

/**
 * Модель списка друзей игрока.
 *
 * @author Ronn
 */
public final class FriendList implements Foldable
{
	private static final FoldablePool<FriendList> pool = Pools.newConcurrentFoldablePool(FriendList.class);

	public static final int FRIEND_LIST_LIMIT = 10;

	public static FriendList getInstance(Player player)
	{
		FriendList list = pool.take();

		if(list == null)
			list = new FriendList();

		list.setOwner(player);

		return list;
	}

	/** пул инфы о друзьях */
	private final FoldablePool<FriendInfo> infoPool;

	/** список друзей */
	private final Array<FriendInfo> friends;
	/** онлаин друзья */
	private final Array<Player> players;

	/** владелец списка */
	private Player owner;

	private FriendList()
	{
		this.infoPool = Pools.newFoldablePool(FriendInfo.class);
		this.friends = Arrays.toArray(FriendInfo.class);
		this.players = Arrays.toArray(Player.class);
	}

	/**
	 * Добавление друга.
	 *
	 * @param info информация о друге.
	 */
	public void addFriend(FriendInfo info)
	{
		friends.add(info);
	}

	/**
	 * Добавление нового друга.
	 *
	 * @param name имя друга.
	 */
	public synchronized boolean addFriend(Player player)
	{
		// получаем владельца списка
		Player owner = getOwner();

		// если его нет, выходим
		if(owner == null)
		{
			Loggers.warning(this, "not found owner");
			return false;
		}

		// если кол-во друзей в списке привысило лимит
		if(size() >= FRIEND_LIST_LIMIT)
		{
			// сообщаем и выходим
			player.sendMessage(MessageType.CANT_ADD_FRIEND_EITHER_YOUR_FRIENDS_LIST_OR_THEIRS_IS_FULL);
			return false;
		}

		// получаем имя игрока
		String name = player.getName();

		// получаем текущий список друзей
		FriendInfo[] friends = getFriends();

		// проверяем есть ли уже такой
		for(int i = 0, length = size(); i < length; i++)
			if(name.equalsIgnoreCase(friends[i].getName()))
				return true;

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// TODO проверка на нахождении в блок листе

    	// получаем контейнер информации о друге
    	FriendInfo info = newFriendInfo();

    	// вносим данные
    	info.setObjectId(player.getObjectId());
    	info.setClassId(player.getClassId());
    	info.setLevel(player.getLevel());
    	info.setName(player.getName());
    	info.setRaceId(player.getRaceId());

    	// вносим в список онлаин игроков
    	players.add(player);

    	// добавляем друга
    	addFriend(info);

    	// вносим запись в БД
    	dbManager.insertFriend(owner.getObjectId(), player.getObjectId());

    	// отправляем пакет с новым списком друзей
    	owner.sendPacket(FriendListInfo.getInstance(owner), true);
    	owner.sendPacket(FriendListState.getInstance(owner), true);

		return true;
	}

	/**
	 * Добавление нового друга.
	 *
	 * @param name имя друга.
	 */
	public synchronized void addFriend(String name)
	{
		// получаем владельца списка
		Player owner = getOwner();

		// если его нет, выходим
		if(owner == null)
		{
			Loggers.warning(this, "not found owner");
			return;
		}

		// если кол-во друзей в списке привысило лимит
		if(size() >= FRIEND_LIST_LIMIT)
		{
			// сообщаем и выходим
			owner.sendMessage(MessageType.CANT_ADD_FRIEND_EITHER_YOUR_FRIENDS_LIST_OR_THEIRS_IS_FULL);
			return;
		}

		// получаем текущий список друзей
		FriendInfo[] friends = getFriends();

		// проверяем есть ли уже такой
		for(int i = 0, length = size(); i < length; i++)
			if(name.equalsIgnoreCase(friends[i].getName()))
			{
				owner.sendMessage(MessageType.THAT_PLAYER_IS_ALREADY_ON_YOUR_FRIENDS_LIST);
				return;
			}

		// получаем игрока из мира
		Player target = World.getPlayer(name);

		// если его нет
		if(target == null)
		{
			// сообщаем что его нет онлаин и выходим
			owner.sendMessage(MessageType.PLAYER_MUST_BE_ONLINE_TO_BE_ADDED_TO_YOUR_LIST);
			return;
		}

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// TODO проверка на нахождении в блок листе

		// получаем список друзей целевого игрока
		FriendList targetList = target.getFriendList();

		// если не дуалось себя туда добавить, выходим
		if(!targetList.addFriend(owner))
			return;

		// получаем контейнер информации о друге
		FriendInfo info = newFriendInfo();

		// вносим данные
		info.setObjectId(target.getObjectId());
		info.setClassId(target.getClassId());
		info.setLevel(target.getLevel());
		info.setName(target.getName());
		info.setRaceId(target.getRaceId());

		// вносим с писок онлаин игроков
		players.add(target);

		// добавляем друга
		addFriend(info);

		// вносим запись в БД
		dbManager.insertFriend(owner.getObjectId(), target.getObjectId());

		// отправляем пакет с новым списком друзей
		owner.sendPacket(FriendListInfo.getInstance(owner), true);
		owner.sendPacket(FriendListState.getInstance(owner), true);

		// отображаем сообщение о добавлении
		PacketManager.addToFriend(owner, target);
	}

	@Override
	public void finalyze()
	{
		FriendInfo[] array = friends.array();

		for(int i = 0, length = friends.size(); i < length; i++)
			infoPool.put(array[i]);

		friends.clear();

		players.clear();

		owner = null;
	}

	/**
	 * Складирование в пул списка.
	 */
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * @return список друзей.
	 */
	public FriendInfo[] getFriends()
	{
		return friends.array();
	}

	/**
	 * @return владелец списка.
	 */
	public Player getOwner()
	{
		return owner;
	}

	/**
	 * @return список онлаин игроков.
	 */
	public Player[] getPlayers()
	{
		return players.array();
	}

	/**
	 * @return новый контейнер информации о друге.
	 */
	public FriendInfo newFriendInfo()
	{
		FriendInfo info = infoPool.take();

		if(info == null)
			info = new FriendInfo();

		return info;
	}

	/**
	 * Обработка входа игрока в игру.
	 *
	 * @param player вошедший игрок.
	 */
	public synchronized void onEnterGame(Player player)
	{
		// получаем владельца списка
		Player owner = getOwner();

		// если его нет, выходим
		if(owner == null)
		{
			Loggers.warning(this, "not found owner");
			return;
		}

		// получаем список друзей
		FriendInfo[] friends = getFriends();

		// перебираем их
		for(int i = 0, length = size(); i < length; i++)
		{
			// получаем контейнер инфы
			FriendInfo info = friends[i];

			// если искомый
			if(info.getObjectId() == player.getObjectId())
			{
				players.add(player);

				// отображаем уведомление
				PacketManager.showEnterFriend(owner, player);

				return;
			}
		}
	}

	/**
	 * Обработка выхода игрока из игры.
	 *
	 * @param player вышедший игрок.
	 */
	public synchronized void onExitGame(Player player)
	{
		// получаем список друзей
		FriendInfo[] friends = getFriends();

		// перебираем их
		for(int i = 0, length = size(); i < length; i++)
		{
			// получаем контейнер инфы
			FriendInfo info = friends[i];

			// если искомый
			if(info.getObjectId() == player.getObjectId())
			{
				// удаляем из списка
				players.fastRemove(i);

				// обновляем уровень
				info.setLevel(player.getLevel());

				return;
			}
		}
	}

	/**
	 * @return кол-во онлаин игроков.
	 */
	public int online()
	{
		return players.size();
	}

	/**
	 * Подготовка списка.
	 */
	public void prepare()
	{
		// получаем список друзей
		FriendInfo[] friends = getFriends();

		// перебираем их
		for(int i = 0, length = size(); i < length; i++)
		{
			FriendInfo info = friends[i];

			Player player = World.getPlayer(info.getObjectId());

			if(player != null)
				players.add(player);
		}
	}

	@Override
	public void reinit(){}


	/**
	 * Удаление друга.
	 *
	 * @param objectId уникальный ид друга.
	 */
	public synchronized void removeFriend(int objectId)
	{
		// получаем владельца списка
		Player owner = getOwner();

		// если его нет, выходим
		if(owner == null)
		{
			Loggers.warning(this, "not found owner");
			return;
		}

		// получаем список друзей
		FriendInfo[] array = getFriends();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// перебираем их
		for(int i = 0, length = size(); i < length; i++)
		{
			FriendInfo info = array[i];

			// лишних пропускаем
			if(info.getObjectId() != objectId)
				continue;

			// удаляем из БД друга
			dbManager.removeFriend(owner.getObjectId(), objectId);

			// удаляем из списка
			friends.fastRemove(i);

			// отправляем пакет с новым списком друзей
			owner.sendPacket(FriendListInfo.getInstance(owner), true);
			owner.sendPacket(FriendListState.getInstance(owner), true);

			// получаем игрока онлаин
			Player target = World.getPlayer(info.getName());

			if(target == null)
				// удаляем из БД друга
				dbManager.removeFriend(objectId, owner.getObjectId());
			else
			{
				// получаем его список друзей
				FriendList targetList = target.getFriendList();

				// удаляемся из него
				targetList.removeFriend(owner);
			}

			// сообщаем об удалении
			PacketManager.removeToFriend(owner, info.getName(), target);

			// складируем в пул контейнер
			infoPool.put(info);

			return;
		}

		// если небыл найден нужный друг, сообщаем
		owner.sendMessage(MessageType.THAT_PLAYER_IS_NOT_ON_YOUR_FRIENDS_LIST);
	}

	/**
	 * Удаление друга из списка друзей.
	 *
	 * @param player бывший друг.
	 */
	public synchronized void removeFriend(Player player)
	{
		// получаем владельца списка
		Player owner = getOwner();

		// если его нет, выходим
		if(owner == null)
		{
			Loggers.warning(this, "not found owner");
			return;
		}

		// получаем уникальный ид удаляемого друга
		int objectId = player.getObjectId();

		// получаем список друзей
		FriendInfo[] array = getFriends();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// перебираем их
		for(int i = 0, length = size(); i < length; i++)
		{
			FriendInfo info = array[i];

			// лишних пропускаем
			if(info.getObjectId() != objectId)
				continue;

			// удаляем из БД друга
			dbManager.removeFriend(owner.getObjectId(), objectId);

			// удаляем из списка
			friends.fastRemove(i);

			// отправляем пакет с новым списком друзей
			owner.sendPacket(FriendListInfo.getInstance(owner), true);
			owner.sendPacket(FriendListState.getInstance(owner), true);

			// складируем в пул контейнер
			infoPool.put(info);

			return;
		}
	}

	/**
	 * @param owner владелец списка.
	 */
	public void setOwner(Player owner)
	{
		this.owner = owner;
	}

	/**
	 * @return кол-во друзей.
	 */
	public int size()
	{
		return friends.size();
	}
}
