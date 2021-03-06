package misc;

public class MutableInt extends Number implements Comparable<Number> {
	private static final long serialVersionUID = 1L;
	public int v;

	public MutableInt(int v) {
		this.v = v;
	}

	@Override
	public int compareTo(Number o) {
		return v;
	}

	@Override
	public int intValue() {
		return v;
	}

	@Override
	public long longValue() {
		return v;
	}

	@Override
	public float floatValue() {
		return v;
	}

	@Override
	public double doubleValue() {
		return v;
	}

	@Override
	public String toString() {
		return String.valueOf(v);
	}
}