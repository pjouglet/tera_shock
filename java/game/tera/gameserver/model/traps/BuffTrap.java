package tera.gameserver.model.traps;

import rlib.geom.Coords;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.Party;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.Skill;
import tera.util.LocalObjects;

/**
 * Модель баф ауры.
 *
 * @author Ronn
 */
public class BuffTrap extends Trap
{
	private static final FoldablePool<BuffTrap> pool = Pools.newConcurrentFoldablePool(BuffTrap.class);

	/**
	 * Создание новой ловукши.
	 *
	 * @param owner создатель и владелец.
	 * @param spawnSkill скил, который спавнит ауру.
	 * @param skill скил, который создает.
	 * @param lifeTime время жизни.
	 * @param radius ражиус активации.
	 * @return новая ловука.
	 */
	public static Trap newInstance(Character owner, Skill spawnSkill, Skill skill, int lifeTime, int radius)
	{
		// извлекаем баф ловушку
		BuffTrap trap = pool.take();

		// если такой нет
		if(trap == null)
		{
			// получаем фабрику ид
			IdFactory idFactory = IdFactory.getInstance();

			// создаем новую
			trap = new BuffTrap(idFactory.getNextTrapId());
		}

		// устанавливаем ид континента
		trap.setContinentId(owner.getContinentId());

		// спавним
		trap.spawnMe(spawnSkill, owner, skill, lifeTime, radius);

		return trap;
	}

	/** список персов, на которых применился */
	private final Array<Character> appled;

	public BuffTrap(int objectId)
	{
		super(objectId);

		this.appled = Arrays.toArray(Character.class);
	}

	@Override
	public boolean activate(TObject object)
	{
		// если объект не персонаж, выходим
		if(!object.isCharacter())
			return false;

		// владелец ауры
		Character owner = getOwner();

		// вошедший персонаж
		Character target = (Character) object;

		// если что-тоиз этого нулевое ,выходим
		if(owner == null || target == null)
			return false;

		// определяем дистанцию цели до цента
		float dist = target.getGeomDistance(x, y);

		// если он не входит в зону либо уже был пробафан, выходим
		if(dist > radius || appled.contains(target))
			return false;

		// флаг, нужно ли его бафать
		boolean active = owner == target;

		// если еще не нужно, то смотрим является ли он соппартийцем
		if(!active)
		{
			// пати владельца
			Party party = owner.getParty();
			// если он в пати вместе сцелью
			if(party != null && target.getParty() == party)
				// активируем баф
				active = true;
		}

		// если всетаки не активируем, выходим
		if(!active)
			return false;

		// применяем баф
		skill.applySkill(owner, target);

		// добавляем цель в уже пробафанные персы
		appled.add(target);

		return false;
	}

	@Override
	public void finalyze()
	{
		super.finalyze();

		// очищаем список пробафанных
		appled.clear();
	}

	@Override
	public void fold()
	{
		pool.put(this);
	}

	/**
	 * Спавн аура хила.
	 */
	public void spawnMe(Skill spawnSkill, Character owner, Skill skill, int lifeTime, int radius)
	{
		this.owner = owner;
		this.skill = skill;
		this.radius = radius;

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		this.lifeTask = executor.scheduleGeneral(this, lifeTime * 1000);

		// расчитываем новую точку на нужном удалении
		float newX = Coords.calcX(owner.getX(), spawnSkill.getRange(), owner.getHeading());
		float newY = Coords.calcY(owner.getY(), spawnSkill.getRange(), owner.getHeading());

		// спавним на этом удалении
		spawnMe(newX, newY, owner.getZ(), 0);

		LocalObjects local = LocalObjects.get();

		// получаем набор персонажей в радиусе ловушки
		Array<Character> chars = World.getAround(Character.class, local.getNextCharList(), owner);

		// если такие есть
		if(!chars.isEmpty())
		{
			//  получаем массив персонажей
			Character[] array = chars.array();

			// перебираем
			for(int i = 0, length = chars.size(); i < length; i++)
				// пробуем активировать
				activate(array[i]);
		}
	}
}
