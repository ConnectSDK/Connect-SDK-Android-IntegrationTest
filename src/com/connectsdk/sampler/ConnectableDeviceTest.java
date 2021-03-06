package com.connectsdk.sampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.DevicePicker;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.sampler.MainActivity;
import com.connectsdk.sampler.R;
import com.connectsdk.sampler.SectionsPagerAdapter;
import com.connectsdk.sampler.fragments.MediaPlayerFragment;
import com.connectsdk.service.AirPlayService;
import com.connectsdk.service.DIALService;
import com.connectsdk.service.DLNAService;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.NetcastTVService;
import com.connectsdk.service.RokuService;
import com.connectsdk.service.WebOSTVService;
import com.robotium.solo.Solo;

public class ConnectableDeviceTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	

	TestUtil testUtil;
	private Solo solo;
	private SectionsPagerAdapter sectionAdapter;
	private AlertDialog alertDialog;
	private ConnectableDevice mTV;
	private  DevicePicker devicePkr;
	private ConnectivityManager cmngr;
	List<String> expectedLauncherCapabilities = new ArrayList<String>();
	List<String> expectedMediaPlayerCapabilities = new ArrayList<String>();
	List<String> expectedMediaControlCapabilities = new ArrayList<String>();
	List<String> expectedPlayListControlCapabilities = new ArrayList<String>();
	List<String> expectedVolumeControlCapabilities = new ArrayList<String>();
	
	List<String> expectedTVControlCapabilities = new ArrayList<String>();
	List<String> expectedExternalInputControlCapabilities = new ArrayList<String>();
	List<String> expectedMouseControlCapabilities = new ArrayList<String>();
	List<String> expectedTextInputControlCapabilities = new ArrayList<String>();
	List<String> expectedPowerControlCapabilities = new ArrayList<String>();
	List<String> expectedKeyControlCapabilities = new ArrayList<String>();
	List<String> expectedToastControlCapabilities = new ArrayList<String>();
	List<String> expectedWebAppLauncherCapabilities = new ArrayList<String>();
	
	
	
	public ConnectableDeviceTest() {
		super("com.connectsdk.sampler", MainActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		sectionAdapter = ((MainActivity)getActivity()).mSectionsPagerAdapter;
		alertDialog = ((MainActivity)getActivity()).dialog;
		mTV = ((MainActivity)getActivity()).mTV;
		devicePkr = ((MainActivity)getActivity()).dp; 
		cmngr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		testUtil = new TestUtil();
		Collection<ConnectableDevice> devices=DiscoveryManager.getInstance().getCompatibleDevices().values();
		Thread.sleep(10000);
		testUtil.getDeviceWithServices(devices);
		expectedLauncherCapabilities = Arrays.asList(testUtil.getCapabilities("Launcher"));
		expectedMediaPlayerCapabilities = Arrays.asList(testUtil.getCapabilities("MediaPlayer"));
		expectedMediaControlCapabilities = Arrays.asList(testUtil.getCapabilities("MediaControl"));
		expectedTVControlCapabilities = Arrays.asList(testUtil.getCapabilities("TVControl"));
		expectedVolumeControlCapabilities = Arrays.asList(testUtil.getCapabilities("VolumeControl"));
		expectedExternalInputControlCapabilities = Arrays.asList(testUtil.getCapabilities("ExternalInputControl"));
		expectedMouseControlCapabilities = Arrays.asList(testUtil.getCapabilities("MouseControl"));
		expectedTextInputControlCapabilities = Arrays.asList(testUtil.getCapabilities("TextInputControl"));
		expectedPowerControlCapabilities = Arrays.asList(testUtil.getCapabilities("PowerControl"));
		expectedKeyControlCapabilities = Arrays.asList(testUtil.getCapabilities("KeyControl"));
		expectedPlayListControlCapabilities = Arrays.asList(testUtil.getCapabilities("PlayListControl"));
		expectedToastControlCapabilities = Arrays.asList(testUtil.getCapabilities("ToastControl"));
		expectedWebAppLauncherCapabilities = Arrays.asList(testUtil.getCapabilities("WebAppLauncher"));
				
	}
	
	public void testConnectedDeviceSupportedServices() throws InterruptedException{
		View actionconnect;
		ListView view;
				
		int count  = 0;
		int i = 1;
		
		while(true){	
			//Verify getPickerDialog is not null and returns an instance of DevicePicker
			devicePkr = ((MainActivity)getActivity()).dp;
			Assert.assertNotNull(devicePkr);
			
			if(!alertDialog.isShowing()){
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);				
				Thread.sleep(10000);
				
			}
			Assert.assertTrue(alertDialog.isShowing());
				
			view = devicePkr.getListView();			
			
			if(i <= count){
				solo.clickInList(i);
				} else {
					break;
				}			
			
			Thread.sleep(2000);
			
			mTV = ((MainActivity)getActivity()).mTV;
			Assert.assertTrue(mTV.isConnected());
			
			Assert.assertFalse(mTV.getCapabilities().isEmpty());			
			Thread.sleep(2000);			
			Assert.assertNotNull(mTV.getConnectedServiceNames());			
						
			sectionAdapter = ((MainActivity)getActivity()).mSectionsPagerAdapter;
			MediaPlayerFragment mediaplayerfragment = (MediaPlayerFragment) sectionAdapter.getFragment(0);
			Assert.assertSame(mediaplayerfragment.getTv() , mTV);
			
			ArrayList<DeviceService> foundServices = new ArrayList<DeviceService>();
			
			Assert.assertNotNull(mTV.getServices());
			
			for (DeviceService service : mTV.getServices()) {
				if (DIALService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((DIALService) service);
					Assert.assertTrue(service.isConnected());
					
				}else if(DLNAService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((DLNAService) service);
					Assert.assertTrue(service.isConnected());
					
				}else if(WebOSTVService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((WebOSTVService) service);
					Assert.assertTrue(service.isConnected());
					
				}else if(AirPlayService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((AirPlayService) service);
					Assert.assertTrue(service.isConnected());
					
				}else if(NetcastTVService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((NetcastTVService) service);
					Assert.assertTrue(service.isConnected());
					
				}
				else if(RokuService.class.isAssignableFrom(service.getClass())) {
					foundServices.add((RokuService) service);
					Assert.assertTrue(service.isConnected());
					
				}
				
				
			}
			
			Assert.assertFalse(foundServices.isEmpty());
			
			actionconnect = solo.getView(R.id.action_connect);
			solo.clickOnView(actionconnect);
			
			Thread.sleep(10000);
			
			Assert.assertFalse(mTV.isConnected());
	        i++;
	   	}
			
		
			
	   	}
	
	public void testSupportedCapabilityForDeviceConnected() throws InterruptedException {
		View actionconnect;
		ListView view;				
			
		int count  = 0;
		int i = 1;
		
		while(true){			
		
			//Verify getPickerDialog is not null and returns an instance of DevicePicker
			devicePkr = ((MainActivity)getActivity()).dp;
			
			if(!alertDialog.isShowing()){
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);				
				Thread.sleep(10000);
				
			}
			Assert.assertTrue(alertDialog.isShowing());
				
			view = devicePkr.getListView();			
			
			if(testUtil.verifyWifiConnected(cmngr) && null != view){
				
				count=view.getCount();
				Assert.assertTrue(count >= 0);
			
		    }
			ArrayList<DeviceService> foundServices = new ArrayList<DeviceService>();
			Boolean hasDIALCapabilities = Boolean.FALSE;
			Boolean hasAirPlayCapabilities = Boolean.FALSE;
			Boolean hasDLNACapabilities = Boolean.FALSE;
			Boolean hasNetcastCapabilities = Boolean.FALSE;
			Boolean hasWebOSCapabilities = Boolean.FALSE;
			Boolean hasRokuCapabilities = Boolean.FALSE;		
					
			
			if(i <= count){
				solo.clickInList(i);
				Thread.sleep(10000);
				
				mTV = ((MainActivity)getActivity()).mTV;
				Assert.assertTrue(mTV.isConnected());			
				Assert.assertFalse(mTV.getCapabilities().isEmpty());
				
				Thread.sleep(2000);						
				
				Assert.assertNotNull(mTV.getServices());
				
				if(testUtil.deviceWithDIALService != null && testUtil.deviceWithDIALService.contains(mTV)){
					DeviceService DIALService = mTV.getServiceByName("DIAL");
					foundServices.add(DIALService);
					
					Assert.assertTrue(DIALService.isConnected());
					
					List<String> actualDeviceDIALCapabilities = DIALService.getCapabilities();
					Assert.assertFalse(actualDeviceDIALCapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceDIALCapabilities, expectedLauncherCapabilities))
					{
						hasDIALCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasDIALCapabilities);
						
					}
					
					Assert.assertTrue("The Connected Device Must Support atleast one Launcher/DIAL capability.",hasDIALCapabilities);
					
				} 
				if(testUtil.deviceWithAirplayService != null && testUtil.deviceWithAirplayService.contains(mTV)){
					DeviceService AirPlayService = mTV.getServiceByName("AirPlay");
					foundServices.add(AirPlayService);
					
					Assert.assertTrue(AirPlayService.isConnected());
					
					List<String> actualDeviceAirPlayCapabilities = AirPlayService.getCapabilities();
					Assert.assertFalse(actualDeviceAirPlayCapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceAirPlayCapabilities, expectedMediaPlayerCapabilities))
					{
						hasAirPlayCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasAirPlayCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceAirPlayCapabilities, expectedMediaControlCapabilities))
					{
						hasAirPlayCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasAirPlayCapabilities);
						
					}
					
					Assert.assertTrue("The Connected Device Must Support atleast one AirPlay capability.",hasAirPlayCapabilities);
					
				}
				if(testUtil.deviceWithDLNAService != null && testUtil.deviceWithDLNAService.contains(mTV)){
					DeviceService DLNAService = mTV.getServiceByName("DLNA");
					foundServices.add(DLNAService);
					
					Assert.assertTrue(DLNAService.isConnected());
					
					List<String> actualDeviceDLNACapabilities = DLNAService.getCapabilities();
					Assert.assertFalse(actualDeviceDLNACapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceDLNACapabilities, expectedMediaPlayerCapabilities))
					{
						hasDLNACapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasDLNACapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceDLNACapabilities, expectedMediaControlCapabilities))
					{
						hasDLNACapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasDLNACapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceDLNACapabilities, expectedPlayListControlCapabilities))
					{
						hasDLNACapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasDLNACapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceDLNACapabilities, expectedVolumeControlCapabilities))
					{
						hasDLNACapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasDLNACapabilities);
						
					}
					
					
					Assert.assertTrue("The Connected Device Must Support atleast one AirPlay capability.",hasDLNACapabilities);
					
				}
				if(testUtil.deviceWithNetcastTVService != null && testUtil.deviceWithNetcastTVService.contains(mTV)){
					DeviceService NetcastTVService = mTV.getServiceByName("Netcast TV");
					foundServices.add(NetcastTVService);
					
					Assert.assertTrue(NetcastTVService.isConnected());
					
					List<String> actualDeviceNetcastTVCapabilities = NetcastTVService.getCapabilities();
					Assert.assertFalse(actualDeviceNetcastTVCapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedMediaPlayerCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedMediaControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedLauncherCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedTVControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedVolumeControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedExternalInputControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedMouseControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedTextInputControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedPowerControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceNetcastTVCapabilities, expectedKeyControlCapabilities))
					{
						hasNetcastCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasNetcastCapabilities);
						
					}
					
					
					Assert.assertTrue("The Connected Device Must Support atleast one AirPlay capability.",hasNetcastCapabilities);
					
				}if(testUtil.deviceWithWebOSTVService != null && testUtil.deviceWithWebOSTVService.contains(mTV)){
					DeviceService WebOSTVService = mTV.getServiceByName("webOS TV");
					foundServices.add(WebOSTVService);
					
					Assert.assertTrue(WebOSTVService.isConnected());
					
					List<String> actualDeviceWebOSTVCapabilities = WebOSTVService.getCapabilities();
					Assert.assertFalse(actualDeviceWebOSTVCapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedMediaPlayerCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedMediaControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedLauncherCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedTVControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedVolumeControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedExternalInputControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedMouseControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedTextInputControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedPowerControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedKeyControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedToastControlCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceWebOSTVCapabilities, expectedWebAppLauncherCapabilities))
					{
						hasWebOSCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasWebOSCapabilities);
						
					}
					
					
					Assert.assertTrue("The Connected Device Must Support atleast one Webos capability.",hasWebOSCapabilities);
					
				}
				if(testUtil.deviceWithRokuService != null && testUtil.deviceWithRokuService.contains(mTV)){
					DeviceService RokuService = mTV.getServiceByName("Roku");
					foundServices.add(RokuService);
					
					Assert.assertTrue(RokuService.isConnected());
					
					List<String> actualDeviceRokuCapabilities = RokuService.getCapabilities();
					Assert.assertFalse(actualDeviceRokuCapabilities.isEmpty());
					
										
					if (!Collections.disjoint(actualDeviceRokuCapabilities, expectedMediaPlayerCapabilities))
					{
						hasRokuCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasRokuCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceRokuCapabilities, expectedMediaControlCapabilities))
					{
						hasRokuCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasRokuCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceRokuCapabilities, expectedLauncherCapabilities))
					{
						hasRokuCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasRokuCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceRokuCapabilities, expectedTextInputControlCapabilities))
					{
						hasRokuCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasRokuCapabilities);
						
					}
					if (!Collections.disjoint(actualDeviceRokuCapabilities, expectedKeyControlCapabilities))
					{
						hasRokuCapabilities = Boolean.TRUE;	
						Assert.assertTrue(hasRokuCapabilities);
						
					}
					
					
					Assert.assertTrue("The Connected Device Must Support atleast one AirPlay capability.",hasNetcastCapabilities);
					
				}
				
				Assert.assertTrue(foundServices.size() > 0);
				
				} else {
					break;
				}			
			

			
			actionconnect = solo.getView(R.id.action_connect);
			solo.clickOnView(actionconnect);
			
			Thread.sleep(10000);
			
			Assert.assertFalse(mTV.isConnected());
	        i++;
	   		}
						
	   	}
			
}
