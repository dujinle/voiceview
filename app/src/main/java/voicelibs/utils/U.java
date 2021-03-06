package voicelibs.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public class U {
    public static final int PREPARE_RECORD = 5;
	public static final int RECORDED = 4;
	public static final int READED = 6;
	public static final int PLAYFINISH = 3;
	public static final int PLAYING = 1;
	public static final int LOADED = 2;
	public static final String DATA_DIRECTORY = Environment
			.getExternalStorageDirectory() + "/record";

	public static final String CONF_DIRECTORY = Environment
			.getExternalStorageDirectory() + "/record/init.conf";
	public static final String RECORD_DIRECTORY = Environment
			.getExternalStorageDirectory() + "/record/recordtmp.wav";
	/**
	 * 创建不同的目录
	 */
	public static void createDirectory() {
		if (sdCardExists()) {
			File file = new File(DATA_DIRECTORY);
			if (!file.exists()) {
				file.mkdirs();
			}
		} else {
			File file = new File(DATA_DIRECTORY);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
	}
	
	/**
	* 判断手机是否有SD卡。
	* 
	* @return 有SD卡返回true，没有返回false。
	*/
	public static boolean hasSDCard() {
	return Environment.MEDIA_MOUNTED.equals(Environment
	.getExternalStorageState());
	}

	/**
	 * 删除单个文件
	 * 
	 * @param filePath
	 *            被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}
	/**
	 * 清空文件夹
	 * 
	 * @param childtaskid
	 * @return 文件删除成功返回true，否则返回false
	 */
	public static boolean deleteDirs(String childtaskid) {
		if (sdCardExists()) {
			File file = new File(DATA_DIRECTORY + "/" + childtaskid);
			if (file.exists()) {
				try {
					clearCache(file);
				} catch (Exception e) {
				}
			}
		}
		return false;
	}
	
	/**
	 * 清空文件夹
	 * @return 文件删除成功返回true，否则返回false
	 */
	public static boolean deleteDirs() {
		if (sdCardExists()) {
			File file = new File(DATA_DIRECTORY);
			if (file.exists()) {
				try {
					clearCache(file);
				} catch (Exception e) {
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @Description 清空文件夹
	 * @author DataTang
	 */
	private static  void clearCache(File file) throws Exception {
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				clearCache(files[i]);
			else {
				files[i].delete();
			}
		}
		file.delete();
	}

	/**
	 * 创建不同的目录
	 */
	public static void createDirectory(String childtaskid) {
		if (sdCardExists()) {
			File file = new File(DATA_DIRECTORY + "/" + childtaskid);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
	}

	public static boolean sdCardExists() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	@SuppressLint("SimpleDateFormat")
	public static String millis2CalendarString(long millis, String format) {
		if (millis > 0) {
			SimpleDateFormat yFormat = new SimpleDateFormat(format);
			Calendar yCalendar = Calendar.getInstance();
			yCalendar.setTimeInMillis(millis);

			try {
				return yFormat.format(yCalendar.getTime());
			} catch (Exception e) {

			} finally {
				yCalendar = null;
				yFormat = null;
				format = null;
			}
		}

		return "";
	}

}
