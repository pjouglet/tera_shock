package tera.gameserver.manager;

import tera.gameserver.model.Account;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.model.UserClient;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelOwerturn;
import tera.gameserver.network.serverpackets.CancelTargetHp;
import tera.gameserver.network.serverpackets.CharShieldBlock;
import tera.gameserver.network.serverpackets.CharTurn;
import tera.gameserver.network.serverpackets.Damage;
import tera.gameserver.network.serverpackets.Emotion;
import tera.gameserver.network.serverpackets.GuildBank;
import tera.gameserver.network.serverpackets.NotifyCharacter;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.gameserver.network.serverpackets.NpcNotice;
import tera.gameserver.network.serverpackets.PlayerBank;
import tera.gameserver.network.serverpackets.PlayerEquipment;
import tera.gameserver.network.serverpackets.InventoryInfo;
import tera.gameserver.network.serverpackets.PlayerList;
import tera.gameserver.network.serverpackets.ShopTradePacket;
import tera.gameserver.network.serverpackets.SkillLockTarget;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.network.serverpackets.TargetHp;
import tera.gameserver.network.serverpackets.UpdateStamina;

/**
 * Класс с набором методов для отпрвки пакетов.
 *
 * @author Ronn
 */
public final class PacketManager
{
	/**
	 * Отобразить разворот персонажа.
	 *
	 * @param character разворачиваемый персонаж.
 	 * @param newHeading новый разворот.
	 * @param time время разворота.
	 */
	public static void showTurnCharacter(Character character, int newHeading, int time)
	{
		character.broadcastPacket(CharTurn.getInstance(character, newHeading, time));
	}

	/**
	 * Уведомление о добавлении игрока в друзья.
	 *
	 * @param player добавляющий игрок.
	 * @param added добавленный игрок.
	 */
	public static void addToFriend(Player player, Player added)
	{
		// создаем сообщение
		SystemMessage message = SystemMessage.getInstance(MessageType.ADDED_USER_NAME_TO_YOU_FRIEND_LIST);

		// вносим имя добавленного игрока
		message.addUserName(added.getName());

		// отправляем добавителю
		player.sendPacket(message, true);

		// создаем сообщение
		message = SystemMessage.getInstance(MessageType.YOU_VE_BEEN_ADDED_TO_USER_NAME_FRIENDS_LIST);
		// вносим имя добавителя
		message.addUserName(player.getName());
		// отправляем добавленному
		added.sendPacket(message, true);
	}

	/**
	 * Удаление отображения хп над головой персонажа у персонажа.
	 *
	 * @param actor тот, кому удалить надо.
	 * @param enemy тот, чье хп надо удалить.
	 */
	public static void cancelTargetHp(Character actor, Character enemy)
	{
		actor.sendPacket(CancelTargetHp.getInstance(enemy), true);
	}

	/**
	 * Уведомление о удалении игрока из друзей.
	 *
	 * @param player удалитель.
	 * @param removedName имя удаляемого друга.
	 * @param removed удаляемый друг.
	 */
	public static void removeToFriend(Player player, String removedName, Player removed)
	{
		// создаем сообщение
		SystemMessage message = SystemMessage.getInstance(MessageType.YOU_REMOVED_USER_NAME_FROM_YOU_FRIENDS_LIST);

		// вносим имя удаляемого игрока
		message.addUserName(removedName);

		// отправляем добавителю
		player.sendPacket(message, true);

		// если удаляемый игрок онлайн
		if(removed != null)
		{
			// создаем сообщение
			message = SystemMessage.getInstance(MessageType.USER_NAME_REMOVED_UYOU_FROM_THEIR_FRIENDS_LIST);
			// вносим имя удалителя
			message.addUserName(player.getName());
			// отправляем добавленному
			removed.sendPacket(message, true);
		}
	}

	/**
	 * Отображения кол-во полученых денег.
	 *
	 * @param actor кто получил.
	 * @param count кол-во денег.
	 */
	public static void showAddGold(Character actor, int count)
	{
		actor.sendPacket(SystemMessage.getInstance(MessageType.ADD_MONEY).addMoney(actor.getName(), count), true);
	}

	/**
	 * Отобразить боевую стойку на указанного персонажа.
	 *
	 * @param npc нпс в боевой стойке.
	 * @param enemy цель НПС.
	 */
	public static void showBattleStance(Npc npc, TObject enemy)
	{
		npc.broadcastPacketToOthers(NpcNotice.getInstance(npc, enemy == null? 0 : enemy.getObjectId(), enemy == null? 0 : enemy.getSubId()));
	}

	/**
	 * Отобразить боевую стойку индивидуально для игрока на указанного персонажа.
	 *
	 * @param player игрок, которому надо отобразить.
	 * @param npc нпс в боевой стойке.
	 * @param enemy цель НПС.
	 */
	public static void showBattleStance(Player player, Npc npc, TObject enemy)
	{
		player.sendPacket(NpcNotice.getInstance(npc, enemy == null? 0 : enemy.getObjectId(), enemy == null? 0 : enemy.getSubId()), true);
	}

	/**
	 * Отобразить опрокидывание персонажа.
	 *
	 * @param character опрокинутый персонаж.
	 */
	public static void showCharacterOwerturn(Character character)
	{
		character.broadcastPacket(CancelOwerturn.getInstance(character));
	}

	/**
	 * Отображение нанесенного урона персонажу.
	 *
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param info данные об атаке.
	 * @param skill атакуемый скил.
	 * @param type тип атаки.
	 */
	public static void showDamage(Character attacker, Character attacked, AttackInfo info, Skill skill, int type)
	{
		attacked.broadcastPacket(Damage.getInstance(attacker, attacked, info, skill, type));
	}

	/**
	 * Отображение нанесенного урона персонажу.
	 *
	 * @param attacker атакующий.
	 * @param attacked атакуемый.
	 * @param damageId ид урона.
	 * @param damage кол-во урона.
	 * @param crit есть ли крит.
	 * @param owerturned опрокидывает ли.
	 * @param type тип урона.
	 */
	public static void showDamage(Character attacker, Character attacked, int damageId, int damage, boolean crit, boolean owerturned, int type)
	{
		attacked.broadcastPacket(Damage.getInstance(attacker, attacked, damageId, damage, crit, owerturned, type));
	}

	/**
	 * Отобразить эффект персонажу.
	 *
	 * @param actor персонаж, которому надо отобразить эффект.
	 * @param effect отображаемый эффект.
	 */
	public static void showEffect(Character actor, Effect effect)
	{
		actor.sendPacket(AppledEffect.getInstance(effect.getEffector(), effect.getEffected(), effect), true);
	}

	/**
	 * Отобразить эмоцию персонажа.
	 *
	 * @param actor персонаж.
	 * @param type тип эмоции.
	 */
	public static void showEmotion(Character actor, EmotionType type)
	{
		actor.broadcastPacket(Emotion.getInstance(actor, type));
	}

	/**
	 * Отображение входа в игру друга.
	 *
	 * @param player игрок.
	 * @param friend вошедший друг.
	 */
	public static void showEnterFriend(Player player, Player friend)
	{
		SystemMessage message = SystemMessage.getInstance(MessageType.USER_NAME_HAS_COME_ONLINE);

		message.addUserName(friend.getName());

		player.sendPacket(message, true);
	}

	/**
	 * Отображение блокировки цели для скила.
	 *
	 * @param caster кастующий.
	 * @param target цель.
	 * @param skill скил.
	 */
	public static void showLockTarget(Character caster, Character target, Skill skill)
	{
		caster.sendPacket(SkillLockTarget.getInstance(target, skill, true), true);
	}

	/**
	 * @param sender отправитель.
	 * @param type тип иконки.
	 */
	public static void showNotifyIcon(Character sender, NotifyType type)
	{
		sender.broadcastPacket(NotifyCharacter.getInstance(sender, type));
	}

	/**
	 * Отображения кол-во денег, потраченых на оплату.
	 *
	 * @param actor кто потратил.
	 * @param count кол-во денег.
	 */
	public static void showPaidGold(Character actor, int count)
	{
		actor.sendPacket(SystemMessage.getInstance(MessageType.PAID_AMOUNT_MONEY).addPaidMoney(count), true);
	}

	/**
	 * Отобразить списк игроков на аккаунте.
	 *
	 * @param client запрашиваемый клиент.
	 * @param account целевой аккаунт.
	 */
	public static void showPlayerList(UserClient client, Account account)
	{
		// ложим на отправку
		client.sendPacket(PlayerList.getInstance(account.getName()), true);
	}

	/**
	 * Отобразить блокировку скила персонажем.
	 *
	 * @param actor блокирующий скил.
	 */
	public static void showShieldBlocked(Character actor)
	{
		actor.broadcastPacket(CharShieldBlock.getInstance(actor));
	}

	/**
	 * Отобразить диалог магазина игроку.
	 *
	 * @param player игрок.
	 * @param dialog диалог магазина.
	 */
	public static void showShopDialog(Player player, ShopDialog dialog)
	{
		player.sendPacket(ShopTradePacket.getInstance(dialog), true);
	}

	/**
	 * Отображениехп над головой персонажа у персонажа.
	 *
	 * @param actor тот, кому показать надо.
	 * @param enemy тот, чье хп надо показать.
	 */
	public static void showTargetHp(Character actor, Character enemy)
	{
		actor.sendPacket(TargetHp.getInstance(enemy, actor.checkTarget(enemy)? TargetHp.RED : TargetHp.BLUE), true);
	}

	/**
	 * Отобразить кол-во используемых итемов.
	 *
	 * @param actor персонаж, использующий итем.
	 * @param id ид итема.
	 * @param count кол-во итемов.
	 */
	public static void showUseItem(Character actor, int id, int count)
	{
		actor.sendPacket(SystemMessage.getInstance(MessageType.ITEM_USE).addItem(id, count), true);
	}

	/**
	 * Обновляет для всех экиперовку игрока.
	 *
	 * @param player игрок.
	 */
	public static void updateEquip(Character player)
	{
		player.broadcastPacket(PlayerEquipment.getInstance(player));
	}

	/**
	 * Обновляет банк гильдии.
	 *
	 * @param player игрок.
	 */
	public static void updateGuildBank(Player player, int startCell)
	{
		player.sendPacket(GuildBank.getInstance(player, startCell), true);
	}

	/**
	 * Обновляет инвентарь игрока.
	 *
	 * @param player игрок.
	 */
	public static void updateInventory(Player player)
	{
		player.sendPacket(InventoryInfo.getInstance(player), true);
	}

	/**
	 * Обновляет банк игрока.
	 *
	 * @param player игрок.
	 */
	public static void updatePlayerBank(Player player, int startCell)
	{
		player.sendPacket(PlayerBank.getInstance(player, startCell), true);
	}

	/**
	 * Обновление стамины игроку.
	 *
	 * @param player игрок, которому надо обновить.
	 */
	public static void updateStamina(Player player)
	{
		player.sendPacket(UpdateStamina.getInstance(player), true);
	}

	/**
	 * Отображение удаление итема.
	 *
	 * @param character персонаж, у которого удаляется итем.
	 * @param item удаляемый итем.
	 */
	public static void showDeleteItem(Character character, ItemInstance item)
	{
		character.sendPacket(SystemMessage.getInstance(MessageType.ITEM_NAME_DESTROYED).addItemName(item.getItemId()), true);
	}

	private PacketManager()
	{
		throw new IllegalArgumentException();
	}
}
