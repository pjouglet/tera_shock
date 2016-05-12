package tera.gameserver.model.territory;

import java.util.concurrent.ScheduledFuture;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Bonfire;
import tera.gameserver.model.TObject;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.stat.MathFunc;
import tera.gameserver.model.skillengine.lambdas.FloatMul;
import tera.gameserver.tables.BonfireTable;

/**
 * Модель территории костра.
 *
 * @author Ronn
 */
public class BonfireTerritory extends AbstractTerritory implements Runnable, Bonfire
{
	/** добавляемая функция при входе в зону костра */
	private static final Func func = new MathFunc(StatType.REGEN_HP, 0x30, null, new FloatMul(3));

	/** ссылка на таск регена сердец */
	private volatile ScheduledFuture<BonfireTerritory> task;

	/** координаты центра костра */
	private float centerX;
	private float centerY;
	private float centerZ;

	public BonfireTerritory(Node node, TerritoryType type)
	{
		super(node, type);

		try
		{
			// созаем твблизу атрибутов
			VarTable vars = VarTable.newInstance(node);

			this.centerX = vars.getFloat("centerX", (minimumX + ((maximumX - maximumX) / 2)));
			this.centerY = vars.getFloat("centerY", (minimumY + ((maximumY - maximumY) / 2)));
			this.centerZ = vars.getFloat("centerZ", (minimumZ + ((maximumZ - maximumZ) / 2)));

			// добавляемся в таблицу костров
			BonfireTable.addBonfire(this);
		}
		catch(Exception e)
		{
			log.warning(e);
			throw e;
		}
	}

	/**
	 * @return точка центра костра.
	 */
	public final float getCenterX()
	{
		return centerX;
	}

	/**
	 * @return точка центра костра.
	 */
	public final float getCenterY()
	{
		return centerY;
	}

	/**
	 * @return точка центра костра.
	 */
	public final float getCenterZ()
	{
		return centerZ;
	}

	@Override
	public void onEnter(TObject object)
	{
		super.onEnter(object);

		// если вошедший объект не игрок, то выходим
		if(!object.isPlayer())
			return;

		// получаем игрока из объекта
		Player player = object.getPlayer();

		// если игрок уже обрабатывается другим костром, то выхоим
		if(!player.addBonfire(this))
			return;

		// добавляем функцию регена игроку
		func.addFuncTo(player);

		// если таска на реген небыло, создаем
		if(task == null)
		{
			synchronized(this)
			{
				if(task == null)
				{
					// получаем исполнительного менеджера
					ExecutorManager executor = ExecutorManager.getInstance();

					task = executor.scheduleGeneralAtFixedRate(this, 3000, 3000);
				}
			}
		}
	}

	@Override
	public void onExit(TObject object)
	{
		super.onExit(object);

		// если объект не игрок, то выходим
		if(!object.isPlayer())
			return;

		// получаем игрока с объекта
		Player player = object.getPlayer();

		// удаляем функцию регена
		func.removeFuncTo(player);
		// удаляемся из обрабатываемых костров
		player.removeBonfire(this);

		// если игроков больше нету и таск еще работает
		if(objects.isEmpty() && task != null)
		{
			synchronized(this)
			{
				if(objects.isEmpty() && task != null)
				{
					// останавливаем таск
					task.cancel(false);
					task = null;
				}
			}
		}
	}

	@Override
	public void run()
	{
		try
		{
			// если объектов нет, выходим
			if(objects.isEmpty())
				return;

			objects.readLock();
			try
			{
				TObject[] array = objects.array();

				// перебираем объекты
				for(int i = 0, length = objects.size(); i < length; i++)
				{
					// получаем игрока с объекта
					Player player = array[i].getPlayer();

					// если игрок есть
					if(player != null)
						// восстанавливаем 1 стамину
						player.addStamina();
				}
			}
			finally
			{
				objects.readUnlock();
			}
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}
}
