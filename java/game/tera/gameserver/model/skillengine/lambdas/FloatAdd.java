package tera.gameserver.model.skillengine.lambdas;

/**
 * Прибавляющий вещественный модификатор.
 *
 * @author Ronn
 * @created 28.02.2012
 */
public final class FloatAdd extends LambdaFloat
{
	public FloatAdd(float value)
	{
		super(value);
	}

	@Override
	public float calc(float val)
	{
		return val + value;
	}
}
