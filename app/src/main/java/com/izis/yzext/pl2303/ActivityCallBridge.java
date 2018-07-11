package com.izis.yzext.pl2303;


public class ActivityCallBridge {

	static ActivityCallBridge callBridge;
	private PL2303Interface mpl2303interface;

	public ActivityCallBridge() {

	}

	public void invokeMethod(String readdata) {
		if (mpl2303interface != null) {
			mpl2303interface.ReadData(readdata);
		}
	}

	public static ActivityCallBridge getInstance() {
		if(callBridge == null){
		callBridge = new ActivityCallBridge();
		}
		return callBridge;
	}

	public void setOnMethodCallback(PL2303Interface mpl2303interface) {
		this.mpl2303interface = mpl2303interface;
	}

	public interface PL2303Interface {
		public void ReadData(String readdata);
	};

}
