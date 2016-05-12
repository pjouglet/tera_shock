package tera.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Модель дуэли между игроками.
 *
 * @author Ronn
 */
public final class Duel extends SafeTask implements Foldable
{
	private static final Logger log = Loggers.getLogger(Duel.class);

	private static final FoldablePool<Duel> pool = Pools.newConcurrentFoldablePool(Duel.class);

	/**
	 * Создание новой дуэли.
	 *
	 * @param actor инициатор.
	 * @param enemy опонент.
	 * @return новая дуэль.
	 */
	public static final Duel newInstance(Player actor, Player enemy)
	{
		Duel duel = pool.take();

		if(duel == null)
			duel = new Duel();

		duel.actor = actor;
		duel.enemy = enemy;
		duel.running = true;

		actor.setDuel(duel);
		enemy.setDuel(duel);

		return duel;
	}

	/** инициатор дуэли */
	private volatile Player actor;
	/** оппонент инициатора */
	private volatile Player enemy;

	/** сохранитель состояния инициатора */
	private final DuelPlayer actorSave;
	/** созранитель состояния опонента */
	private final DuelPlayer enemySave;

	/** активировался ли */
	private volatile boolean active;
	/** запущен ли */
	private volatile boolean running;

	/** ссылка на таск */
	private volatile ScheduledFuture<Duel> schedule;

	public Duel()
	{
		this.actorSave = new DuelPlayer();
		this.enemySave = new DuelPlayer();
	}

	/**
	 * Остановка дуэли.
	 *
	 * @param disruption сорвали ли дуэь.
	 * @param separated разошлись ли.
	 */
	public synchronized void cancel(boolean disruption, boolean separated)
	{
		if(!isRunning())
			return;

		// если есть активный таск
		if(schedule != null)
		{
			// отменяем
			schedule.cancel(false);
			// зануляем
			schedule = null;
		}

		// останавливаем дуэль
		setRunning(false);

		// инициатор
		Player actor = getActor();
		// опонент
		Player enemy = getEnemy();

		// зануляем дуэли
		actor.setDuel(null);
		enemy.setDuel(null);

		// если причина не в разбежке
		if(!separated)
		{
			// обновляем друг другу цвета ников
			actor.updateColor(enemy);
			enemy.updateColor(actor);

			// удаляем хп над головой друг другу
			PacketManager.cancelTargetHp(actor, enemy);
			PacketManager.cancelTargetHp(enemy, actor);
		}

		// смотри сумон инициатора
		Summon summon = actor.getSummon();

		// если суммон есть
		if(summon != null)
			// отменяем его атаку
			summon.getAI().abortAttack();

		// смотрим суммон опонента
		summon = enemy.getSummon();

		// если суммон есть
		if(summon != null)
			// отменяем его атаку
			summon.getAI().abortAttack();

		// если отмена по причине разбежки/вмешательства
		if(separated || disruption)
		{
			actor.sendMessage(MessageType.THE_DUEL_HAS_ENDED);
			enemy.sendMessage(MessageType.THE_DUEL_HAS_ENDED);
		}
		// если другая причина
		else
		{
			actor.sendMessage(MessageType.CANCELED_TIME_IS_UP);
			enemy.sendMessage(MessageType.CANCELED_TIME_IS_UP);
		}

		// сохраняем дуэль в пул
		pool.put(this);
	}

	/**
	 * Отмена дуэли.
	 *
	 * @param player игрок, который отменил дуэль.
	 */
	public synchronized void cancel(Player player)
	{
		if(!isRunning())
			return;

		// синхронизируемся на игроке
		synchronized(player)
		{
			// применяем проигрышное хп
			player.setCurrentHp(1);
			// завершаем дуэль
			finish();
		}
	}

	@Override
	public void finalyze()
	{
		actor = null;
		enemy = null;
	}

	/**
	 * Завершение дуэли.
	 */
	public synchronized void finish()
	{
		if(!isRunning())
			return;

		// инициатор
		Player actor = getActor();
		// опонент
		Player enemy = getEnemy();

		// зануляем дуэль
		actor.setDuel(null);
		enemy.setDuel(null);

		// обновляем друг другу цвета ников
		actor.updateColor(enemy);
		enemy.updateColor(actor);

		// удаляем хп над головой друг другу
		PacketManager.cancelTargetHp(actor, enemy);
		PacketManager.cancelTargetHp(enemy, actor);

		// смотри сумон инициатора
		Summon summon = actor.getSummon();

		// если суммон есть
		if(summon != null)
			// отменяем его атаку
			summon.getAI().abortAttack();

		// смотрим суммон опонента
		summon = enemy.getSummon();

		// если суммон есть
		if(summon != null)
			// отменяем его атаку
			summon.getAI().abortAttack();

		// создаем пакет с инфой кто проиграл и победил
		SystemMessage packet = SystemMessage.getInstance(MessageType.WINNER_DEFEATED_LOSER_IN_A_DUEL);

		// если инициатор победил
		if(actor.getCurrentHp() > enemy.getCurrentHp())
		{
			// указываем имя победителя
			packet.addWinner(actor.getName());
			// указываем имя проигравшего
			packet.addLoser(enemy.getName());

			// отправляем сообщения о дуэли
			actor.sendMessage(MessageType.DUEL_WON);
			enemy.sendMessage(MessageType.DUEL_LOST);
		}
		else
		{
			// указываем имя победителя
			packet.addWinner(enemy.getName());
			// указываем имя проигравшего
			packet.addLoser(actor.getName());

			// отправляем сообщения о дуэли
			actor.sendMessage(MessageType.DUEL_LOST);
			enemy.sendMessage(MessageType.DUEL_WON);
		}

		// увеличиваем счетчик отправок
		packet.increaseSends();
		packet.increaseSends();

		// ложим на отправку
		actor.sendPacket(packet, false);
		enemy.sendPacket(packet, false);

		// останавливаем
		setRunning(false);

		// восстанавливаем состояние инициатора
		actorSave.restore(actor);
		// восстанавливаем состояние опонента
		enemySave.restore(enemy);

		// сохраняем в пул
		pool.put(this);
	}

	/**
	 * @return инициатор дуэли.
	 */
	public Player getActor()
	{
		return actor;
	}

	/**
	 * @return опонент инициатора дуэли.
	 */
	public Player getEnemy()
	{
		return enemy;
	}

	/**
	 * @param player участник дуэли.
	 * @return соперник дуэли.
	 */
	public Player getEnemy(Player player)
	{
		if(player == actor)
			return enemy;
		else if(player == enemy)
			return actor;

		return null;
	}

	/**
	 * @return началась ли дуэль.
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * @return запущен ли дуэль.
	 */
	public boolean isRunning()
	{
		return running;
	}

	@Override
	public void reinit()
	{
		active = false;
	}

	@Override
	protected synchronized void runImpl()
	{
		if(!isRunning())
			return;

		// инициатор
		Player actor = getActor();
		// опонент
		Player enemy = getEnemy();

		if(actor.getDuel() != this || enemy.getDuel() != this)
			log.warning("incorrect work duel.");

		// если дуэль активна
		if(isActive())
			// прерываем дуэль
			cancel(false, false);
		else
		{
			// активируем
			setActive(true);

			// обновляем друг другу цвета ников
			actor.updateColor(enemy);
			enemy.updateColor(actor);

			// отображаем хп над головой друг другу
			PacketManager.showTargetHp(actor, enemy);
			PacketManager.showTargetHp(enemy, actor);

			// сохраняем состояние инициатора
			actorSave.save(actor);
			// сохраняем состояние опонента
			enemySave.save(enemy);

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// создаем таск
			schedule = executor.scheduleGeneral(this, 300000);
		}
	}

	/**
	 * @param active активный ли бой.
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}

	/**
	 * @param actor инициатор дуэли.
	 */
	public void setActor(Player actor)
	{
		this.actor = actor;
	}

	/**
	 * @param enemy опонент инициатора дуэли.
	 */
	public void setEnemy(Player enemy)
	{
		this.enemy = enemy;
	}

	/**
	 * @param running запущен ли дуэль.
	 */
	public void setRunning(boolean running)
	{
		this.running = running;
	}

	/**
	 * Обновление дуэли.
	 *
	 * @param skill ударяемый скил.
	 * @param info инфа об атакею
	 * @param attacker атакаующий персонаж.
	 * @param attacked атакуемый персонаж.
	 * @return прервать ли дальше обработку удара.
	 */
	public boolean update(Skill skill, AttackInfo info, Character attacker, Character attacked)
	{
		// получаем дуэль атакующего
		Duel duel = attacker.getDuel();

		// если он не в дуэли либо в другой дуэли
		if(!attacker.isSummon() && (duel == null || duel != this))
			// прерываем дуэль
			cancel(true, false);
		// если он в этой же дуэли
		else if(duel != null && duel == this)
		{
			// и удар не заблокирован и привышает хп игрока
			if(!info.isBlocked() && info.getDamage() >= attacked.getCurrentHp() - 1)
			{
				// обнуляем хп игрока
				attacked.setCurrentHp(1);
				// завершаем дуэль
				finish();
				// выходим
				return true;
			}
		}
		// если это сумон
		else if(attacker.isSummon())
		{
			// получаем владельца сумона
			Character owner = attacker.getOwner();

			// если владельца нет либо он не из этой дуэли
			if(owner == null || owner.getDuel() != this)
				// дуэль обрываем
				duel.cancel(true, false);
			// если сумон добил игрока
			else if(!info.isBlocked() && info.getDamage() >= attacked.getCurrentHp() - 1)
			{
				// обнуляем хп игрока
				attacked.setCurrentHp(1);
				// завершаем дуэль
				finish();
				// выходим
				return true;
			}
		}

		return false;
	}
}
