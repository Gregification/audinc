package DOMViewer.nodeObjects;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/*
 * a raw text container
 */
public class LC_text extends FSLContent<String> {
	protected String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		content.setBorder(BorderFactory.createTitledBorder(
		         BorderFactory.createEtchedBorder(), "My Demo Table", TitledBorder.LEFT, TitledBorder.TOP));
	}

	public LC_text(String text) {
		super(text);
	}

	@Override public void initContent() {
		content = new JTextArea(value);
		
	}
	
//////////////////
//getters and setters
//////////////////
	public String getText() {
		return value;
	}

	public void setText(String text) {
		this.value = text;
	}

	@Override public void setIsEditable(Boolean isEditable) {
		this.isEditable = isEditable;
		((JTextArea)content).setEditable(isEditable);
	}
}
