package FileMetadata;

/*
 * see this for more attributes to add -> https://docs.oracle.com/javase/tutorial/essential/io/fileAttr.html#posix
 */
public interface EnumFileAttribute {
//	public EnumSet<? extends EnumFileAttribute> getValues();
	public String getTitle();
	public String getDescription();
	public boolean isEditable();
}
