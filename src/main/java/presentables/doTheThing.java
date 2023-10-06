package presentables;

import presentables.custom_doTheThingIfNotNull;

public class doTheThing {
	public String title = "default doTheThing title";
	public custom_doTheThingIfNotNull<Object> task;

	public void doTheThing(String title, custom_doTheThingIfNotNull<Object> task) {
		this.title = title;
		this.task = task;
	}
	
	public void doo(Object thing) {
		task.doTheThing(thing);
	}
	
	@Override public String toString() {
		return title;
	}
}
