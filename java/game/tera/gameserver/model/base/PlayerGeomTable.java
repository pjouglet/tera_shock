package tera.gameserver.model.base;

/**
 * Таблицп размеров моделей игроков.
 *
 * @author Ronn
 * @created 20.03.2012
 */
public abstract class PlayerGeomTable
{
	/** таблица роста */
	private static final float[][] tableHeight = 
	{
		//HUMAN
		{45F, 45F},
		//ELF
		{45F, 45F},
		//AMAN
		{50F, 50F},
		//CASTANIC
		{45F, 45F},
		//POPORI
		{33F, 33F},
		//BARAKA
		{55F, 55F},
	};
	
	/** таблица ширины */
	private static final float[][] tableRadius = 
	{
		//HUMAN
		{10F, 10F},
		//ELF
		{10F, 10F},
		//AMAN
		{15F, 10F},
		//CASTANIC
		{10F, 10F},
		//POPORI
		{13F, 8F},
		//BARAKA
		{20F, 20F},
	};
	
	/**
	 * Высота модели указанной расы и пола.
	 * 
	 * @param race ид расы.
	 * @param sex пол.
	 * @return высота.
	 */
	public static float getHeight(int race, int sex)
	{
		return tableHeight[race][sex];
	}
	
	/**
	 * Радиус модели указанной расы и пола.
	 * 
	 * @param race ид расы.
	 * @param sex пол.
	 * @return радиус.
	 */
	public static float getRadius(int race, int sex)
	{
		return tableRadius[race][sex];
	}
}
