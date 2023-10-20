package DOMViewer;

import java.util.EnumSet;

public interface PopupOptionable {
	public abstract PopupOptionable getChildOf();
	public abstract String getTitle();
	public abstract String getTooltipText();
	public abstract EnumSet<? extends PopupFilterable> getDisplayFlags();
	public abstract Enum<? extends PopupOptionable>[] getValues();
}
