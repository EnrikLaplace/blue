package blue.lang;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import blue.util.FileUtils;
import blue.util.StreamUtils;
import blue.util.Utils;

/**
 * 
 * @author Matteo
 *
 */
public class Zip implements Iterable<Zip.ZipElement> {
	
	/**
	 * Zip element
	 * 
	 * @author Matteo
	 *
	 */
	public class ZipElement {
		
		private ZipEntry el;
		private String path;
		private boolean doDelete;
		private InputStream saveStream;

		public ZipElement(ZipEntry el) {
			this.el = el;
			this.path =  el.getName();
		}
		
		public boolean isDirectory(){
			return el.isDirectory();
		}
		
		public String getPath() {
			return path;
		}
		
		public String getName(){
			String[] pathes = path.split("/");
			if(pathes.length==1){
				return pathes[0];
			}
			if(pathes[pathes.length-1]==""){
				return pathes[pathes.length-2];
			}
			return pathes[pathes.length-1];
		}
		@Override
		public String toString() {
			return path;
		}
		
		public void delete(){
			doDelete = true;
			if(isDirectory()){
				for(ZipElement cc:getContent()){
					cc.doDelete = true;
				}
			}
		}
		
		public URL toUrl(){
			String url = "jar:file:/" + file.getAbsolutePath().replace("\\", "/") + "!/" + path;
			return Utils.toUrl(url);
		}
	    
	    public void add(File file){
	    	if(!isDirectory()){
	    		return;
	    	}
	    	addElement(file, path);
	    }
		
		public File unzip(File dest){
			if(!dest.isDirectory()){
				dest.mkdirs();
			}
			File mf = new File(dest, getName());
			ZipFile zz = Zip.this.open(false);
			if(isDirectory()){
				mf.mkdirs();
				for(ZipElement cc:getContent()){
					String subs = cc.path.substring(path.length());
					File mf2 = new File(mf,subs);
					if(cc.isDirectory()){
						mf2.mkdirs();
					}else{
						try {
							StreamUtils.save(zz.getInputStream(cc.getEntry()), mf2);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}else{
				try {
					StreamUtils.save(zz.getInputStream(el), mf);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				zz.close();
			} catch (IOException e) {
			}
			return mf;
		}
		
		public ZipElement[] getContent(){
			return getContent(true);
		}
		
		public ZipElement[] getContent(boolean recursive){
			// define content
			List<ZipElement> ret = new ArrayList<ZipElement>();
			for(ZipElement ee:elements.get()){
				if(ee.path.startsWith(path) && ee != this){
					if(!recursive){
						if(ee.path.replace(path, "").contains("/")){
							continue;
						}
					}
					ret.add(ee);
				}
			}
			return ret.toArray(new ZipElement[ret.size()]);
		}
		
		ZipEntry getEntry() {
			return el;
		}
	}

	private Cached<ZipElement[]> elements = new Cached<ZipElement[]>() {
		
		private Time lastUpdate = new Time(0);
		
		public boolean isExpired() {
			return file.lastModified() > lastUpdate.millis();
		}
		
		@Override
		protected ZipElement[] update() {
			List<ZipElement> ret = new ArrayList<ZipElement>();
			ZipFile pn = open(false);
			Enumeration<? extends ZipEntry> list = pn.entries();
			while(list.hasMoreElements()){
				ret.add(new ZipElement(list.nextElement()));
			}
			lastUpdate.reset();
			return ret.toArray(new ZipElement[ret.size()]);
		}
	};
	
	private List<ZipElement> addList = new ArrayList<ZipElement>();
    
    
    
    // =======================================
    
    private final File file;
    
    // =======================================

    public Zip(File file){
        assert !file.isDirectory();
        this.file = file;
    }

    public Zip(InputStream source){
    	// save to temp file
    	this.file = FileUtils.createTempFile();
    	StreamUtils.save(source, this.file);
    }
    
    // =======================================

    // ----------------------------------------
    //              INTERNAL
    // ----------------------------------------
    
    private ZipFile open(boolean allowDelete){
        try {
            return new ZipFile(file,ZipFile.OPEN_READ | (allowDelete?ZipFile.OPEN_DELETE:0));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private void addElement(File file, String path){
    	if(path.equals("/")){
    		path = "";
    	}
    	if(!file.exists()){
    		return;
    	}
    	if(file.isDirectory()){
    		addList.add(new ZipElement(new ZipEntry(path + file.getName() + "/")));
    		for(File ff:file.listFiles()){
    			addElement(ff, path + file.getName() + "/");
    		}
    	}else{
    		ZipElement el = new ZipElement(new ZipEntry(path + file.getName()));
    		try {
				el.saveStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
    		addList.add(el);
    	}
    }
    
    private void do_save(File dest){
    	File tmp = new File(dest.getAbsolutePath() + ".tmp");
    	try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp,true)));
			zos.setMethod(ZipOutputStream.DEFLATED);
			if(file.exists()){
				ZipFile rzip = open(false);
				List<String> duplPath = new ArrayList<String>();
				for(ZipElement dd:addList){
					duplPath.add(dd.path);
				}
				for(ZipElement el:elements.get()) {
		    		if(!el.doDelete && !duplPath.contains(el.path)){
//	    				System.out.println(el.path);
	    				zos.putNextEntry(new ZipEntry(el.el));
		    			if(!el.el.isDirectory()){
		    				StreamUtils.copy(rzip.getInputStream(el.el), zos, true);
		    			}
		    			try{
			    	    	zos.closeEntry();
		    			}catch(IOException e){
		    				System.out.println(el.path);
		    				throw e;
		    			}
		    		}
		    	}
				rzip.close();
			}
			for(ZipElement el:addList) {
				zos.putNextEntry(new ZipEntry(el.el));
    			if(!el.el.isDirectory()){
    				StreamUtils.copy(el.saveStream, zos, true);
    			}
    	    	zos.closeEntry();
			}
			addList.clear();
	    	zos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	FileUtils.copy(tmp, dest);
    	tmp.delete();
    }

    // ==========================================================
    
    /**
     * Detects pending changes in archive
     * 
     * @return
     */
    public boolean isPending(){
    	synchronized (this) {
	    	for(ZipElement ee:elements.get()){
	    		if(ee.doDelete){
	    			return true;
	    		}
	    	}
	    	return addList.size() > 0;
    	}
    }
    
    /**
     * reverts all pending changes
     */
    public void revert(){
    	synchronized (this) {
        	for(ZipElement ee:elements.get()){
        		ee.doDelete=false;
        	}
        	addList.clear();
		}
    }
    
    /**
     * Save all pending changes
     */
    public void save(){
    	synchronized (this) {
    		do_save(file);
    	}
    }

    
    /**
     * copy current archive. All pending changes are moved to destination file with no changes in current target archive
     */
    public void save(File dest){
    	synchronized (this) {
    		do_save(dest);
    	}
    }
    
    /**
     * List all zip entry in archive
     * 
     * @return
     */
    public ZipElement[] list(){
    	return elements.get();
    }
    
    /**
     * Add new entry. Edit will be finalized with save()
     * 
     * @param file
     */
    public void add(File file){
    	synchronized (this) {
    		addElement(file, "");
    	}
    }

    
    /**
     * Add new entry in specified path. Edit will be finalized with save()
     * 
     * @param file
     */
    public void add(File file, String path){
    	synchronized (this) {
	    	if(!path.endsWith("/")){
	    		path += "/";
	    	}
    		addElement(file, path);
    	}
    }

    
    /**
     * Add new entry in specified path. Edit will be finalized with save()
     * 
     * @param file
     */
    public void add(InputStream source, String path){
    	synchronized (this) {
	    	if(!path.endsWith("/")){
	    		path += "/";
	    	}
	    	ZipElement el = new ZipElement(new ZipEntry(path));
	    	el.saveStream = source;
			addList.add(el);
    	}
    }
    
    /**
     * get specified entry
     * 
     * @param path
     * @return
     */
    public ZipElement get(String path){
    	for(ZipElement ee:elements.get()){
    		if(ee.path.equals(path.trim()) || ee.path.equals(path.trim() + "/")){
    			return ee;
    		}
    	}
    	return null;
    }
    
    /**
     * unzip all content in destination directory. All existing files will be replaced
     * 
     * @param dest
     */
    public void unzip(File dest){
    	assert !dest.isFile();
    	if(!dest.isDirectory()){
    		dest.mkdirs();
    	}
		ZipFile zip = this.open(false);
    	for(ZipElement ee:elements.get()){
    		File elFile = new File(dest, ee.getPath());
    		if(ee.isDirectory()){
    			elFile.mkdirs();
    		}else{
    			try {
        			InputStream str = zip.getInputStream(ee.el);
        			StreamUtils.save(str, elFile);
					str.close();
				} catch (IOException e) {
				}
    		}
    	}
		try {
			zip.close();
			System.gc();
		} catch (IOException e) {
		}
    }
    
    /**
     * Get current archive.
     * 
     * @return
     */
    public File toFile() {
        return file;
    }
    
    // -------------------------------------------------------

    /**
     * All archive entries iterator
     * 
     */
	public Iterator<ZipElement> iterator() {
		return new Iterator<Zip.ZipElement>() {
			private ZipElement[] list = elements.get();
			int curr = -1;
			
			public void remove() {
				if(curr>=0){
					list[curr].delete();
				}
			}
			
			public ZipElement next() {
				return list[++curr];
			}
			
			public boolean hasNext() {
				return list.length > (curr+1);
			}
		};
	}
}
