package tera.gameserver.network.crypt;

/**
 * Модель ключа криптора.
 */
public final class CryptKey
{
	/** размер ключа */
    protected int size;
    /** первая позиция */
    protected int firstPos;
    /** вторая позиция */
    protected int secondPos;
    /** максимальнаяпозиция */
    protected int maxPos;
    /** ключ */
    protected int key;

    /** буффер */
    protected byte[] buffer;

    /** сумма */
    protected long sum;

    protected CryptKey(int pos, int size)
	{
        this.secondPos = pos;
        this.maxPos = pos;
        this.size = size;
        this.buffer = new byte[size * 4];
    }

    /**
     * @return буффер.
     */
    public byte[] getBuffer()
	{
		return buffer;
	}

    /**
     * @return первая позиция.
     */
    public int getFirstPos()
	{
		return firstPos;
	}

    /**
     * @return ключ.
     */
    public int getKey()
	{
		return key;
	}

    /**
     * @return вторая позиция.
     */
    public int getSecondPos()
	{
		return secondPos;
	}

    /**
     * @return размер.
     */
    public int getSize()
	{
		return size;
	}

    /**
     * @return сумма.
     */
    public long getSum()
	{
		return sum;
	}

    /**
     * @param buffer буффер.
     */
    public void setBuffer(byte[] buffer)
	{
		this.buffer = buffer;
	}

    /**
     * @param firstPos первая позиция.
     */
    public void setFirstPos(int firstPos)
	{
		this.firstPos = firstPos;
	}

    /**
     * @param key ключ.
     */
    public void setKey(int key)
	{
		this.key = key;
	}

    /**
     * @param secondPos вторая позиция.
     */
    public void setSecondPos(int secondPos)
	{
		this.secondPos = secondPos;
	}

    /**
     * @param sum сумма.
     */
    public void setSum(long sum)
	{
		this.sum = sum;
	}
}
