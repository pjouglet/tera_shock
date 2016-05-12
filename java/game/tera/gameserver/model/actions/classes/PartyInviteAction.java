package tera.gameserver.model.actions.classes;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.Party;
import tera.gameserver.model.World;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionDoned;
import tera.gameserver.network.serverpackets.ActionInvite;
import tera.gameserver.network.serverpackets.AppledAction;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Модель акшена для приглашения игрока в пати.
 *
 * @author Ronn
 * @created 06.03.2012
 */
public class PartyInviteAction extends PlayerAction
{
	@Override
	public void assent(Player player)
	{
		// получам инициатора
		Player actor = getActor();
		// получаем цель
		Player target = getTarget();

		super.assent(player);

		if(!test(actor, target))
			return;

		ActionType type = getType();

		// отправляем необходимые пакеты
		actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);

		// получаем пати инициатора
		Party party = actor.getParty();

		// если ее нету
		if(party == null)
			// создаем новую
			party = Party.newInstance(actor, objectId);

		// добавляем человека в пати
		party.addPlayer(target);
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем целевого игрока
		Player target = getTarget();

		// если все есть
		if(actor != null && target != null)
		{
			ActionType type = getType();

			// отправляем небоходимый пакет
			actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
			target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		}

		super.cancel(player);
	}

	@Override
	public ActionType getType()
	{
		return ActionType.PARTY;
	}

	@Override
	public void init(Player actor, String name)
	{
		// ищим игрока поблизости
		Player target = World.getAroundByName(Player.class, actor, name);

		// если не нашли
		if(target == null)
			// ищим во всем мире
			target = World.getPlayer(name);

		this.actor = actor;
		this.target = target;
	}

	@Override
	public synchronized void invite()
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем цель
		Player target = getTarget();

		// если кого-то из них нету, выходим
		if(actor == null || target == null)
			return;

		// запоминаем у них акшен
		actor.setLastAction(this);
		target.setLastAction(this);

		ActionType type = getType();

		// отправляем соответсвующие пакеты
		actor.sendPacket(AppledAction.newInstance(actor, target, type.ordinal(), objectId), true);
		target.sendPacket(ActionInvite.getInstance(actor.getName(), target.getName(), type.ordinal(), objectId), true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем новый таск
		setSchedule(executor.scheduleGeneral(this, 20000));
	}

	@Override
	public boolean test(Player actor, Player target)
	{
		// если кого-то нет, выходим
		if(actor == null)
			return false;

		// получаем группу игрока
		Party party = actor.getParty();

		// если группа уже есть и она переполнена
		if(party != null && party.size() > 4)
		{
			actor.sendMessage(MessageType.THE_PARTY_IS_FULL);
			return false;
		}

		// если игрок не онлаин
		if(target == null)
		{
			actor.sendMessage(MessageType.THAT_CHARACTER_ISNT_ONLINE);
			return false;
		}

		// если цель уже в пати, выходим
		if(target.getParty() != null)
		{
			actor.sendMessage(MessageType.TARGET_IS_ALREADY_IN_THE_SAME_GROUP);
			return false;
		}

		// если цель уже занята другим предложением
		if(target.getLastAction() != null)
		{
			actor.sendMessage(MessageType.TARGET_IS_BUSY);
			return false;
		}

		// если цель в пвп режиме
		if(target.isPvPMode())
		{
			actor.sendMessage(MessageType.YOU_CANT_INVITE_PVP_PLAYER_TO_A_PARTY);
			return false;
		}

		// если целевой игрок мертв
    	if(target.isDead())
    	{
    		actor.sendPacket(SystemMessage.getInstance(MessageType.USER_NAME_IS_DEAD).addUserName(target.getName()), true);
    		return false;
    	}

		return true;
	}
}
