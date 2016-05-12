package tera.gameserver.model.skillengine.funcs.chance;

import rlib.util.array.Array;
import rlib.util.array.Arrays;

/**
 * Менеджер шансовы функций.
 *
 * @author Ronn
 */
public final class ChanceFuncManager
{
	private static ChanceFuncManager instance;

	public static ChanceFuncManager getInstance()
	{
		if(instance == null)
			instance = new ChanceFuncManager();

		return instance;
	}

	/** список шансовых функций */
	private final Array<ChanceFunc> funcs;

	private ChanceFuncManager()
	{
		this.funcs = Arrays.toArray(ChanceFunc.class);
	}
	/**
	 * @param func новая функция.
	 */
	public void add(ChanceFunc func)
	{
		funcs.add(func);
	}

	/**
	 * Инициализация функций.
	 */
	public void prepare()
	{
		for(ChanceFunc func : funcs)
			func.prepare();
	}
}
