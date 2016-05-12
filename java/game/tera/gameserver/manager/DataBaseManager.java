package tera.gameserver.manager;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import rlib.database.ConnectFactory;
import rlib.database.DBUtils;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.Wraps;
import tera.Config;
import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionState;
import tera.gameserver.model.Account;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.FriendInfo;
import tera.gameserver.model.FriendList;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildIcon;
import tera.gameserver.model.GuildMember;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.GuildRankLaw;
import tera.gameserver.model.ReuseSkill;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.PlayerEquipment;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.inventory.PlayerBank;
import tera.gameserver.model.inventory.PlayerInventory;
import tera.gameserver.model.items.CrystalInstance;
import tera.gameserver.model.items.CrystalList;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.ItemLocation;
import tera.gameserver.model.playable.DeprecatedPlayerFace;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.model.playable.PlayerPreview;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestDate;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.network.serverpackets.PlayerDeleteResult;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.tables.PlayerTable;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.tables.TerritoryTable;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.NpcTemplate;
import tera.gameserver.templates.PlayerTemplate;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

import com.jolbox.bonecp.BoneCPConfig;

/**
 * Менеджер для работы с БД.
 * 
 * @author Ronn
 * @created 04.03.2012
 */
public final class DataBaseManager {

	private static final Logger LOGGER = Loggers.getLogger(DataBaseManager.class);

	private static final String UPDATE_ITEM = "UPDATE `items` SET `item_count`=?, `owner_id`=?, `location`=?, `index`=?, `has_crystal`=?, `autor`=?, `bonus_id`=?, `enchant_level`=?, `owner_name`=? WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_DATA_ITEM = "UPDATE `items` SET `item_count`=?, `has_crystal`=?, `autor`=?, `bonus_id`=?, `enchant_level`=?, `owner_name`=?  WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_LOCATION_ITEM = "UPDATE `items` SET `owner_id`=?, `location`=?, `index`=? WHERE `object_id`=? LIMIT 1";
	private static final String CREATE_ITEM = "INSERT INTO `items` (object_id, item_id, item_count, location) VALUES (?,?,?,?)";

	private static final String UPDATE_INVENTORY = "UPDATE `character_inventors` SET `owner_id`=? WHERE `owner_id`=? LIMIT 1";
	private static final String CREATE_INVENTORY = "INSERT INTO `character_inventors` (owner_id, level) VALUES (?,?)";

	private static final String INSERT_ACCOUNT_BANK = "INSERT INTO `account_bank` (account_name) VALUES (?)";
	private static final String RESTORE_ACCOUNT_BANK = "SELECT * FROM `account_bank` WHERE `account_name` = ? LIMIT 1";

	private static final String CREATE_SKILL = "INSERT INTO `character_skills` (object_id, class_id, skill_id) VALUES (?,?,?)";
	private static final String DELETE_SKILL = "DELETE FROM `character_skills` WHERE `object_id`=? AND `class_id`=? AND `skill_id`=? LIMIT 1";

	private static final String UPDATE_PLAYER_LEVEL = "UPDATE `characters` SET `level`=? WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_PLAYER_ZONE_ID = "UPDATE `characters` SET `zone_id`=? WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_PLAYER_CONTINENT_ID = "UPDATE `characters` SET `continent_id`=? WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_PLAYER_GUILD = "UPDATE `characters` SET `guild_id`=?, `guild_rank`=? WHERE `object_id`=? LIMIT 1";
	private static final String UPDATE_PLAYER_TITLE = "UPDATE `characters` SET `title`= ? WHERE `object_id`= ? LIMIT 1";
	private static final String UPDATE_PLAYER_GUILD_NOTE = "UPDATE `characters` SET `guild_note`= ? WHERE `object_id`= ? LIMIT 1";

	private static final String CREATE_QUEST = "REPLACE INTO `character_quests` (object_id, quest_id, state, date) VALUES (?,?,?,?)";
	private static final String UPDATE_QUEST = "UPDATE `character_quests` SET `state` = ?, `panel_state` = ? WHERE `object_id` = ? AND `quest_id` = ?";
	private static final String FINISH_QUEST = "UPDATE `character_quests` SET `state` = 0, date = ? WHERE `object_id` = ? AND `quest_id` = ?";
	private static final String REMOVE_QUEST = "DELETE FROM `character_quests` WHERE `object_id` = ? AND `quest_id` = ?";

	private static final String STORE_QUEST_VAR = "REPLACE INTO `character_quest_vars` (object_id, quest_id, name, value) VALUES (?,?,?,?)";
	private static final String RESTORE_QUEST_VAR = "SELECT * FROM `character_quest_vars` WHERE `object_id` = ? AND quest_id = ?";
	private static final String CLEAR_QUEST_VAR = "DELETE FROM `character_quest_vars` WHERE `object_id` = ? AND quest_id = ?";

	private static final String CREATE_GUILD_RANK = "INSERT INTO `guild_ranks` (`guild_id`, `rank_name`, `order`, `law`) VALUES (?,?,?,?)";
	private static final String REMOVE_GUILD_RANK = "DELETE FROM `guild_ranks` WHERE `guild_id` = ? AND `order` = ? LIMIT 1";
	private static final String REMOVE_GUILD_RANK_FOR_PLAYERS = "UPDATE `characters` SET `guild_rank` = ? WHERE `guild_id` = ? AND `guild_rank` = ? LIMIT ?";
	private static final String UPDATE_GUILD_ICON = "UPDATE `guilds` SET `icon` = ?, icon_name = ? WHERE `id` = ? LIMIT 1";
	private static final String UPDATE_GUILD_RANK = "UPDATE `guild_ranks` SET `rank_name` = ?, law = ? WHERE `order` = ? AND `guild_id` = ? LIMIT 1";
	private static final String UPDATE_GUILD_TITLE = "UPDATE `guilds` SET `title` = ? WHERE `id` = ? LIMIT 1";
	private static final String UPDATE_GUILD_MESSAGE = "UPDATE `guilds` SET `message` = ? WHERE `id` = ? LIMIT 1";

	private static final String REMOVE_WAIT_ITEM = "DELETE FROM `wait_items` WHERE `order` = ? LIMIT 1";
	private static final String REMOVE_WAIT_SKILL = "DELETE FROM `wait_skills` WHERE `order` = ? LIMIT 1";

	private static final String ADD_STORE_TERRITORY = "INSERT INTO `character_territories` (object_id, territory_id) VALUES (?,?)";

	private static final String SELECT_SERVER_VARIABLES = "SELECT * FROM `server_variables`";
	private static final String REMOVE_SERVER_VARIABLE = "DELETE FROM `server_variables` WHERE `var_name` = ? LIMIT 1";
	private static final String INSERT_SERVER_VARIABLE = "INSERT INTO `server_variables` (var_name, var_value) VALUES(?,?)";
	private static final String UPDATE_SERVER_VARIABLE = "UPDATE `server_variables` SET `var_value` = ? WHERE `var_name` = ? LIMIT 1";

	private static final String SELECT_PLAYER_VARIABLES = "SELECT * FROM `character_variables` WHERE `object_id` = ?";
	private static final String REMOVE_PLAYER_VARIABLE = "DELETE FROM `character_variables` WHERE `object_id` = ? AND `var_name` = ? LIMIT 1";
	private static final String INSERT_PLAYER_VARIABLE = "INSERT INTO `character_variables` (object_id, var_name, var_value) VALUES(?,?,?)";
	private static final String UPDATE_PLAYER_VARIABLE = "UPDATE `character_variables` SET `var_value` = ? WHERE `object_id` = ? AND `var_name` = ? LIMIT 1";

	private static final String SELECT_BOSS_SPAWNS = "SELECT * FROM `boss_spawn`";
	private static final String INSERT_BOSS_SPAWNS = "INSERT INTO `boss_spawn` (npc_id, npc_type, spawn) VALUES(?,?,?)";
	private static final String UPDATE_BOSS_SPAWNS = "UPDATE `boss_spawn` SET `spawn` = ? WHERE `npc_id` = ? AND `npc_type` = ?";

	private static final String SELECT_PLAYER_FRIENDS = "SELECT `friend_id`, `friend_note` FROM `character_friends` WHERE `object_id` = ?";
	private static final String SELECT_PLAYER_FRIEND = "SELECT `class_id`, `race_id`, `level`, char_name FROM `characters` WHERE `object_id` = ? LIMIT 1";
	private static final String INSERT_PLAYER_FRIEND = "INSERT INTO `character_friends` (object_id, friend_id, friend_note) VALUES(?,?,?)";
	// private static final String UPDATE_PLAYER_FRIEND_NOTE =
	// "UPDATE `character_friends` SET `friend_note` = ? WHERE `object_id` = ? AND `friend_id` = ? LIMIT 1";
	private static final String REMOME_PLAYER_FRIEND = "DELETE FROM `character_friends` WHERE `object_id` = ? AND `friend_id` = ? LIMIT 1";

	private static final String SELECT_REGION_STATUS = "SELECT * FROM `region_status` WHERE `region_id` = ? LIMIT 1";
	private static final String SELECT_REGION_REGISTER = "SELECT * FROM `region_war_register` WHERE `region_id` = ?";
	private static final String INSERT_REGION_STATUS = "INSERT INTO `region_status` (region_id, owner_id, state) VALUES(?,?,?)";
	private static final String INSERT_REGION_REGISTER_GUILD = "INSERT INTO `region_war_register` (region_id, guild_id) VALUES(?,?)";
	private static final String REMOVE_REGION_REGISTER_GUILD = "DELETE FROM `region_war_register` WHERE `region_id` = ? AND `guild_id` = ? LIMIT 1";
	private static final String UPDATE_REGION_STATE = "UPDATE `region_status` SET `state`= ? WHERE `region_id`= ? LIMIT 1";
	private static final String UPDATE_REGION_OWNER = "UPDATE `region_status` SET `owner_id`= ? WHERE `region_id`= ? LIMIT 1";

	private static final String SELECT_GUILDS = "SELECT * FROM `guilds`";
	private static final String SELECT_GUILD_NAME = "SELECT id FROM `guilds` WHERE `name`= ?";
	private static final String SELECT_GUILD_RANKS = "SELECT * FROM `guild_ranks` WHERE `guild_id` = ?";
	private static final String SELECT_GUILD_BANK_ITEMS = "SELECT * FROM `items` WHERE `owner_id` = ? AND `location` = '" + ItemLocation.GUILD_BANK.ordinal() + "' LIMIT "
			+ (Config.WORLD_GUILD_BANK_MAX_SIZE + 1);
	private static final String SELECT_GUILD_MEMBERS = "SELECT class_id, level, char_name, guild_note, zone_id, object_id, race_id, guild_rank, sex, last_online FROM `characters` WHERE `guild_id` = ?";
	private static final String REMOVE_GUILD = "DELETE FROM `guilds` WHERE `id` = ? LIMIT 1";
	private static final String REMOVE_GUILD_MEMBERS = "UPDATE `characters` SET `guild_id` = '0', `guild_rank` = '0' WHERE `guild_id` = ?";
	private static final String INSERT_GUILD = "INSERT INTO `guilds` (`id`, `name`, `title`, `level`) VALUES (?, ?, ''," + 1 + ")";

	private static final String SELECT_PLAYER_LIST = "SELECT `object_id` FROM `characters` WHERE `account_name` = ? LIMIT 8";

	private static final String SELECT_PLAYER_OJECT_ID = "SELECT object_id FROM `characters` WHERE `char_name`= ? LIMIT 1";
	private static final String SELECT_PLAYER_FACE = "SELECT * FROM `character_faces` WHERE `objectId`= ? LIMIT 1";
	private static final String SELECT_PLAYER_PREVIEW = "SELECT * FROM `characters` WHERE `object_id`= ? LIMIT 1";
	private static final String SELECT_PLAYER_ACCOUNT = "SELECT `char_name` FROM `characters` WHERE `account_name`= ? LIMIT 8";
	private static final String DELETE_PLAYER = "DELETE FROM `characters` WHERE `object_id` = ? AND `account_name` = ? LIMIT 1";
	private static final String SELECT_PLAYER_CHECK_NAME = "SELECT object_id FROM `characters` WHERE `char_name` = ? LIMIT 1";
	private static final String SELECT_PLAYER_CHECK_ID = "SELECT `object_id` FROM `characters` WHERE `object_id`= ? LIMIT 1";
	private static final String UPDATE_PLAYER_LOCATION = "UPDATE `characters` SET `x` = ?, `y` = ?, `z` = ?, `heading` = ?, `continent_id` = ?, `zone_id` = ? WHERE `char_name`= ? LIMIT 1";
	private static final String UPDATE_PLAYER_CLASS = "UPDATE `characters` SET `class_id` = ? WHERE `object_id`= ? LIMIT 1";
	private static final String UPDATE_PLAYER_RACE = "UPDATE `characters` SET `race_id` = ?, `sex` = ? WHERE `object_id`= ? LIMIT 1";

	private static final String SELECT_ACCOUNT = "SELECT password, email, last_ip, allow_ips, comments, end_block, end_pay, access_level FROM `accounts` WHERE `login`= ? LIMIT 1";
	private static final String INSERT_ACCOUNT = "INSERT INTO `accounts` (login, password, email, access_level, end_pay, end_block, last_ip, allow_ips, comments) VALUES (?,?,?,?,?,?,?,?,?)";

	private static final String INSERT_PLAYER_APPEARANCE = "INSERT INTO `character_appearances` (object_id, face_color, face_skin, adorments_skin, features_skin, features_color, voice, bone_structure_brow, "
			+ "bone_structure_cheekbones, bone_structure_jaw, bone_structure_jaw_jut, ears_rotation, ears_extension, ears_trim, ears_size, eyes_width, eyes_height, eyes_separation, eyes_angle,"
			+ "eyes_inner_brow, eyes_outer_brow, nose_extension, nose_size, nose_bridge, nose_nostril_width, nose_tip_width, nose_tip, nose_nostril_flare, mouth_pucker,"
			+ "mouth_position, mouth_width, mouth_lip_thickness, mouse_corners, eyes_shape, nose_bend, bone_structure_jaw_width, mouth_gape) VALUES "
			+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_PLAYER_APPEARANCE = "UPDATE `character_appearances` SET `face_color` = ?, `face_skin` = ?, `adorments_skin` = ?, `features_skin` = ?, `features_color` = ?, `voice` = ?, `bone_structure_brow` = ?, "
			+ "`bone_structure_cheekbones` = ?, `bone_structure_jaw` = ?, `bone_structure_jaw_jut` = ?, `ears_rotation` = ?, `ears_extension` = ?, `ears_trim` = ?, `ears_size` = ?, `eyes_width` = ?, `eyes_height` = ?, `eyes_separation` = ?, `eyes_angle` = ?,"
			+ "`eyes_inner_brow` = ?, `eyes_outer_brow` = ?, `nose_extension` = ?, `nose_size` = ?, `nose_bridge` = ?, `nose_nostril_width` = ?, `nose_tip_width` = ?, `nose_tip` = ?, `nose_nostril_flare` = ?, `mouth_pucker` = ?,"
			+ "`mouth_position` = ?, `mouth_width` = ?, `mouth_lip_thickness` = ?, `mouse_corners` = ?, `eyes_shape` = ?, `nose_bend` = ?, `bone_structure_jaw_width` = ?, `mouth_gape` = ? WHERE `object_id` = ? LIMIT 1";

	private static final String SELECT_PLAYER_APPEARANCE = "SELECT face_color, face_skin, adorments_skin, features_skin, features_color, voice, bone_structure_brow, "
			+ "bone_structure_cheekbones, bone_structure_jaw, bone_structure_jaw_jut, ears_rotation, ears_extension, ears_trim, ears_size, eyes_width, eyes_height, eyes_separation, eyes_angle,"
			+ "eyes_inner_brow, eyes_outer_brow, nose_extension, nose_size, nose_bridge, nose_nostril_width, nose_tip_width, nose_tip, nose_nostril_flare, mouth_pucker,"
			+ "mouth_position, mouth_width, mouth_lip_thickness, mouse_corners, eyes_shape, nose_bend, bone_structure_jaw_width, mouth_gape FROM `character_appearances` WHERE `object_id`= ? LIMIT 1";

	private static DataBaseManager instance;

	public static DataBaseManager getInstance() {
		if(instance == null)
			instance = new DataBaseManager(Config.DATA_BASE_CONFIG, Config.DATA_BASE_DRIVER);

		return instance;
	}

	/** фабрика подключений */
	private final ConnectFactory connectFactory;

	private DataBaseManager(BoneCPConfig config, String driver) {
		this.connectFactory = ConnectFactory.newBoneCPConnectFactory(config, driver);
	}

	/**
	 * Проверка на свободность названия гильдии.
	 * 
	 * @param name название гильдии.
	 * @return подходит ли название.
	 */
	public final boolean checkGuildName(String name) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_GUILD_NAME);
			statement.setString(1, name);

			rset = statement.executeQuery();

			return !rset.next();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return false;
	}

	/**
	 * Очистка переменных квеста.
	 * 
	 * @param state состояние квеста.
	 */
	public final void clearQuestVar(QuestState state) {
		Player player = state.getPlayer();

		if(player == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CLEAR_QUEST_VAR);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, state.getQuestId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Создает в бд новый аккаунт.
	 * 
	 * @param accountName имя аккаунта.
	 * @param password пароль аккаунта.
	 * @param address ип с которого создан аккаунт.
	 * @return новый аккаунт.
	 */
	public final Account createAccount(String accountName, String password, InetAddress address) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			Account newAccount = Account.valueOf(accountName, password, address != null ? address.getHostAddress() : "", "auto");

			statement = con.prepareStatement(INSERT_ACCOUNT);
			statement.setString(1, newAccount.getName());
			statement.setString(2, newAccount.getPassword());
			statement.setString(3, newAccount.getEmail());
			statement.setInt(4, newAccount.getAccessLevel());
			statement.setLong(5, -1L);
			statement.setLong(6, -1L);
			statement.setString(7, newAccount.getLastIP());
			statement.setString(8, newAccount.getAllowIPs());
			statement.setString(9, newAccount.getComments());
			statement.execute();

			return newAccount;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return null;
	}

	/**
	 * Создает в бд новый банк аккаунта.
	 * 
	 * @param accountName имя аккаунта.
	 * @return успешно ли создан банк.
	 */
	public final boolean createAccountBank(String accountName) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_ACCOUNT_BANK);
			statement.setString(1, accountName);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создает строку в БД с новой внешностью.
	 * 
	 * @param face внешность игрока.
	 * @return успешно ли создано поле.
	 */
	public final boolean createFace(DeprecatedPlayerFace face) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con
					.prepareStatement("INSERT INTO `character_faces` (objectId, faceColor, hairColor, eyebrowsFirstVal, eyebrowsSecondVal, eyebrowsThridVal, eyeFirstVal, eyeSecondVal, eyeThridVal, eyePosVertical, eyeWidth, eyeHeight, chin, cheekbonePos, earsFirstVal, earsSecondVal, earsThridVal, earsFourthVal, noseFirstVal, noseSecondVal, noseThridVal, noseFourthVal, noseFifthVal, lipsFirstVal, lipsSecondVal, lipsThridVal, lipsFourthVal, lipsFifthVal, lipsSixthVal, cheeks, bridgeFirstVal, bridgeSecondVal, bridgeThridVal, temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11, temp12, temp13, temp14, temp15, temp16, temp17, temp18) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, face.getObjectId());
			statement.setInt(2, face.getFaceColor());
			statement.setInt(3, face.getHairColor());
			statement.setInt(4, face.getEyebrowsFirstVal());
			statement.setInt(5, face.getEyebrowsSecondVal());
			statement.setInt(6, face.getEyebrowsThridVal());
			statement.setInt(7, face.getEyeFirstVal());
			statement.setInt(8, face.getEyeSecondVal());
			statement.setInt(9, face.getEyeThridVal());
			statement.setInt(10, face.getEyePosVertical());
			statement.setInt(11, face.getEyeWidth());
			statement.setInt(12, face.getEyeHeight());
			statement.setInt(13, face.getChin());
			statement.setInt(14, face.getCheekbonePos());
			statement.setInt(15, face.getEarsFirstVal());
			statement.setInt(16, face.getEarsSecondVal());
			statement.setInt(17, face.getEarsThridVal());
			statement.setInt(18, face.getEarsFourthVal());
			statement.setInt(19, face.getNoseFirstVal());
			statement.setInt(20, face.getNoseSecondVal());
			statement.setInt(21, face.getNoseThridVal());
			statement.setInt(22, face.getNoseFourthVal());
			statement.setInt(23, face.getNoseFifthVal());
			statement.setInt(24, face.getLipsFirstVal());
			statement.setInt(25, face.getLipsSecondVal());
			statement.setInt(26, face.getLipsThridVal());
			statement.setInt(27, face.getLipsFourthVal());
			statement.setInt(28, face.getLipsFifthVal());
			statement.setInt(29, face.getLipsSixthVal());
			statement.setInt(30, face.getCheeks());
			statement.setInt(31, face.getBridgeFirstVal());
			statement.setInt(32, face.getBridgeSecondVal());
			statement.setInt(33, face.getBridgeThridVal());
			statement.setInt(34, face.tempVals[0]);
			statement.setInt(35, face.tempVals[1]);
			statement.setInt(36, face.tempVals[2]);
			statement.setInt(37, face.tempVals[3]);
			statement.setInt(38, face.tempVals[4]);
			statement.setInt(39, face.tempVals[5]);
			statement.setInt(40, face.tempVals[6]);
			statement.setInt(41, face.tempVals[7]);
			statement.setInt(42, face.tempVals[8]);
			statement.setInt(43, face.tempVals[9]);
			statement.setInt(44, face.tempVals[10]);
			statement.setInt(45, face.tempVals[11]);
			statement.setInt(46, face.tempVals[12]);
			statement.setInt(47, face.tempVals[13]);
			statement.setInt(48, face.tempVals[14]);
			statement.setInt(49, face.tempVals[15]);
			statement.setInt(50, face.tempVals[16]);
			statement.setInt(51, face.tempVals[17]);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создание ранка для гильдии.
	 * 
	 * @param guild гильдия.
	 * @param rank ранг.
	 * @return успешно ли создан.
	 */
	public final boolean createGuildRank(Guild guild, GuildRank rank) {
		if(rank == null || guild == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CREATE_GUILD_RANK);
			statement.setInt(1, guild.getId());
			statement.setString(2, rank.getName());
			statement.setInt(3, rank.getIndex());
			statement.setInt(4, rank.getLawId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создание строки в БД нового инвенторя.
	 * 
	 * @param owner владелец инвенторя.
	 * @param inventory инвентарь.
	 * @return успешно ли создан.
	 */
	public final boolean createInventory(Character owner, Inventory inventory) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CREATE_INVENTORY);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, inventory.getLevel());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создание строки в БД нового итема.
	 * 
	 * @param item новый итем.
	 * @return успешно ли создан.
	 */
	public final boolean createItem(ItemInstance item) {
		if(item == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CREATE_ITEM);
			statement.setInt(1, item.getObjectId());
			statement.setInt(2, item.getItemId());
			statement.setLong(3, item.getItemCount());
			statement.setInt(4, 3);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создает строку в БД с новым игроком.
	 * 
	 * @param player новый игрок.
	 * @param accountName имя аккаунта игрока.
	 * @return успешно ли создан.
	 */
	public final boolean createPlayer(Player player, String accountName) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();
			statement = con
					.prepareStatement("INSERT INTO `characters` (account_name, object_id, class_id, race_id, sex, char_name, heading, online_time, create_time, end_ban, end_chat_ban, title, guild_id, access_level, level, exp, hp, mp, x, y, z, heart, attack_counter, pvp_count, pve_count, guild_rank) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, accountName);
			statement.setInt(2, player.getObjectId());
			statement.setInt(3, player.getClassId());
			statement.setInt(4, player.getRaceId());
			statement.setInt(5, player.getSexId());
			statement.setString(6, player.getName());
			statement.setInt(7, player.getHeading());
			statement.setLong(8, player.getOnlineTime());
			statement.setLong(9, player.getCreateTime());
			statement.setLong(10, 0);
			statement.setLong(11, 0);
			statement.setString(12, player.getTitle());
			statement.setInt(13, player.getGuildId());
			statement.setInt(14, player.getAccessLevel());
			statement.setByte(15, (byte) player.getLevel());
			statement.setLong(16, player.getExp());
			statement.setInt(17, player.getCurrentHp());
			statement.setInt(18, player.getCurrentMp());
			statement.setDouble(19, player.getX());
			statement.setDouble(20, player.getY());
			statement.setDouble(21, player.getZ());
			statement.setInt(22, player.getStamina());
			statement.setInt(23, 0);
			statement.setInt(24, 0);
			statement.setInt(25, 0);
			statement.setInt(26, 0);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Создание записи о начале квеста игроком.
	 * 
	 * @param state состояние квеста.
	 */
	public final void createQuest(QuestState state) {
		Player player = state.getPlayer();

		if(player == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CREATE_QUEST);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, state.getQuestId());
			statement.setInt(3, state.getState());
			statement.setLong(4, 0);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Создание строки в БД нового скила.
	 * 
	 * @param owner владелец скила.
	 * @param skill скил.
	 * @return успешно ли создан.
	 */
	public final boolean createSkill(Character owner, Skill skill) {
		if(!owner.isPlayer())
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(CREATE_SKILL);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, skill.getClassId());
			statement.setInt(3, skill.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Удаляет игрока с БД.
	 * 
	 * @param objectId ид игрока.
	 * @param accountName имя аккаунта.
	 * @return результат удаления.
	 */
	public final int deletePlayer(int objectId, String accountName) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(DELETE_PLAYER);
			statement.setInt(1, objectId);
			statement.setString(2, accountName);

			int count = statement.executeUpdate();

			return count > 0 ? PlayerDeleteResult.SUCCESSFUL : PlayerDeleteResult.FAILED;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return PlayerDeleteResult.FAILED;
	}

	/**
	 * Удаление строки в БД нового скила.
	 * 
	 * @param owner владелец скила.
	 * @param skill скил.
	 * @return успешно ли создан.
	 */
	public final boolean deleteSkill(Character owner, Skill skill) {
		if(!owner.isPlayer())
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(DELETE_SKILL);
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, skill.getClassId());
			statement.setInt(3, skill.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Удаление строки в БД ожидающего итема.
	 * 
	 * @param order индекс итема.
	 * @return успешно ли удален.
	 */
	public final boolean deleteWaitItem(int order) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();
			statement = con.prepareStatement(REMOVE_WAIT_ITEM);
			statement.setInt(1, order);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Удаление строки в БД ожидающего скила.
	 * 
	 * @param order индекс скила.
	 * @return успешно ли удален.
	 */
	public final boolean deleteWaitSkill(int order) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_WAIT_SKILL);
			statement.setInt(1, order);
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Завершение квеста.
	 * 
	 * @param player игрок.
	 * @param date дата завершения.
	 */
	public final void finishQuest(Player player, QuestDate date) {
		if(player == null || date == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(FINISH_QUEST);
			statement.setLong(1, date.getTime());
			statement.setInt(2, player.getObjectId());
			statement.setInt(3, date.getQuestId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Загружает игрока с БД.
	 * 
	 * @param objectId ид игрока.
	 * @param account аккаунт игрока.
	 * @return загруженный игрок.
	 */
	public final Player fullRestore(int objectId, Account account) {
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// ссылка на загруженного игрока
		Player player = null;

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		// получаем таблицу шаблонов игроков
		PlayerTable playerTable = PlayerTable.getInstance();

		// получаем таблицу территорий
		TerritoryTable territoryTable = TerritoryTable.getInstance();

		// получаем менеджера гильдии
		GuildManager guildManager = GuildManager.getInstance();

		try {
			// получаем конект к БД
			con = connectFactory.getConnection();

			// создаем запрос
			statement = con.createStatement();

			int hp = 0;
			int mp = 0;
			int heart = 0;

			// загрузка игрока
			{
				// запрашиваем данные об нужном игроке
				rset = statement.executeQuery("SELECT * FROM `characters` WHERE `object_id`= " + objectId + " LIMIT 1");

				if(!rset.next()) {
					LOGGER.warning("not found player for " + objectId);
					return null;
				}

				// если несходится аккаунт, выходим
				if(!rset.getString("account_name").equalsIgnoreCase(account.getName())) {
					LOGGER.warning("incorrect account for player");
					return null;
				}

				PlayerClass playerclass = PlayerClass.values()[rset.getByte("class_id")];
				Sex sex = Sex.valueOf(rset.getByte("sex"));
				Race race = Race.valueOf(rset.getByte("race_id"), sex);

				// получаем шаблон игрока
				PlayerTemplate template = playerTable.getTemplate(playerclass, race, sex);

				player = new Player(objectId, template);

				player.setName(rset.getString("char_name"));
				player.setGuildNote(rset.getString("guild_note"));
				player.setTitle(rset.getString("title"));

				player.setHeading(rset.getInt("heading"));
				player.setPvECount(rset.getInt("pve_count"));
				player.setPvPCount(rset.getInt("pvp_count"));
				player.setKarma(rset.getInt("karma"));

				player.setOnlineTime(rset.getLong("online_time"));
				player.setCreateTime(rset.getLong("create_time"));
				player.setEndBan(rset.getLong("end_ban"));
				player.setEndChatBan(rset.getLong("end_chat_ban"));

				player.setPvPMode(player.getKarma() > 0);

				Guild guild = guildManager.getGuild(rset.getInt("guild_id"));

				if(guild != null) {
					player.setGuild(guild);
					player.setGuildRank(guild.getRank(rset.getInt("guild_rank")));
				} else {
					player.setGuildId(0);
					player.setGuildRank(null);
				}

				player.setAccessLevel(rset.getInt("access_level"));
				player.setAttackCounter(rset.getByte("attack_counter"));
				player.setLevel(rset.getByte("level"));
				player.setZoneId(rset.getInt("zone_id"));
				player.setContinentId(rset.getInt("continent_id"));
				player.setExp(rset.getInt("exp"));
				player.setEnergyLevel(rset.getInt("collect_energy"));
				player.setMiningLevel(rset.getInt("collect_mining"));
				player.setPlantLevel(rset.getInt("collect_plant"));

				heart = rset.getInt("heart");
				hp = rset.getInt("hp");
				mp = rset.getInt("mp");

				player.setXYZInvisible(rset.getFloat("x"), rset.getFloat("y"), rset.getFloat("z"));

				// закрываем результат
				DBUtils.closeResultSet(rset);
			}

			// загружаем вншность игрока
			PlayerAppearance appearance = loadPlayerAppearance(objectId);

			// если внешность была загружена
			if(appearance != null)
				// устанавливаем ее
				player.setAppearance(appearance, false);
			else {
				LOGGER.warning("not found appearance for objectId " + objectId);

				// получаем расу игрока
				Race race = player.getRace();

				// получаем базову внешнсоть для него
				appearance = race.getAppearance(player.getSex()).copy();

				// устанавливаем ее
				player.setAppearance(appearance, true);
			}

			// загрузка скилов
			{
				// запрашиваем скилы игрока
				rset = statement.executeQuery("SELECT * FROM `character_skills` WHERE `object_id`= " + objectId);

				// если есть скилы
				while(rset.next()) {
					// получаем ид скила
					int id = rset.getInt("skill_id");
					// получаем класс скила
					int classId = rset.getByte("class_id");

					// получаем темплейт скила
					SkillTemplate skill = skillTable.getSkill(classId, id);

					// если его нет, пропускаем
					if(skill == null) {
						LOGGER.warning("not found skill id " + id + " class " + classId);
						continue;
					}

					// добавляем игроку скил
					player.addSkill(skill, false);
				}

				// закрываем результат по скилам
				DBUtils.closeResultSet(rset);

				// запрашиваем ожидающие скилы
				rset = statement.executeQuery("SELECT * FROM `wait_skills` WHERE `char_name` = '" + player.getName() + "';");

				// если такие есть
				while(rset.next()) {
					// получаем ид скила
					int id = rset.getInt("skill_id");
					// получаем класс скила
					int classId = rset.getInt("skill_class");

					// если этого скила у игрока еще нету
					if(player.getSkill(id) == null) {
						// получаем темплейт скила
						SkillTemplate[] skill = skillTable.getSkills(classId, id);

						// если скила такого неет, сообщаем
						if(skill == null)
							LOGGER.warning("not found skill id " + id + " class " + classId);
						else
							// иначе добавляем игроку
							player.addSkills(skill, false);
					}

					// удаляем этот скил из табилцы
					deleteWaitSkill(rset.getInt("order"));
				}

				// закрываем результат
				DBUtils.closeResultSet(rset);
			}

			Inventory inventory = null;

			{
				rset = statement.executeQuery("SELECT * FROM `character_inventors` WHERE `owner_id` = " + objectId + " LIMIT 1");

				if(rset.next()) {
					inventory = PlayerInventory.newInstance(player, rset.getInt("level"));
				} else {
					inventory = PlayerInventory.newInstance(player);
				}

				DBUtils.closeResultSet(rset);
			}

			// устанавливаем новый инвентарь игроку
			player.setInventory(inventory);

			// создаем контейнер экиперовки игрока
			Equipment equipment = PlayerEquipment.newInstance(player);

			// вставляем его игроку
			player.setEquipment(equipment);

			// создаем банк
			Bank bank = PlayerBank.newInstance(player);

			// добавляем его игроку
			player.setBank(bank);

			Array<ItemInstance> items = local.getNextItemList();

			{
				rset = statement.executeQuery("SELECT * FROM `items` WHERE `owner_id` = " + objectId + " AND `location` < " + ItemLocation.BANK.ordinal());

				loadItems(rset, items);

				DBUtils.closeResultSet(rset);

				rset = statement.executeQuery("SELECT * FROM `items` WHERE `owner_id` = " + account.getBankId() + " AND `location` = " + ItemLocation.BANK.ordinal());

				loadItems(rset, items);

				ItemInstance[] array = items.array();

				ItemTable itemTable = ItemTable.getInstance();

				for(int i = 0, length = items.size(); i < length; i++) {

					ItemInstance item = array[i];

					CrystalList crystals = item.getCrystals();

					if(crystals == null) {
						continue;
					}

					DBUtils.closeResultSet(rset);

					rset = statement.executeQuery("SELECT * FROM `items` WHERE `owner_id` = " + item.getObjectId() + " AND `location` = " + ItemLocation.CRYSTAL.ordinal() + " LIMIT "
							+ item.getSockets());

					while(rset.next()) {

						ItemTemplate template = itemTable.getItem(rset.getInt("item_id"));

						if(template == null) {
							LOGGER.warning("not found item " + rset.getInt("item_id"));
							continue;
						}

						CrystalInstance crystal = (CrystalInstance) template.newInstance(rset.getInt("object_id"));

						crystal.setOwnerId(item.getObjectId());
						crystal.setIndex(rset.getInt("index"));
						crystal.setLocation(ItemLocation.VALUES[rset.getInt("location")]);
						crystal.setItemCount(rset.getInt("item_count"));
						crystal.setEnchantLevel(rset.getShort("enchant_level"));
						crystal.setBonusId(rset.getInt("bonus_id"));
						crystal.setAutor(rset.getString("autor"));

						crystals.put(crystal, null, null);
					}
				}

				DBUtils.closeResultSet(rset);
			}

			ItemInstance[] array = items.array();

			for(int i = 0, length = items.size(); i < length; i++) {

				ItemInstance item = array[i];

				switch(item.getLocation()) {
					case INVENTORY:
						inventory.setItem(item, item.getIndex());
						break;
					case EQUIPMENT:
						equipment.setItem(item, item.getIndex());
						break;
					case BANK:
						bank.setItem(item.getIndex(), item);
						break;
					default:
						LOGGER.warning("incorrect location for item " + item);
				}
			}

			{
				rset = statement.executeQuery("SELECT `item_id`, `item_count`, `enchant_level`, `order` FROM `wait_items` WHERE `char_name` = '" + player.getName() + "';");

				ItemTable itemTable = ItemTable.getInstance();

				while(rset.next()) {

					ItemTemplate template = itemTable.getItem(rset.getInt(1));

					if(template == null) {
						continue;
					}

					ItemInstance item = template.newInstance();

					if(item == null) {
						continue;
					}

					if(item.isStackable()) {
						item.setItemCount(rset.getInt(2));
					}

					item.setEnchantLevel(rset.getInt(3));

					updateItem(item);

					if(inventory.putItem(item)) {
						deleteWaitItem(rset.getInt(4));
					}
				}

				DBUtils.closeResultSet(rset);
			}

			{
				rset = statement.executeQuery("SELECT * FROM `character_save_effects` WHERE `object_id` = " + objectId);

				SkillTemplate skill = null;

				int skillId = 0;
				int count = 0;
				int duration = 0;
				int order = 0;
				int classId = 0;

				EffectList effectList = player.getEffectList();

				while(rset.next()) {
					classId = rset.getByte("class_id");
					skillId = rset.getInt("skill_id");
					order = rset.getByte("effect_order");
					count = rset.getInt("count");
					duration = rset.getInt("duration");

					if(skill == null || skill.getId() != skillId)
						skill = skillTable.getSkill(classId, skillId);

					if(skill == null)
						continue;

					EffectTemplate[] templates = skill.getEffectTemplates();

					if(templates.length <= order || order < 0)
						continue;

					EffectTemplate template = templates[order];

					Effect effect = template.newInstance(player, player, skill);

					if(effect.getCount() == 1)
						effect.setPeriod(template.getTime() - duration);
					else
						effect.setCount(count);

					effectList.addEffect(effect);
				}

				DBUtils.closeResultSet(rset);
			}

			player.setStamina(heart);
			player.setCurrentHp(hp);
			player.setCurrentMp(mp);

			// загрузка откатов скилов
			{
				rset = statement.executeQuery("SELECT * FROM `character_skill_reuses` WHERE `object_id` = " + objectId);

				Table<IntKey, ReuseSkill> reuses = player.getReuseSkills();

				while(rset.next()) {
					ReuseSkill reuse = ReuseSkill.newInstance(rset.getInt("skill_id"), 0);
					reuse.setItemId(rset.getInt("item_id"));
					reuse.setEndTime(rset.getLong("end_time"));
					reuses.put(reuse.getSkillId(), reuse);
				}

				DBUtils.closeResultSet(rset);
			}

			// получаем менеджера квестов
			QuestManager questManager = QuestManager.getInstance();

			// загрузка квестов
			{
				// запрашиваем все квесты того игрока
				rset = statement.executeQuery("SELECT * FROM `character_quests` WHERE `object_id`= " + objectId);

				// получаем квест лист игрока
				QuestList questList = player.getQuestList();

				// перебираем
				while(rset.next()) {
					// получаем квест
					Quest quest = questManager.getQuest(rset.getInt("quest_id"));

					// если такого в таблице квестов нет, сообщаем
					if(quest == null) {
						LOGGER.warning("not found quest for id " + rset.getInt("quest_id"));
						continue;
					}

					// получаем время выполнения
					long time = rset.getLong("date");

					// если оно уже выполнено
					if(time > 0)
						// вносим как выполненный
						questList.addCompleteQuest(questList.newQuestDate(time, quest));
					else {
						// получаем стадию квеста
						int state = rset.getInt("state");

						// создаем контейнер состояния квеста
						QuestState questState = questList.newQuestState(player, quest, state);

						// подготавливаем
						questState.prepare();

						// вносим состояние на панели
						questState.setPanelState(QuestPanelState.valueOf(rset.getInt("panel_state")));

						// добавляем в активные квесты
						questList.addActiveQuest(questState);
					}
				}
			}

			// загрузка территорий
			{
				// запрашиваем все территории, в которых побывал игрок
				rset = statement.executeQuery("SELECT * FROM `character_territories` WHERE `object_id`= " + objectId);

				// перебираем их
				while(rset.next()) {
					// получаем территори.
					Territory territory = territoryTable.getTerritory(rset.getInt("territory_id"));

					// вносим в таблицу игрока
					player.storeTerritory(territory, false);
				}

				DBUtils.closeResultSet(rset);
			}

			// загрузка раскладки и настроек
			{
				rset = statement.executeQuery("SELECT * FROM `character_hotkey` WHERE `object_id`= " + objectId + " LIMIT 1");

				if(rset.next())
					player.setHotkey(rset.getBytes("data"), false);

				DBUtils.closeResultSet(rset);

				rset = statement.executeQuery("SELECT * FROM `character_settings` WHERE `object_id`= " + objectId + " LIMIT 1");

				if(rset.next())
					player.setSettings(rset.getBytes("data"), false);
			}

			// загружаем переменные игрока
			loadPlayerVars(objectId, player.getVariables());

			// загрузка друзей игрока
			loadFriends(objectId, player.getFriendList());
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return player;
	}

	/**
	 * Полное сохранение игрока.
	 * 
	 * @param player игрок, которого нужно сохранить.
	 * @return сохранился ли.
	 */
	public final boolean fullStore(Player player) {
		if(player == null || player.isDeleted())
			return false;

		Connection con = null;
		Statement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.createStatement();

			// сохранение игрока
			{
				statement.executeUpdate("UPDATE `characters` SET `heading` =  '" + player.getHeading() + "' , `x` = '" + player.getX() + "', `y` = '" + player.getY() + "', `z` = '" + player.getZ()
						+ "', " + " `online_time` = '" + player.getOnlineTime() + "', `end_ban` = '" + player.getEndBan() + "', `end_chat_ban` = '" + player.getEndChatBan() + "', `exp` = '"
						+ player.getExp() + "', " + "`hp` = " + player.getCurrentHp() + ", `mp` = " + player.getCurrentMp() + ", `heart` = " + player.getStamina() + ", " + "`attack_counter` = '"
						+ player.getAttackCounter() + "', `pvp_count` = '" + player.getPvpCount() + "', `karma` = '" + player.getKarma() + "', `pve_count` = '" + player.getPveCount()
						+ "', `collect_mining` = '" + player.getMiningLevel() + "', `collect_plant` = '" + player.getPlantLevel() + "', `collect_energy` = '" + player.getEnergyLevel()
						+ "', `last_online` = '" + (System.currentTimeMillis() / 1000) + "', `continent_id` = '" + player.getContinentId() + "' WHERE `object_id` = '" + player.getObjectId()
						+ "' LIMIT 1");
			}

			if(player.isChangedFace()) {
				// TODO
			}

			Table<IntKey, ReuseSkill> reuses = player.getReuseSkills();

			if(!reuses.isEmpty()) {
				StringBuilder message = new StringBuilder("REPLACE INTO `character_skill_reuses` VALUES ");

				int counter = 0;

				long time = System.currentTimeMillis();

				for(ReuseSkill reuse : reuses) {
					if(reuse.getEndTime() < time)
						continue;

					counter++;

					if(counter == 1)
						message.append("('");
					else
						message.append(", ('");

					message.append(player.getObjectId()).append("', '").append(reuse.getSkillId()).append("', '").append(reuse.getItemId()).append("', '").append(reuse.getEndTime()).append("')");
				}

				message.append(";");

				if(counter > 0)
					statement.executeUpdate(message.toString());
			}

			statement.execute("DELETE FROM `character_save_effects` WHERE `object_id` = " + player.getObjectId());

			EffectList effectList = player.getEffectList();

			if(effectList.size() > 0) {
				effectList.lock();
				try {
					Array<Effect> effects = effectList.getEffects();

					Effect[] array = effects.array();

					for(int i = 0, length = effects.size(); i < length; i++) {
						Effect effect = array[i];

						if(effect == null || effect.isAura() || effect.isEnded())
							continue;

						statement.executeUpdate("REPLACE INTO `character_save_effects` (object_id, class_id, skill_id, effect_order, count, duration) VALUES(" + player.getObjectId() + ", "
								+ effect.getSkillClassId() + ", " + effect.getSkillId() + ", " + effect.getOrder() + ", " + effect.getCount() + ", " + effect.getTime() + ")");
					}
				} finally {
					effectList.unlock();
				}
			}

			// сохраняем переменные в БД
			player.saveVars();

			return storeSettingsAndHotKeys(player);
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Получение кол-ва персонажей на аккаунте.
	 * 
	 * @param account имя аккаунта.
	 * @return кол-во кол-во персонажей на нем.
	 */
	public final int getAccountSize(String account) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			int number = 0;

			// получаем коннект к БД.
			con = connectFactory.getConnection();

			// задаем запрос
			statement = con.prepareStatement("SELECT COUNT(char_name) FROM `characters` WHERE `account_name`=? LIMIT 8");
			statement.setString(1, account);

			// выполняем запрос
			rset = statement.executeQuery();

			// считаем кол-во
			while(rset.next())
				number = rset.getInt(1);

			// возвращаем кол-во
			return number;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return 0;
	}

	/**
	 * @return фабрика подключений к БД.
	 */
	public final ConnectFactory getConnectFactory() {
		return connectFactory;
	}

	/**
	 * Внесение спавна босса в БД.
	 * 
	 * @param template темплейт босса.
	 * @param spawn время спавна босса.
	 */
	public final void insertBossSpawns(NpcTemplate template, long spawn) {
		if(template == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_BOSS_SPAWNS);
			statement.setInt(1, template.getTemplateId());
			statement.setInt(2, template.getTemplateType());
			statement.setLong(3, spawn);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Внесение друга в БД.
	 * 
	 * @param objectId ид игрока.
	 * @param friendId ид друга.
	 */
	public final void insertFriend(int objectId, int friendId) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_PLAYER_FRIEND);
			statement.setInt(1, objectId);
			statement.setInt(2, friendId);
			statement.setString(3, Strings.EMPTY);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Добавление гильдии в БД.
	 * 
	 * @param guild гильдия.
	 */
	public final boolean insertGuild(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_GUILD);
			statement.setInt(1, guild.getObjectId());
			statement.setString(2, guild.getName());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Добавление игрока переменной.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @param name название переменной.
	 * @param value значение переменной.
	 */
	public final void insertPlayerVar(int objectId, String name, String value) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_PLAYER_VARIABLE);
			statement.setInt(1, objectId);
			statement.setString(2, name);
			statement.setString(3, value);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Внесение в БД записи с внешностью персонажа.
	 * 
	 * @param appearance внешность игрока.
	 */
	public final void insertPlayerAppearance(PlayerAppearance appearance) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_PLAYER_APPEARANCE);
			statement.setInt(1, appearance.getObjectId());
			statement.setInt(2, appearance.getFaceColor());
			statement.setInt(3, appearance.getFaceSkin());
			statement.setInt(4, appearance.getAdormentsSkin());
			statement.setInt(5, appearance.getFeaturesSkin());
			statement.setInt(6, appearance.getFeaturesColor());
			statement.setInt(7, appearance.getVoice());
			statement.setInt(8, appearance.getBoneStructureBrow());
			statement.setInt(9, appearance.getBoneStructureCheekbones());
			statement.setInt(10, appearance.getBoneStructureJaw());
			statement.setInt(11, appearance.getBoneStructureJawJut());
			statement.setInt(12, appearance.getEarsRotation());
			statement.setInt(13, appearance.getEarsExtension());
			statement.setInt(14, appearance.getEarsTrim());
			statement.setInt(15, appearance.getEarsSize());
			statement.setInt(16, appearance.getEyesWidth());
			statement.setInt(17, appearance.getEyesHeight());
			statement.setInt(18, appearance.getEyesSeparation());
			statement.setInt(19, appearance.getEyesAngle());
			statement.setInt(20, appearance.getEyesInnerBrow());
			statement.setInt(21, appearance.getEyesOuterBrow());
			statement.setInt(22, appearance.getNoseExtension());
			statement.setInt(23, appearance.getNoseSize());
			statement.setInt(24, appearance.getNoseBridge());
			statement.setInt(25, appearance.getNoseNostrilWidth());
			statement.setInt(26, appearance.getNoseTipWidth());
			statement.setInt(27, appearance.getNoseTip());
			statement.setInt(28, appearance.getNoseNostrilFlare());
			statement.setInt(29, appearance.getMouthPucker());
			statement.setInt(30, appearance.getMouthPosition());
			statement.setInt(31, appearance.getMouthWidth());
			statement.setInt(32, appearance.getMouthLipThickness());
			statement.setInt(33, appearance.getMouthCorners());
			statement.setInt(34, appearance.getEyesShape());
			statement.setInt(35, appearance.getNoseBend());
			statement.setInt(36, appearance.getBoneStructureJawWidth());
			statement.setInt(37, appearance.getMothGape());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление в БД записи с внешностью персонажа.
	 * 
	 * @param appearance внешность игрока.
	 */
	public final boolean updatePlayerAppearance(PlayerAppearance appearance) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_APPEARANCE);
			statement.setInt(1, appearance.getFaceColor());
			statement.setInt(2, appearance.getFaceSkin());
			statement.setInt(3, appearance.getAdormentsSkin());
			statement.setInt(4, appearance.getFeaturesSkin());
			statement.setInt(5, appearance.getFeaturesColor());
			statement.setInt(6, appearance.getVoice());
			statement.setInt(7, appearance.getBoneStructureBrow());
			statement.setInt(8, appearance.getBoneStructureCheekbones());
			statement.setInt(9, appearance.getBoneStructureJaw());
			statement.setInt(10, appearance.getBoneStructureJawJut());
			statement.setInt(11, appearance.getEarsRotation());
			statement.setInt(12, appearance.getEarsExtension());
			statement.setInt(13, appearance.getEarsTrim());
			statement.setInt(14, appearance.getEarsSize());
			statement.setInt(15, appearance.getEyesWidth());
			statement.setInt(16, appearance.getEyesHeight());
			statement.setInt(17, appearance.getEyesSeparation());
			statement.setInt(18, appearance.getEyesAngle());
			statement.setInt(19, appearance.getEyesInnerBrow());
			statement.setInt(20, appearance.getEyesOuterBrow());
			statement.setInt(21, appearance.getNoseExtension());
			statement.setInt(22, appearance.getNoseSize());
			statement.setInt(23, appearance.getNoseBridge());
			statement.setInt(24, appearance.getNoseNostrilWidth());
			statement.setInt(25, appearance.getNoseTipWidth());
			statement.setInt(26, appearance.getNoseTip());
			statement.setInt(27, appearance.getNoseNostrilFlare());
			statement.setInt(28, appearance.getMouthPucker());
			statement.setInt(29, appearance.getMouthPosition());
			statement.setInt(30, appearance.getMouthWidth());
			statement.setInt(31, appearance.getMouthLipThickness());
			statement.setInt(32, appearance.getMouthCorners());
			statement.setInt(33, appearance.getEyesShape());
			statement.setInt(34, appearance.getNoseBend());
			statement.setInt(35, appearance.getBoneStructureJawWidth());
			statement.setInt(36, appearance.getMothGape());
			statement.setInt(37, appearance.getObjectId());

			return statement.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Загрузка внешности игрока из БД.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @return внешность игрока.
	 */
	public final PlayerAppearance loadPlayerAppearance(int objectId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_APPEARANCE);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			if(rset.next()) {
				// создаем контейнер внешности
				PlayerAppearance appearance = PlayerAppearance.getInstance(objectId);

				appearance.setFaceColor(rset.getInt(1));
				appearance.setFaceSkin(rset.getInt(2));
				appearance.setAdormentsSkin(rset.getInt(3));
				appearance.setFeaturesSkin(rset.getInt(4));
				appearance.setFeaturesColor(rset.getInt(5));
				appearance.setVoice(rset.getInt(6));
				appearance.setBoneStructureBrow(rset.getInt(7));
				appearance.setBoneStructureCheekbones(rset.getInt(8));
				appearance.setBoneStructureJaw(rset.getInt(9));
				appearance.setBoneStructureJawJut(rset.getInt(10));
				appearance.setEarsRotation(rset.getInt(11));
				appearance.setEarsExtension(rset.getInt(12));
				appearance.setEarsTrim(rset.getInt(13));
				appearance.setEarsSize(rset.getInt(14));
				appearance.setEyesWidth(rset.getInt(15));
				appearance.setEyesHeight(rset.getInt(16));
				appearance.setEyesSeparation(rset.getInt(17));
				appearance.setEyesAngle(rset.getInt(18));
				appearance.setEyesInnerBrow(rset.getInt(19));
				appearance.setEyesOuterBrow(rset.getInt(20));
				appearance.setNoseExtension(rset.getInt(21));
				appearance.setNoseSize(rset.getInt(22));
				appearance.setNoseBridge(rset.getInt(23));
				appearance.setNoseNostrilWidth(rset.getInt(24));
				appearance.setNoseTipWidth(rset.getInt(25));
				appearance.setNoseTip(rset.getInt(26));
				appearance.setNoseNostrilFlare(rset.getInt(27));
				appearance.setMouthPucker(rset.getInt(28));
				appearance.setMouthPosition(rset.getInt(29));
				appearance.setMouthWidth(rset.getInt(30));
				appearance.setMouthLipThickness(rset.getInt(31));
				appearance.setMouthCorners(rset.getInt(32));
				appearance.setEyesShape(rset.getInt(33));
				appearance.setNoseBend(rset.getInt(34));
				appearance.setBoneStructureJawWidth(rset.getInt(35));
				appearance.setMothGape(rset.getInt(36));

				return appearance;
			}

			LOGGER.warning("not found appearance for " + objectId);
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return null;
	}

	/**
	 * Внесение региона в БД.
	 * 
	 * @param region новый регион.
	 */
	public final void insertRegion(Region region) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_REGION_STATUS);
			statement.setInt(1, region.getId());
			statement.setInt(2, 0);
			statement.setInt(3, 0);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Внесение зарегестрированной гильдии на осаду региона в БД.
	 * 
	 * @param region регион.
	 * @param guild зарегестрированная гильдия.
	 */
	public final boolean insertRegionGuildRegister(Region region, Guild guild) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_REGION_REGISTER_GUILD);
			statement.setInt(1, region.getId());
			statement.setInt(2, guild.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Добавление серверной переменной.
	 * 
	 * @param name название переменной.
	 * @param value значение переменной.
	 */
	public final void insertServerVar(String name, String value) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(INSERT_SERVER_VARIABLE);
			statement.setString(1, name);
			statement.setString(2, value);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Проверка на занятость указанного ника.
	 * 
	 * @param name проверяемый ник.
	 * @return свободен ли.
	 */
	public final boolean isFreeName(String name) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_CHECK_NAME);
			statement.setString(1, name);

			rset = statement.executeQuery();

			return !rset.next();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return false;
	}

	/**
	 * Проверка на занятость ид игрока.
	 * 
	 * @param objectId проверяемый ид.
	 * @return свободен ли.
	 */
	public final boolean isFreePlayerId(int objectId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_CHECK_ID);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			return !rset.next();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return false;
	}

	/**
	 * Загрузка спавнов боссов с БД.
	 * 
	 * @param spawns таблица спавнов.
	 */
	public final void loadBossSpawns(Table<IntKey, Table<IntKey, Wrap>> spawns) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_BOSS_SPAWNS);
			rset = statement.executeQuery();

			while(rset.next()) {
				int id = rset.getInt("npc_id");
				int type = rset.getInt("npc_type");

				Table<IntKey, Wrap> table = spawns.get(id);

				if(table == null) {
					table = Tables.newIntegerTable();

					spawns.put(id, table);
				}

				table.put(type, Wraps.newLongWrap(rset.getLong("spawn"), true));
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка друга игрока с БД.
	 * 
	 * @param objectId уникальный ид дрга.
	 * @param friendList список друзей игрока.
	 */
	public final void loadFriend(int objectId, FriendList friendList) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_FRIEND);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			if(rset.next()) {
				FriendInfo info = friendList.newFriendInfo();

				info.setObjectId(objectId);

				info.setClassId(rset.getInt(1));
				info.setRaceId(rset.getInt(2));
				info.setLevel(rset.getInt(3));
				info.setName(rset.getString(4));

				friendList.addFriend(info);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка друзей игрока с БД.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @param friendList список друзей игрока.
	 */
	public final void loadFriends(int objectId, FriendList friendList) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_FRIENDS);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			while(rset.next())
				loadFriend(rset.getInt(1), friendList);

			friendList.prepare();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Метод получение из результата списка итемов.
	 */
	public final void loadItems(ResultSet rset, Array<ItemInstance> items) {
		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		try {
			while(rset.next()) {
				// получаем if,kjy итема
				ItemTemplate template = itemTable.getItem(rset.getInt("item_id"));

				// если его нет, пропускаем
				if(template == null) {
					LOGGER.warning("not found item " + rset.getInt("item_id"));
					continue;
				}

				// создаем новый инстанс с указанным ид
				ItemInstance item = template.newInstance(rset.getInt("object_id"));

				// вносим данные об итеме
				item.setIndex(rset.getInt("index"));
				item.setLocation(ItemLocation.valueOf(rset.getInt("location")));
				item.setOwnerId(rset.getInt("owner_id"));
				item.setItemCount(rset.getLong("item_count"));
				item.setEnchantLevel(rset.getShort("enchant_level"));
				item.setBonusId(rset.getInt("bonus_id"));
				item.setAutor(rset.getString("autor"));
				item.setOwnerName(rset.getString("owner_name"));

				if(!item.isEnchantable() && item.getEnchantLevel() > 0) {
					item.setEnchantLevel(0);
					updateItem(item);
				}

				// добавляем в список
				items.add(item);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		}
	}

	/**
	 * Загрузка игрока переменных с БД.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @param table контейнер переменных.
	 */
	public final void loadPlayerVars(int objectId, Table<String, Wrap> table) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_VARIABLES);
			statement.setInt(1, objectId);
			rset = statement.executeQuery();

			while(rset.next())
				table.put(rset.getString("var_name"), Wraps.newIntegerWrap(Integer.parseInt(rset.getString("var_value")), true));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка региона с БД.
	 * 
	 * @param region загружаемый регион.
	 */
	public final void loadRegion(Region region) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		// получаем менеджера гильдий
		GuildManager guildManager = GuildManager.getInstance();

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_REGION_STATUS);
			statement.setInt(1, region.getId());

			rset = statement.executeQuery();

			if(!rset.next())
				insertRegion(region);
			else {
				// вносим состояние региона
				region.setState(RegionState.valueOf(rset.getInt("state")));

				// получаем ид владельца
				int ownerId = rset.getInt("owner_id");

				// если есть
				if(ownerId > 0)
					// вносим владельца
					region.setOwner(guildManager.getGuild(ownerId));
			}

		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка зарегестрированных гильдий на регион с БД.
	 * 
	 * @param region загружаемый регион.
	 */
	public final void loadRegionRegister(Region region) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		// получаем менеджера гильдий
		GuildManager guildManager = GuildManager.getInstance();

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_REGION_REGISTER);
			statement.setInt(1, region.getId());

			rset = statement.executeQuery();

			while(rset.next()) {
				Guild guild = guildManager.getGuild(rset.getInt("guild_id"));

				if(guild != null)
					region.addRegisterGuild(guild);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка серверных переменных с БД.
	 * 
	 * @param variables контейнер переменных.
	 */
	public final void loadServerVars(Table<String, String> variables) {
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_SERVER_VARIABLES);
			rset = statement.executeQuery();

			while(rset.next())
				variables.put(rset.getString("var_name"), rset.getString("var_value"));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Удаление друга игрока из БД.
	 * 
	 * @param objectId ид игрока.
	 * @param friendId ид друга.
	 */
	public final void removeFriend(int objectId, int friendId) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOME_PLAYER_FRIEND);
			statement.setInt(1, objectId);
			statement.setInt(2, friendId);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаление зарегестрированной гильдии из БД.
	 * 
	 * @param region регион.
	 * @param guild отрегистрируемая гильдия.
	 */
	public final void removeRegionRegisterGuild(Region region, Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_REGION_REGISTER_GUILD);
			statement.setInt(1, region.getId());
			statement.setInt(2, guild.getId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаление гильдии из БД.
	 * 
	 * @param guild гильдия.
	 */
	public final void removeGuild(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_GUILD);
			statement.setInt(1, guild.getObjectId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаление всех членов гильдии из БД.
	 * 
	 * @param guild гильдия.
	 */
	public final void removeGuildMembers(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_GUILD_MEMBERS);
			statement.setInt(1, guild.getObjectId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление строки в БД гильдии игрока.
	 * 
	 * @param member обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean removeGuildRank(Guild guild, GuildRank rank) {
		if(guild == null || rank == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_GUILD_RANK);
			statement.setInt(1, guild.getId());
			statement.setInt(2, rank.getIndex());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД гильдии игрока.
	 * 
	 * @param member обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean removeGuildRankForPlayer(Guild guild, GuildRank def, GuildRank rank) {
		if(guild == null || rank == null || def == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_GUILD_RANK_FOR_PLAYERS);
			statement.setInt(1, def.getIndex());
			statement.setInt(2, guild.getId());
			statement.setInt(3, rank.getIndex());
			statement.setInt(4, guild.size());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Удаление игрока переменной.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @param name название переменной.
	 */
	public final void removePlayerVar(int objectId, String name) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_PLAYER_VARIABLE);
			statement.setInt(1, objectId);
			statement.setString(2, name);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * удаление квеста.
	 * 
	 * @param player игрок.
	 * @param quest квест.
	 */
	public final void removeQuest(Player player, Quest quest) {
		if(player == null || quest == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_QUEST);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, quest.getId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаление серверной переменной.
	 * 
	 * @param name название переменной.
	 */
	public final void removeServerVar(String name) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(REMOVE_SERVER_VARIABLE);
			statement.setString(1, name);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Загружает аккаунт с БД.
	 * 
	 * @param accountName имя аккаунта.
	 * @return загруженный аккаунт.
	 */
	public final Account restoreAccount(String accountName) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		Account account = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_ACCOUNT);
			statement.setString(1, accountName);

			rset = statement.executeQuery();

			if(rset.next())
				account = Account.valueOf(accountName, rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4), rset.getString(5), rset.getLong(6), rset.getLong(7), rset.getInt(8));
		} catch(Exception e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return account;
	}

	/**
	 * Получает ид банка закрепленного за аккаунтом.
	 * 
	 * @param accountName имя аккаунта.
	 * @return ид банка.
	 */
	public final int restoreAccountBank(String accountName) {
		PreparedStatement statement = null;
		ResultSet rset = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(RESTORE_ACCOUNT_BANK);
			statement.setString(1, accountName);

			rset = statement.executeQuery();

			if(rset.next())
				return rset.getInt("bank_id");
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return -1;
	}

	/**
	 * Загружает с БД внешность игрока.
	 * 
	 * @param objectId уникальных ид игрока.
	 * @return внешность игрока.
	 */
	public final DeprecatedPlayerFace restoreFace(int objectId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_FACE);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			if(!rset.next()) {
				LOGGER.warning("not found player face for " + objectId);
				return null;
			}

			DeprecatedPlayerFace face = DeprecatedPlayerFace.newInstance(objectId);

			face.setFaceColor(rset.getInt("faceColor"));
			face.setHairColor(rset.getInt("hairColor"));
			face.setEyebrowsFirstVal(rset.getInt("eyebrowsFirstVal"));
			face.setEyebrowsSecondVal(rset.getInt("eyebrowsSecondVal"));
			face.setEyebrowsThridVal(rset.getInt("eyebrowsThridVal"));
			face.setEyeFirstVal(rset.getInt("eyeFirstVal"));
			face.setEyeSecondVal(rset.getInt("eyeSecondVal"));
			face.setEyeThridVal(rset.getInt("eyeThridVal"));
			face.setEyePosVertical(rset.getInt("eyePosVertical"));
			face.setEyeWidth(rset.getInt("eyeWidth"));
			face.setEyeHeight(rset.getInt("eyeHeight"));
			face.setChin(rset.getInt("chin"));
			face.setCheekbonePos(rset.getInt("cheekbonePos"));
			face.setEarsFirstVal(rset.getInt("earsFirstVal"));
			face.setEarsSecondVal(rset.getInt("earsSecondVal"));
			face.setEarsThridVal(rset.getInt("earsThridVal"));
			face.setEarsFourthVal(rset.getInt("earsFourthVal"));
			face.setNoseFirstVal(rset.getInt("noseFirstVal"));
			face.setNoseSecondVal(rset.getInt("noseSecondVal"));
			face.setNoseThridVal(rset.getInt("noseThridVal"));
			face.setNoseFourthVal(rset.getInt("noseFourthVal"));
			face.setNoseFifthVal(rset.getInt("noseFifthVal"));
			face.setLipsFirstVal(rset.getInt("lipsFirstVal"));
			face.setLipsSecondVal(rset.getInt("lipsSecondVal"));
			face.setLipsThridVal(rset.getInt("lipsThridVal"));
			face.setLipsFourthVal(rset.getInt("lipsFourthVal"));
			face.setLipsFifthVal(rset.getInt("lipsFifthVal"));
			face.setLipsSixthVal(rset.getInt("lipsSixthVal"));
			face.setCheeks(rset.getInt("cheeks"));
			face.setBridgeFirstVal(rset.getInt("bridgeFirstVal"));
			face.setBridgeSecondVal(rset.getInt("bridgeSecondVal"));
			face.setBridgeThridVal(rset.getInt("bridgeThridVal"));

			for(int i = 0; i < face.tempVals.length; i++)
				face.tempVals[i] = rset.getInt("temp" + (i + 1));

			return face;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return null;
	}

	/**
	 * Загрузка всех итемов банка гильдии из БД.
	 * 
	 * @param guild гильдия.
	 */
	public final void restoreGuildBankItems(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_GUILD_BANK_ITEMS);
			statement.setInt(1, guild.getObjectId());

			// делаем выборку по всем гильдиям
			rset = statement.executeQuery();

			// создаем буферный список итемов
			Array<ItemInstance> items = Arrays.toArray(ItemInstance.class);

			// загружаем итемы
			loadItems(rset, items);

			// если итемов нет, пропускаем
			if(items.isEmpty())
				return;

			// получаем банк итемов
			Bank bank = guild.getBank();

			// вносим итемы в банк
			for(ItemInstance item : items)
				bank.setItem(item.getIndex(), item);
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка всех членов гильдии из БД.
	 * 
	 * @param guild гильдия.
	 */
	public final void restoreGuildMembers(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_GUILD_MEMBERS);
			statement.setInt(1, guild.getObjectId());

			// делаем выборку по всем гильдиям
			rset = statement.executeQuery();

			// загружаем мемберов
			while(rset.next()) {
				GuildMember member = GuildMember.newInstance();

				member.setClassId(rset.getInt(1));
				member.setLevel(rset.getInt(2));
				member.setName(rset.getString(3));
				member.setNote(rset.getString(4));
				member.setZoneId(rset.getInt(5));
				member.setObjectId(rset.getInt(6));
				member.setOnline(false);
				member.setRaceId(rset.getInt(7));
				member.setRank(guild.getRank(rset.getInt(8)));
				member.setSex(rset.getInt(9));
				member.setLastOnline(rset.getInt(10));

				guild.addMember(member);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка всех рангов гильдии из БД.
	 * 
	 * @param guild гильдия.
	 */
	public final void restoreGuildRanks(Guild guild) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_GUILD_RANKS);
			statement.setInt(1, guild.getObjectId());

			// делаем выборку по всем гильдиям
			rset = statement.executeQuery();

			// загружаем ранги
			while(rset.next())
				guild.addRank(GuildRank.newInstance(rset.getString("rank_name"), GuildRankLaw.valueOf(rset.getInt("law")), rset.getInt("order")));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка всех гильдий из БД.
	 * 
	 * @param guilds таблица гильдий.
	 */
	public final void restoreGuilds(Table<IntKey, Guild> guilds) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_GUILDS);

			// делаем выборку по всем гильдиям
			rset = statement.executeQuery();

			// загружаем гильдии
			while(rset.next())
				guilds.put(
						rset.getInt("id"),
						new Guild(rset.getString("name"), rset.getString("title"), rset.getString("message"), rset.getInt("id"), rset.getInt("level"), new GuildIcon(rset.getString("icon_name"), rset
								.getBytes("icon"))));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка списка персоажей на указанном аккаунте.
	 * 
	 * @param playerList список персонажей.
	 * @param accountName имя аккаунта.
	 */
	public final void restorePlayerList(Array<PlayerPreview> playerList, String accountName) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_LIST);
			statement.setString(1, accountName);

			rset = statement.executeQuery();

			while(rset.next()) {
				// получаем уникальный ид игрока
				int objectId = rset.getInt("object_id");

				// загружаем его превью
				PlayerPreview playerPreview = restorePreview(objectId);

				// если загрузка не удалась, пропускаем
				if(playerPreview == null)
					continue;

				// добавляем превью в список
				playerList.add(playerPreview);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загрузка списка имен персоажей на указанном аккаунте.
	 * 
	 * @param playerNames список имен персонажей.
	 * @param accountName имя аккаунта.
	 */
	public final void restorePlayerNames(Array<String> playerNames, String accountName) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_ACCOUNT);
			statement.setString(1, accountName);

			rset = statement.executeQuery();

			while(rset.next())
				playerNames.add(rset.getString(1));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загружает с БД превью игрока.
	 * 
	 * @param objectId уникальных ид игрока.
	 * @return превью игрока.
	 */
	public final PlayerPreview restorePreview(int objectId) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_PREVIEW);
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			// если не нашли персонажа
			if(!rset.next()) {
				LOGGER.warning("not found player for " + objectId);
				return null;
			}

			PlayerPreview playerPreview = PlayerPreview.newInstance(objectId);

			playerPreview.setSex(rset.getByte("sex"));
			playerPreview.setRaceId(rset.getByte("race_id"));
			playerPreview.setClassId(rset.getByte("class_id"));
			playerPreview.setLevel(rset.getByte("level"));
			playerPreview.setOnlineTime(rset.getLong("online_time"));
			playerPreview.setName(rset.getString("char_name"));

			// загружаем внешность игрока
			PlayerAppearance appearance = loadPlayerAppearance(objectId);

			// если загрузить внешность не удалось, выходим
			if(appearance == null)
				return null;

			Equipment equipment = PlayerEquipment.newInstance(null);

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			// загрузка экиперовки
			{
				rset = statement.executeQuery("SELECT * FROM `items` WHERE `owner_id` = " + objectId + " AND `location` = " + ItemLocation.EQUIPMENT.ordinal());

				while(rset.next()) {
					ItemTemplate template = itemTable.getItem(rset.getInt("item_id"));

					if(template == null)
						continue;

					ItemInstance item = template.newInstance(rset.getInt("object_id"));

					item.setIndex(rset.getInt("index"));
					item.setLocation(ItemLocation.VALUES[rset.getInt("location")]);
					item.setOwnerId(objectId);
					item.setItemCount(rset.getLong("item_count"));
					item.setEnchantLevel(rset.getShort("enchant_level"));
					item.setBonusId(rset.getInt("bonus_id"));
					item.setAutor(rset.getString("autor"));

					equipment.setItem(item, item.getIndex());
				}
			}

			playerPreview.setAppearance(appearance).setEquipment(equipment);

			return playerPreview;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return null;
	}

	/**
	 * Загрузка переменных квеста.
	 * 
	 * @param state состояние квеста.
	 */
	public final void restoreQuestVar(QuestState state) {
		Player player = state.getPlayer();

		if(player == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(RESTORE_QUEST_VAR);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, state.getQuestId());

			rset = statement.executeQuery();

			while(rset.next())
				state.setVar(rset.getString("name"), Wraps.newIntegerWrap(rset.getInt("value"), true));
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Загружает с БД откаты скилов игрока.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @return reuses таблица откатов скилов.
	 */
	public final Table<IntKey, ReuseSkill> restoreReuseSkills(int objectId) {
		Table<IntKey, ReuseSkill> reuses = Tables.newConcurrentIntegerTable();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement("SELECT * FROM `character_skill_reuses` WHERE `object_id` = ? ");
			statement.setInt(1, objectId);

			rset = statement.executeQuery();

			while(rset.next()) {
				ReuseSkill reuse = ReuseSkill.newInstance(rset.getInt("skill_id"), 0);
				reuse.setItemId(rset.getInt("item_id"));
				reuse.setEndTime(rset.getLong("end_time"));
				reuses.put(reuse.getSkillId(), reuse);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return reuses;
	}

	/**
	 * Загружает скилы игрока с БД.
	 * 
	 * @param player игрок, для которого загружаются скилы.
	 */
	public final void restoreSkills(Player player) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement("SELECT * FROM `character_skills` WHERE `object_id`= ? ");
			statement.setInt(1, player.getObjectId());

			rset = statement.executeQuery();

			SkillTable skillTable = SkillTable.getInstance();

			while(rset.next()) {
				int id = rset.getInt("skill_id");
				byte classId = rset.getByte("class_id");

				SkillTemplate skill = skillTable.getSkill(classId, id);

				if(skill == null)
					continue;

				player.addSkill(skill, false);
			}
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Сохраняет настройки клиента и горячих клавишь игрока.
	 * 
	 * @param player игрок.
	 */
	public final boolean storeSettingsAndHotKeys(Player player) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			byte[] bytes = player.getHotkey();

			if(player.isChangeHotkey() && bytes != null) {
				statement = con.prepareStatement("REPLACE INTO `character_hotkey` (object_id, data) VALUES (?,?)");

				statement.setInt(1, player.getObjectId());
				statement.setBytes(2, bytes);

				statement.executeUpdate();

				DBUtils.closeStatement(statement);
			}

			bytes = player.getSettings();

			if(player.isChangedSettings() && bytes != null) {
				statement = con.prepareStatement("REPLACE INTO `character_settings` (object_id, data) VALUES (?,?)");

				statement.setInt(1, player.getObjectId());
				statement.setBytes(2, bytes);

				statement.executeUpdate();

				DBUtils.closeStatement(statement);
			}

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeConnection(con);
		}

		return false;
	}

	/**
	 * Создание строки в БД пройденной территории.
	 * 
	 * @param player игрок который побывал в территории.
	 * @param territory территория, в которой побывал игрок.
	 * @return успешно ли создан.
	 */
	public final boolean storeTerritory(Player player, Territory territory) {
		if(territory == null || player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(ADD_STORE_TERRITORY);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, territory.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Сохранение значения переменной квеста.
	 * 
	 * @param state состояние квеста.
	 * @param name название переменной.
	 * @param wrap значение переменной.
	 */
	public final void storyQuestVar(QuestState state, String name, Wrap wrap) {
		Player player = state.getPlayer();

		if(player == null || wrap == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(STORE_QUEST_VAR);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, state.getQuestId());
			statement.setString(3, name);
			statement.setInt(4, wrap.getInt());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление аккаунта
	 * 
	 * @param account
	 * @param address
	 */
	public final void updateAccount(Account account, InetAddress address) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement("UPDATE `accounts` SET `end_block`=?, `last_ip`=? WHERE `login`=?");
			statement.setLong(1, -1L);
			statement.setString(2, address != null ? address.getHostAddress() : "");
			statement.setString(3, account.getName());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление спавна босса в БД.
	 * 
	 * @param template темплейт босса.
	 * @param spawn время спавна босса.
	 */
	public final void updateBossSpawns(NpcTemplate template, long spawn) {
		if(template == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_BOSS_SPAWNS);
			statement.setLong(1, spawn);
			statement.setInt(2, template.getTemplateType());
			statement.setInt(3, template.getTemplateType());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление строки в БД итема.
	 * 
	 * @param item итем.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateDataItem(ItemInstance item) {
		if(item == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_DATA_ITEM);
			statement.setLong(1, item.getItemCount());
			statement.setInt(2, item.hasCrystals() ? 1 : 0);
			statement.setString(3, item.getAutor());
			statement.setInt(4, item.getBonusId());
			statement.setInt(5, item.getEnchantLevel());
			statement.setString(6, item.getOwnerName());
			statement.setInt(7, item.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление аккаунта
	 * 
	 * @param account
	 * @param address
	 */
	public final void updateFullAccount(Account account) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement("UPDATE `accounts` SET `email` = ?, `access_level` = ?, `end_pay` = ?, `end_block`= ?, `last_ip`= ?, `allow_ips` = ?, `comments` = ? WHERE `login`=?");
			statement.setString(1, account.getEmail());
			statement.setInt(2, account.getAccessLevel());
			statement.setLong(3, account.getEndPay());
			statement.setLong(4, account.getEndBlock());
			statement.setString(5, account.getLastIP());
			statement.setString(6, account.getAllowIPs());
			statement.setString(7, account.getComments());
			statement.setString(8, account.getName());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обоновление иконки гильдии.
	 * 
	 * @param guild гильдия.
	 * @return успешно ли сообновлена.
	 */
	public final boolean updateGuildIcon(Guild guild) {
		if(guild == null)
			return false;

		GuildIcon icon = guild.getIcon();

		if(icon == null || !icon.hasIcon())
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_GUILD_ICON);
			statement.setBytes(1, icon.getIcon());
			statement.setString(2, icon.getName());
			statement.setInt(3, guild.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление сообщения для гильдии.
	 * 
	 * @param guild гильдия.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateGuildMessage(Guild guild) {
		if(guild == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_GUILD_MESSAGE);
			statement.setString(1, guild.getMessage());
			statement.setInt(2, guild.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление ранга для гильдии.
	 * 
	 * @param guild гильдия.
	 * @param rank ранг.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateGuildRank(Guild guild, GuildRank rank) {
		if(rank == null || guild == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_GUILD_RANK);
			statement.setString(1, rank.getName());
			statement.setInt(2, rank.getLawId());
			statement.setInt(3, rank.getIndex());
			statement.setInt(4, guild.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление титула для гильдии.
	 * 
	 * @param guild гильдия.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateGuildTitle(Guild guild) {
		if(guild == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_GUILD_TITLE);
			statement.setString(1, guild.getTitle());
			statement.setInt(2, guild.getId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД инвенторя.
	 * 
	 * @param owner владелец инвенторя.
	 * @param inventory инвентарь.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateInventory(Character owner, Inventory inventory) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_INVENTORY);
			statement.setLong(1, inventory.getLevel());
			statement.setInt(2, owner.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД итема.
	 * 
	 * @param item итем.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateItem(ItemInstance item) {
		if(item == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_ITEM);
			statement.setLong(1, item.getItemCount());
			statement.setInt(2, item.getOwnerId());
			statement.setInt(3, item.getLocationId());
			statement.setInt(4, item.getIndex());
			statement.setInt(5, item.hasCrystals() ? 1 : 0);
			statement.setString(6, item.getAutor());
			statement.setInt(7, item.getBonusId());
			statement.setInt(8, item.getEnchantLevel());
			statement.setString(9, item.getOwnerName());
			statement.setInt(10, item.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД местоположения итема.
	 * 
	 * @param item итем.
	 * @return успешно ли обновлен.
	 */
	public final boolean updateLocationItem(ItemInstance item) {
		if(item == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_LOCATION_ITEM);
			statement.setInt(1, item.getOwnerId());
			statement.setInt(2, item.getLocationId());
			statement.setInt(3, item.getIndex());
			statement.setInt(4, item.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД континента игрока.
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerContinentId(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_CONTINENT_ID);
			statement.setInt(1, player.getContinentId());
			statement.setInt(2, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД гильдии игрока.
	 * 
	 * @param member обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerGuild(Guild guild, GuildMember member) {
		if(member == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_GUILD);
			statement.setInt(1, guild != null ? guild.getId() : 0);
			statement.setInt(2, member.getRankId());
			statement.setInt(3, member.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление строки в БД гильдии игрока.
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerGuild(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_GUILD);
			statement.setInt(1, player.getGuildId());
			statement.setInt(2, player.getGuildRankId());
			statement.setInt(3, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление гилд пометки игрок в БД
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerGuildNote(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_GUILD_NOTE);
			statement.setString(1, player.getGuildNote());
			statement.setInt(2, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление состояние региона в БД.
	 * 
	 * @param region обновляемый регион.
	 */
	public final void updateRegionState(Region region) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_REGION_STATE);
			statement.setInt(1, region.getState().ordinal());
			statement.setInt(2, region.getId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление владельца региона в БД.
	 * 
	 * @param region обновляемый регион.
	 */
	public final void updateRegionOwner(Region region) {
		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_REGION_OWNER);
			statement.setInt(1, region.getOwnerId());
			statement.setInt(2, region.getId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление строки в БД уровня игрока.
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerLevel(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_LEVEL);
			statement.setInt(1, player.getLevel());
			statement.setInt(2, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление титула игрок в БД
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerTitle(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_TITLE);
			statement.setString(1, player.getTitle());
			statement.setInt(2, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление игрока переменной.
	 * 
	 * @param objectId уникальный ид игрока.
	 * @param name название переменной.
	 * @param value значение переменной.
	 */
	public final void updatePlayerVar(int objectId, String name, String value) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_VARIABLE);
			statement.setString(1, value);
			statement.setInt(2, objectId);
			statement.setString(3, name);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление строки в БД зоны игрока.
	 * 
	 * @param player обновляемый игрок.
	 * @return успешно ли обновлен.
	 */
	public final boolean updatePlayerZoneId(Player player) {
		if(player == null)
			return false;

		Connection con = null;
		PreparedStatement statement = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_ZONE_ID);
			statement.setInt(1, player.getZoneId());
			statement.setInt(2, player.getObjectId());
			statement.execute();

			return true;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Обновление стадии квеста.
	 * 
	 * @param state состояние квеста.
	 */
	public final void updateQuest(QuestState state) {
		Player player = state.getPlayer();

		if(player == null)
			return;

		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_QUEST);
			statement.setInt(1, state.getState());
			statement.setInt(2, state.getPanelStateId());
			statement.setInt(3, player.getObjectId());
			statement.setInt(4, state.getQuestId());
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление серверной переменной.
	 * 
	 * @param name название переменной.
	 * @param value значение переменной.
	 */
	public final void updateServerVar(String name, String value) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_SERVER_VARIABLE);
			statement.setString(1, value);
			statement.setString(2, name);
			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление положения игрока в БД.
	 * 
	 * @param location позиция игрока.
	 * @param name имя игрока.
	 */
	public final void updatePlayerLocation(Location location, String name, int zoneId) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_LOCATION);
			statement.setInt(1, (int) location.getX());
			statement.setInt(2, (int) location.getY());
			statement.setInt(3, (int) location.getZ());
			statement.setInt(4, location.getHeading());
			statement.setInt(5, location.getContinentId());
			statement.setInt(6, zoneId);
			statement.setString(7, name);

			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление класса игрока в БД.
	 * 
	 * @param objectId ид игрока.
	 * @param cs класс игрока.
	 */
	public final void updatePlayerClass(int objectId, PlayerClass cs) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_CLASS);
			statement.setInt(1, cs.ordinal());
			statement.setInt(2, objectId);

			statement.execute();
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление расы игрока в БД.
	 * 
	 * @param objectId ид игрока.
	 * @param race раса игрока.
	 * @param sex пол игрока.
	 */
	public final boolean updatePlayerRace(int objectId, Race race, Sex sex) {
		PreparedStatement statement = null;
		Connection con = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(UPDATE_PLAYER_RACE);
			statement.setInt(1, race.getId());
			statement.setInt(2, sex.ordinal());
			statement.setInt(3, objectId);

			return statement.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCS(con, statement);
		}

		return false;
	}

	/**
	 * Получение ид игрока с указанным именем
	 * 
	 * @param name имя игрока.
	 * @return уникальный ид.
	 */
	public final int getPlayerObjectId(String name) {
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try {
			con = connectFactory.getConnection();

			statement = con.prepareStatement(SELECT_PLAYER_OJECT_ID);
			statement.setString(1, name);

			rset = statement.executeQuery();

			if(rset.next())
				return rset.getInt(1);
		} catch(SQLException e) {
			LOGGER.warning(e);
		} finally {
			DBUtils.closeDatabaseCSR(con, statement, rset);
		}

		return 0;
	}
}
