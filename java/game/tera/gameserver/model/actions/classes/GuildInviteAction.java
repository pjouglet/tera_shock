package tera.gameserver.model.actions.classes;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.World;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionDoned;
import tera.gameserver.network.serverpackets.ActionInvite;

/**
 * Модель акшена для взятия игрока в гильдию
 *
 * @author Ronn
 */
public class GuildInviteAction extends PlayerAction
{
	@Override
	public synchronized void assent(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем цель инициатора
		Player target = getTarget();

		super.assent(player);

		if(!test(actor, target))
			return;

		// получаем тип акшена
		ActionType type = getType();

		// получаем гильдию инициатора
		Guild guild = actor.getGuild();

		// отправляем нужные пакеты
		actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), player.getObjectId(), player.getSubId(), type.ordinal(), objectId), true);
		player.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), player.getObjectId(), player.getSubId(), type.ordinal(), objectId), true);

		// добавляем в гильдию игрока
		guild.joinMember(player);

		// обновляем гильдию
		actor.updateGuild();

		// сообщаем о вступлении человека в гильдию
		player.sendMessage("Вы вступили в \"" + guild.getName() + "\"!");
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем цель его
		Player target = getTarget();

		// если все есть
		if(actor != null && target != null)
		{
			ActionType type = getType();

			// рассылаем пакеты
			actor.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
			target.sendPacket(ActionDoned.getInstance(actor.getObjectId(), actor.getSubId(), target.getObjectId(), target.getSubId(), type.ordinal(), objectId), true);
		}

		super.cancel(player);
	}

	@Override
	public ActionType getType()
	{
		return ActionType.INVITE_GUILD;
	}

	@Override
	public void init(Player actor, String name)
	{
		// пытаемся получить игрка из окружения
		Player target = World.getAroundByName(Player.class, actor, name);

		// если нету
		if(target == null)
			// берем из мира
			target = World.getPlayer(name);

		this.actor = actor;
		this.target = target;
	}

	@Override
	public void invite()
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем цель
		Player target = getTarget();

		// если кого-то из них нету, выходим
		if(actor == null || target == null)
			return;

		// получаем гильдию инициатора
		Guild guild = actor.getGuild();

		// если ее нет, выходим
		if(guild == null)
		{
			actor.sendMessage("You don't have a Guild");
			return;
		}

		// запоминаем акшен
		actor.setLastAction(this);
		target.setLastAction(this);

		ActionType type = getType();

		// отправляем пакет
		target.sendPacket(ActionInvite.getInstance(actor.getName(), target.getName(), type.ordinal(), objectId), true);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем таск
		setSchedule(executor.scheduleGeneral(this, 20000));
	}

	@Override
	protected boolean test(Player actor, Player target)
	{
		// если кого-то из них нет, выходим
		if(target == null || actor == null)
			return false;

		// получаем права инициатора
		GuildRank actorRank = actor.getGuildRank();

		// если их нет либо они не позволяют инвайтить, выходим
		if(actorRank == null || !actorRank.isChangeLineUp())
		{
			actor.sendMessage("You can't invite to Guild");
			return false;
		}

		// если цель слишком далеко
		/*if(!actor.isInRange(target, Config.WORLD_GUILD_INVITE_MAX_RANGE))
		{
			actor.sendMessage("Игрок слишком далеко.");
			return false;
		}*/

		// получаем гильдию цели
		Guild guild = target.getGuild();

		// если она у него есть, выходим
		if(guild != null)
		{
			actor.sendMessage("The player is already a member of another Guild");
			return false;
		}

		// если цель уже занята другим предложением
		if(target.getLastAction() != null)
		{
			actor.sendMessage(MessageType.TARGET_IS_BUSY);
			return false;
		}

		return true;
	}
}
