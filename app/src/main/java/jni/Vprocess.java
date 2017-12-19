package jni;
public class Vprocess {
	private WavInfo winfo;
	private long winfol = -1;

	public WavInfo getCWinfo(){
		return winfo;
	}

	public long getLWinfo(){
		return winfol;
	}


	public  void init_real(int size,int fs,int fsize,int move) throws Exception{
		this.winfol = VprocessJNI.init_real(size, fs, fsize, move);
		if (this.winfol  == -1) {
			throw new Exception("init obj failed");
		}
	}

	public void pfeat_real(double[] data) throws Exception{
		VprocessJNI.pfeat_real(winfol,data);
	}

}
