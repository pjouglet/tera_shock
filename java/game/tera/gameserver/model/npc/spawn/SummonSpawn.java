package tera.gameserver.model.npc.spawn;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * @author Ronn
 */
public class SummonSpawn extends SafeTask implements Spawn {

	private static final Logger LOGGER = Loggers.getLogger(SummonSpawn.class);

	/** шаблон сумона */
	private final NpcTemplate template;
	/** место спавна */
	private final Location location;

	/** конфиг АИ суммона */
	private final ConfigAI configAI;
	/** класс АИ суммона */
	private final NpcAIClass aiClass;

	/** время жизни суммона */
	private final int lifeTime;

	/** владелец сумона */
	private volatile Character owner;

	/** отспавненный сумон */
	private volatile Summon spawned;
	/** мертвый сумон */
	private volatile Summon dead;

	/** ссылка на задачу спавна */
	private volatile ScheduledFuture<SummonSpawn> schedule;

	public SummonSpawn(NpcTemplate template, ConfigAI configAI, NpcAIClass aiClass, int lifeTime) {
		this.template = template;
		this.configAI = configAI;
		this.aiClass = aiClass;
		this.lifeTime = lifeTime;
		this.location = new Location();
	}

	@Override
	public void doDie(Npc npc) {
		if(!npc.isSummon()) {
			return;
		}

		setDead((Summon) npc);
		setSpawned(null);
		deSpawn();
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public int getTemplateId() {
		return template.getTemplateId();
	}

	@Override
	public int getTemplateType() {
		return template.getTemplateType();
	}

	@Override
	public void setLocation(Location location) {
		this.location.set(location);
	}

	@Override
	public synchronized void start() {

		Summon spawned = getSpawned();

		if(spawned != null) {
			LOGGER.warning(this, "found duplicate spawn!");
			return;
		}

		Character owner = getOwner();

		if(owner == null) {
			return;
		}

		Location location = getLocation();

		Summon summon = getDead();

		if(summon != null) {
			summon.finishDead();
			summon.reinit();
			summon.setOwner(owner);
			summon.spawnMe(location);
		} else {
			summon = (Summon) template.newInstance();
			summon.setOwner(owner);
			summon.setSpawn(this);
			summon.setAi(aiClass.newInstance(summon, configAI));
			summon.spawnMe(location);
		}

		setDead(null);
		setSpawned(summon);
		owner.setSummon(summon);

		ExecutorManager executorManager = ExecutorManager.getInstance();
		setSchedule(executorManager.scheduleGeneral(this, lifeTime));
	}

	@Override
	public synchronized void stop() {

		ScheduledFuture<SummonSpawn> schedule = getSchedule();

		if(schedule != null) {
			schedule.cancel(true);
			setSchedule(null);
		}

		Summon spawned = getSpawned();

		if(spawned != null) {
			spawned.remove();
		}
	}

	/**
	 * @return владелец сумона.
	 */
	public Character getOwner() {
		return owner;
	}

	/**
	 * @param owner владелец сумона.
	 */
	public void setOwner(Character owner) {
		this.owner = owner;
	}

	/**
	 * @param dead мертвый сумон.
	 */
	public void setDead(Summon dead) {
		this.dead = dead;
	}

	/**
	 * @param spawned отспавненный суммон.
	 */
	public void setSpawned(Summon spawned) {
		this.spawned = spawned;
	}

	/**
	 * @return отспавненный суммон.
	 */
	public Summon getSpawned() {
		return spawned;
	}

	public Summon getDead() {
		return dead;
	}

	@Override
	protected void runImpl() {
		deSpawn();
	}

	/**
	 * Деспавн суммона.
	 */
	private synchronized void deSpawn() {

		Summon spawned = getSpawned();

		if(spawned != null) {
			spawned.remove();
		}

		ScheduledFuture<SummonSpawn> schedule = getSchedule();

		if(schedule != null) {
			schedule.cancel(true);
			setSchedule(null);
		}
	}

	/**
	 * @returnс сылка на задачу деспавна сумона.
	 */
	public ScheduledFuture<SummonSpawn> getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule ссылка на задачу деспавна сумона.
	 */
	public void setSchedule(ScheduledFuture<SummonSpawn> schedule) {
		this.schedule = schedule;
	}

	@Override
	public Location[] getRoute() {
		return null;
	}
}
