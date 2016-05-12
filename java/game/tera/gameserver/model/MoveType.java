package tera.gameserver.model;

/**
 * Перечисление типов движения.
 *
 * @author Ronn
 */
public enum MoveType
{
	/** бег */
	RUN,

	/** падение после бега */
	RUN_FALL(true, false),

	/** ускореный бег */
	SPRINT,

	NONE3,

	/** прыжок */
	JUMP(false, true),

	NONE5,

	/** остановка */
	STOP,

	/** плавание */
	SWIM_RUN,
	/** павание на месте */
	SWIM_STOP,

	/** падение после прыжка */
	JUMP_FALL(true, true),

	NONE10,

	NONE11,

	NONE12,

	NONE13,
	;

	public static final int count = values().length;

	public static final MoveType[] values = values();

	public static int getId(MoveType type)
	{
		return type == null? 0 : type.ordinal();
	}

	public static MoveType valueOf(int id)
	{
		if(id < 0 || id >= count)
			return RUN;

		return values[id];
	}

	private boolean fall;

	private boolean jump;

	private MoveType(){}

	private MoveType(boolean fall, boolean jump)
	{
		this.fall = fall;
		this.jump = jump;
	}

	public boolean isFall()
	{
		return fall;
	}

	public boolean isJump()
	{
		return jump;
	}
}
