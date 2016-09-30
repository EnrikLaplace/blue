/*
 * CachedFile.java
 *
 * (C) 2016 - 2016 Cedac Software S.r.l.
 */
package blue.lang;

import java.io.File;
import java.util.Date;

import blue.util.FileUtils;

public class CachedFile {
    
    // ===============================================
    
    private File tempDir;
    private String fileName;
    // format: cached:time:name
    private static final String SEPARATOR = "_._";
    
    // ===============================================
    
    public CachedFile(File tempDir, String fileName){
        this.tempDir = tempDir;
        this.fileName = fileName;
        this.tempDir.mkdirs();
    }
    
    // ===============================================
    
    private Date getTimeByFile(File file){
        if(file == null){
            return null;
        }
        String[] data = file.getName().split(SEPARATOR);
        Date time = new Date(Long.parseLong(data[1]));
        return time;
    }
    
    private File generateCachedFile(Date time){
        return new File(tempDir, "cached"+SEPARATOR+time.getTime()+SEPARATOR+fileName);
    }
    
    private String getOriginalFileName(File file){
        String[] data = file.getName().split(SEPARATOR);
        return data[2];
    }
    
    private boolean isCachedFile(File file){
        return file.getName().split(SEPARATOR).length==3;
    }
    
    private File getLastFile(){
        // get last cached file
        Date bestTime = null;
        File bestFile = null;
        for(File ff:tempDir.listFiles()){
            if(isCachedFile(ff)){
                if(getOriginalFileName(ff).equals(fileName)) {
                    Date ftime = getTimeByFile(ff);
                    if(bestTime == null || bestTime.before(ftime)){
                        bestTime = ftime;
                        bestFile = ff;
                    }
                }
            }
        }
        return bestFile;
    }
    
    // ===============================================
    
    public boolean isCached(){
        return getLastFile() != null;
    }
    
    public boolean isCached(Date time){
        Date cTime = getTime();
        return (cTime != null && cTime.after(time));
    }
    
    public Date getTime(){
        return getTimeByFile(getLastFile());
    }
    
    public File getCached(File dest){
        if(!isCached()){
            return null;
        }
        return FileUtils.copy(getLastFile(), dest);
    }
    
    public boolean addCached(File file, Date time){
        if(isCached(time)){
            return false;
        }
//        if(!file.getName().equals(fileName)){
//            return false;
//        }
        // generate dest file
        File dest = generateCachedFile(time);
        // copy file
        return FileUtils.copy(file, dest) != null;
    }
    
    // ===============================================
}
