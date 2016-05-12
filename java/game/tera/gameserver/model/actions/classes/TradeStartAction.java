package tera.gameserver.model.actions.classes;

import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.World;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.actions.dialogs.ActionDialogType;
import tera.gameserver.model.actions.dialogs.TradeDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionDoned;
import tera.gameserver.network.serverpackets.AppledAction;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Моделт акшена приглашения на обмен вещами.
 *
 * @author Ronn
 */
public class TradeStartAction extends PlayerAction
{
	@Override
	public synchronized void assent(Player player)
	{
		// получам инициатора
		Player actor = getActor();
		// получаем цель
		Player target = getTarget();

		super.assent(player);

		// если кого-то из них нету, выходим
		if(actor == null || target == null)
		{
			log.warning(this, new Exception("not found actor or target"));
			return;
		}

		ActionType type = getType();

		// рассылаем пакеты
		actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);

		// создаем диалог трейда
		TradeDialog dialog = TradeDialog.newInstance(actor, target);

		// если он неудачно инициализировался
		if(!dialog.init())
			// закрываем
			dialog.cancel(null);
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем его цель
		Player target = getTarget();

		// если оба есть
		if(actor != null && target != null)
		{
			// получаем тип
			ActionType type = getType();

			SystemMessage packet = null;

			if(player == actor)
			{
				// создаем сообщение о том, что инициатор отменил
				packet = SystemMessage.getInstance(MessageType.USERNAME_REJECTED_A_TRADE);
				// добавляем имя инициатора
				packet.addUserName(actor.getName());
				// отправляем пакет
				target.sendPacket(packet, true);
			}
			else if(player == target)
			{
				// создаем сообщение о том, что опонент отменил
				packet = SystemMessage.getInstance(MessageType.OPPONENT_REJECTED_A_TRADE);
				// добавляем имя опонента
				packet.addOpponent(target.getName());
				// отправляем пакет
				actor.sendPacket(packet, true);
			}

			// отправляем соответствующие пакеты
			actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
			target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		}

		super.cancel(player);
	}

	@Override
	public ActionType getType()
	{
		return ActionType.TRADE;
	}

	@Override
	public void init(Player actor, String name)
	{
		// ищем игрока рядом
		Player target = World.getAroundByName(Player.class, actor, name);

		// если не нашли
		if(target == null)
			// ищем в мире
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
		{
			log.warning(this, new Exception("not found actor or target"));
			return;
		}

		// запоминаем акщен
		actor.setLastAction(this);
		target.setLastAction(this);

		// создаем сообщение о запросе
		SystemMessage packet = SystemMessage.getInstance(MessageType.USERNAME_REQUESTED_A_TRADE);
		// добавляем имя инициатора
		packet.addUserName(actor.getName());
		// отправляем пакет
		target.sendPacket(packet, true);

		ActionType type = getType();

		// отправляем соответствующие пакеты
		actor.sendPacket(AppledAction.newInstance(actor, target, type.ordinal(), objectId), true);
		target.sendPacket(AppledAction.newInstance(actor, target, type.ordinal(), objectId), true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запукскаем таск
		setSchedule(executor.scheduleGeneral(this, 42000));
	}

	@Override
	public boolean test(Player actor, Player target)
	{
		// если кого-то нет, выходим
		if(actor == null)
			return false;

		// получаем текущий диалог
		ActionDialog dialog = actor.getLastActionDialog();

		// если он есть и это диалог трейда
		if(dialog != null && dialog.getType() == ActionDialogType.TRADE_DIALOG)
		{
			actor.sendMessage(MessageType.YOU_CANT_TRADE_THAT);
			return false;
		}

		// если игрок не онлаин
		if(target == null)
		{
			actor.sendMessage(MessageType.THAT_CHARACTER_ISNT_ONLINE);
			return false;
		}

		// если цель слишком далеко
		if(!actor.isInRange(target, Config.WORLD_TRADE_MAX_RANGE))
		{
			actor.sendMessage(MessageType.TOO_FAR_AWAY);
			return false;
		}

		// если цель в бою
		if(target.isBattleStanced())
		{
			actor.sendMessage(MessageType.TARGET_IS_IN_COMBAT);
			return false;
		}

		// если цель уже занята другим предложением
		if(target.getLastAction() != null)
		{
			actor.sendMessage(MessageType.TARGET_IS_BUSY);
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
