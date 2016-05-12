package tera.gameserver.model.geom;

/**
 * Интерфейс для реализации геометрии персонажа.
 * 
 * @author Ronn
 */
public interface Geom
{
	/**
	 * @return высота модели.
	 */
	public float getHeight();
	
	/**
	 * @return радиус модели.
	 */
	public float getRadius();
	
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
	public boolean isHit(float attackerX, float attackerY, float attackerZ, float attackerHeight, float attackerRadius);
	
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
	public boolean isHit(float startX, float startY, float startZ, float endX, float endY, float endZ, float attackerRadius, boolean checkHeight);
}
