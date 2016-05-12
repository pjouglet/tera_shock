package tera.gameserver.model.skillengine.lambdas;

/**
 * Устанавливающий вещественный модификатор.
 *
 * @author Ronn
 * @created 28.02.2012
 */
public final class FloatSet extends LambdaFloat
{
	public FloatSet(float value)
	{
		super(value);
	}

	@Override
	public float calc(float val)
	{
		return value;
	}
}
