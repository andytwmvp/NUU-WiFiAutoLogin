package tw.edu.nuu.network;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiControl {
    
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    
    public WifiControl(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }
    
    //¶}±ÒWifi
    public boolean wifiStatus() {
        boolean wifiBool = true;
        if (!wifiManager.isWifiEnabled()) {  
            wifiBool = wifiManager.setWifiEnabled(true);
        }
        return wifiBool;
    }  
  
    public boolean addNetwork(WifiConfiguration wcg) {
        while(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING ) {
            try{
                Thread.currentThread();  
                Thread.sleep(100);  
            }catch(InterruptedException ie) {  
            }  
        }
        int wcgID = wifiManager.addNetwork(wcg);  
        boolean enableWifi =  wifiManager.enableNetwork(wcgID, true);
        return enableWifi;
    }
    
    public void closeWifi() { 
        if (wifiManager.isWifiEnabled()) { 
            wifiManager.setWifiEnabled(false); 
        } 
    } 
    
    public WifiConfiguration createWifiInfo(String SSID) { 
          WifiConfiguration wifiConfig = new WifiConfiguration(); 
          wifiConfig.allowedKeyManagement.clear(); 
          wifiConfig.SSID = "\"" + SSID + "\"";   
          
          WifiConfiguration tempConfig = this.isExsits(SSID);           
          if(tempConfig != null) {  
              wifiManager.updateNetwork(tempConfig);
          }
          
          wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
          return wifiConfig; 
    } 
    
    public void connectWifi(String SSID) {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                 wifiManager.disconnect();
                 wifiManager.enableNetwork(i.networkId, true);
                 wifiManager.reconnect();
                 break;
            }      
        }
    }
    
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\""+SSID+"\"")) {  
                return existingConfig;  
            }
        }  
        return null;   
    }
}
