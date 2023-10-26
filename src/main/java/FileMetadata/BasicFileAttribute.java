package FileMetadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.function.Function;

import presentables.CheckedBiFunction;
/*
 * value reference -> https://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/BasicFileAttributes.html
 */
public enum BasicFileAttribute implements EnumFileAttribute {
		CREATION_TIME		(BasicFileAttributes::creationTime,
							null,
							"the logged 'creation' time"),
		LAST_ACCESS_TIME	(BasicFileAttributes::lastAccessTime,
							null,
							"the logged 'last accessed' time"),
		LAST_MODIFIED_TIME	(BasicFileAttributes::lastModifiedTime,
							(p, v) -> {
								Files.setLastModifiedTime(p, (FileTime)v);
								return null;
							},
							"the logged 'last modified' time"),
		SIZE				(BasicFileAttributes::size,
							null,
							"file size in bytes"),
		IS_DIRECTORY		(BasicFileAttributes::isDirectory,
							null,
							"take a guess"),
		IS_OTHER			(BasicFileAttributes::isOther,
							null,
							"Tells whether the file is something other than a regular file, directory, or symbolic link."),
		IS_REGULAR_FILE		(BasicFileAttributes::isRegularFile,
							null,
							"Tells weather the file is a regular file with opaque content."),
		IS_SYMBOLIC_LINK	(BasicFileAttributes::isSymbolicLink,
							null,
							"Tells weather the file is a symbolic link.")
	;
	
	private String 
		title,
		description;
	private Function<BasicFileAttributes, Object> fetcher;
	private CheckedBiFunction<Path, Object, Object, Exception> setter;
	
	public Object fetch(BasicFileAttributes view) {
		return fetcher.apply(view);
	}
	
	public Object set(Path path, Object value) throws Exception{
		return setter.apply(path, value);
	}
	
	private BasicFileAttribute(
				Function<BasicFileAttributes, Object> fetcher,
				CheckedBiFunction<Path, Object, Object, Exception> setter,
				String title,
				String desc
			){
		
		this.fetcher = fetcher;
		this.setter = setter;
		this.title = title;
		this.description = desc;
	}
	private BasicFileAttribute(
			Function<BasicFileAttributes, Object> fetcher,
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
