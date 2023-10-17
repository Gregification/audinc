//package DOMViewer;
//
//import java.nio.file.Path;
//
//public record DOMNodeObject(
//		String title,
//		Object value,
//		int flags
//		) {
//	
//	public static int 
//		FLAG_IS_LEAF = 1,			//if it is a terminating file in the file system
//		FLAG_IS_CONTENT = 1 << 1;	//if it is a component element of a leaf
//	
//	
//	public DOMNodeObject(String title, Object value) {
//		this(title, value, genFlags(value));
//	}
//	
//	@Override public String toString() {
//		return title;
//	}
//	
//	public static int genFlags(Object obj) {
//		int flag = 0;
//		if(obj instanceof Path) {
//			var file = ((Path)obj).toFile();
//			if(file.exists() && file.isFile()) {
//				flag |= DOMNodeObject.FLAG_IS_LEAF;
//			}
//		}
//		return flag;
//	}
//	
//	public boolean isContent() {
//		return (flags & DOMNodeObject.FLAG_IS_CONTENT) != 0;
//	}
//	
//	public boolean isLeaf() {
//		return (flags & DOMNodeObject.FLAG_IS_LEAF) != 0;
//	}
//}
