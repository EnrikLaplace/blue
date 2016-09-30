package blue.net.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import blue.util.StreamUtils;

public class HttpFiles {
	
	// ==================================================
	
	private final static String crlf = "\r\n";
	private final static String twoHyphens = "--";
	private final static String boundary =  "*****";
	
	// ==================================================
	
	class FileUpload {
		private String name;
		private String fileName;
		private InputStream data;
		
		public String getFileName() {
			return fileName;
		}
		
		public String getName() {
			return name;
		}
	}
	
	// ==================================================
	
	private ArrayList<FileUpload> files = new ArrayList<FileUpload>();
	
	// ==================================================
	
	public int size() {
		return files.size();
	}
	
	public void clear() {
		files.clear();
	}
	
	public void add(String name, String fileName, InputStream data){
		FileUpload file = new FileUpload();
		file.data = data;
		file.name = name;
		file.fileName = fileName;
		files.add(file);
	}
	
	// ==================================================
	
	void write(HttpURLConnection conn) {
		if(files.size() == 0) {
			return; // do nothing
		}
		
		// force output and header info
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		
		try {
			DataOutputStream request = new DataOutputStream(conn.getOutputStream());
			
			// write all files
			for(FileUpload f:files) {
				// header
				request.writeBytes(twoHyphens + boundary + crlf);
				request.writeBytes("Content-Disposition: form-data; name=\"" +
				    f.name + "\";filename=\"" + 
				    f.fileName + "\"" + crlf);
				request.writeBytes(crlf);
				
				// data
				StreamUtils.copy(f.data, request);
				// ...
				request.writeBytes(crlf);
			}
			
			// close
			request.writeBytes(twoHyphens + boundary + 
			    twoHyphens + crlf);
			
			// close output
			request.flush();
			request.close();
			
			// clear
			files.clear();
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
