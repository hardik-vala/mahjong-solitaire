package mahjong;

/** A single tile within the game.
 * Tiles are unique only by their group value and subindex within that group---there should
 * never be two tiles with the same group and subindex.
 */
public class Tile implements Comparable<Tile> {
	/** Group value. */
	private int value;
	/** Index within the group. */
	private int subindex;

	/** Position in tile array. */
	private int z;
	/** Position in tile array. */
	private int y;
	/** Position in tile array. */
	private int x;

	/** Tile constructor.
	 * @param val group value.
	 * @param index sub-index within the group.
	 */
	public Tile(int val, int index) {
		value = val;
		subindex = index;
	}

	/** Set location in the board.
	 * @param z1 z-coord.
	 * @param y1 y-coord.
	 * @param x1 x-coord.
	 */
	public void setCoord(int z1, int y1, int x1) {
		z = z1;
		y = y1;
		x = x1;
	}

	/** Getter.
	 * @return z-coord.
	 */
	public int getZ() {
		return z;
	}

	/** Getter.
	 * @return y-coord.
	 */
	public int getY() {
		return y;
	}

	/** Getter.
	 * @return x-coord.
	 */
	public int getX() {
		return x;
	}

	/** Getter.
	 * @return the group value
	 */
	public int getValue() {
		return value;
	}

	/** Setter.
	 * @param v value to set
	 */
	public void setValue(int v) {
		value = v;
	}

	/** Getter.
	 * @return the subindex
	 */
	public int getSubindex() {
		return subindex;
	}

	/////////////////////////////
	public void setSubindex(int s) {
		subindex = s;
	}

	/** Interface method.
	 * @param t The Tile to compare to.
	 * @return -1, 0, or 1.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Tile t) {
		if (value < t.value || (value == t.value && subindex < t.subindex)) {
			return -1;
		}
		if (value > t.value || (value == t.value && subindex > t.subindex)) {
			return 1;
		}
		return 0;
	}

	/** Checks if this tile is a matching tile for the given one.
	 * @param t Tile to check.
	 * @return true if this tile matches the input, false otherwise.
	 */
	public boolean matches(Tile t) {
		return (t.value == value);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Tile t = (Tile) obj;
			if (value == t.value && subindex == t.subindex) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value << 2 + subindex;
	}

	public String toString() {
		return "Z-coordinate: " + getZ() + "\n"
			 + "Y-coordinate: " + getY() + "\n"
			 + "X-coordinate: " + getX() + "\n"
			 + "Value: " + getValue() + "\n"
			 + "Subindex: " + getSubindex() + "\n";
	}

}
