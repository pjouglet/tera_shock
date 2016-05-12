package tera.gameserver.model.geom;

import tera.gameserver.model.Character;

/**
 * Фундаментальная модель геометрии персонажа.
 *
 * @author Ronn
 */
public abstract class AbstractGeom<E extends Character> implements Geom
{
	/** персонаж */
	protected E character;

	/** радиус модели */
	protected float radius;
	/** высота модели */
	protected float height;

	/**
	 * @param character персонаж.
	 * @param height высота модели.
	 * @param radius радиус модели.
	 */
	public AbstractGeom(E character, float height, float radius)
	{
		this.character = character;
		this.height = height;
		this.radius = radius;
	}

	/**
	 * @return владелец геометрии.
	 */
	protected E getCharacter()
	{
		return character;
	}

	@Override
	public float getHeight()
	{
		return height;
	}

	@Override
	public float getRadius()
	{
		return radius;
	}
}
