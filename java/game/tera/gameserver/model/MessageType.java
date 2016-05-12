package tera.gameserver.model;

/**
 * Перечисление заголовков системный сообщений.
 *
 * @author Ronn
 */
public enum MessageType
{
	NO_TERRAIN_FOUND_PLEASE_TELEPORT_TO_ANOTHER_AREA("@18"),
	YOU_CANT_TELEPORT_RIGHT_NOW("@19"),

	//@18 - No terrain found. Please teleport to another area.
	//@19 - You can't teleport right now.

	THAT_ITEM_IS_UNAVAILABLE_TO_YOUR_CLASS("@28"),
	YOU_MUST_BE_A_HIGHER_LEVEL_TO_USE_THAT("@29"),

	YOU_CANNOT_USE_THAT_SKILL_AT_THE_MOMENT("@36"),

	NOT_ENOUGH_MP("@37"),

	INVENTORY_IS_FULL("@39"),

	ALL_CRYSTAL_SLOTS_ARE_FULL("@51"),

	THAT_ITEM_CANTT_BE_STORED_IN_THE_BANK("@53"),

	ANOTHER_PLAYER_IS_ALREADY_GATHERING_THAT("@61"),

	YOU_CANT_DO_THAT_RIGHT_NOW_TRY_AGAINT_IN_A_MOMENT("@62"),

	GATHERING_SUCCESSFUL("@63"),
	GATHERING_FAILED("@64"),
	GATHERING_INTERRUPTED("@65"),
	GATHERING_CANCELED("@66"),

	YOU_MINING_HAS_INCREASED_TO_PROF("@67"),
	YOU_PLANT_COLLECTING_HAS_INCREASED_TO_PROF("@68"),
	YOU_ESSENSE_GATHERING_HAS_INCREASED_TO_PROF("@70"),

	//@84 - Your pet has been destroyed.
	YOUR_PET_HAS_BEEN_DESTRUYED("@84"),
	ATTACKER_DESTROYED_YOUR_PET("@86"),

	THAT_CHARACTER_ISNT_ONLINE("@113"),

	YOU_KILLED_PLAYER("@119"),
	PLAYER_KILLED_YOU("@120"),

	// @193 - Your party has disbanded.
	YOUR_PARTY_HAS_DISBANDED("@193"),

	// @197 - You can't group with someone preparing for PvP.
	YOU_CANT_GROUP_WITH_SAMEONE_PREPARING_FOR_PVP("@197"),

	// @204 - The party is full.
	THE_PARTY_IS_FULL("@204"),

	YOU_ARE_NOT_THE_GUILD_MASTER("@276"),

	GUILD_CREATE_SUCCESSFUL("@289"),
	GUILD_INVITE_MEMBER("@290"),

	DISCOVERED_SECTION_NAME("@324"),
	TARGET_IS_BUSY("@334"),
	// @334 - Target is busy.
	//@324 - Discovered: {sectionName}

	YOU_CANT_DISCARD_ITEM_NAME("@339"),
	YOU_CANT_TRADE("@340"),

	ITEM_USE("@346"),

	THAT_ITEM_IS_SOULBOUND("@347"),

	TRADE_CANCELED("@353"),

	//@354 - {Opponent} canceled the trade.
	OPPONENT_CANCELED_THE_TRADE("@354"),
	TRADE_COMPLETED("@355"),

	TRADE_HAS_BEGUN("@356"),
	// @356 - Trade has begun.
	TOO_FAR_AWAY("@357"),
	// @357 - Too far away.

	TARGET_IS_IN_COMBAT("@359"),
	//@359 - Target is in combat.

	YOU_CANT_TRADE_THAT("@363"),
	//@363 - You can't trade that

	USERNAME_REQUESTED_A_TRADE("@372"),
	USERNAME_REJECTED_A_TRADE("@373"),

	OPPONENT_REJECTED_A_TRADE("@374"),

	ADD_MONEY("@380"),

	//@431 - Players must be online to be added to your friends list.
	PLAYER_MUST_BE_ONLINE_TO_BE_ADDED_TO_YOUR_LIST("@431"),
	//@432 - Added {UserName} to your friends list.
	ADDED_USER_NAME_TO_YOU_FRIEND_LIST("@432"),
	//@433 - You've been added to {UserName}'s friends list.
	YOU_VE_BEEN_ADDED_TO_USER_NAME_FRIENDS_LIST("@433"),

	//@434 - You can only send three friend invites to any one person each day.
	//@435 - That player blocked you.

	//@436 - You removed {UserName} from your friends list.
	YOU_REMOVED_USER_NAME_FROM_YOU_FRIENDS_LIST("@436"),
	//@437 - {UserName}  removed you from their friends list.
	USER_NAME_REMOVED_UYOU_FROM_THEIR_FRIENDS_LIST("@437"),
	//@438 - That player is already on your friends list.
	THAT_PLAYER_IS_ALREADY_ON_YOUR_FRIENDS_LIST("@438"),
	//@439 - That player is not on your friends list.
	THAT_PLAYER_IS_NOT_ON_YOUR_FRIENDS_LIST("@439"),
	//@440 - Can't add friend. Either your friends list or theirs is full.
	CANT_ADD_FRIEND_EITHER_YOUR_FRIENDS_LIST_OR_THEIRS_IS_FULL("@440"),

	//@442 - {UserName} has come online.
	USER_NAME_HAS_COME_ONLINE("@442"),

	CANCELED_TIME_IS_UP("@486"),

	REQUESTOR_CHALLANGES_YOU_TO_A_DUEL("@489"),

	TARGET_REJERECT_THE_DUEL("@490"),
	TARGET_ACCEPTED_THE_DUEL("@491"),

	YOU_ARE_IN_A_DUEL_NOW("@494"),

	YOU_CANT_DUEL_WITH_SOMEONE_IN_PVP("@496"),
	//@496 - You can't duel with someone in PvP.

	WINNER_DEFEATED_LOSER_IN_A_DUEL("@501"),

	DUEL_WON("@502"),
	DUEL_LOST("@503"),

	THE_DUEL_HAS_ENDED("@508"),

	BATTLE_STANCE_ON("@600"),
	BATTLE_STANCE_OFF("@601"),

	//@602 - {PartyPlayerName} is dead.
	PARTY_PLAYER_NAME_IS_DEAD("@602"),

	YOU_DONT_HAVE_ENOUGH_GOLD("#@614"),

	NEW_QUEST_QUEST_NAME("@624"),
	CONGRATULATIONS_QUEST_NAME_COMPLETED("@625"),
	//@625 - Congratulations! "{QuestName}" completed.
	//@624 - New quest: {QuestName}.

	ABANDONED_QUEST_NAME("@627"),

	YOU_CANT_MAKE_A_CAMPFIRE_RIGHT_NOW("@628"),
	THERE_ANOTHER_CAMPFIRE_NEAR_HERE("@629"),

	CHARMS_CAN_ONLY_BE_USED_NEAR_A_CAMPFIRE("@631"),

	YOU_ARE_RECHARGING_STAMINE("@632"),
	YOU_ARE_NO_LONGER_RECHARGING_STAMINA("@633"),

	YOUVE_LEARNED_SKILL_NAME("@650"),

	YOU_CANT_LEARN_SKILLS_AT_THIS_LEVEL("@651"),

	YOU_DONT_HAVE_ENOUGH_GOLD_TO_LEARN_THAT_SKILL("@652"),

	YOU_NEED_TO_LEARN_ANOTHER_SKILL_FRST_BEFORE_YOU_CAN_LEARN_THIS_SKILL("@654"),

	USER_NAME_IS_DEAD("@655"),

	ITEM_NAME_DESTROYED("@664"),
	//@664 - {ItemName} destroyed.

	//@679 - {PartyPlayerName} picked up {ItemName} x {ItemAmount}.
	PARTY_PLAYER_NAME_PICK_UP_ITEM_NAME_ITEM_AMOUNT("@679"),

	// @684 - {PartyPlayerName} is now party leader.
	PARTY_PLAYER_NAME_IS_NOW_PARTY_LEADER("@684"),

	//@685 - You've been kicked from the party.
	YOU_VE_BEEN_KICKED_FROM_THE_PARTY("@685"),

	// @686 - You are now party leader.
	YPU_ARE_NOW_PARTY_LEADER("@686"),

	QUEST_TRACKER_DISPLAYS_UP_TO_7_QUESTS("@791"),

	QUEST_NAME_CANT_BE_ABANDONED("@883"),

	YOU_NOT_IN_GUILD("@925"),

	//@1073 - you can't invite PvP player to a party
	YOU_CANT_INVITE_PVP_PLAYER_TO_A_PARTY("@1073"),

	// @1094 - target is already in the same group
	TARGET_IS_ALREADY_IN_THE_SAME_GROUP("@1094"),

	PAID_AMOUNT_MONEY("@1101"),
	//@1101 - paid {amount@money}
	YOU_CANT_USE_FIREWOOD_RIGHT_NOW("@1110"),

	;

	private String name;

	private MessageType(String name)
	{
		this.name = name;
	}

	/**
	 * @return заголовок сообщения.
	 */
	public String getName()
	{
		return name;
	}
}
