package jni;

public class VprocessJNI {

	static{
		System.loadLibrary("process");
	}
	public static native long init_real(int size,int fs,int fsize,int move);
	public static native void pfeat_real(long inst,double[] data);
	public static native double[] compare_real(long inst1,long inst2);
	public static native long get_handler(String filename);
	public static native double[] read_wav(long inst,int size);
	public static native long get_write_handler(String filename,int fs,int bits,int channels);
	public static native int write_wav(long inst,short[] data,int size);
	public static native void close_file(long inst);

}
