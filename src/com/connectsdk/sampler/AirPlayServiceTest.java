package com.connectsdk.sampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.DevicePicker;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.sampler.TestUtil.Condition;
import com.connectsdk.sampler.fragments.MediaPlayerFragment;
import com.connectsdk.sampler.util.TestResponseObject;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.sessions.LaunchSession;
import com.robotium.solo.Solo;

public class AirPlayServiceTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	
	private List<ConnectableDevice> deviceWithAirplayService = null;
	private TestUtil testUtil;
	
	private Solo solo;
	private SectionsPagerAdapter sectionAdapter;
	private AlertDialog alertDialog;
	private ConnectableDevice mTV;
	private  DevicePicker devicePkr;
	private ConnectivityManager cmngr;
	private int totalConnectableDevices;
	
	
	private List<String> expectedMediaPlayerCapabilities = new ArrayList<String>();
	private List<String> expectedMediaControlCapabilities = new ArrayList<String>();
	
	private MediaPlayerFragment mediaplayerfragment;
	private TestResponseObject responseObject;
	private LaunchSession launchSession;
	private View actionconnect;
	
	
	public AirPlayServiceTest() {
		super("com.connectsdk.sampler", MainActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
		alertDialog = ((MainActivity)getActivity()).dialog;
		sectionAdapter = ((MainActivity)getActivity()).mSectionsPagerAdapter;
		mTV = ((MainActivity)getActivity()).mTV;
		devicePkr = ((MainActivity)getActivity()).dp; 
		cmngr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		testUtil = new TestUtil();
				
		testUtil.waitForCondition(new Condition() {
			
			@Override
			public boolean compare() {
				return DiscoveryManager.getInstance().getCompatibleDevices().values().isEmpty();
			}
		}, "devices.isEmpty()" );
		testUtil.getDeviceWithServices(DiscoveryManager.getInstance().getCompatibleDevices().values());
		expectedMediaPlayerCapabilities = Arrays.asList(testUtil.getCapabilities("MediaPlayer"));
		expectedMediaControlCapabilities = Arrays.asList(testUtil.getCapabilities("MediaControl"));
		mediaplayerfragment = (MediaPlayerFragment) sectionAdapter.getFragment(0);
		responseObject = mediaplayerfragment.testResponse;
				
	}
	
	public ListView getViewCount(){
		
		int count  = 0;				
		View actionconnect;
			//Verify getPickerDialog is not null and returns an instance of DevicePicker
			devicePkr = ((MainActivity)getActivity()).dp;
			
			if(!alertDialog.isShowing()){
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);				
			}
			
			testUtil.waitForCondition(new Condition() {
					
					@Override
					public boolean compare() {
						return !alertDialog.isShowing();
					}
				}, "!alertDialog.isShowing()" );
			Assert.assertTrue(alertDialog.isShowing());
				
			ListView view  = devicePkr.getListView();
			totalConnectableDevices = DiscoveryManager.getInstance().getCompatibleDevices().values().size();
			
			int waitCount = 0;			
			while(view.getCount() < totalConnectableDevices){					
					if(waitCount > TestConstants.WAIT_COUNT){
						break;
					} else {
					try {
						Thread.sleep(TestConstants.WAIT_TIME_IN_MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					waitCount++;
					}
					Log.d("", "Waiting till count == 0 -----------------------------------"+waitCount);
					}
			
				if(testUtil.verifyWifiConnected(cmngr) && null != view){
					
					count=view.getCount();
					Assert.assertTrue(count >= 0);
				
			    }
				return view;
		
	
	}
	
	public void testPickDeviceWithAirplayService() throws InterruptedException{
		int i = 1;
		
		while(true){
		
			ListView view = getViewCount();
		  
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(testUtil.deviceWithAirplayService != null && testUtil.deviceWithAirplayService.contains(mTV)){					
					solo.clickInList(i);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
				}else{
					i++;
					continue;
				}	    	
				
				} else {
					break;
				}			
				
			mTV = ((MainActivity)getActivity()).mTV;
			Assert.assertTrue(mTV.isConnected());
				
				
			//verify connected service name is DIAL
			Assert.assertTrue(mTV.getServiceByName("AirPlay").isConnected());			
			
				Assert.assertFalse(mTV.getCapabilities().isEmpty());
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				testUtil.waitForCondition(new Condition() {
					
					@Override
					public boolean compare() {
						return mTV.isConnected();
					}
				}, "mTV.isConnected()");
				
				Assert.assertFalse(mTV.isConnected());
				i = i+1;
			}
	}
		
		public void testAPSMediaPlayerLaunchImage() throws InterruptedException{
						
			int i = 1;
						
			while(true){
							
				ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService deviceService = mTV.getServiceByName("AirPlay");
						
						solo.clickInList(i);
						testUtil.waitForCondition(new Condition() {
							
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
						
						Assert.assertTrue(mTV.isConnected());
						Assert.assertTrue(deviceService.isConnected());
												
						List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
						
						testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
						
						//Verify Photo or MediaPlayer.Display.Image Capability
					    if(null != testUtil.photo && actualDeviceAirPlayCapabilities.contains(TestConstants.Display_Image)){
					    	Assert.assertTrue(testUtil.photo.isEnabled());
					    	
					    	Assert.assertFalse(responseObject.isSuccess);
							Assert.assertFalse(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							
					    	solo.clickOnButton(testUtil.photo.getText().toString());
					    	
					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image");
					    	responseObject = mediaplayerfragment.testResponse;					    	
					    	
					    	Assert.assertTrue(responseObject.isSuccess);
					    	Assert.assertTrue(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertFalse(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image));
							}
					    } else{
						i++;
						continue;
					}
				} else {
					break;
				}			
					
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}
        
		public void testAPSMediaPlayerImageCloseCapability() throws InterruptedException{
			
			int i = 1;			
			
			while(true){
			
				ListView view = getViewCount();
								
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
						solo.clickInList(i);
						testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
						
						List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
						testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
						
						//Verify Close when photo is launched
					    if(null != testUtil.photo && actualDeviceAirPlayCapabilities.contains(TestConstants.Display_Image)){
					    						
					    	solo.clickOnButton(testUtil.photo.getText().toString());

					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image");
					    	
					    	responseObject = mediaplayerfragment.testResponse;					    	
					    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Display_image));
							
							 if(null != testUtil.close && actualDeviceAirPlayCapabilities.contains(TestConstants.Close)){
							    	Assert.assertTrue(testUtil.close.isEnabled());							    							    	
							    	solo.clickOnButton(testUtil.close.getText().toString());
							    	
							    	testUtil.waitForCondition(new Condition() {
										
										@Override
										public boolean compare() {
											return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media);
										}
									}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media");
							    	responseObject = mediaplayerfragment.testResponse;							    	
							    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media));									
									Assert.assertFalse(testUtil.close.isEnabled());
									}							 
							}	
					  
					    } else{
						i++;
						continue;
					}
				} else {
					break;
				}			
				
					
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}
		
		public void testAPSMediaPlayerVideoCloseCapability() throws InterruptedException{
		
		
		int i = 1;			
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					
				    //Verify close when video is launched
				    if(null != testUtil.video && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
						
				    	Assert.assertTrue(testUtil.video.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.video.getText().toString());
						testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video");
				    						    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	Assert.assertTrue(testUtil.pause.isEnabled());
				    	Assert.assertTrue(testUtil.stop.isEnabled());
				    	Assert.assertTrue(testUtil.rewind.isEnabled());
				    	Assert.assertTrue(testUtil.fastforward.isEnabled());
				    	Assert.assertTrue(testUtil.close.isEnabled());
				    	
				    	Assert.assertNotNull(MediaPlayerFragment.launchSession);
				    	
						 if(null != testUtil.close && actualDeviceAirPlayCapabilities.contains(TestConstants.Close)){
						    	Assert.assertTrue(testUtil.close.isEnabled());							    							    	
						    	solo.clickOnButton(testUtil.close.getText().toString());
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media");
						    	
						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media));									
								Assert.assertFalse(testUtil.close.isEnabled());
						    	Assert.assertFalse(testUtil.play.isEnabled());
						    	Assert.assertFalse(testUtil.pause.isEnabled());
						    	Assert.assertFalse(testUtil.stop.isEnabled());
						    	Assert.assertFalse(testUtil.rewind.isEnabled());
						    	Assert.assertFalse(testUtil.fastforward.isEnabled());
						    	Assert.assertNull(MediaPlayerFragment.launchSession);
								
								}							 
						}
				  
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}
			
public void testAPSMediaPlayerAudioCloseCapability() throws InterruptedException{
		
		int i = 1;			
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					//Verify close when audio is launched
				    if(null != testUtil.audio && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
						
				    	Assert.assertTrue(testUtil.audio.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.audio.getText().toString());
				    	testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio");
				    						    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	Assert.assertTrue(testUtil.pause.isEnabled());
				    	Assert.assertTrue(testUtil.stop.isEnabled());
				    	Assert.assertTrue(testUtil.rewind.isEnabled());
				    	Assert.assertTrue(testUtil.fastforward.isEnabled());
				    	Assert.assertTrue(testUtil.close.isEnabled());
				    	Assert.assertNotNull(MediaPlayerFragment.launchSession);
				    	
						 if(null != testUtil.close && actualDeviceAirPlayCapabilities.contains(TestConstants.Close)){
						    	Assert.assertTrue(testUtil.close.isEnabled());							    							    	
						    	solo.clickOnButton(testUtil.close.getText().toString());
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media");

						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Closed_Media));									
								Assert.assertFalse(testUtil.close.isEnabled());
						    	Assert.assertFalse(testUtil.play.isEnabled());
						    	Assert.assertFalse(testUtil.pause.isEnabled());
						    	Assert.assertFalse(testUtil.stop.isEnabled());
						    	Assert.assertFalse(testUtil.rewind.isEnabled());
						    	Assert.assertFalse(testUtil.fastforward.isEnabled());
						    	Assert.assertNull(MediaPlayerFragment.launchSession);
								
								}							 
						}
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}
		
			
		public void testAPSSupportedMediaPlayerCapability() throws InterruptedException{
			
			int i = 1;			
			
			while(true){
			
				ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService airPlayService = mTV.getServiceByName("AirPlay");
						List<String> actualDeviceAirPlayCapabilities = airPlayService.getCapabilities();
						Assert.assertFalse(actualDeviceAirPlayCapabilities.isEmpty());
						
						if (!Collections.disjoint(actualDeviceAirPlayCapabilities, expectedMediaPlayerCapabilities))
						{
							Assert.assertTrue(true);
						}else{
							Assert.assertTrue("The Connected Device must support atleast one AirPlay capabilities.", false);
						
						}			
						
						
					}else{
						i++;
						continue;
					}
				} else {
					break;
				}			
					
				
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}
		
		public void testAPSSupportedMediaControlCapability() throws InterruptedException{
			
			int i = 1;
			
			
			while(true){
			
ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService airPlayService = mTV.getServiceByName("AirPlay");
						List<String> actualDeviceAirPlayCapabilities = airPlayService.getCapabilities();
						Assert.assertFalse(actualDeviceAirPlayCapabilities.isEmpty());
						
						if (!Collections.disjoint(actualDeviceAirPlayCapabilities, expectedMediaPlayerCapabilities))
						{
							Assert.assertTrue(true);
						}else{
							Assert.assertTrue("The Connected Device must support atleast one AirPlay capabilities.", false);
						
						}	
														
						
					}else{
						i++;
						continue;
					}
				} else {
					break;
				}			
				
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
						i = i+1;
				}
		}
		
		public void testAPSMediaPlayerLaunchVideo() throws InterruptedException{
			
			int i = 1;
			
			
			while(true){
			
ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService airPlayService = mTV.getServiceByName("AirPlay");
										 	
						solo.clickInList(i);
						testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
						Assert.assertTrue(airPlayService.isConnected());
						
						
						List<String> actualDeviceAirPlayCapabilities = airPlayService.getCapabilities();
						
						if(actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
							Assert.assertTrue(true);
						}
						
						testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
						
						//Verify Video or MediaPlayer.Play.Video Capability
					    if(null != testUtil.video && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
					    	Assert.assertTrue(testUtil.video.isEnabled());
					    	
					    	Assert.assertFalse(responseObject.isSuccess);
							Assert.assertFalse(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							
					    	solo.clickOnButton(testUtil.video.getText().toString());
    	
					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video");
					    	
					    	responseObject = mediaplayerfragment.testResponse;
					    	
					    	Assert.assertTrue(responseObject.isSuccess);
					    	Assert.assertTrue(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertFalse(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video));
							}
					    } else{
						i++;
						continue;
					}
				} else {
					break;
				}			
				
					
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}
		
		public void testAPSMediaPlayerLaunchAudio() throws InterruptedException{
			
			int i = 1;
			
			
			while(true){
			
ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService airPlayService = mTV.getServiceByName("AirPlay");
										 	
						solo.clickInList(i);
						testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
						Assert.assertTrue(mTV.isConnected());
						Assert.assertTrue(airPlayService.isConnected());
						
						
						List<String> actualDeviceAirPlayCapabilities = airPlayService.getCapabilities();
						
						if(actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
							Assert.assertTrue(true);
						}
						
						testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
						
						//Verify Audio or MediaPlayer.Play.Audio Capability
					    if(null != testUtil.audio && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
					    	Assert.assertTrue(testUtil.audio.isEnabled());
					    	
					    	Assert.assertFalse(responseObject.isSuccess);
							Assert.assertFalse(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							
					    	solo.clickOnButton(testUtil.audio.getText().toString());
					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio");
					    						    	
					    	responseObject = mediaplayerfragment.testResponse;
					    	
					    	Assert.assertTrue(responseObject.isSuccess);
					    	Assert.assertTrue(responseObject.httpResponseCode == TestResponseObject.SuccessCode);
							Assert.assertFalse(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Default));
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio));
							}
					    } else{
						i++;
						continue;
					}
				} else {
					break;
				}						
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}
		
	public void testAPSMediaControlAudioPlayPauseCapability() throws InterruptedException{
		
		int i = 1;			
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					//Thread.sleep(10000);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					
					
					//Verify pause when video is launched/play
				    if(null != testUtil.audio && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
						
				    	Assert.assertTrue(testUtil.audio.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.audio.getText().toString());
						//Thread.sleep(20000);
				    	testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio");
				    	
				    			    						    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	Assert.assertTrue(testUtil.pause.isEnabled());
				    						    	
						 if(null != testUtil.pause && actualDeviceAirPlayCapabilities.contains(TestConstants.Pause)){
						    	Assert.assertTrue(testUtil.pause.isEnabled());
						    	solo.clickOnButton(testUtil.pause.getText().toString());
								//Thread.sleep(10000);
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media");
						    	
						    	
						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media));									
						    	Assert.assertTrue(testUtil.play.isEnabled());
						    	
						    	//After pause click on play 
						    	solo.clickOnButton(testUtil.play.getText().toString());
								//Thread.sleep(10000);
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");
						    	
						    	
								responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));									
								
								}							 
						}
				  
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}	
public void testAPSMediaControlVideoPlayPauseCapability() throws InterruptedException{
			
			int i = 1;			
			
			while(true){
			
ListView view = getViewCount();
				
				if(i <= view.getCount()){
										
					mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
					if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
					
						DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
						solo.clickInList(i);
						//Thread.sleep(10000);
						testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
						Assert.assertTrue(deviceService.isConnected());						
						
						List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
						testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
						
						
						
						//Verify pause when video is launched/play
					    if(null != testUtil.video && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
    						
					    	Assert.assertTrue(testUtil.video.isEnabled());
					    	
					    	solo.clickOnButton(testUtil.video.getText().toString());
							//Thread.sleep(20000);
					    						    	
					    		testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video");
							
					    						    	
					    	responseObject = mediaplayerfragment.testResponse;					    	
					    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video));
					    	Assert.assertTrue(testUtil.play.isEnabled());
					    	Assert.assertTrue(testUtil.pause.isEnabled());
					    						    	
							 if(null != testUtil.pause && actualDeviceAirPlayCapabilities.contains(TestConstants.Pause)){
							    	Assert.assertTrue(testUtil.pause.isEnabled());							    							    	
							    	solo.clickOnButton(testUtil.pause.getText().toString());
									//Thread.sleep(10000);
							    	
							    	testUtil.waitForCondition(new Condition() {
										
										@Override
										public boolean compare() {
											return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media);
										}
									}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media");
							    	
							    	responseObject = mediaplayerfragment.testResponse;							    	
							    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Paused_Media));									
							    	Assert.assertTrue(testUtil.play.isEnabled());
							    	
							    	//After pause click on play 
							    	solo.clickOnButton(testUtil.play.getText().toString());
									//Thread.sleep(10000);
							    	
							    	testUtil.waitForCondition(new Condition() {
										
										@Override
										public boolean compare() {
											return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
										}
									}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");
							    	
									responseObject = mediaplayerfragment.testResponse;	
									Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));	
									
									}							 
							}
					
					    } else{
						i++;
						continue;
					}
				} else {
					break;
				}			
				
					
					actionconnect = solo.getView(R.id.action_connect);
					solo.clickOnView(actionconnect);
					
					i = i+1;
				}
		}

public void testAPSMediaControlVideoPlayRewindCapability() throws InterruptedException{
	
	int i = 1;			
	
	while(true){
	
		ListView view = getViewCount();
		
		if(i <= view.getCount()){
								
			mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
			if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
			
				DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
				solo.clickInList(i);
				//Thread.sleep(10000);
				testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
				Assert.assertTrue(deviceService.isConnected());						
				
				List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
				testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
				
				
				
				//Verify pause when video is launched/play
			    if(null != testUtil.video && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
					
			    	Assert.assertTrue(testUtil.video.isEnabled());
			    	
			    	solo.clickOnButton(testUtil.video.getText().toString());
					//Thread.sleep(20000);
			    	
			    	testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video);
						}
					}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video");
			    	
			    						    	
			    	responseObject = mediaplayerfragment.testResponse;					    	
			    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video));
			    	Assert.assertTrue(testUtil.play.isEnabled());
			    	launchSession = MediaPlayerFragment.launchSession;
			    	Assert.assertNotNull(launchSession);
			    	Assert.assertTrue(testUtil.rewind.isEnabled());
			    						    	
					 if(null != testUtil.rewind && actualDeviceAirPlayCapabilities.contains(TestConstants.Rewind)){
					    	Assert.assertTrue(testUtil.rewind.isEnabled());							    							    	
					    	solo.clickOnButton(testUtil.rewind.getText().toString());
							//Thread.sleep(10000);
					    	
					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media");
					    	
					    	responseObject = mediaplayerfragment.testResponse;							    	
					    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media));									
					    	Assert.assertTrue(testUtil.play.isEnabled());
					    	Assert.assertNotNull(launchSession);
					    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
					    	
					    	//After pause click on play 
					    	solo.clickOnButton(testUtil.play.getText().toString());
							//Thread.sleep(10000);
					    	
					    	testUtil.waitForCondition(new Condition() {
								
								@Override
								public boolean compare() {
									return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
								}
							}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");

							responseObject = mediaplayerfragment.testResponse;	
							Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));
							Assert.assertNotNull(launchSession);
					    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
							
							}							 
					}
			
			    } else{
				i++;
				continue;
			}
		} else {
			break;
		}			
		
			
			actionconnect = solo.getView(R.id.action_connect);
			solo.clickOnView(actionconnect);
			
			i = i+1;
		}
}

	public void testAPSMediaControlAudioPlayRewindCapability() throws InterruptedException{
		
		int i = 1;	
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					//Thread.sleep(10000);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					
					
					//Verify pause when video is launched/play
				    if(null != testUtil.audio && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
						
				    	Assert.assertTrue(testUtil.audio.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.audio.getText().toString());
						//Thread.sleep(20000);
				    	
				    	testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio");
				    						    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	launchSession = MediaPlayerFragment.launchSession;
				    	Assert.assertNotNull(launchSession);
				    	Assert.assertTrue(testUtil.rewind.isEnabled());
				    						    	
						 if(null != testUtil.rewind && actualDeviceAirPlayCapabilities.contains(TestConstants.Rewind)){
						    	Assert.assertTrue(testUtil.rewind.isEnabled());							    							    	
						    	solo.clickOnButton(testUtil.rewind.getText().toString());
								//Thread.sleep(10000);
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media");

						    	
						    	
						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Rewind_Media));									
						    	Assert.assertTrue(testUtil.play.isEnabled());
						    	Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
						    	
						    	//After pause click on play 
						    	solo.clickOnButton(testUtil.play.getText().toString());
								//Thread.sleep(10000);
						    	

						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");

								responseObject = mediaplayerfragment.testResponse;	
								Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));
								Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
								
								}							 
						}
				
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}
	
	public void testAPSMediaControlVideoPlayFastForwardCapability() throws InterruptedException{
		
		int i = 1;			
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					//Thread.sleep(10000);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					
					
					//Verify pause when video is launched/play
				    if(null != testUtil.video && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Video)){
						
				    	Assert.assertTrue(testUtil.video.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.video.getText().toString());
						//Thread.sleep(20000);
				    	
				    	testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video");
				    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Video));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	launchSession = MediaPlayerFragment.launchSession;
				    	Assert.assertNotNull(launchSession);
				    	Assert.assertTrue(testUtil.rewind.isEnabled());
				    						    	
						 if(null != testUtil.fastforward && actualDeviceAirPlayCapabilities.contains(TestConstants.FastForward)){
						    	Assert.assertTrue(testUtil.fastforward.isEnabled());							    							    	
						    	solo.clickOnButton(testUtil.fastforward.getText().toString());
								//Thread.sleep(10000);
						    	
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media");
						    	
						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media));									
						    	Assert.assertTrue(testUtil.play.isEnabled());
						    	Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
						    	
						    	//After pause click on play 
						    	solo.clickOnButton(testUtil.play.getText().toString());
								//Thread.sleep(10000);
						    	
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");

								responseObject = mediaplayerfragment.testResponse;	
								Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));
								Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
								
								}							 
						}
				
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}
	
	public void testAPSMediaControlAudioPlayFastForwardCapability() throws InterruptedException{
		
		int i = 1;			
		
		while(true){
		
			ListView view = getViewCount();
			
			if(i <= view.getCount()){
									
				mTV = (ConnectableDevice) view.getItemAtPosition(i-1);
				if(!testUtil.deviceWithAirplayService.isEmpty() && testUtil.deviceWithAirplayService.contains(mTV)){	
				
					DeviceService deviceService = mTV.getServiceByName("AirPlay");										 	
					solo.clickInList(i);
					//Thread.sleep(10000);
					testUtil.waitForCondition(new Condition() {
						
						@Override
						public boolean compare() {
							return !mTV.isConnected();
						}
					}, "!mTV.isConnected()");
					Assert.assertTrue(mTV.isConnected());
					Assert.assertTrue(deviceService.isConnected());						
					
					List<String> actualDeviceAirPlayCapabilities = deviceService.getCapabilities();
					testUtil.getAssignedMediaButtons(((MainActivity)getActivity()).mSectionsPagerAdapter);
					
					
					
					//Verify pause when video is launched/play
				    if(null != testUtil.audio && actualDeviceAirPlayCapabilities.contains(TestConstants.Play_Audio)){
						
				    	Assert.assertTrue(testUtil.audio.isEnabled());
				    	
				    	solo.clickOnButton(testUtil.audio.getText().toString());
						//Thread.sleep(20000);
				    	
				    	
				    	testUtil.waitForCondition(new Condition() {
							
							@Override
							public boolean compare() {
								return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio);
							}
						}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio");
				    						    	
				    	responseObject = mediaplayerfragment.testResponse;					    	
				    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Play_Audio));
				    	Assert.assertTrue(testUtil.play.isEnabled());
				    	launchSession = MediaPlayerFragment.launchSession;
				    	Assert.assertNotNull(launchSession);
				    	Assert.assertTrue(testUtil.fastforward.isEnabled());
				    						    	
						 if(null != testUtil.fastforward && actualDeviceAirPlayCapabilities.contains(TestConstants.FastForward)){
						    	Assert.assertTrue(testUtil.fastforward.isEnabled());							    							    	
						    	solo.clickOnButton(testUtil.fastforward.getText().toString());
								//Thread.sleep(10000);
						    	
						    	
						    	testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media");
						    	
						    	responseObject = mediaplayerfragment.testResponse;							    	
						    	Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.FastForward_Media));									
						    	Assert.assertTrue(testUtil.play.isEnabled());
						    	Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
						    	
						    	//After pause click on play 
						    	solo.clickOnButton(testUtil.play.getText().toString());
								//Thread.sleep(10000);
						    							    	
						    		testUtil.waitForCondition(new Condition() {
									
									@Override
									public boolean compare() {
										return !mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media);
									}
								}, "!mediaplayerfragment.testResponse.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media");
						    		
								responseObject = mediaplayerfragment.testResponse;	
								Assert.assertTrue(responseObject.responseMessage.equalsIgnoreCase(TestResponseObject.Played_Media));
								Assert.assertNotNull(launchSession);
						    	Assert.assertSame(launchSession ,MediaPlayerFragment.launchSession);
								
								}							 
						}
				
				    } else{
					i++;
					continue;
				}
			} else {
				break;
			}			
			
				
				actionconnect = solo.getView(R.id.action_connect);
				solo.clickOnView(actionconnect);
				
				i = i+1;
			}
	}
	
}
