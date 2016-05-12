package tera.gameserver.model;

import java.util.Comparator;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.ArrayComparator;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharPickUpItem;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.network.serverpackets.PartyInfo;
import tera.gameserver.network.serverpackets.PartyLeave;
import tera.gameserver.network.serverpackets.PartyMemberCoords;
import tera.gameserver.network.serverpackets.PartyMemberEffectList;
import tera.gameserver.network.serverpackets.PartyMemberInfo;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.util.LocalObjects;

/**
 * Модель группы игроков.
 *
 * @author Ronn
 * @created 06.03.2012
 */
public final class Party implements Foldable
{
	private static final Logger log = Loggers.getLogger(Party.class);

	private static final FoldablePool<Party> pool = Pools.newConcurrentFoldablePool(Party.class);

	public static final int MAX_RANGE = 1000;

	/**
	 * Создание новой группы.
	 *
	 * @param leader лидер группы.
	 * @param objectId ид группы.
	 * @return новая группа.
	 */
	public static final Party newInstance(Player leader, int objectId)
	{
		Party party = pool.take();

		if(party == null)
			party = new Party();

		party.leader = leader;
		party.objectId = objectId;

		leader.setParty(party);

		party.members.add(leader);

		return party;
	}

	/** компаратор для сортировки группы */
	private final Comparator<Player> sorter;

	/** состав группы */
	private final Array<Player> members;
	/** игроки поднявшие уже итем */
	private final Array<Player> pickUped;

	/** лидер группы */
	private volatile Player leader;

	/** ид группы */
	private int objectId;

	/** можно ли поднимать лут во время боя */
	private boolean lootInCombat;
	/** рандомное ли поднятие лута */
	private boolean roundLoot;

	private Party()
	{
		// создаем компоратор для сортировки мемберов пати
		sorter = new ArrayComparator<Player>()
		{
			@Override
			protected int compareImpl(Player first, Player second)
			{
				if(first == getLeader())
					return 1;

				return -1;
			}
		};

		// создаем массив участников группы.
		members = Arrays.toConcurrentArray(Player.class, 5);

		// создаем учет получателей итемов
		pickUped = Arrays.toArray(Player.class, 5);

		setLootInCombat(true);
		setRoundLoot(true);
	}

	/**
	 * Добавление опыта членам группы.
	 *
	 * @param added кол-во полученого опыта с нпс.
	 * @param npc убитый нпс.
	 */
	public void addExp(long added, Character topDamager, Npc npc)
	{
		// если экспы нет или нпс нет, выходим
		if(added < 0 || npc == null)
			return;

		// получаем членов группы
		Array<Player> members = getMembers();

		// счетчик доступных мемберов
		int counter = 0;

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// определяем максимальный уровень группы
			int minLevel = Config.WORLD_PLAYER_MAX_LEVEL;
			// определям минимальный уровень группы
			int maxLevel = 0;

			// перебираем членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если он не далеко от убитого нпс, плюсуем
				if(npc.getDistance3D(member) < MAX_RANGE)
					counter++;

				// обновляем минимальный и максимальный уровень в пати
				maxLevel = Math.max(member.getLevel(), maxLevel);
				minLevel = Math.min(member.getLevel(), minLevel);
			}

			//если состав группы не подходит для выдачи экспы, выходим
			if(Math.abs(maxLevel - minLevel) > 6 || counter < 1)
				return;

			// делим добавляемую экспу на кол-во игроков, которым она пойдет
			added /= counter;

			// если экспы в итоге нету, выходим
			if(added < 1)
				return;

			// если больше одного человека получают экспу
			if(counter > 2)
				// увеличиваем на пати рейт
				added *= Config.SERVER_PARTY_RATE_EXP;

			// перебераем опять членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// пполучаем члена группы
				Player member = array[i];

				// если есть сильнейший атакующий и член группы имеет с ним разницу по лвлу выше 5, пропускаем
				if(topDamager != null && Math.abs(topDamager.getLevel() - member.getLevel()) > 5)
					continue;

				// если член группы далеко от убитого нпс, пропускаем
				if(npc.getDistance3D(member) > MAX_RANGE)
					continue;

				// рассчет итоговой экспы
				float reward = added;

				// если активирован ПА, умножаем на рейт ПА
				if(Config.ACCOUNT_PREMIUM_EXP && member.hasPremium())
					reward *= Config.ACCOUNT_PREMIUM_EXP_RATE;

				// рассчитываем разницу члена группы с нпс по лвлам
				int diff = Math.abs(member.getLevel() - npc.getLevel());

				// применяем штраф
				if(diff >= Npc.PENALTY_EXP.length)
					reward *= 0F;
				else if(diff > 5)
					reward *= Npc.PENALTY_EXP[diff];

				// если в итоге экспа есть
				if(reward >= 1)
					// выдаем экспу
					member.addExp((int) reward, npc, npc.getName());
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Добавление нового игрока в группу.
	 *
	 * @param newMember новый игрок.
	 * @return true если добавлен
	 */
	public boolean addPlayer(Player newMember)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// если игроков уже максимум, выходим
			if(members.size() > 4)
				return false;

			// если игрок почему-то уже есть, выходим
			if(members.contains(newMember))
				return false;

			// добавляем в пати
			members.add(newMember);
		}
		finally
		{
			members.writeUnlock();
		}

		// записываем пати игроку
		newMember.setParty(this);

		// применяем цвета ников
		updatePartyColorName(newMember);
		// обновляем инфо о пати
		updateInfo();
		// обновляем статы
		updateStat();

		return true;
	}

	/**
	 * Удаление всех членов группы.
	 */
	public void allRemove()
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// пакет исключения из пати
			ServerPacket packet = PartyLeave.getInstance();

			// получаем список членов группы
			Player[] array = members.array();

			// отсылаем всем пакет исключения из пати
			for(int i = 0, length = members.size(); i < length; i++)
				packet.increaseSends();

			// отсылаем всем пакет исключения из пати
			for(int i = 0, length = members.size(); i < length; i++)
				array[i].sendPacket(packet, false);
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Изменение настроек группы.
	 *
	 * @param lootIsCombat можно ли подбирать лут в бою.
	 * @param roundLoot рандомный подбор ли лута.
	 */
	public void change(boolean lootIsCombat, boolean roundLoot)
	{
		// применяем флаг подьема в бою
		setLootInCombat(lootIsCombat);

		// применяем тип лута
		setRoundLoot(roundLoot);

		// обновляем инфу о пати
		updateInfo();
	}

	/**
	 * Роспуск группы.
	 *
	 * @param player лидер группы.
	 */
	public void disband(Player player)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// если игрок не лидер группы, выходим
			if(!isLeader(player))
				return;

			// пакет исключения из пати
			ServerPacket packet = PartyLeave.getInstance();
			ServerPacket message = SystemMessage.getInstance(MessageType.YOUR_PARTY_HAS_DISBANDED);

			// получаем список членов группы
			Player[] array = members.array();

			// отсылаем всем пакет исключения из пати
			for(int i = 0, length = members.size(); i < length; i++)
			{
				packet.increaseSends();
				message.increaseSends();
			}

			// отсылаем всем пакет исключения из пати
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// зануляем группу
				member.setParty(null);
				// отправляем пакет исключения из группы
				member.sendPacket(packet, false);
				// отправляем уведомление о роспуске
				member.sendPacket(message, false);
			}

			// обновляем всем цвета ника
			for(int i = 0, length = members.size(); i < length; i++)
				// обновляем цвета ников
				updatePartyColorName(array[i]);

			// ложим в пул
			pool.put(this);
		}
		finally
		{
			members.writeUnlock();
		}
	}

	@Override
	public void finalyze()
	{
		// обнуляем лидера
		leader = null;

		// обнуляем ид пати
		objectId = 0;

		// очищаем список мемберов
		members.clear();
	}

	/**
	 * @return лидер группы.
	 */
	public Player getLeader()
	{
		return leader;
	}

	/**
	 * @return ид лидера группы.
	 */
	public int getLeaderId()
	{
		if(leader != null)
			return leader.getObjectId();

		return -1;
	}

	/**
	 * @return состав группы.
	 */
	public Array<Player> getMembers()
	{
		return members;
	}

	/**
	 * @return ид группы.
	 */
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * Является ли указанный игрок лидером этой пати.
	 *
	 * @param player проверяемый игрок.
	 * @return является ли лидером.
	 */
	public boolean isLeader(Player player)
	{
		return player == leader;
	}

	/**
	 * @return lootInCombat можно ли в бою поднимать лут.
	 */
	public final boolean isLootInCombat()
	{
		return lootInCombat;
	}

	/**
	 * @return по кгругу ли лут.
	 */
	public final boolean isRoundLoot()
	{
		return roundLoot;
	}

	/**
	 * Выкидывание члена группы из группы.
	 *
	 * @param player лидер группы.
	 * @param objectId ид выкидываемого члена группы.
	 */
	public void kickPlayer(Player player, int objectId)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// если игрок не лидер группы, выходим
			if(!isLeader(player))
				return;

			// получаем список членов группы
			Player[] array = members.array();

			// исчим члена группы с нужным ид
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это искомый член группы
				if(member.getObjectId() == objectId)
				{
					// удаляем его
					removePlayer(member);
					// сообщаем о том, что игрока кикнули
					member.sendMessage(MessageType.YOU_VE_BEEN_KICKED_FROM_THE_PARTY);
					// выходим
					return;
				}
			}
		}
		finally
		{
			members.writeUnlock();
		}
	}

	/**
	 * Смена лидера группы.
	 *
	 * @param player текущий лидер группы.
	 * @param objectId ид нового лидера группы.
	 */
	public void makeLeader(Player player, int objectId)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// если игрок не лидер группы, выходим
			if(!isLeader(player))
				return;

			// получаем список членов группы
			Player[] array = members.array();

			// исчим члена группы с нужным ид
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это искомый член группы
				if(member.getObjectId() == objectId)
				{
					// ставим его лидером
					setLeader(member);

					// создаем пакет с сообщением
					SystemMessage message = SystemMessage.getInstance(MessageType.PARTY_PLAYER_NAME_IS_NOW_PARTY_LEADER);

					// добавляем имя нового лидера
					message.add("PartyPlayerName", member.getName());

					// отправляем пакет
					sendPacket(member, message);

					// отправляем сообщение лидеру
					member.sendMessage(MessageType.YPU_ARE_NOW_PARTY_LEADER);

        			// обновляем инфу о пати
        			updateInfo();

        			// выходим
        			return;
				}
			}
		}
		finally
		{
			members.writeUnlock();
		}
	}

	/**
	 * Обработка поднятия итема членом группы.
	 *
	 * @param item поднятый итем.
	 * @param owner член группы, который поднял.
	 * @return был ли взят итем.
	 */
	public boolean pickUpItem(ItemInstance item, Character owner)
	{
		if(owner.isBattleStanced() && !isLootInCombat())
		{
			owner.sendMessage("Нельзя поднимать в бою.");
			return false;
		}

		// если поднимаемый итем деньги
		if(item.getItemId() == Inventory.MONEY_ITEM_ID)
			return pickUpMoney(item, owner);
		else
		{
			// если стоит не рандом
			if(item.isHerb() || !isRoundLoot())
				return pickUpNoRound(item, owner);
			else
				return pickUpRound(item, owner);
		}
	}

	/**
	 * Обработка поднятия денег.
	 *
	 * @param item поднимаемый итем.
	 * @param owner тот кто поднял итем.
	 * @return был ли поднят итем.
	 */
	private boolean pickUpMoney(ItemInstance item, Character owner)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// счетчик игроков в радиусе
			int counter = 0;

			// перебираем членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если игрок поднял или в близком радиусе, плюсуем к счетчику
				if(owner == member || owner.getDistance3D(member) < MAX_RANGE)
					counter++;
			}

			// если почему-то ненашлось подходящих, выходим
			if(counter < 1)
				return false;

			// делим добавляемую экспу на кол-во игроков, которым она пойдет
			int money = (int) Math.max(item.getItemCount() / counter, 1);

			// отображаем подъем итема
			owner.broadcastPacket(CharPickUpItem.getInstance(owner, item));

			// перебираем членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если игрок поднял или в близком радиусе, плюсуем к счетчику
				if(owner == member || owner.getDistance3D(member) < MAX_RANGE)
				{
					// получаем инвентарь игрока
					Inventory inventory = member.getInventory();

					// если инвентарь есть
					if(inventory != null)
					{
						// выдаем деньги
						inventory.addMoney(money);

						// отображаем получение денег
						PacketManager.showAddGold(member, money);

						// получаем менеджера событий
						ObjectEventManager eventManager = ObjectEventManager.getInstance();

						// обновляем инвентарь
						eventManager.notifyInventoryChanged(member);
						// уведомляем о подъеме итема
						eventManager.notifyPickUpItem(owner, item);

						// получаем менеджера по игровому логированию
						GameLogManager gameLogger = GameLogManager.getInstance();

						// записываем событие выдачи итема
						gameLogger.writeItemLog(member.getName() + " pick up item [id = " + item.getItemId() + ", count = " + money + ", name = " + item.getName() + "]");
					}
				}
			}

			return true;
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Поднятие без рандома итема игроком.
	 *
	 * @param item поднятый итем.
	 * @param owner игрок, который поднял.
	 * @return был ли поднят итем.
	 */
	private boolean pickUpNoRound(ItemInstance item, Character owner)
	{
		// получаем инвентарь персонажа
		Inventory inventory = owner.getInventory();

		// если его нет, выходим
		if(inventory == null)
			return false;

		long itemCount = item.getItemCount();

		// пробуем положить итем в инвентарь
		if(!inventory.putItem(item))
			owner.sendMessage(MessageType.INVENTORY_IS_FULL);
		else
		{
			// отображаем подъем итема
			owner.broadcastPacket(CharPickUpItem.getInstance(owner, item));

			// получаем менеджера по игровому логированию
			GameLogManager gameLogger = GameLogManager.getInstance();

			// записываем событие выдачи итема
			gameLogger.writeItemLog(owner.getName() + " pick up item [id = " + item.getItemId() + ", count = " + itemCount + ", name = " + item.getName() + "]");

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// обновляем инвентарь
			eventManager.notifyInventoryChanged(owner);
			// увндомляем о подъеме итема
			eventManager.notifyPickUpItem(owner, item);

			// ссылка на пакет сообщения
			ServerPacket packet = MessageAddedItem.getInstance(owner.getName(), item.getItemId(), (int) itemCount);

			// отправляем сообщение
			owner.sendPacket(packet, true);

			// создаем сообщение о поднятии итема
			SystemMessage message = SystemMessage.getInstance(MessageType.PARTY_PLAYER_NAME_PICK_UP_ITEM_NAME_ITEM_AMOUNT);

			// добавляем имя получившего
			message.add("PartyPlayerName", owner.getName());
			// добавляем кол-во итемов
			message.addItem(item.getItemId(), (int) itemCount);

			// отправляем пакет
			sendPacket(owner.getPlayer(), message);

			// если стоит лут по кругу
			if(!item.isHerb() && isRoundLoot())
				// вносим в список получивших
				pickUped.add(owner.getPlayer());

			return true;
		}

		return false;
	}

	/**
	 * Обработка поднятия итема по кругу.
	 *
	 * @param item поднятый итем.
	 * @param owner тот кто поднял.
	 * @return был ли поднят итем.
	 */
	private boolean pickUpRound(ItemInstance item, Character owner)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// игроК, которому выдадим итем
			Player target = null;

			// перебираем членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если игрок поднял или в близком радиусе, плюсуем к счетчику
				if((owner == member || owner.getDistance3D(member) < MAX_RANGE) && !pickUped.contains(member))
				{
					// запоминаем получателя
					target = member;
					// выходим с цикла
					break;
				}
			}

			// если подходящего не нашли
			if(target == null)
			{
				// очищаем список поднявших
				pickUped.clear();

				// перебираем членов группы
				for(int i = 0, length = members.size(); i < length; i++)
				{
					// получаем члена группы
					Player member = array[i];

					// если игрок поднял или в близком радиусе, плюсуем к счетчику
					if(owner == member || owner.getDistance3D(member) < MAX_RANGE && !pickUped.contains(member))
					{
						// запоминаем получателя
						target = member;
						// выходим с цикла
						break;
					}
				}
			}

			// если игрока подходящего не нашли
			if(target == null)
				// выходим
				return false;

			// получаем инвентарь персонажа
			Inventory inventory = target.getInventory();

			// если его нет, выходим
			if(inventory == null)
				return false;

			// получаем кол-во поднятых итемов
			long itemCount = item.getItemCount();

			// пробуем положить итем в инвентарь
			if(!inventory.putItem(item))
			{
				// сообщаем о заполнености инвентаря
				target.sendMessage(MessageType.INVENTORY_IS_FULL);

				// если игрок не подниматель
				if(target != owner)
					// пробуем положить в инвентарь поднявшему
					return pickUpNoRound(item, owner);
			}
			else
			{
				// отображаем подъем итема
				owner.broadcastPacket(CharPickUpItem.getInstance(owner, item));

				// получаем менеджера по игровому логированию
				GameLogManager gameLogger = GameLogManager.getInstance();

				// записываем событие выдачи итема
				gameLogger.writeItemLog(target.getName() + " pick up item [id = " + item.getItemId() + ", count = " + itemCount + ", name = " + item.getName() + "]");

				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(target);
				// увндомляем о подъеме итема
				eventManager.notifyPickUpItem(target, item);

				// ссылка на пакет сообщения
				ServerPacket packet = MessageAddedItem.getInstance(target.getName(), item.getItemId(), (int) itemCount);

				// отправляем сообщение
				target.sendPacket(packet, true);

				// создаем сообщение о поднятии итема
				SystemMessage message = SystemMessage.getInstance(MessageType.PARTY_PLAYER_NAME_PICK_UP_ITEM_NAME_ITEM_AMOUNT);

				// добавляем имя получившего
				message.add("PartyPlayerName", target.getName());
				// добавляем кол-во итемов
				message.addItem(item.getItemId(), (int) itemCount);

				// отправляем пакет
				sendPacket(target, message);

				// добавляем в список поднимавших
				pickUped.add(target);

				return true;
			}

			return false;
		}
		finally
		{
			members.writeUnlock();
		}
	}

	@Override
	public void reinit()
	{
		lootInCombat = true;
		roundLoot = true;
	}

	/**
	 * Удаление игрока из пати.
	 *
	 * @param oldMember удаляемый игрок.
	 * @return удален ли игрок.
	 */
	public boolean removePlayer(Player oldMember)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.writeLock();
		try
		{
			// если игрок был удален из списка
			if(!members.fastRemove(oldMember))
				return false;
			else
			{
				// зануляем ему пати
				oldMember.setParty(null);

				// обновляем цвета ников
				updatePartyColorName(oldMember);

				// отправляем пакет лива с пати
				oldMember.sendPacket(PartyLeave.getInstance(), true);

				// если остается меньше 2х мемберов
				if(members.size() < 2)
				{
					// достаем последнего игрока
					Player last = members.first();

					// зануляем и ему пати
					last.setParty(null);

					// и ему отправялем пакет лива с пати
					last.sendPacket(PartyLeave.getInstance(), true);

					// ложим пати в пул
					pool.put(this);

					return true;
				}

				// если это был лидер пати
				if(oldMember == leader)
					// ставим нового лидера
					setLeader(members.first());
			}
		}
		finally
		{
			members.writeUnlock();
		}

		// всех ливаем
		allRemove();
		// восстанавливаем новый состав
		updateInfo();
		// обновляем все статы
		updateStat();

		return true;
	}

	/**
	 * Отправка сис. сообщения в чат в рамках группы.
	 *
	 * @param type тип сообщения.
	 */
	public void sendMessage(MessageType type)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// отправка всем мемберам
			for(int i = 0, length = members.size(); i < length; i++)
				array[i].sendMessage(type);
		}
		finally
		{
			members.readUnlock();
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
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// создаем пакет с сообщением
			CharSay packet = CharSay.getInstance(player.getName(), message, SayType.PARTY_CHAT, player.getObjectId(), player.getSubId());

			// получаем список членов группы
			Player[] array = members.array();

			// отправка всем мемберам
			for(int i = 0, length = members.size(); i < length; i++)
				packet.increaseSends();

			// отправка всем мемберам
			for(int i = 0, length = members.size(); i < length; i++)
				array[i].sendPacket(packet, false);
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Отправка пакета в рамках группы.
	 *
	 * @param packet gfrtn.
	 */
	public void sendPacket(Player player, ServerPacket packet)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// отправка всем мемберам
			for(int i = 0, length = members.size() - 1; i < length; i++)
				packet.increaseSends();

			// если нет исключение
			if(player == null)
				// увеличиваем счетчик отправок
				packet.increaseSends();

			// отправка всем мемберам
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый член группы, то пропускаем
				if(member == player)
					continue;

				// отправляем пакет
				member.sendPacket(packet, false);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * @param newLeader лидер группы.
	 */
	public void setLeader(Player newLeader)
	{
		// если новый лидер есть старый, выходим
		if(leader == newLeader)
			return;

		// применяем нового лидера
		leader = newLeader;

		// сортируем мемберов
		members.sort(sorter);
	}

	/**
	 * @param lootInCombat можно ли в бою поднимать лут.
	 */
	public final void setLootInCombat(boolean lootInCombat)
	{
		this.lootInCombat = lootInCombat;
	}

	/**
	 * @param objectId ид группы.
	 */
	public final void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param roundLoot по кгругу ли лут.
	 */
	public final void setRoundLoot(boolean roundLoot)
	{
		this.roundLoot = roundLoot;
	}

	/**
	 * @return текущий размер группы.
	 */
	public int size()
	{
		return members.size();
	}

	/**
	 * Обновление координат члена группы.
	 *
	 * @param player член группы.
	 */
	public void updateCoords(Player player)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		// создаем пакет с координатами мембера
		ServerPacket packet = PartyMemberCoords.getInstance(player);

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// увеличиваем счетчик отправок
				packet.increaseSends();
			}

			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// отправляем пакет
				member.sendPacket(packet, false);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Обновление эффект листа члена группы.
	 *
	 * @param player член группы.
	 */
	public void updateEffects(Player player)
	{
		EffectList effectList = player.getEffectList();

		if(effectList == null)
		{
			log.warning("not found effect list.");
			return;
		}

		// создаем пакет с списком эффектов члена группы
		PartyMemberEffectList packet = PartyMemberEffectList.getInstance(player);

		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// перебираем членов группы
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// увеличиваем счетчик отправок
				packet.increaseSends();
			}

			// перебираем еще раз
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// отправляем пакет
				member.sendPacket(packet, false);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Обновление состава группы.
	 */
	public void updateInfo()
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		// пакет со составом группы
		ServerPacket packet = PartyInfo.getInstance(this);

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// подсчитываем сколько будет отправлений
			for(int i = 0, length = members.size(); i < length; i++)
				packet.increaseSends();

			// отпрвавляем членам группы пакет
			for(int i = 0, length = members.size(); i < length; i++)
				array[i].sendPacket(packet, false);
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Полное обновление члена группы.
	 *
	 * @param player член группы.
	 */
	public void updateMember(Player player)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		// создаем пакет со статами члена группы
		ServerPacket stats = PartyMemberInfo.getInstance(player);
		// создаем пакет с позицией члена группы
		ServerPacket coords = PartyMemberCoords.getInstance(player);
		// создаем пакет с эффект листом члена группы
		ServerPacket effects = PartyMemberEffectList.getInstance(player);

		members.readLock();
		try
		{
			// получаем список группы
			Player[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// увеличиваем счетчик отправко пакетов
				effects.increaseSends();
				stats.increaseSends();
				coords.increaseSends();
			}

			// перебираем их еще раз
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, пропускаем
				if(member == player)
					continue;

				// ложим на отправку пакеты
				member.sendPacket(effects, false);
				member.sendPacket(stats, false);
				member.sendPacket(coords, false);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Установка синего цвета ника для членов пати.
	 *
	 * @param player член группы.
	 */
	public void updatePartyColorName(Player player)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый, то пропускаем
				if(member == player)
					continue;

				// обновляем новому игроку цвет ника мембера
				player.updateColor(member);
				// обновляем мемберу цвет ника нового игрока
				member.updateColor(player);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}

	/**
	 * Общее обновление информации о членах группы друг для друга.
	 */
	public void updateStat()
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем локальный список игроков
		Array<Player> players = local.getNextPlayerList();

		// вносим туда членов группы
		players.addAll(members);

		// получаем список членов группы
		Player[] array = players.array();

		// обновляем всех
		for(int i = 0, length = players.size(); i < length; i++)
			updateMember(array[i]);
	}

	/**
	 * Обновление информации об члене группы.
	 *
	 * @param player член группы.
	 */
	public void updateStat(Player player)
	{
		// получаем членов группы
		Array<Player> members = getMembers();

		// создаем пакет с информацией о члене группы
		ServerPacket packet = PartyMemberInfo.getInstance(player);

		members.readLock();
		try
		{
			// получаем список членов группы
			Player[] array = members.array();

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый член группы, то пропускаем
				if(member == player)
					continue;

				// увеличиваем счетчик отправки пакета
				packet.increaseSends();
			}

			// перебираем их
			for(int i = 0, length = members.size(); i < length; i++)
			{
				// получаем члена группы
				Player member = array[i];

				// если это обновляемый член группы, то пропускаем
				if(member == player)
					continue;

				// ложим на отправку пакеет
				member.sendPacket(packet, false);
			}
		}
		finally
		{
			members.readUnlock();
		}
	}
}
