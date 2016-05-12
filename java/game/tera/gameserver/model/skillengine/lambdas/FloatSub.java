package tera.gameserver.model.skillengine.lambdas;

/**
 * Отнимающий вещественный модификатор.
 *
 * @author Ronn
 * @created 28.02.2012
 */
public final class FloatSub extends LambdaFloat
{
	public FloatSub(float value)
	{
		super(value);
	}

	@Override
	public float calc(float val)
	{
		return val - value;
	}
}
