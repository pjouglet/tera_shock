package tera.gameserver.model.actions.classes;

import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Duel;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.World;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionDoned;
import tera.gameserver.network.serverpackets.AppledAction;
import tera.gameserver.network.serverpackets.DuelStart;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Модель запуска акшена дуэли.
 *
 * @author Ronn
 */
public class DuelStartAction extends PlayerAction
{
	@Override
	public synchronized void assent(Player player)
	{
		// цель акшена
		Player target = getTarget();
		// инициатор акшена
		Player actor = getActor();

		super.assent(player);

		// если условия не выполнены, отменяем
		if(!test(actor, target))
		{
			cancel(null);
			return;
		}

		// получаем тип акшена
		ActionType type = getType();

		// отправляем необходимые пакеты
		actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);

		// создаем сообщение о том, что игрок согласился на дуэль
		SystemMessage packet = SystemMessage.getInstance(MessageType.TARGET_ACCEPTED_THE_DUEL);

		// добавляем имя игрока
		packet.addTarget(target.getName());
		// отправляем пакет
		actor.sendPacket(packet, true);

		actor.sendPacket(DuelStart.getInstance(), true);
		target.sendPacket(DuelStart.getInstance(), true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем таск начала дуэли
		executor.scheduleGeneral(Duel.newInstance(actor, target), 5500);
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем опонента инициатора
		Player target = getTarget();

		// если все есть
		if(actor != null && target != null)
		{
			ActionType type = getType();

			SystemMessage packet;

			// если отменивший опонент
			if(player == target)
			{
				// создаем пакет с сообщением
				packet = SystemMessage.getInstance(MessageType.TARGET_REJERECT_THE_DUEL);
				// добавляем имя отменившего
				packet.addTarget(target.getName());
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
		return ActionType.DUEL;
	}

	@Override
	public void init(Player actor, String name)
	{
		this.actor = actor;
		this.target = World.getAroundByName(Player.class, actor, name);
	}

	@Override
	public synchronized void invite()
	{
		// получаем инициатора
		Player actor = getActor();

		// получаем цель инициатора
		Player target = getTarget();

		// если кого-то из них нет, выходим
		if(actor == null || target == null)
		{
			log.warning(this, new Exception("not found actor or target"));
			return;
		}

		// запоминаем приглашение
		actor.setLastAction(this);
		target.setLastAction(this);

		// получаем тип приглашения
		ActionType type = getType();

		// лтправляем необходимые пакеты
		actor.sendPacket(AppledAction.newInstance(actor, target, type.ordinal(), objectId), true);
		target.sendPacket(AppledAction.newInstance(actor, target, type.ordinal(), objectId), true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем новый отложенный таск
		setSchedule(executor.scheduleGeneral(this, 30000));
	}

	@Override
	public boolean test(Player actor, Player target)
	{
		// если кого-то их них нет, выходим
		if(actor == null)
			return false;

		// если игрок уже в дуэли
		if(actor.getDuel() != null)
		{
			actor.sendMessage(MessageType.YOU_ARE_IN_A_DUEL_NOW);
			return false;
		}

		// если игрок не онлаин
		if(target == null)
		{
			actor.sendMessage(MessageType.THAT_CHARACTER_ISNT_ONLINE);
			return false;
		}

		// если цель слишком далеко
		if(!actor.isInRange(target, Config.WORLD_DUEL_MAX_RANGE))
		{
			actor.sendMessage(MessageType.TOO_FAR_AWAY);
			return false;
		}

		// если цель уже занята другим предложением
		if(target.getLastAction() != null)
		{
			actor.sendMessage(MessageType.TARGET_IS_BUSY);
			return false;
		}

		// если игрок в пвп режиме
		if(actor.isPvPMode() || actor.isBattleStanced())
		{
			actor.sendMessage(MessageType.YOU_CANT_DUEL_WITH_SOMEONE_IN_PVP);
			return false;
		}

		// еси кто-то из них на ивенте, выходим
		if(actor.isEvent() || target.isEvent())
		{
			actor.sendMessage("Нельзя начинать дуэль на ивенте.");
			return false;
		}

		if(target.isBattleStanced() || target.isPvPMode())
		{
			actor.sendMessage(MessageType.TARGET_IS_IN_COMBAT);
			return false;
		}

		// если кто-то из них уже в дуэли, выходим
		if(target.hasDuel())
		{
			actor.sendMessage("Игрок уже в дуэли.");
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
