package blue.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

public final class FileUtils {
	private FileUtils(){}
	
	// ==================================
	

    public static File copy(File source, File dest){
        return copy(source, dest, false);
    }
    
    
    public static File copy(File source, File dest, boolean ignoreHidden){
	    try {
	        if(source.isHidden() && ignoreHidden) {
	            return null;
	        }
	        if(source.isDirectory()){
	            if(!dest.exists()) {
	                dest.mkdirs();
	            }
	            if(!dest.isDirectory()) {
	                throw new RuntimeException("Incompatible output: " + dest.getAbsolutePath());
	            }
	        }
	        if(source.isDirectory()){
	            // copy content
	            if(source.list() != null){
	                for(File f:source.listFiles()) {
	                    String destName = f.getCanonicalFile().getName();
	                    if(source.isDirectory()) {
	                        destName += "/";
	                    }
	                    if(f.isHidden() && ignoreHidden) {
	                        continue;
	                    }
	                    File res = copy(f,new File(dest,destName),ignoreHidden);
	                    // error?
	                    if(res == null) {
	                        return null;
	                    }
	                }
	            }
	        } else {
	            FileInputStream c1 = new FileInputStream(source);
	            FileOutputStream c2 = new FileOutputStream(dest,false);
	            StreamUtils.copy(c1, c2);
	            c1.close();
	            c2.close();
	        }
            return dest;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
    
    /**
     * Read file line by line
     * 
     * @param file
     * @param rdr
     * @return
     */
    public static boolean processFileByLine(File file, IFileReader rdr){
    	BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			int row = 0;
    	    for(String line; (line = br.readLine()) != null; ) {
    	        // process the line
    	    	rdr.readln(line, row++);
    	    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}
		return true;
    }

	
	/**
	 * Resolve path
	 * 
	 * @param parent
	 * @param path
	 * @return
	 */
	public static File resolve(File parent, String path){
		if(path == null){
			return parent;
		}
		try {
			if(parent == null){
				return new File(path).getCanonicalFile();
			}
			// generate file
			File calc = new File(parent, path);
			return calc.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Temporary system folder
	 * 
	 * @return
	 */
	public static File tempFolder(){
	    return new File(System.getProperty("java.io.tmpdir"));
	}
	
	public static File createTempFile(String fileName){
		return new File(tempFolder(), fileName);
	}
	
	public static File createTempFile(){
		return createTempFile(UUID.randomUUID().toString());
	}
    
    public static boolean isLocked(File file){
        if(file.isDirectory()) {
            File[] list = file.listFiles();
            if(list == null) {
                return false;
            }
            for(File f:list) {
                if(isLocked(f)) {
                    return true;
                }
            }
            return false; // no lock found in folder
        }
        // check
        FileOutputStream st = null;
        try {
            // remove read-only
            boolean isReadOnly = !file.canWrite();
            if(isReadOnly) {
                setReadOnly(file, false);
            }
            st = new FileOutputStream(file);
            st.close();
            if(isReadOnly) {
                setReadOnly(file, true);
            }
            return false;
        } catch (FileNotFoundException e) {
            // file is locked
            return true;
        } catch (IOException e) {
            // exception ... ?
            e.printStackTrace();
            return true;
        } finally {
            if(st != null) {
                try {
                    st.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    /**
     * 
     * @param file
     * @return
     */
    public static boolean setReadOnly(File file, boolean readOnly){
        if(file.listFiles() != null) {
            for(File f:file.listFiles()) {
                setReadOnly(f, readOnly);
            }
        }
        if(readOnly) {
            return file.setReadOnly();
        }else{
            return file.setWritable(true);
        }
    }
}
