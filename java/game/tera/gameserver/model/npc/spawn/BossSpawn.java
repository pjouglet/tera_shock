package tera.gameserver.model.npc.spawn;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.BossSpawnManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Модель спавна РБ.
 *
 * @author Ronn
 */
public class BossSpawn extends NpcSpawn
{
	/** время респавна */
	private long respawnDate;

	public BossSpawn(Node node, VarTable vars, NpcTemplate template, Location location, int respawn, int random, int minRadius, int maxRadius, ConfigAI config, NpcAIClass aiClass)
	{
		super(node, vars, template, location, respawn, random, minRadius, maxRadius, config, aiClass);

		// получаем менеджер спавна боссов
		BossSpawnManager spawnManager = BossSpawnManager.getInstance();

		// вносим в список спавнов РБ
		if(!spawnManager.addSpawn(this))
			throw new IllegalArgumentException("found duplicate boss spawn");

		// устанавливаем время респа
		setRespawnDate(spawnManager.getSpawn(getTemplate()));
	}

	@Override
	public synchronized void doDie(Npc npc)
	{
		// время для респавна
		int time = 0;

		// получаем рандомный бонус
		int randomTime = getRandomTime();

		// получаем время респа
		int respawnTime = getRespawnTime();

		// если время респа статичное
		if(randomTime == 0)
			// создаем таск со статичным временем
			time = respawnTime * 1000;
		else
			// создаем таск с рандом временем
			time = getRandom().nextInt(Math.max(0, respawnTime - randomTime), respawnTime + randomTime) * 1000;

		// запоминаем время респа
		setRespawnDate(System.currentTimeMillis() + time);

		// получаем менеджер спавна боссов
		BossSpawnManager spawnManager = BossSpawnManager.getInstance();

		// обновляем спавн этого босса
		spawnManager.updateSpawn(getTemplate(), getRespawnTime());

		super.doDie(npc);
	}

	/**
	 * Запуск респавна.
	 */
	public synchronized void doRespawn()
	{
		// если остановлен, не респавним
		if(isStoped())
			return;

		// если уже идет таск респавна, выходим
		if(schedule != null)
			return;

		// получаем текщее время
		long current = System.currentTimeMillis();

		// получаем время респа
		long respawnDate = getRespawnDate();

		// если время респа отсутствует или уже пройдено, то сразу спавним
		if(respawnDate == -1 || respawnDate < current)
		{
			doSpawn();
			return;
		}

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем таск респавна
		schedule = executor.scheduleGeneral(this, respawnDate - current);
	}

	/**
	 * @return время респавна.
	 */
	public long getRespawnDate()
	{
		return respawnDate;
	}

	/**
	 * @param время респавна.
	 */
	public void setRespawnDate(long respawnDate)
	{
		this.respawnDate = respawnDate;
	}

	@Override
	public void start()
	{
		// уберам флаг остановки спавна
		setStoped(false);

		// запускаем респ
		doRespawn();
	}
}