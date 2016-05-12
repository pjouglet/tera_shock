package tera.gameserver.model.geom;

import rlib.geom.Geometry;

import tera.gameserver.model.Character;

/**
 * Модель геометрии нпс.
 *
 * @author Ronn
 */
public final class NpcGeom extends AbstractGeom<Character>
{
	/**
	 * @param character npc.
	 * @param height высота модели.
	 * @param radius радиус модели.
	 */
	public NpcGeom(Character character, float height, float radius)
	{
		super(character, height, radius);
	}

	/**
	 * Рассчет попадания массовыми ударами в геом персонажа.
	 *
	 * @param attackerX точка атаки.
	 * @param attackerY точка атаки.
	 * @param attackerZ точка атаки.
	 * @param attackerHeight высота атаки.
	 * @param attackerRadius радиус атаки.
	 * @return есть ли попадание.
	 */
	public boolean isHit(float attackerX, float attackerY, float attackerZ, float attackerHeight, float attackerRadius)
	{
		return attackerRadius >= character.getDistance(attackerX, attackerY, attackerZ) - radius;
	}

	/**
	 * Определение попадания луча в гео объекта.
	 *
	 * @param startX точка старта луча.
	 * @param startY точка старта луча.
	 * @param startZ точка старта луча.
	 * @param endX точка конца луча.
	 * @param endY точка конца луча.
	 * @param endZ точка конца луча.
	 * @param attackerRadius радиус луча.
	 * @param checkHeight проверять ли высоту.
	 * @return если ли прохождение в геом.
	 */
	public boolean isHit(float startX, float startY, float startZ, float endX, float endY, float endZ, float attackerRadius, boolean checkHeight)
	{
		// получаем персонажа
		Character character = getCharacter();

		// получаем его координаты
		float x = character.getX();
		float y = character.getY();
		float z = character.getZ();

		// определяем базовую дистанцию
		float baseDistance = Geometry.getDistanceToLine(startX, startY, endX, endY, x, y) - attackerRadius;

		if(baseDistance > radius)
			return false;

		if(checkHeight)
		{
			// получаем высоту модели
			float height = getHeight();

			if(Geometry.getDistanceToLine(startX, startY, startZ, endX, endY, endZ, x, y, z) - attackerRadius - baseDistance >= height)
				return false;

			if(Geometry.getDistanceToLine(startX, startY, startZ, endX, endY, endZ, x, y, z + height) - attackerRadius - baseDistance >= height)
				return false;
		}

		return true;
	}
}
