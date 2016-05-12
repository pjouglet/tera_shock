package tera.gameserver.model.traps;

import java.util.concurrent.ScheduledFuture;

import rlib.geom.Coords;
import rlib.util.array.Array;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.Config;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.CharObjectDelete;
import tera.gameserver.network.serverpackets.TrapInfo;
import tera.util.LocalObjects;

/**
 * Модель ловушки в тере.
 * 
 * @author Ronn
 */
public class Trap extends TObject implements Foldable, Runnable {

	private static final FoldablePool<Trap> pool = Pools.newConcurrentFoldablePool(Trap.class);

	/**
	 * Создание новой ловукши.
	 * 
	 * @param owner создатель и владелец.
	 * @param skill скил, который создает.
	 * @param lifeTime время жизни.
	 * @param radius ражиус активации.
	 * @return новая ловука.
	 */
	public static Trap newInstance(Character owner, Skill skill, int range, int lifeTime, int radius) {

		Trap trap = pool.take();

		if(trap == null) {
			IdFactory idFactory = IdFactory.getInstance();
			trap = new Trap(idFactory.getNextTrapId());
		}

		trap.setContinentId(owner.getContinentId());
		trap.spawnMe(owner, skill, range, lifeTime, radius);

		return trap;
	}

	/** владелец ловушки */
	protected Character owner;

	/** атакующий скил */
	protected Skill skill;

	/** время жизни */
	protected ScheduledFuture<? extends Runnable> lifeTask;

	/** радиус активации */
	protected int radius;

	public Trap(int objectId) {
		super(objectId);
	}

	/**
	 * Обработка активации ловушки.
	 * 
	 * @param object объект, который изменил свое положение.
	 */
	public boolean activate(TObject object) {

		if(!object.isCharacter())
			return false;

		Character owner = getOwner();
		Character target = object.getCharacter();

		if(owner == null || owner == target || !owner.checkTarget(target)) {
			return false;
		}

		float dist = target.getGeomDistance(x, y);

		if(dist > radius) {
			return false;
		}

		if(lifeTask != null) {
			lifeTask.cancel(false);
			lifeTask = null;
		}

		ExecutorManager executor = ExecutorManager.getInstance();
		executor.scheduleGeneral(this, 100);

		return true;
	}

	@Override
	public void addMe(Player player) {
		player.sendPacket(TrapInfo.getInstance(this), true);
	}

	@Override
	public synchronized void deleteMe() {

		if(deleted) {
			return;
		}

		super.deleteMe();

		fold();
	}

	@Override
	public void finalyze() {
		this.owner = null;
		this.skill = null;
		this.lifeTask = null;
	}

	/**
	 * Складировать.
	 */
	public void fold() {
		pool.put(this);
	}

	/**
	 * @return владелец ловушки.
	 */
	public Character getOwner() {
		return owner;
	}

	/**
	 * @return атакующий скил.
	 */
	public Skill getSkill() {
		return skill;
	}

	@Override
	public int getSubId() {
		return Config.SERVER_TRAP_SUB_ID;
	}

	/**
	 * @return ид темплейта ловушки.
	 */
	@Override
	public int getTemplateId() {
		return skill != null ? skill.getIconId() : 0;
	}

	@Override
	public Trap getTrap() {
		return this;
	}

	@Override
	public boolean isTrap() {
		return true;
	}

	@Override
	public void reinit() {
		IdFactory idFactory = IdFactory.getInstance();
		this.objectId = idFactory.getNextTrapId();
	}

	@Override
	public void removeMe(Player player, int type) {
		player.sendPacket(CharObjectDelete.getInstance(this), true);
	}

	@Override
	public void run() {

		Skill skill = getSkill();

		if(skill != null && lifeTask == null)
			skill.useSkill(owner, x, y, z);

		deleteMe();
	}

	/**
	 * Спавн в мир ловушку.
	 * 
	 * @param owner владелец.
	 * @param skill скил ловушки.
	 * @param lifeTime время жизни.
	 * @param radius радиус активации.
	 */
	public void spawnMe(Character owner, Skill skill, int range, int lifeTime, int radius) {
		this.owner = owner;
		this.skill = skill;
		this.radius = radius;

		spawnMe(Coords.calcX(owner.getX(), range, owner.getHeading()), Coords.calcY(owner.getY(), range, owner.getHeading()), owner.getZ(), 0);

		LocalObjects local = LocalObjects.get();

		Array<Character> chars = World.getAround(Character.class, local.getNextCharList(), this, radius);

		ExecutorManager executor = ExecutorManager.getInstance();

		if(chars.isEmpty())
			this.lifeTask = executor.scheduleGeneral(this, lifeTime * 1000);
		else {

			Character[] array = chars.array();

			for(int i = 0, length = chars.size(); i < length; i++) {

				Character target = array[i];

				if(owner.checkTarget(target)) {
					executor.scheduleGeneral(this, 100);
					return;
				}
			}

			this.lifeTask = executor.scheduleGeneral(this, lifeTime * 1000);
		}
	}
}
