package DOMViewer;

import java.util.Map;
import java.util.function.Function;

public record DOMNodeValue(
		Map<Object, Object> attributes,
		Function<DOMNodeValue, ? extends String> nameKey){	//im going to morb. why does no one say anything about the Function interface. all the "custom_"... under presentables is trash now
	
	@Override public String toString() {
		return nameKey.apply(this);
	}
}
