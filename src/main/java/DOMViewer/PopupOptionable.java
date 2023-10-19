package DOMViewer;

import java.util.EnumSet;

public interface PopupOptionable {
	public PopupOptionable getChildOf();
	public abstract String getTooltipText();
	public abstract EnumSet<? extends PopupFilterable> getDisplayFlags();
}
