package tera.gameserver.model.skillengine.lambdas;

/**
 * Вещественный модификатор.
 *
 * @author Ronn
 * @created 28.02.2012
 */
public abstract class LambdaFloat implements Lambda
{
	/** значение модификатора */
	protected float value;

	public LambdaFloat(float value)
	{
		super();
		
		this.value = value;
	}
	
	@Override
	public Object getValue()
	{
		return value;
	}
}
