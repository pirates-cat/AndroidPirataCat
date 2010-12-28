package cat.pirata.extra;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;

public class CtrlFile {
	
	private static CtrlFile INSTANCE = null;
	private Context context = null;
	
	public static CtrlFile getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CtrlFile();
		}
		return INSTANCE;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void saveFile(String filename, String content) {
		try {
			FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readFile(String filename) {
		try {
			FileInputStream fis = context.openFileInput(filename);
			int current = 0;
			ByteArrayBuffer baf = new ByteArrayBuffer(65535);
			while((current = fis.read()) != -1) {
				baf.append((byte)current);
			}
			fis.close();
			return new String(baf.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return new String();
		}
	}
}

