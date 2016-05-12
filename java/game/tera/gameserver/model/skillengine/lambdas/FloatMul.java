package tera.gameserver.model.skillengine.lambdas;

/**
 * Умножающий вещественный модификатор.
 *
 * @author Ronn
 * @created 28.02.2012
 */
public final class FloatMul extends LambdaFloat
{
	public FloatMul(float value)
	{
		super(value);
	}

	@Override
	public float calc(float val)
	{
		return val * value;
	}
}
