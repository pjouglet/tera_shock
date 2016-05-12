package tera.gameserver.network;

import java.util.HashSet;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.network.clientpackets.AssentTrade;
import tera.gameserver.network.clientpackets.CanBeUsedName;
import tera.gameserver.network.clientpackets.CancelTrade;
import tera.gameserver.network.clientpackets.ClientKey;
import tera.gameserver.network.clientpackets.ClientPacket;
import tera.gameserver.network.clientpackets.EnchantFinish;
import tera.gameserver.network.clientpackets.EnteredWorld;
import tera.gameserver.network.clientpackets.NameChange;
import tera.gameserver.network.clientpackets.PlayerClimb;
import tera.gameserver.network.clientpackets.PlayerMove;
import tera.gameserver.network.clientpackets.PlayerMoveOnSkill;
import tera.gameserver.network.clientpackets.PlayerSay;
import tera.gameserver.network.clientpackets.PrivateSay;
import tera.gameserver.network.clientpackets.QuestMovieEnded;
import tera.gameserver.network.clientpackets.RequestActionAgree;
import tera.gameserver.network.clientpackets.RequestActionCancel;
import tera.gameserver.network.clientpackets.RequestActionInvite;
import tera.gameserver.network.clientpackets.RequestAddEnchantItem;
import tera.gameserver.network.clientpackets.RequestAuthLogin;
import tera.gameserver.network.clientpackets.RequestBankAdd;
import tera.gameserver.network.clientpackets.RequestBankChangeTab;
import tera.gameserver.network.clientpackets.RequestBankMovingItem;
import tera.gameserver.network.clientpackets.RequestBankSub;
import tera.gameserver.network.clientpackets.RequestCancelQuest;
import tera.gameserver.network.clientpackets.RequestClientClose;
import tera.gameserver.network.clientpackets.RequestCollectResourse;
import tera.gameserver.network.clientpackets.RequestConfirmServer;
import tera.gameserver.network.clientpackets.RequestCreatePlayer;
import tera.gameserver.network.clientpackets.RequestDeleteItem;
import tera.gameserver.network.clientpackets.RequestDeletePlayer;
import tera.gameserver.network.clientpackets.RequestDialogCancel;
import tera.gameserver.network.clientpackets.RequestDressingItem;
import tera.gameserver.network.clientpackets.RequestDuelCancel;
import tera.gameserver.network.clientpackets.RequestFriendAdd;
import tera.gameserver.network.clientpackets.RequestFriendList;
import tera.gameserver.network.clientpackets.RequestFriendRemove;
import tera.gameserver.network.clientpackets.RequestGuildChangeRank;
import tera.gameserver.network.clientpackets.RequestGuildCreateRank;
import tera.gameserver.network.clientpackets.RequestGuildExclude;
import tera.gameserver.network.clientpackets.RequestGuildIcon;
import tera.gameserver.network.clientpackets.RequestGuildInfo;
import tera.gameserver.network.clientpackets.RequestGuildLeave;
import tera.gameserver.network.clientpackets.RequestGuildLoadIcon;
import tera.gameserver.network.clientpackets.RequestGuildMakeLeader;
import tera.gameserver.network.clientpackets.RequestGuildRemoveRank;
import tera.gameserver.network.clientpackets.RequestGuildUpdateMessage;
import tera.gameserver.network.clientpackets.RequestGuildUpdateNote;
import tera.gameserver.network.clientpackets.RequestGuildUpdateRank;
import tera.gameserver.network.clientpackets.RequestGuildUpdateTitle;
import tera.gameserver.network.clientpackets.RequestInventoryInfo;
import tera.gameserver.network.clientpackets.RequestInventoryInfoItem;
import tera.gameserver.network.clientpackets.RequestInventoryMovingItem;
import tera.gameserver.network.clientpackets.RequestItemTemplateInfo;
import tera.gameserver.network.clientpackets.RequestLocalTeleport;
import tera.gameserver.network.clientpackets.RequestLockOnTarget;
import tera.gameserver.network.clientpackets.RequestNpcAddBuyShop;
import tera.gameserver.network.clientpackets.RequestNpcAddSellShop;
import tera.gameserver.network.clientpackets.RequestNpcConfirmShop;
import tera.gameserver.network.clientpackets.RequestNpcConfirmSkillShop;
import tera.gameserver.network.clientpackets.RequestNpcInteraction;
import tera.gameserver.network.clientpackets.RequestNpcLink;
import tera.gameserver.network.clientpackets.RequestNpcStartPegasFly;
import tera.gameserver.network.clientpackets.RequestNpcSubBuyShop;
import tera.gameserver.network.clientpackets.RequestNpcSubSellShop;
import tera.gameserver.network.clientpackets.RequestPartyChange;
import tera.gameserver.network.clientpackets.RequestPartyDisband;
import tera.gameserver.network.clientpackets.RequestPartyInvite;
import tera.gameserver.network.clientpackets.RequestPartyKick;
import tera.gameserver.network.clientpackets.RequestPartyLeave;
import tera.gameserver.network.clientpackets.RequestPartyMakeLeader;
import tera.gameserver.network.clientpackets.RequestPickUpItem;
import tera.gameserver.network.clientpackets.RequestPlayerList;
import tera.gameserver.network.clientpackets.RequestRessurect;
import tera.gameserver.network.clientpackets.RequestRestart;
import tera.gameserver.network.clientpackets.RequestServerCheck;
import tera.gameserver.network.clientpackets.RequestSkillAction;
import tera.gameserver.network.clientpackets.RequestSortInventory;
import tera.gameserver.network.clientpackets.RequestStartClimb;
import tera.gameserver.network.clientpackets.RequestStartEmotion;
import tera.gameserver.network.clientpackets.RequestState;
import tera.gameserver.network.clientpackets.RequestTradeAddItem;
import tera.gameserver.network.clientpackets.RequestTradeLock;
import tera.gameserver.network.clientpackets.RequestUpdateQuestPanel;
import tera.gameserver.network.clientpackets.RequestUseDefenseSkill;
import tera.gameserver.network.clientpackets.RequestUseItem;
import tera.gameserver.network.clientpackets.RequestUseQueueSkill;
import tera.gameserver.network.clientpackets.RequestUseRangeSkill;
import tera.gameserver.network.clientpackets.RequestUseRushSkill;
import tera.gameserver.network.clientpackets.RequestUseScroll;
import tera.gameserver.network.clientpackets.RequestUseShortSkill;
import tera.gameserver.network.clientpackets.RequestWorldZone;
import tera.gameserver.network.clientpackets.SelectSkillLearn;
import tera.gameserver.network.clientpackets.SelectedPlayer;
import tera.gameserver.network.clientpackets.UpdateClientSetting;
import tera.gameserver.network.clientpackets.UpdateHotKey;
import tera.gameserver.network.clientpackets.UpdateTitle;

/**
 * Перечисление типов клиентских пакетов.
 * 
 * @author Ronn
 */
public enum ClientPacketType
{
	/** перемещения игрока, версия 172 */
	PLAYER_MOVE(0x5A4B, new PlayerMove()),
	/** запрос на перемещение во время лазания */
	PLAYER_CLIMB(0xEDC9, new PlayerClimb()),
	/** запрос на лазание */
	REQUESTPLAYER_CLIMB(0xBB06, new RequestStartClimb()),
	/** пакет с оправкой движения, версия */
	PLAYER_SKILL_MOVE(0x91A5, new PlayerMoveOnSkill()),
	/** сообщение от игрока, версия 172 */
	PLAYER_SAY(0xAE9B, new PlayerSay()),
	/** приватный месседж от игрока, версия 172 */
	PLAYER_PRIVATE_SAY(0xCADA, new PrivateSay()),
	/** обновление титула игрока */
	UPDATE_TITLE(0x9CD4, new UpdateTitle()),

	/** запрос ид зоны клиентом, верия 172 */
	REQUEST_WORLD_ZONE(0x5E43, new RequestWorldZone()),
	/** запрос на возможность войти в состояние */
	REQUEST_STATE(0xAB5E, new RequestState()),

	/** запрос содержания инвенторя, версия 172 */
	REQUEST_INVENTORY_INFO(0xEEF6, new RequestInventoryInfo()),
	/** запрос на одевание придмета, версия 172 */
	REQUEST_DRESSING_ITEM(0xC7B5, new RequestDressingItem()),
	/** запрос на снятие предмета, версия 172 */
	REQUEST_TAKING_ITEM(0xB983, new RequestDressingItem()),
	/** запрос на перемещение итема, версия 172 */
	REQUEST_INVENTORY_MOVING_ITEM(0x9BDA, new RequestInventoryMovingItem()),
	/** запрос на перемещение итема, версия 172 */
	REQUEST_BANK_MOVING_ITEM(0xEFC9, new RequestBankMovingItem()),
	/** запрос на смену вкладки в банке */
	REQUEST_BANK_CHANGE_TAB(0xDBB0, new RequestBankChangeTab()),
	/** запрос на сортировку инвенторя, версия 172 */
	REQUEST_SORT_INVENTORY(0xA92D, new RequestSortInventory()), //
	/** запрос на удаление итема с инвенторя, версия 172 */
	REQUEST_DELETE_ITEM(0xD9EC, new RequestDeleteItem()), // 14 00 35 F2 96 5F 00 00 00 00 00 00 04 00 00 00
	/** пакет, запрашивающий информацию о итеме, версия 172 */
	REQUEST_INVENTORY_ITEM_INFO(0xBC87, new RequestInventoryInfoItem()), // 28 00 F4 C4 1E 00 13 00 00 00 C4 71 00 00 00 00
	/** запрос на поднятие итема, версия 172 */
	REQUEST_PICK_UP_ITEM(0x4E2D, new RequestPickUpItem()),
	/** заппрос на использование итема, версия 172 */
	REQUEST_USE_ITEM(0xE307, new RequestUseItem()), // 3A 00 F6 B0 AB 76 00 00 00 00 00 00 47 1F 00 00
	/** запрос на использование свитка */
	REQUEST_USE_SCROLL(0x8386, new RequestUseScroll()),
	/** запрос на отображение инфы об темплейте итема, версия 172 */
	REQUEST_ITEM_TEMPLATE_INFO(0xA9DA, new RequestItemTemplateInfo()),

	/** запрос на запуск эмоции, версия 172 */
	REQUEST_START_EMOTION(0xA2F1, new RequestStartEmotion()),

	/** запрос инфы о клане, версия 172 */
	REQUEST_GUILD_INFO(0x81A4, new RequestGuildInfo()),
	/** запрос выхода из клана, версия 172 */
	REQUEST_GUILD_LEAVE(0x9EBD, new RequestGuildLeave()),
	/** апрос на исключение участника гильдии */
	REQUEST_GUILD_EXLUDE(0x8EB7, new RequestGuildExclude()),
	/** запрос на изменение ранга участника гильдии */
	REQUEST_GUILD_UPDATE_RANK(0xFF13, new RequestGuildUpdateRank()),
	/** запрос на удаление ранга гильдии */
	REQUEST_GUILD_REMOVE_RANK(0x81BD, new RequestGuildRemoveRank()),
	/** запрос на изменение прав у ранга */
	REQUEST_GUILD_CHANGE_RANK(0xD3D0, new RequestGuildChangeRank()),
	/** запрос на создание низкоправного ранга */
	REQUEST_GUILD_CREATE_RANK(0x7A56, new RequestGuildCreateRank()),
	/** запрос на загрузку иконки */
	REQUEST_GUIL_LOAD_ICON(0xF883, new RequestGuildLoadIcon()),
	/** запрос на иконку гильдии */
	REQUEST_GUILD_ICON_INFO(0xAC93, new RequestGuildIcon()),
	/** запрос на передачу мастера гильдии */
	REQUEST_GUILD_MAKE_LEADER(0xAD5E, new RequestGuildMakeLeader()),
	/** запрос на обновление титула гильдии */
	REQUEST_GUILD_UPDATE_TITLE(0xBACE, new RequestGuildUpdateTitle()),
	/** запрос на обновление титула гильдии */
	REQUEST_GUILD_UPDATE_MESSAGE(0xFDC7, new RequestGuildUpdateMessage()),
	/** запрос на обновление заметки игрока гильдии */
	REQUEST_GUILD_UPDATE_NOTE(0x4FE9, new RequestGuildUpdateNote()),

	/** запрос на юз мили скила, версия 172 */
	REQUEST_USE_SHORT_SKILL(0xCF34, new RequestUseShortSkill()),
	/** запрос на юз серийного скила, версия 172 */
	REQUEST_USE_QUEUE_SKILL(0xCED8, new RequestUseQueueSkill()),
	/** запрос на юз рендж скила, версия 172 */
	REQUEST_USE_RANGE_SKILL(0x7017, new RequestUseRangeSkill()),
	/** запрос на юз догоняющего скила, версия 172 */
	REQUEST_USE_RUSH_SKILL(0xBF14, new RequestUseRushSkill()),
	/** запрос на юз деф скила, версия 172 */
	REQUEST_USE_DEFENSE_SKILL(0x57E6, new RequestUseDefenseSkill()),
	/** запрос на добавление цели к лок он скилу */
	REQUEST_LOCK_ON_TARGET(0xD1F2, new RequestLockOnTarget()),
	/** запрос на действие над скилом */
	REQUEST_SKILL_ACTION(0xA6B8, new RequestSkillAction()),

	/** обновление параметров раскладки, версия 172 */
	UPDATE_HOT_KEY(0xB6BA, new UpdateHotKey()),
	/** обновление настроек клиента, версия 172 */
	UPDATE_CLIENT_SETTING(0xCEF8, new UpdateClientSetting()),

	/** запрос на воскрешение в город, версия 172 */
	REQUEST_RESSURECT(0x50D1, new RequestRessurect()), // 0C 00 20 E0 00 00 00 00 FF FF FF FF
	/** запрос на проверку сервера, версия 172 */
	REQUEST_CONFIRM_SERVER(0x9FCA, new RequestConfirmServer()),
	REQUEST_CHECK_SERVER(0xD6C0, new RequestServerCheck()),

	/** запрос на выход на выбор персонажей, версия 172 */
	REQUEST_RESTART(0xF8B1, new RequestRestart()),
	/** запрос на выход из игры, версия 172 */
	REQUEST_CLIENT_CLOSE(0xC873, new RequestClientClose()),

	REQUEST_LOCAL_TELEPORT(0xCADC, new RequestLocalTeleport()),

	REQUEST_DUEL_CANCEL(0xF0AE, new RequestDuelCancel()),

	/** запрос на взаимодействие с нпс, версия 172 */
	REQUEST_NPC_INTERACTION(0xB6AE, new RequestNpcInteraction()), // 10 00 66 C3 1D F0 D4 1E 00 00 00 00 00 00 00 00
	/** запрос на обработку кнопки в диалоге, версия 172 */
	REQUEST_NPC_LINK(0xBF7B, new RequestNpcLink()), // 14 00 20 D9 40 7B D5 1E 01 00 00 00 FF FF FF FF
	/** клиентский пакет о пожении итема в магазин покупки, версия 172 */
	REQUEST_NPC_ADD_BUY_SHOP(0xF406, new RequestNpcAddBuyShop()), // 1C 00 CC EB F5 5F 00 00 00 00 00 00 65 B1 B6 48
	/** клиентский пакет о удаление итема из трейд покупки, версия 172 */
	REQUEST_NPC_SUB_BUY_SHOP(0xA087, new RequestNpcSubBuyShop()), // 20 00 12 CC F5 5F 00 00 00 00 00 00 65 B1 B6 48
	/** клиентский пакет о пожении итема в трейд покупки, версия 172 */
	REQUEST_NPC_ADD_SELL_SHOP(0x8EC1, new RequestNpcAddSellShop()), // 18 00 5E F5 F5 5F 00 00 00 00 00 00 65 B1 B6 48
	/** клиентский пакет о удаление итема из трейд покупки, версия 172 */
	REQUEST_NPC_SUB_SELL_SHOP(0x51D3, new RequestNpcSubSellShop()), // 18 00 74 D1 F5 5F 00 00 00 00 00 00 65 B1 B6 48
	/** клиентский пакет о подтверждении сделки в магазине, версия 172 */
	REQUEST_NPC_CONFIRM_SHOP(0x8082, new RequestNpcConfirmShop()), // 10 00 77 65 F5 5F 00 00 00 00 00 00 65 B1 B6 48
	/** запрос на изучение скила, версия 172 */
	REQUEST_NPC_CONFIRM_SKILL_SHOP(0xC456, new RequestNpcConfirmSkillShop()),
	/** запрос на добавление в банк */
	REQUEST_NPC_BANK_ADD(0xF25D, new RequestBankAdd()),
	/** запрос на получение итема из банка */
	REQUEST_NPC_BANK_SUB(0xEF1D, new RequestBankSub()),

	/** запрос на обновлении квеста на панели */
	REQUEST_QUEST_PANEL(0xB1B2, new RequestUpdateQuestPanel()),
	/** запрос на отмену квеста */
	REQUEST_QUEST_CANCEL(0x59AE, new RequestCancelQuest()),

	// TODO
	/** клиентский пакет с выбором скила для изучения */
	CLIENT_SELECT_SKILL_LEARN(0xE7E0, new SelectSkillLearn()),

	/** запрос на запуск полета на пегасе по указанному маршруту, версия 172 */
	REQUEST_NPC_START_PEGAS_FLY(0xC762, new RequestNpcStartPegasFly()), // 08 00 62 C7 08 00 00 00

	/** запрос на закрытие диалогов, версия 172 */
	REQUEST_DIALOG_CANCEL(0xBFB9, new RequestDialogCancel()), // 0C 00 31 C5 09 00 00 00 65 B1 B6 48

	/** запрос на блокировку трейда, версия 172 */
	REQUEST_TRADE_LOCK(0x637C, new RequestTradeLock()),
	/** запрос на добавление итема в трейд, версия 172 */
	REQUEST_TRADE_ADD_ITEM(0x683A, new RequestTradeAddItem()),
	/** согласие на начало трейда, версия 172 */
	ASSENT_TRADE(0x65A6, new AssentTrade()), // A6 65 03 00
	/** отмена трейда, версия 172 0x72D6 */
	CANCEL_TRADE(0x72D6, new CancelTrade()), // D6 72 03 00 00 00 00 00 00 00

	QUEST_MOVIE_ENDED(0x920B, new QuestMovieEnded()),

	/** запрос на приглашение игрока в акшен, версия 172 */
	REQUEST_ACTION_INVITE(0xADE2, new RequestActionInvite()),
	/** согласие на участие в акшене, версия 172 */
	REQUEST_ACTION_AGREE(0xA05D, new RequestActionAgree()),
	/** отмена акшена игроком, версия 172 */
	REQUEST_ACTION_CANCEL(0xE58F, new RequestActionCancel()),

	/** запрос на приглашение человека в пати, версия 172 */
	REQUEST_PARTY_INVITE(0x0111, new RequestPartyInvite()),
	/** запрос на выход из пати, версия 172 */
	REQUEST_PARTY_LEAVE(0x8002, new RequestPartyLeave()),
	/** запрос на изменение группе */
	REQUEST_PARTY_CHANGE(0x784F, new RequestPartyChange()),
	/** запрос на смену лидера в группе */
	REQUEST_PARTY_MAKE_LEADER(0xB14B, new RequestPartyMakeLeader()),
	/** запрос на кик из группы */
	REQUEST_PARTY_KICK(0xFD15, new RequestPartyKick()),
	/** запрос на расформирование группы */
	REQUEST_PARTY_DISBAND(0xC041, new RequestPartyDisband()),

	/** клиентский запрос на сбор ресурса */
	REQUEST_COLLECT_RESOURSE(0x94FD, new RequestCollectResourse()),

	/** запрос на список друзей */
	REQUEST_FRIEND_LIST(0xAB9A, new RequestFriendList()),
	/** запрос на добавление нового друга */
	REQUEST_FRIEND_ADD(0xDE4A, new RequestFriendAdd()),
	/** запрос на удаление друга */
	REQUEST_FRIEND_REMOVE(0x5FA8, new RequestFriendRemove()),

	/** запрос на авторизацию на сервере, версия 172 */
	REQUEST_AUTH_LOGIN(0x4DBC, new RequestAuthLogin()),
	/** запрос на создание игрока, версия 172 */
	REQUEST_CREATE_PLAYER(0x8CE9, new RequestCreatePlayer()),
	/** запрос на удаление игрока, версия 172 */
	REQUEST_DELETE_PLAYER(0xD5F7, new RequestDeletePlayer()),
	/** запрос списка игроков на аккаунте, версия 172 */
	REQUEST_PLAYER_LIST(0xBCB9, new RequestPlayerList()),
	/** проверка на корректность имени, версия 172 */
	CAN_BE_USED_NAME(0x56E3, new CanBeUsedName()),
	/** пакет, запрашивающий возможность применить новое имя игроку, версия 172 */
	NAME_CHANGED(0xBB7A, new NameChange()),

	/** пакет с клиентским ключем */
	CLIENT_KEY(0xFFFF, new ClientKey()),

	/** запрос на добавление предмета в диалог заточки */
	REQUEST_ADD_ENCHANT_ITEM(0x5E4E, new RequestAddEnchantItem()),
	/** уведомление о завершении анимации заточки */
	ENCHANT_FINISH(0xDDB4, new EnchantFinish()),

	/** пакет выбора персонажа, версия */
	PLAYER_SELECTED_PACKET(0xE3A4, new SelectedPlayer()),
	/** пакет входа в мир персонажа, версия */
	PLAYER_ENTERED_PACKET(0x8E3C, new EnteredWorld());

	private static final Logger log = Loggers.getLogger(ClientPacketType.class);

	/** массив пакетов */
	private static ClientPacket[] packets;

	/**
	 * Возвращает новый экземпляр пакета в соответствии с опкодом
	 * 
	 * @param opcode опкод пакета.
	 * @return экземпляр нужного пакета.
	 */
	public static ClientPacket createPacket(int opcode)
	{
		ClientPacket packet = packets[opcode];
		return packet == null ? null : packet.newInstance();
	}

	/**
	 * Инициализация клиентских пакетов.
	 */
	public static void init()
	{
		HashSet<Integer> set = new HashSet<Integer>();

		ClientPacketType[] elements = values();

		for (ClientPacketType packet : elements)
		{
			int index = packet.getOpcode();

			if (set.contains(index))
				log.warning("found duplicate opcode " + index + " or " + Integer.toHexString(packet.getOpcode()) + " for " + packet + "!");

			set.add(index);
		}

		set.clear();

		packets = new ClientPacket[Short.MAX_VALUE * 2 + 2];

		for (ClientPacketType element : elements)
			packets[element.getOpcode()] = element.getPacket();

		log.info("client packets prepared.");
	}

	/** пул клиенстких пакетов */
	private final FoldablePool<ClientPacket> pool;

	/** экземпляр пакета */
	private final ClientPacket packet;

	/** опкод пакета */
	private int opcode;

	/**
	 * @param opcode опкод пакета.
	 * @param packet экземпляр пакета.
	 */
	private ClientPacketType(int opcode, ClientPacket packet)
	{
		this.opcode = opcode;
		this.packet = packet;
		this.packet.setPacketType(this);
		this.pool = Pools.newConcurrentFoldablePool(ClientPacket.class);
	}

	/**
	 * @return опкод пакета.
	 */
	public final int getOpcode()
	{
		return opcode;
	}

	/**
	 * @return экземпляр пакета.
	 */
	public final ClientPacket getPacket()
	{
		return packet;
	}

	/**
	 * @return получить пул пакетов соотвествующего типа.
	 */
	public final FoldablePool<ClientPacket> getPool()
	{
		return pool;
	}

	/**
	 * @param opcode опкод пакета.
	 */
	public final void setOpcode(int opcode)
	{
		this.opcode = opcode;
	}
}