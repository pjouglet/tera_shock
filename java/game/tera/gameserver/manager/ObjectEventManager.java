package tera.gameserver.manager;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.Guild;
import tera.gameserver.model.Party;
import tera.gameserver.model.SkillLearn;
import tera.gameserver.model.TObject;
import tera.gameserver.model.ai.AI;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.listeners.DeleteListener;
import tera.gameserver.model.listeners.DieListener;
import tera.gameserver.model.listeners.LevelUpListener;
import tera.gameserver.model.listeners.PlayerSelectListener;
import tera.gameserver.model.listeners.PlayerSpawnListener;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Playable;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestEventType;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestUtils;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.ChanceType;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.util.LocalObjects;

/**
 * Менеджер обработки событий игровых объектов в игре.
 *
 * @author Ronn
 */
public final class ObjectEventManager
{
	private static final Logger log = Loggers.getLogger(ObjectEventManager.class);

	private static ObjectEventManager instance;

	public static ObjectEventManager getInstance()
	{
		if(instance == null)
			instance = new ObjectEventManager();

		return instance;
	}

	/** слушатели убийств */
	private final Array<DieListener> dieListeners;
	/** слушатели удаляемых объектов */
	private final Array<DeleteListener> deleteListeners;
	/** набор слушателей лвл апов игроков */
	private final Array<LevelUpListener> levelUpListeners;
	/** набор слушателей спавнов игроков */
	private final Array<PlayerSpawnListener> playerSpawnListeners;
	/** набор слушателей выбора игрока для входа */
	private final Array<PlayerSelectListener> playerSelectListeners;

	private ObjectEventManager()
	{
		dieListeners = Arrays.toConcurrentArray(DieListener.class);
		deleteListeners = Arrays.toConcurrentArray(DeleteListener.class);
		levelUpListeners = Arrays.toConcurrentArray(LevelUpListener.class);
		playerSpawnListeners = Arrays.toConcurrentArray(PlayerSpawnListener.class);
		playerSelectListeners = Arrays.toConcurrentArray(PlayerSelectListener.class);

		log.info("initialized.");
	}

	/**
	 * @return список слушателей выбора игрока.
	 */
	public Array<PlayerSelectListener> getPlayerSelectListeners()
	{
		return playerSelectListeners;
	}

	/**
	 * @param listener добавляемый слушатель.
	 */
	public final void addDeleteListener(DeleteListener listener)
	{
		deleteListeners.add(listener);
	}

	/**
	 * @param listener добавляемый слушатель.
	 */
	public final void addDieListener(DieListener listener)
	{
		dieListeners.add(listener);
	}

	/**
	 * Доабвление слушателей лвл апов.
	 */
	public final void addLevelUpListener(LevelUpListener listener)
	{
		levelUpListeners.add(listener);
	}

	/**
	 * Доабвление слушателей спавнов игроков.
	 */
	public final void addPlayerSpawnListener(PlayerSpawnListener listener)
	{
		playerSpawnListeners.add(listener);
	}

	/**
	 * Доабвление слушателей выбора игроков.
	 */
	public final void addPlayerSelectListener(PlayerSelectListener listener)
	{
		playerSelectListeners.add(listener);
	}

	/**
	 * @return получаем слушателей по удалению объектов.
	 */
	public Array<DeleteListener> getDeleteListeners()
	{
		return deleteListeners;
	}

	/**
	 * @return слушатели убийств.
	 */
	public Array<DieListener> getDieListeners()
	{
		return dieListeners;
	}

	/**
	 * @return слушатели изменения уровней игроков.
	 */
	public Array<LevelUpListener> getLevelUpListeners()
	{
		return levelUpListeners;
	}

	/**
	 * @return слушатели спавна игроков.
	 */
	public Array<PlayerSpawnListener> getPlayerSpawnListeners()
	{
		return playerSpawnListeners;
	}

	/**
	 * Уведомление о добавлении НПС игроку.
	 *
	 * @param player игрок, которому добавился НПС.
	 * @param npc добавляемый НПС.
	 */
	public void notifyAddNpc(Player player, Npc npc)
	{
		// высвечиваем доступный квест
		npc.updateQuestInteresting(player, false);

		// получаем список квестов игрока
		QuestList questList = player.getQuestList();

		// если есть активные квесты
		if(questList != null && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// запоминаем игрока
			event.setPlayer(player);
			// запоминаем нпс
			event.setNpc(npc);
			// запоминаем тип ивента
			event.setType(QuestEventType.ADD_NPC);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление об изменение уровня агрессии объекта по отношению к аткующему.
	 *
	 * @param object атакуемый.
	 * @param attacker атакующий.
	 * @param aggro кол-во добавленых агр очков.
	 */
	public final void notifyAgression(Character object, Character attacker, long aggro)
	{
		object.getAI().notifyAgression(attacker, aggro);
	}

	/**
	 * Уведомление о наложении эффекта на персонажа.
	 *
	 *@param effected на кого наложили эффект.
	 *@param effect наложенный эффект.
	 */
	public final void notifyAppliedEffect(Character effected, Effect effect)
	{
		effected.getAI().notifyAppliedEffect(effect);
	}

	/**
	 * Уведомление об прибытии объекта в назначенную точку.
	 *
	 * @param object прибывший объект.
	 */
	public final void notifyArrived(Character object)
	{
		object.getAI().notifyArrived();
	}

	/**
	 * Уведомление об блокировке перемещения объекта.
	 *
	 * @param object заблокированный объект.
	 */
	public final void notifyArrivedBlocked(Character object)
	{
		object.getAI().notifyArrivedBlocked();
	}

	/**
	 * Уведомление о прибытии объекта к указанной цели.
	 *
	 * @param object прибывший объект.
	 * @param target цель прибытия.
	 */
	public final void notifyArrivedTarget(Character object, TObject target)
	{
		object.getAI().notifyArrivedTarget(target);
	}

	/**
	 * Уведомление о атаке объектом.
	 *
	 * @param attacker атакующий объект.
	 * @param attacked атакуемый объект.
	 * @param skill атакующий скилл.
	 * @param damage кол-во урона.
	 * @param crit является ли удар критическим.
	 */
	public final void notifyAttack(Character attacker, Character attacked, Skill skill, int damage, boolean crit)
	{
		// уведомляем АИ об этом
		attacker.getAI().notifyAttack(attacked, skill, damage);

		// получаем список эффектов
		EffectList effectList = attacker.getEffectList();

		// завершаем эффекты, завершаемые при атаке кого-то
		effectList.exitNoAttackEffects();

		// если атакуемый на пегасе
		if(attacker.isOnMount())
			// слезаем с пегаса
			attacker.getOffMount();

		// получаем владельца атакуемого
		Character owner = attacker.getOwner();

		// если владелец есть
		if(owner != null)
		{
			// если он на пегасе
			if(owner.isOnMount())
				// слазим с пегаса
				owner.getOffMount();

			// запускаем боевую стойку
			owner.startBattleStance(attacker);
		}

		// запускаем боевую стойку
		attacker.startBattleStance(attacked);

		// запускаем шансовые функции
		attacker.applyChanceFunc(crit? ChanceType.ON_CRIT_ATTACK : ChanceType.ON_ATTACK, attacked, skill);
	}

	/**
	 * Уведомление о атаке объекта.
	 *
	 * @param attacked атакуемый объект.
	 * @param attacker атакующий объект.
	 * @param skill атакующий скил.
	 * @param damage кол-во урона.
	 * @param crit является ли удар критическим.
	 */
	public final void notifyAttacked(Character attacked, Character attacker, Skill skill, int damage, boolean crit)
	{
		// увндомляем АИ об этом
		attacked.getAI().notifyAttacked(attacker, skill, damage);

		// получаем список эффектов персонажа
		EffectList effectList = attacked.getEffectList();

		// завершаем эффекты, завершаемые при получении урона
		effectList.exitNoAttackedEffects();

		// если атакуемый на пегасе
		if(attacked.isOnMount())
			// слазим с пегаса
			attacked.getOffMount();

		// получаем владельца атакуемого
		Character owner = attacked.getOwner();

		// если владелец есть
		if(owner != null)
		{
			// если он на пегасе
			if(owner.isOnMount())
				// слазим с пегаса
				owner.getOffMount();

			// запускаем боевую стойку
			owner.startBattleStance(attacker);
		}

		// запускаем боевую стойку
		attacked.startBattleStance(attacker);

		// вызываем шансовые функции
		attacked.applyChanceFunc(crit? ChanceType.ON_CRIT_ATTACKED : ChanceType.ON_ATTACKED, attacker, skill);
	}

	/**
	 * Уведомление об изминении уровня играющего.
	 *
	 * @param playable объект.
	 */
	public final void notifyChangedLevel(Playable playable)
	{
		// если персонаж игрока
		if(playable.isPlayer())
		{
			// получаем игрока
			Player player = playable.getPlayer();

			// получаем гильдию игрока
			Guild guild = playable.getGuild();

			// если гильдия есть
			if(guild != null)
				// обновляем параметры игрока в ней
				guild.updateMember(player);

			// обновляем информацию о игроке
			player.updateInfo();

			// получаем менеджера БД
			DataBaseManager manager = DataBaseManager.getInstance();

			// обновляем уровень в БД
			manager.updatePlayerLevel(player);

			// получаем список слушателей
			Array<LevelUpListener> listeners = getLevelUpListeners();

			// если слушателей нет, выходим
			if(listeners.isEmpty())
				return;

			listeners.readLock();
			try
			{
				LevelUpListener[] array = listeners.array();

				for(int i = 0, length = listeners.size(); i < length; i++)
					array[i].onLevelUp(player);
			}
			finally
			{
				listeners.readUnlock();
			}
		}
	}

	/**
	 * Уведомление об изминении локации играющего.
	 *
	 * @param playable объект.
	 */
	public final void notifyChangedZoneId(Playable playable)
	{
		// если персонаж игрок
		if(playable.isPlayer())
		{
			// получаем игрока
			Player player = playable.getPlayer();

			// получаем гильдию
			Guild guild = playable.getGuild();

			// если гильдия есть
			if(guild != null)
				// обновляем игрока в гильдии
				guild.updateMember(player);

			// получаем менеджера БД
			DataBaseManager manager = DataBaseManager.getInstance();

			// обновляем зону в БД
			manager.updatePlayerZoneId(player);
		}
	}

	/**
	 * Уведомление об сборе ресурса.
	 *
	 * @param resourse собранный ресурс.
	 * @param collector персонаж, который собрал.
	 */
	public final void notifyCollect(ResourseInstance resourse, Character collector)
	{
		// если ресурса нет, выходим
		if(resourse == null)
		{
			log.warning(new Exception("not found resourse"));
			return;
		}

		// если собирателя нет, выходим
		if(collector == null)
		{
			log.warning(new Exception("not found collector"));
			return;
		}

		// получаем АИ сборщика
		AI ai = collector.getAI();

		// если АИ есть
		if(ai != null)
			// его тоже уведомляем
			ai.notifyCollectResourse(resourse);

		// получаем игрока из сборщика
		Player player = collector.getPlayer();

		// если игрок есть
		if(player != null)
		{
			// получаем его список квестов
			QuestList questList = player.getQuestList();

			// если есть активные квесты
			if(questList != null && questList.hasActiveQuest())
			{
				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем ивент
				QuestEvent event = local.getNextQuestEvent();

				// запоминаем игрока
				event.setPlayer(player);
				// заносим тип ивента
				event.setType(QuestEventType.COLLECT_RESOURSE);
				// запоминаем ресурс
				event.setResourse(resourse);

				// уведомляем квесты
				QuestUtils.notifyQuests(questList.getActiveQuests(), event);
			}
		}
	}

	/**
	 * Уведомление о смерти персонажа.
	 *
	 * @param character умерший персонаж.
	 * @param killer убийца персонажа.
	 */
	public final void notifyDead(Character character, Character killer)
	{
		// сообщаем АИ о смерти
		character.getAI().notifyDead(killer);

		// если убитый является НПС
		if(character.isNpc())
		{
			// получаем НПС
			Npc npc = character.getNpc();

			// получаем главного дэмагера
			Character damager = npc.getMostDamager();

			// если есть топ демагер
			if(damager != null)
			{
				// определяем потенциального выполнителя квестов
				Character questor = damager.isSummon()? damager.getOwner() : damager;

				// определяем список квестов
				QuestList questList = questor == null? null : questor.getQuestList();

				// если есть активные квесты
				if(questList != null && questList.hasActiveQuest())
				{
					// получаем локальные объекты
					LocalObjects local = LocalObjects.get();

					// квестовый ивент
					QuestEvent event = local.getNextQuestEvent();

					// вставляем нпс
					event.setNpc(npc);
					// вставляем игрока
					event.setPlayer(questor.getPlayer());
					// тип ивента
					event.setType(QuestEventType.KILL_NPC);

					// уведомляем квесты
					QuestUtils.notifyQuests(questList.getActiveQuests(), event);

					// получаем группу выполнителя
					Party party = questor.getParty();

					// если группа есть
					if(party != null)
					{
						// получаем список членов группы
						Array<Player> members = party.getMembers();

						members.readLock();
						try
						{
							Player[] array = members.array();

							for(int i = 0, length = members.size(); i < length; i++)
							{
								// получаем члена группы
								Player member = array[i];

								// если его нет или это квестор, пропускаем
								if(member == null || member == questor)
									continue;

								// получаем список квестов члена группы
								questList = member.getQuestList();

								// если есть активные квесты
								if(questList != null && questList.hasActiveQuest())
								{
									// вставляем нпс
									event.setNpc(npc);
									// вставляем игрока
									event.setPlayer(member);
									// тип ивента
									event.setType(QuestEventType.KILL_NPC);

									// уведомляем квесты
									QuestUtils.notifyQuests(questList.getActiveQuests(), event);
								}
							}
						}
						finally
						{
							members.readUnlock();
						}
					}
				}
			}
		}

		// получаем список слушателей
		Array<DieListener> listeners = getDieListeners();

		// если нет слушателей, выходим
		if(listeners.isEmpty())
			return;

		listeners.readLock();
		try
		{
			DieListener[] array = listeners.array();

			// перебираем слушателей
			for(int i = 0, length = listeners.size(); i < length; i++)
				array[i].onDie(killer, character);
		}
		finally
		{
			listeners.readUnlock();
		}
	}

	/**
	 * Уведомление о удалении объекта.
	 *
	 * @param object удаленный объект.
	 */
	public final void notifyDelete(TObject object)
	{
		// получаем слушателей
		Array<DeleteListener> listeners = getDeleteListeners();

		if(listeners.isEmpty())
			return;

		listeners.readLock();
		try
		{
			DeleteListener[] array = listeners.array();

			for(int i = 0, length = listeners.size(); i < length; i++)
				array[i].onDelete(object);
		}
		finally
		{
			listeners.readUnlock();
		}
	}

	/**
	 * Уведомление об изминение экиперовки.
	 *
	 * @param owner владелец equipment.
	 */
	public final void notifyEquipmentChanged(Character owner)
	{
		if(owner == null)
			return;

		// если персонаж игрок
		if(owner.isPlayer())
		{
			// обновляем его экиперовку
			PacketManager.updateEquip(owner);

			// обновляем инвентарь
			PacketManager.updateInventory(owner.getPlayer());
		}
	}

	/**
	 * Уведомление о завершении каста скилла объектом.
	 *
	 * @param object кастующий объект.
	 * @param skill кастуемый скил.
	 */
	public final void notifyFinishCasting(Character object, Skill skill)
	{
		// записываем название скила
		object.setLastSkillName(skill.getSkillName());

		// записываем время завершения этого каста
		object.setLastCast(System.currentTimeMillis());

		// уведомляем АИ об завершении каста скила
		object.getAI().notifyFinishCasting(skill);
	}

	/**
	 * Уведомление о завершениисбора ресурса сборщиком.
	 *
	 * @param object собирающий ресурс.
	 * @param resourse собираемый ресурс..
	 */
	public final void notifyFinishCollect(Character object, ResourseInstance resourse)
	{
		// уведомляем АИ об завершении каста скила
		object.getAI().notifyCollectResourse(resourse);
	}

	/**
	 * Уведомление об изминении банка.
	 *
	 * @param owner владелец банка.
	 */
	public final void notifyGuildBankChanged(Character owner, int startCell)
	{
		if(owner == null || !owner.isPlayer())
			return;

		// обновляем банк гильдии
		PacketManager.updateGuildBank(owner.getPlayer(), startCell);
	}

	/**
	 * Уведомление об изминение состояния хп.
	 *
	 * @param owner вперсонаж, у которого было изминение.
	 */
	public final void notifyHpChanged(Character owner)
	{
		owner.updateHp();
	}

	/**
	 * Уведомление о добавлении итема в инвентарь.
	 *
	 * @param object владелец инвенторя.
	 * @param item добавленный итем.
	 */
	public final void notifyInventoryAddItem(Character object, ItemInstance item)
	{
		if(object == null || item == null)
		{
			log.warning(new Exception("not found object or item"));
			return;
		}

		// получаем список квестов
		QuestList questList = object.getQuestList();

		// если есть активные квесты
		if(questList != null && object.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим итем
			event.setItem(item);
			// вносим игрока
			event.setPlayer(object.getPlayer());
			// меняем на тип добавления в инвентарь
			event.setType(QuestEventType.INVENTORY_ADD_ITEM);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление об изминении инвенторя.
	 *
	 * @param owner владелец инвенторя.
	 */
	public final void notifyInventoryChanged(Character owner)
	{
		if(owner == null || !owner.isPlayer())
			return;

		PacketManager.updateInventory(owner.getPlayer());
	}

	/**
	 * Уведомление о удалении объектом итема.
	 *
	 * @param object владелец инвенторя.
	 * @param item удаленный итем.
	 */
	public final void notifyInventoryRemoveItem(Character object, ItemInstance item)
	{
		if(object == null || item == null)
		{
			log.warning(new Exception("not found object or item"));
			return;
		}

		// получаем список квестов персонажа
		QuestList questList = object.getQuestList();

		// если есть активные квесты
		if(questList != null && object.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим итем
			event.setItem(item);
			// вносим игрока
			event.setPlayer(object.getPlayer());
			// ставим тип поднятия итема
			event.setType(QuestEventType.INVENTORY_REMOVE_ITEM);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление об изминение состояния ьз.
	 *
	 * @param owner вперсонаж, у которого было изминение.
	 */
	public final void notifyMpChanged(Character owner)
	{
		owner.updateMp();
	}

	/**
	 * Уведомлении о том, что персонаж опрокинул кого-то.
	 *
	 * @param attacker тот кто опрокинул.
	 * @param attacked опрокинутый.
	 * @param skill используемый скил для удара.
	 */
	public final void notifyOwerturn(Character attacker, Character attacked, Skill skill)
	{
		attacker.applyChanceFunc(ChanceType.ON_OWERTURN, attacked, skill);
	}

	/**
	 * Уведомлении о том, что персонаж был опрокинут.
	 *
	 * @param attacked опрокинутый.
	 * @param attacker атакующий.
	 * @param skill используемый скил для удара.
	 */
	public final void notifyOwerturned(Character attacked, Character attacker, Skill skill)
	{
		attacked.applyChanceFunc(ChanceType.ON_OWERTURNED, attacker, skill);
	}

	/**
	 * Уведомление о поднятие объектом итем с земли.
	 *
	 * @param object поднявший итем объект.
	 * @param item поднятый итем.
	 */
	public final void notifyPickUpItem(Character object, ItemInstance item)
	{
		if(object == null || item == null)
		{
			log.warning(new Exception("not found object or item"));
			return;
		}

		// уведоиляем АИ об поднятии итема
		object.getAI().notifyPickUpItem(item);

		// получаем список квестов
		QuestList questList = object.getQuestList();

		// если есть активные квесты
		if(questList != null && object.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим итем
			event.setItem(item);
			// вносим игрока
			event.setPlayer(object.getPlayer());
			// ставим тип поднятия итема
			event.setType(QuestEventType.PICK_UP_ITEM);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление об изминении банка.
	 *
	 * @param owner владелец банка.
	 */
	public final void notifyPlayerBankChanged(Character owner, int startCell)
	{
		if(owner == null || !owner.isPlayer())
			return;

		PacketManager.updatePlayerBank(owner.getPlayer(), startCell);
	}

	/**
	 * Уведомление о завершение просмотра квестового мувика.
	 *
	 * @param character персонаж, который просматривал.
	 * @param movieId ид мувика.
	 * @param force принудительное ли завершение.
	 */
	public final void notifyQuestMovieEnded(Player character, int movieId, boolean force)
	{
		// получаем его список квестов
		QuestList questList = character.getQuestList();

		// если списка нет, выходим
		if(questList == null)
		{
			log.warning(new Exception("not found quest list for player " + character.getName()));
			return;
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем ивент
		QuestEvent event = local.getNextQuestEvent();

		// вносим игрока
		event.setPlayer(character);
		// ставим тип окончания мувика
		event.setType(QuestEventType.QUEST_MOVIE_ENDED);
		// вносим ид мувика
		event.setValue(movieId);

		// уведомляем квесты
		QuestUtils.notifyQuests(questList.getActiveQuests(), event);
	}

	/**
	 * Уведомлении о том, что персонаж заблокировал удар другого персонажа.
	 *
	 * @param attacked блокирующий.
	 * @param attacker атакующий.
	 * @param skill используемый скил для удара.
	 */
	public final void notifyShieldBlocked(Character attacked, Character attacker, Skill skill)
	{
		attacked.addDefenseCounter();
		attacked.applyChanceFunc(ChanceType.ON_SHIELD_BLOCK, attacker, skill);
	}

	/**
	 * Уведомление о изучении скила персонажем.
	 *
	 * @param character изучающий скил.
	 * @param learn изучаемый скил.
	 */
	public final void notifySkillLearned(Character character, SkillLearn learn)
	{
		if(character == null || learn == null)
		{
			log.warning(new Exception("not found object or item"));
			return;
		}

		// получаем квестовый лист объекта
		QuestList questList = character.getQuestList();

		// если это игрок с активными квестами
		if(questList != null && character.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем свободный ивент квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим игрока
			event.setPlayer(character.getPlayer());
			// ставим тип поднятия итема
			event.setType(QuestEventType.SKILL_LEARNED);
			// ставим ид скила
			event.setValue(learn.getId());

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление об спавне объекта.
	 *
	 * @param object отспавненный объект.
	 */
	public final void notifySpawn(TObject object)
	{
		// если объекта нет, выходим
		if(object == null)
			return;

		// получаем АИ объекта
		AI ai = object.getAI();

		// если АИ есть
		if(ai != null)
			// его тоже уведомляем
			ai.notifySpawn();

		// получаем игрока с объекта
		Player player = object.getPlayer();

		// если игрок есть
		if(player != null)
		{
			// получаем его квест лист
			QuestList questList = player.getQuestList();

			// если квест лист есть и есть активные квесты
			if(questList != null && questList.hasActiveQuest())
			{
				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем ивент
				QuestEvent event = local.getNextQuestEvent();

				// запоминаем игрока
				event.setPlayer(player);
				// заносим тип ивента
				event.setType(QuestEventType.PLAYER_SPAWN);

				// уведомляем квесты
				QuestUtils.notifyQuests(questList.getActiveQuests(), event);
			}

			// получаем списки слушателей
			Array<PlayerSpawnListener> listeners = getPlayerSpawnListeners();

			if(listeners.isEmpty())
				return;

			listeners.readLock();
			try
			{
				// получаем массив слушателей
				PlayerSpawnListener[] array = listeners.array();

				for(int i = 0, length = listeners.size(); i < length; i++)
					array[i].onSpawn(player);
			}
			finally
			{
				listeners.readUnlock();
			}
		}
	}

	/**
	 * Уведомление о выборе игрока для входа в игру.
	 *
	 * @param player выбранный игрок.
	 */
	public final void notifyPlayerSelect(Player player)
	{
		// получаем список слушателей выбора
		Array<PlayerSelectListener> listeners = getPlayerSelectListeners();

		// если их нет, выходим
		if(listeners.isEmpty())
			return;

		listeners.readLock();
		try
		{
			PlayerSelectListener[] array = listeners.array();

			// перебираем и обрабатываем событие
			for(int i = 0, length = listeners.size(); i < length; i++)
				array[i].onSelect(player);
		}
		finally
		{
			listeners.readUnlock();
		}
	}

	/**
	 * Уведомление об изминение состояния усталости.
	 *
	 * @param owner вперсонаж, у которого было изминение.
	 */
	public final void notifyStaminaChanged(Character owner)
	{
		// обнловляем стамину
		owner.updateStamina();

		// получаем список квестов
		QuestList questList = owner.getQuestList();

		// если список есть, персонаж игрок и у него есть активные квесты
		if(questList != null && owner.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим игрока
			event.setPlayer(owner.getPlayer());

			// вносим тип события
			event.setType(QuestEventType.CHANGED_HEART);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * Уведомление о начале каста скила объектом.
	 *
	 * @param object кастующий объект.
	 * @param skill кастуемый скил.
	 */
	public final void notifyStartCasting(Character object, Skill skill)
	{
		// если объекта нету ,выходим
		if(object == null)
			return;

		// получаем АИ объекта
		AI ai = object.getAI();

		// если АИ объекта есть
		if(ai != null)
			// уведомляем его
			ai.notifyStartCasting(skill);
	}

	/**
	 * Уведомление об начале диалога объекта с игроком.
	 *
	 * @param object разговариемый объект.
	 * @param player разговариваемый игрок.
	 */
	public final void notifyStartDialog(Character object, Player player)
	{
		// если объекта нет, выходим
		if(object == null)
			return;

		// получаем АИ объекта
		AI ai = object.getAI();

		// если АИ есть
		if(ai != null)
			// уведомляем
			ai.notifyStartDialog(player);
	}

	/**
	 * Уведомление об изминение статов игрока.
	 *
	 * @param owner игрок.
	 */
	public final void notifyStatChanged(Character owner)
	{
		if(owner == null)
			return;

		owner.updateInfo();
	}

	/**
	 * Уведомление об завершении диалога объекта с игроком.
	 *
	 * @param object разговариемый объект.
	 * @param player разговариваемый игрок.
	 */
	public final void notifyStopDialog(Character object, Player player)
	{
		// если объекта нет, выходим
		if(object == null)
			return;

		// получаем АИ объекта
		AI ai = object.getAI();

		// если АИ есть
		if(ai != null)
			// уведомляем
			ai.notifyStopDialog(player);
	}

	/**
	 * Уведомление об использовании итема.
	 *
	 * @param item используемый итем.
	 * @param owner вперсонаж, который использовал итем.
	 */
	public final void notifyUseItem(ItemInstance item, Character owner)
	{
		// получаем список квестов
		QuestList questList = owner.getQuestList();

		// если есть активные квесты
		if(owner.isPlayer() && questList.hasActiveQuest())
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// получаем событие квеста
			QuestEvent event = local.getNextQuestEvent();

			// вносим итем
			event.setItem(item);
			// вносим игрока
			event.setPlayer(owner.getPlayer());
			// вносим тип события
			event.setType(QuestEventType.USE_ITEM);

			// уведомляем квесты
			QuestUtils.notifyQuests(questList.getActiveQuests(), event);
		}
	}

	/**
	 * @param listener удаляемый листенер.
	 */
	public final void removeDeleteListener(DeleteListener listener)
	{
		deleteListeners.fastRemove(listener);
	}

	/**
	 * @param listener удаляемый листенер.
	 */
	public final void removeDieListener(DieListener listener)
	{
		dieListeners.fastRemove(listener);
	}

	/**
	 * Удаление слушателя лвл апов.
	 */
	public void removeLevelUpListener(LevelUpListener listener)
	{
		levelUpListeners.fastRemove(listener);
	}

	/**
	 * Удаление слушателя спавнов игроков.
	 */
	public void removePlayerSpawnListener(PlayerSpawnListener listener)
	{
		playerSpawnListeners.fastRemove(listener);
	}

	/**
	 * Удаление слушателя спавнов игроков.
	 */
	public void removePlayerSelectListener(PlayerSelectListener listener)
	{
		playerSelectListeners.fastRemove(listener);
	}
}
