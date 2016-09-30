package blue.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import blue.runtime.App;

public class ZipExe {
	
	// ===================================================
	
	private ZipExe(){ }
	
	// ===================================================
	
	public static final App open(Class<?> zipRef, String exe, String ... pars){
		try {
			return new App(getFile(exe, zipRef).toURL(), pars);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static final URI getFile(String exe, Class<?> zipRef){
		return getFile(exe, zipRef, null);
	}
	
	public static final URI getFile(String exe, Class<?> zipRef, File tempDir){
        try {
    		URI uri = getJarURI(zipRef);
			return getFile(uri, exe, tempDir);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	// ===================================================
	
	private static URI getJarURI(Class<?> ref) throws URISyntaxException {
		final ProtectionDomain domain;
		final CodeSource source;
		final URL url;
		final URI uri;

		domain = ref.getProtectionDomain();
		source = domain.getCodeSource();
		url = source.getLocation();
		uri = url.toURI();

		return (uri);
	}

	private static URI getFile(final URI where, final String fileName, final File tempDir) throws ZipException, IOException {
		final File location;
		final URI fileURI;

		location = new File(where);

		// not in a JAR, just return the path on disk
		if (location.isDirectory()) {
			fileURI = URI.create(where.toString() + fileName);
		} else {
			final ZipFile zipFile;

			zipFile = new ZipFile(location);

			try {
				fileURI = extract(zipFile, fileName, tempDir);
			} finally {
				zipFile.close();
			}
		}

		return (fileURI);
	}

	private static URI extract(final ZipFile zipFile, final String fileName, final File tempDir) throws IOException {
		File tempFile = null;
		final ZipEntry entry;
		final InputStream zipStream;
		OutputStream fileStream;

//		String pathName = Utils.getFileNameInPath(path.getPath());
		String fname = Utils.getFileNameInPath(fileName);

		if(tempDir == null){
			tempFile = File.createTempFile(fname, Long.toString(System.currentTimeMillis()));
		}else{
			tempFile = new File(tempDir, fname);
		}
		tempFile.deleteOnExit();
		entry = zipFile.getEntry(fileName);

		if (entry == null) {
			throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());
		}

		zipStream = zipFile.getInputStream(entry);
		fileStream = null;

		try {
			final byte[] buf;
			int i;

			fileStream = new FileOutputStream(tempFile);
			buf = new byte[1024];
			i = 0;

			while ((i = zipStream.read(buf)) != -1) {
				fileStream.write(buf, 0, i);
			}
		} finally {
			close(zipStream);
			close(fileStream);
		}

		return (tempFile.toURI());
	}

	private static void close(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
