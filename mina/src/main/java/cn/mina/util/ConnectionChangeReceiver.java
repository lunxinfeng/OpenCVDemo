package cn.mina.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import cn.mina.constant.PublicClass;
import cn.mina.mina.MinaPushService;
import cn.mina.mina.MinaPushServiceOpen;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		 	ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        boolean mobmetboolean =false;
	        boolean wifinetboolean =false;
	        if(mobNetInfo!=null){
	        	mobmetboolean = mobNetInfo.isConnected();
	        }
	        if(wifiNetInfo!=null){
	        	wifinetboolean = wifiNetInfo.isConnected();
	        }
	        
	        if (!mobmetboolean&& !wifinetboolean) {
	        	PublicClass.CONNECTION_STATE = false;
	        }else {
	        	if(!PublicClass.CONNECTION_STATE){
	        	PublicClass.CONNECTION_STATE = true;
	        	int x = 1;
	        	if(MinaPushService.isInit()){
	        		Intent myIntent = new Intent(context,MinaPushService.class);
					myIntent.putExtra("again", x);
					context.startService(myIntent);
	        	}
	        	if(MinaPushServiceOpen.isInit()){
	        		Intent myIntent2 = new Intent(context,MinaPushServiceOpen.class);
					myIntent2.putExtra("again", x);
					context.startService(myIntent2);
	        	}
	        	}
	        }
	}

}
