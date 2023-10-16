package DOMViewer;

public record DOMNodeObject(
		String title,
		Object value
		) {
	public String title() {
		return title;
	}

	public Object value() {
		return value;
	}

	@Override public String toString() {
		return title;
	}
}
