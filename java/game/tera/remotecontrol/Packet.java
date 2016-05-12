package tera.remotecontrol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Пакет
 *
 * @author Ronn
 * @created 26.03.2012
 */
public final class Packet implements Serializable
{
	/** long Packet.java */
	private static final long serialVersionUID = -5515767380967971929L;
	
	/** тип пакета */
	private PacketType type;
	
	/** передоваемая информация */
	private Serializable[] values;
	
	/** позиция в масиве значений */
	private int ordinal;
	
	/**
	 * @param type
	 * @param values
	 */
	public Packet(PacketType type, Serializable...values)
	{
		this.type = type;
		this.values = values;
	}

	/**
	 * @return тип пакета
	 */
	public PacketType getType()
	{
		return type;
	}
	
	/**
	 * @return values
	 */
	public Serializable[] getValues()
	{
		return values;
	}
	
	/**
	 * @return есть ли еще элементы
	 */
	public boolean hasNext()
	{
		return ordinal < values.length;
	}
	
	/**
	 * @return next object
	 */
	public Object next()
	{
		return values[ordinal++];
	}
	
	/**
	 * @param type
	 * @return next T object
	 */
	public <T> T next(Class<T> type)
	{
		return type.cast(values[ordinal++]);
	}
	
	/**
	 * @return next boolean
	 */
	public boolean nextBoolean()
	{
		return (Boolean) values[ordinal++];
	}
	
	/**
	 * @return next double
	 */
	public double nextDouble()
	{
		return (Double) values[ordinal++];
	}
	
	/**
	 * @return next float
	 */
	public float nextFloat()
	{
		return (Float) values[ordinal++];
	}
	
	/**
	 * @return next int
	 */
	public int nextInt()
	{
		return (Integer) values[ordinal++];
	}
	
	/**
	 * @return next long
	 */
	public long nextLong()
	{
		return (Long) values[ordinal++];
	}
	
	/**
	 * @return next string
	 */
	public String nextString()
	{
		return values[ordinal++].toString();
	}
	
	/**
	 * @param values
	 */
	public void setValues(Serializable... values)
	{
		this.values = values;
	}

	@Override
	public String toString()
	{
		return "Packet  " + (type != null ? "type = " + type + ", " : "") + (values != null ? "values = " + Arrays.toString(values) + ", " : "") + "ordinal = " + ordinal;
	}
}
