package jni;

public class VprocessJNI {

	static{
		System.loadLibrary("process");
	}
	public static native long init_real(String conf);
	public static native void pfeat_real(long inst,double[] data);
	public static native double[] compare_real(long inst1,long inst2);
	public static native int set_wave_reader(long inst,String filename);
	public static native int set_wave_writer(long inst,String filename);
	public static native double[] read_wav(long inst,int size);
	public static native short[] read_short_wav(long inst,int size);
	public static native int write_wav(long inst,short[] data,int size,int flg);
	public static native void close_file(long inst);

}
