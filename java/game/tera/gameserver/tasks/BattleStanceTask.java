package tera.gameserver.tasks;

import java.util.concurrent.ScheduledFuture;

import rlib.util.SafeTask;
import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.PlayerBattleStance;

/**
 * Модель таска боевой стойки.
 *
 * @author Ronn
 */
public class BattleStanceTask extends SafeTask
{
	/** персонаж */
	private final Character character;

	/** последнее время активации */
	private long last;

	/** сам таск */
	private volatile ScheduledFuture<BattleStanceTask> schedule;

	/**
	 * @param character персонаж, который будет входить в боевуб стойку.
	 */
	public BattleStanceTask(Character character)
	{
		this.character = character;
	}

	/**
	 * @return время последнего обновления.
	 */
	public long getLast()
	{
		return last;
	}

	/**
	 * @return ссылка на таск.
	 */
	public ScheduledFuture<BattleStanceTask> getSchedule()
	{
		return schedule;
	}

	/**
	 * Запуск боевой стойки.
	 */
	public synchronized void now()
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// создаем таск
		schedule = executor.scheduleGeneral(this, Config.WORLD_PLAYER_TIME_BATTLE_STANCE);

		// отправляем пакет
		character.broadcastPacket(PlayerBattleStance.getInstance(character, PlayerBattleStance.STANCE_ON));

		// запоминаем время
		setLast(System.currentTimeMillis());
	}

	@Override
	protected synchronized void runImpl()
	{
		// отстанавливаем боевую стойку
		character.stopBattleStance();
	}

	/**
	 * @param last время последнего обновления.
	 */
	public void setLast(long last)
	{
		this.last = last;
	}

	/**
	 * @param schedule ссылка на таск.
	 */
	public void setSchedule(ScheduledFuture<BattleStanceTask> schedule)
	{
		this.schedule = schedule;
	}

	/**
	 * Остановка таска.
	 */
	public synchronized void stop()
	{
		// получаем текущий таск
		ScheduledFuture<BattleStanceTask> schedule = getSchedule();

		// если он есть
		if(schedule != null)
		{
			// останавливаем
			schedule.cancel(false);
			// зануляем
			setSchedule(null);
		}

		// отправляем пакет
		character.broadcastPacket(PlayerBattleStance.getInstance(character, PlayerBattleStance.STANCE_OFF));
	}

	/**
	 * Обновление времени боевой стойки.
	 */
	public synchronized void update()
	{
		// если не проло и 2 секунду после обновления таска, выходим
		if(System.currentTimeMillis() - getLast() < 2000L)
			return;

		// получаем текущий таск
		ScheduledFuture<BattleStanceTask> schedule = getSchedule();

		// если он есть
		if(schedule != null)
			// останавливаем
			schedule.cancel(false);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// ставим новый таск
		setSchedule(executor.scheduleGeneral(this, Config.WORLD_PLAYER_TIME_BATTLE_STANCE));

		// запоминаем время
		setLast(System.currentTimeMillis());
	}
}
