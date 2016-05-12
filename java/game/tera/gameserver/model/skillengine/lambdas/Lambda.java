package tera.gameserver.model.skillengine.lambdas;

/**
 * Интерфейс для реализации модификаторов статов.
 * 
 * @author Ronn
 */
public interface Lambda
{
	/**
	 * @param val исходное значение.
	 * @return конечное значение.
	 */
	public float calc(float val);

	/**
	 * @return модификатор.
	 */
	public Object getValue();
}
