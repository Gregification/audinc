package FileMetadata;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.function.Function;

import presentables.CheckedBiFunction;

/*
 * reference -> https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFilePermissions.html
 * PosixFilePermission reference -> https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFilePermission.html
 */

public enum POSIXFileAttribute implements EnumFileAttribute{
		
	;
	
	private String 
	title,
	description;
	private Function<PosixFileAttributes, Object> fetcher;
	private CheckedBiFunction<Path, Object, Object, Exception> setter;
	
	public Object fetch(PosixFileAttributes view) {
		return fetcher.apply(view);
	}
	
	public Object set(Path path, Object value) throws Exception {
		return setter.apply(path, value);
	}
	
	private POSIXFileAttribute(
				Function<PosixFileAttributes, Object> fetcher,
				CheckedBiFunction<Path, Object, Object, Exception> setter,
				String title,
				String desc
			){
		
		this.fetcher = fetcher;
		this.setter = setter;
		this.title = title;
		this.description = desc;
	}
	private POSIXFileAttribute(
				Function<PosixFileAttributes, Object> fetcher,
				CheckedBiFunction<Path, Object, Object, Exception> setter,
				String desc
			){
		this(fetcher, setter, "", desc);
		title = this.name().toLowerCase().replace('_', ' ');
	}	
	
	@Override public String getTitle() { 		return title;	}
	@Override public String getDescription() {	return description; }
	@Override public boolean isEditable() { 	return setter == null; }
}
