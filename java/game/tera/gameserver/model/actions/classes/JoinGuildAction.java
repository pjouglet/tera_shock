package tera.gameserver.model.actions.classes;

import java.util.concurrent.ScheduledFuture;

import rlib.util.SafeTask;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.GuildManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Guild;
import tera.gameserver.model.Party;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionInvite;
import tera.gameserver.network.serverpackets.GuildCheckName;
import tera.gameserver.network.serverpackets.GuildInfo;
import tera.gameserver.network.serverpackets.GuildMembers;

/**
 * Модель акшена для просьбы взять игрока в пати.
 *
 * @author Ronn
 */
public class JoinGuildAction extends SafeTask implements Action, Foldable
{
	private static final FoldablePool<JoinGuildAction> pool = Pools.newConcurrentFoldablePool(JoinGuildAction.class);

	/**
	 * Создание акшена.
	 *
	 * @param actor актор.
	 * @param guildName имя гильдии.
	 * @param members соппартийцы.
	 * @return новый акшен.
	 */
	public static JoinGuildAction newInstance(Player actor, String guildName, Array<Player> members, int price)
	{
		JoinGuildAction action = pool.take();

		if(action == null)
			action = new JoinGuildAction();

		// получаем фабрику ИД
		IdFactory idFactory = IdFactory.getInstance();

		action.actor = actor;
		action.objectId = idFactory.getNextActionId();
		action.guildName = guildName;
		action.members = members;
		action.result = members.size() - 1;
		action.price = price;

		return action;
	}

	/** создатель клана */
	private Player actor;

	/** название гильдии */
	private String guildName;

	/** согласившиеся игроки */
	private Array<Player> players;
	/** соппартийцы */
	private Array<Player> members;

	/** ссылка на таск */
	protected ScheduledFuture<?> schedule;

	/** ид гильдии */
	private int objectId;
	/** нужное кол-во ассентов */
	private int result;
	/** стоимость создания */
	private int price;

	public JoinGuildAction()
	{
		this.players = Arrays.toConcurrentArray(Player.class);
	}

	@Override
	public synchronized void assent(Player player)
	{
		// добавляем игрока в список согласных
		players.add(player);

		// если еще не все согласились, выходим
		if(players.size() < result)
			return;

		try
		{
			// останавливаем таск на выключение.
			if(schedule != null)
			{
				schedule.cancel(false);
				schedule = null;
			}

			if(actor.hasGuild())
			{
				player.sendMessage("You already have a Guild.");
				return;
			}

			Party party = player.getParty();

			if(party == null)
			{
				player.sendMessage("You must be in party.");
				return;
			}

			Array<Player> members = party.getMembers();

			members.readLock();
			try
			{
				Player[] array = members.array();

				for(int i = 0, length = members.size(); i < length; i++)
					if(array[i].hasGuild())
					{
						actor.sendMessage("A member of you party have already a Guild.");
						return;
					}
			}
			finally
			{
				members.readUnlock();
			}

			Inventory inventory = actor.getInventory();

			// проверяем наличие денег
			if(inventory.getMoney() < price)
			{
				actor.sendMessage("You don't have enough money.");
				return;
			}

			// получаем менеджера гильдий
			GuildManager guildManager = GuildManager.getInstance();

			// пытаемся создать клан
			Guild clan = guildManager.createNewGuild(guildName, actor);

			// если создать не удалось, завершаем
			if(clan == null)
				actor.sendMessage("Failed to create Guild.");
			else
			{
				// забираем деньги
				inventory.subMoney(price);

				// получаем логера игровых событий
				GameLogManager gameLogger = GameLogManager.getInstance();

				// записываем событие о затраты денег на создание клана
				gameLogger.writeItemLog(player.getName() + " buy guild for " + price + " gold");

				// получаем менеджера игровых событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// уведомляем про изменения инвенторя
				eventManager.notifyInventoryChanged(actor);

				// отправялем пакает об корректности названии клана
				actor.sendPacket(GuildCheckName.getInstance(guildName), true);

				members.readLock();
				try
				{
					Player[] array = members.array();

					// пробегаемся по соппартийцам
					for(int i = 0, length = members.size(); i < length; i++)
					{
						Player member = array[i];

						// добавляем их в клан
						if(member != actor)
							clan.joinMember(member);
					}

					//отправляем всем клан инфо
					for(int i = 0, length = members.size(); i < length; i++)
					{
						Player mem = array[i];

						mem.sendPacket(GuildInfo.getInstance(mem), true);
						mem.sendPacket(GuildMembers.getInstance(mem), true);
					}
				}
				finally
				{
					members.readUnlock();
				}
			}
		}
		finally
		{
			clear();
		}
	}

	@Override
	public synchronized void cancel(Player palyer)
	{
		members.readLock();
		try
		{
			Player[] array = members.array();

			for(int i = 0, length = members.size(); i < length; i++)
				array[i].sendMessage("Создание клана прервано из-за не согласия всех участников группы.");

			actor.sendMessage("Создание клана прервано из-за не согласия всех участников группы.");
		}
		finally
		{
			members.readUnlock();
		}

		if(schedule != null)
		{
			schedule.cancel(false);
			schedule = null;
		}

		clear();

		pool.put(this);
	}

	/**
	 * Очистка акшена.
	 */
	protected void clear()
	{
		actor.setLastAction(null);

		Player[] array = members.array();

		for(int i = 0, length = members.size(); i < length; i++)
			array[i].setLastAction(null);

		actor = null;

		players.clear();
	}

	@Override
	public void finalyze()
	{
		objectId = 0;
		result = 0;
	}

	@Override
	public Player getActor()
	{
		return actor;
	}

	@Override
	public int getId()
	{
		return ActionType.CREATE_GUILD.ordinal();
	}

	@Override
	public int getObjectId()
	{
		return objectId;
	}

	@Override
	public Player getTarget()
	{
		return null;
	}

	@Override
	public ActionType getType()
	{
		return ActionType.CREATE_GUILD;
	}

	@Override
	public void init(Player actor, String name)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void invite()
	{
		if(members.size() < 2)
		{
			cancel(actor);
			return;
		}

		ActionInvite action = ActionInvite.getInstance(actor.getName(), guildName, ActionType.CREATE_GUILD.ordinal(), objectId);

		actor.setLastAction(this);

		members.readLock();
		try
		{
			Player[] array = members.array();

			for(int i = 0, length = members.size(); i < length; i++)
			{
				Player player = array[i];

				if(player == actor)
					continue;

				action.increaseSends();
			}

			for(int i = 0, length = members.size(); i < length; i++)
			{
				Player player = array[i];

				if(player == actor)
					continue;

				player.setLastAction(this);
				player.sendPacket(action, false);
			}
		}
		finally
		{
			members.readUnlock();
		}

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// отправляем на выполнение
		schedule = executor.scheduleGeneral(this, 60000);
	}

	@Override
	public void reinit(){}

	@Override
	protected void runImpl()
	{
		cancel(actor);
	}

	@Override
	public void setActor(Player actor)
	{
		this.actor = actor;
	}

	@Override
	public void setTarget(Object target){}

	@Override
	public boolean test()
	{
		return true;
	}
}
