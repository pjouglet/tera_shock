package tera.gameserver.model.skillengine.lambdas;

/**
 * Делящий вещественный модификатор.
 *
 * @author Кonn
 * @created 28.02.2012
 */
public final class FloatDiv extends LambdaFloat
{
	public FloatDiv(float value)
	{
		super(value);
	}

	@Override
	public float calc(float val)
	{
		return val / value;
	}
}
