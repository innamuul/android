package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import mega.privacy.android.app.CameraSyncService;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaAttributes;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.TwoLineCheckPreference;
import mega.privacy.android.app.lollipop.tasks.ClearCacheTask;
import mega.privacy.android.app.lollipop.tasks.ClearOfflineTask;
import mega.privacy.android.app.lollipop.tasks.GetCacheSizeTask;
import mega.privacy.android.app.lollipop.tasks.GetOfflineSizeTask;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaNode;

//import android.support.v4.preference.PreferenceFragment;

@SuppressLint("NewApi")
public class SettingsFragmentLollipop extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener{

	Context context;
	private MegaApiAndroid megaApi;
	Handler handler = new Handler();
	
	private static int REQUEST_DOWNLOAD_FOLDER = 1000;
	private static int REQUEST_CODE_TREE_LOCAL_CAMERA = 1014;
	private static int REQUEST_CAMERA_FOLDER = 2000;
	private static int REQUEST_MEGA_CAMERA_FOLDER = 3000;
	private static int REQUEST_LOCAL_SECONDARY_MEDIA_FOLDER = 4000;
	private static int REQUEST_MEGA_SECONDARY_MEDIA_FOLDER = 5000;
	
	public static String CATEGORY_PIN_LOCK = "settings_pin_lock";
	public static String CATEGORY_STORAGE = "settings_storage";
	public static String CATEGORY_CAMERA_UPLOAD = "settings_camera_upload";
	public static String CATEGORY_ADVANCED_FEATURES = "advanced_features";

	public static String KEY_PIN_LOCK_ENABLE = "settings_pin_lock_enable";
	public static String KEY_PIN_LOCK_CODE = "settings_pin_lock_code";
	public static String KEY_STORAGE_DOWNLOAD_LOCATION = "settings_storage_download_location";
	public static String KEY_STORAGE_DOWNLOAD_LOCATION_SD_CARD_PREFERENCE = "settings_storage_download_location_sd_card_preference";
	public static String KEY_STORAGE_ASK_ME_ALWAYS = "settings_storage_ask_me_always";
	public static String KEY_STORAGE_ADVANCED_DEVICES = "settings_storage_advanced_devices";
	public static String KEY_CAMERA_UPLOAD_ON = "settings_camera_upload_on";
	public static String KEY_CAMERA_UPLOAD_HOW_TO = "settings_camera_upload_how_to_upload";
	public static String KEY_CAMERA_UPLOAD_CHARGING = "settings_camera_upload_charging";
	public static String KEY_KEEP_FILE_NAMES = "settings_keep_file_names";
	public static String KEY_CAMERA_UPLOAD_WHAT_TO = "settings_camera_upload_what_to_upload";
	public static String KEY_CAMERA_UPLOAD_CAMERA_FOLDER = "settings_local_camera_upload_folder";
	public static String KEY_CAMERA_UPLOAD_CAMERA_FOLDER_SDCARD = "settings_local_camera_upload_folder_sdcard";
	public static String KEY_CAMERA_UPLOAD_MEGA_FOLDER = "settings_mega_camera_folder";
	
	public static String KEY_SECONDARY_MEDIA_FOLDER_ON = "settings_secondary_media_folder_on";
	public static String KEY_LOCAL_SECONDARY_MEDIA_FOLDER = "settings_local_secondary_media_folder";
	public static String KEY_MEGA_SECONDARY_MEDIA_FOLDER = "settings_mega_secondary_media_folder";
	
	public static String KEY_CACHE = "settings_advanced_features_cache";
	public static String KEY_OFFLINE = "settings_advanced_features_offline";
	
	public static String KEY_ABOUT_PRIVACY_POLICY = "settings_about_privacy_policy";
	public static String KEY_ABOUT_TOS = "settings_about_terms_of_service";
	public static String KEY_ABOUT_SDK_VERSION = "settings_about_sdk_version";
	public static String KEY_ABOUT_APP_VERSION = "settings_about_app_version";
	public static String KEY_ABOUT_CODE_LINK = "settings_about_code_link";
	
	public final static int CAMERA_UPLOAD_WIFI_OR_DATA_PLAN = 1001;
	public final static int CAMERA_UPLOAD_WIFI = 1002;
	
	public final static int CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS = 1001;
	public final static int CAMERA_UPLOAD_FILE_UPLOAD_VIDEOS = 1002;
	public final static int CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS_AND_VIDEOS = 1003;
	
	public final static int STORAGE_DOWNLOAD_LOCATION_INTERNAL_SD_CARD = 1001;
	public final static int STORAGE_DOWNLOAD_LOCATION_EXTERNAL_SD_CARD = 1002;
	
	PreferenceCategory pinLockCategory;
	PreferenceCategory storageCategory;
	PreferenceCategory cameraUploadCategory;
	PreferenceCategory advancedFeaturesCategory;

	SwitchPreference pinLockEnableSwitch;
	TwoLineCheckPreference pinLockEnableCheck;
	Preference pinLockCode;
	Preference downloadLocation;
	Preference downloadLocationPreference;
	Preference cameraUploadOn;
	ListPreference cameraUploadHow;
	ListPreference cameraUploadWhat;
	TwoLineCheckPreference cameraUploadCharging;
	TwoLineCheckPreference keepFileNames;
	Preference localCameraUploadFolder;
	Preference localCameraUploadFolderSDCard;
	Preference megaCameraFolder;
	Preference aboutPrivacy;
	Preference aboutTOS;
	Preference aboutSDK;
	Preference aboutApp;
	Preference codeLink;
	Preference secondaryMediaFolderOn;
	Preference localSecondaryFolder;
	Preference megaSecondaryFolder;
	Preference advancedFeaturesCache;
	Preference advancedFeaturesOffline;
	
	TwoLineCheckPreference storageAskMeAlways;
	TwoLineCheckPreference storageAdvancedDevices;
	
	boolean cameraUpload = false;
	boolean secondaryUpload = false;
	boolean charging = false;
	boolean pinLock = false;
	boolean askMe = false;
	boolean fileNames = false;
	boolean advancedDevices = false;
	
	DatabaseHandler dbH;
	
	MegaPreferences prefs;
	String wifi = "";
	String camSyncLocalPath = "";
	boolean isExternalSDCard = false;
	Long camSyncHandle = null;
	MegaNode camSyncMegaNode = null;
	String camSyncMegaPath = "";
	String fileUpload = "";
	String downloadLocationPath = "";
	String ast = "";
	String pinLockCodeTxt = "";
	
	//Secondary Folder
	String localSecondaryFolderPath = "";
	Long handleSecondaryMediaFolder = null;
	MegaNode megaNodeSecondaryMediaFolder = null;
	String megaPathSecMediaFolder = "";

	int numberOfClicksSDK = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		
        if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		dbH = DatabaseHandler.getDbHandler(context);
		prefs = dbH.getPreferences();
		
		super.onCreate(savedInstanceState);	
        
        addPreferencesFromResource(R.xml.preferences);
        
        storageCategory = (PreferenceCategory) findPreference(CATEGORY_STORAGE);
		cameraUploadCategory = (PreferenceCategory) findPreference(CATEGORY_CAMERA_UPLOAD);	
		pinLockCategory = (PreferenceCategory) findPreference(CATEGORY_PIN_LOCK);
		advancedFeaturesCategory = (PreferenceCategory) findPreference(CATEGORY_ADVANCED_FEATURES);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			pinLockEnableSwitch = (SwitchPreference) findPreference(KEY_PIN_LOCK_ENABLE);
			pinLockEnableSwitch.setOnPreferenceClickListener(this);
		}
		else{
			pinLockEnableCheck = (TwoLineCheckPreference) findPreference(KEY_PIN_LOCK_ENABLE);
			pinLockEnableCheck.setOnPreferenceClickListener(this);
		}

		
		pinLockCode = findPreference(KEY_PIN_LOCK_CODE);
		pinLockCode.setOnPreferenceClickListener(this);
		
		downloadLocation = findPreference(KEY_STORAGE_DOWNLOAD_LOCATION);
		downloadLocation.setOnPreferenceClickListener(this);
		
		downloadLocationPreference = findPreference(KEY_STORAGE_DOWNLOAD_LOCATION_SD_CARD_PREFERENCE);
		downloadLocationPreference.setOnPreferenceClickListener(this);
		
		storageAskMeAlways = (TwoLineCheckPreference) findPreference(KEY_STORAGE_ASK_ME_ALWAYS);
		storageAskMeAlways.setOnPreferenceClickListener(this);
		
		storageAdvancedDevices = (TwoLineCheckPreference) findPreference(KEY_STORAGE_ADVANCED_DEVICES); 
		storageAdvancedDevices.setOnPreferenceClickListener(this);
		
		cameraUploadOn = findPreference(KEY_CAMERA_UPLOAD_ON);
		cameraUploadOn.setOnPreferenceClickListener(this);
		
		cameraUploadHow = (ListPreference) findPreference(KEY_CAMERA_UPLOAD_HOW_TO);
		cameraUploadHow.setOnPreferenceChangeListener(this);
		
		cameraUploadWhat = (ListPreference) findPreference(KEY_CAMERA_UPLOAD_WHAT_TO);
		cameraUploadWhat.setOnPreferenceChangeListener(this);
		
		cameraUploadCharging = (TwoLineCheckPreference) findPreference(KEY_CAMERA_UPLOAD_CHARGING);
		cameraUploadCharging.setOnPreferenceClickListener(this);
		
		keepFileNames = (TwoLineCheckPreference) findPreference(KEY_KEEP_FILE_NAMES);
		keepFileNames.setOnPreferenceClickListener(this);
		
		localCameraUploadFolder = findPreference(KEY_CAMERA_UPLOAD_CAMERA_FOLDER);	
		localCameraUploadFolder.setOnPreferenceClickListener(this);
		
		localCameraUploadFolderSDCard = findPreference(KEY_CAMERA_UPLOAD_CAMERA_FOLDER_SDCARD);
		localCameraUploadFolderSDCard.setOnPreferenceClickListener(this);
		
		megaCameraFolder = findPreference(KEY_CAMERA_UPLOAD_MEGA_FOLDER);	
		megaCameraFolder.setOnPreferenceClickListener(this);
		
		secondaryMediaFolderOn = findPreference(KEY_SECONDARY_MEDIA_FOLDER_ON);	
		secondaryMediaFolderOn.setOnPreferenceClickListener(this);
		
		localSecondaryFolder= findPreference(KEY_LOCAL_SECONDARY_MEDIA_FOLDER);	
		localSecondaryFolder.setOnPreferenceClickListener(this);
		
		megaSecondaryFolder= findPreference(KEY_MEGA_SECONDARY_MEDIA_FOLDER);	
		megaSecondaryFolder.setOnPreferenceClickListener(this);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			log("lollipop version check storage");
			storageCategory.removePreference(storageAdvancedDevices);
			File[] fs = context.getExternalFilesDirs(null);
			if (fs.length == 1){
				log("fs.length == 1");
				storageCategory.removePreference(downloadLocationPreference);
			}
			else{
				if (fs.length > 1){
					log("fs.length > 1");
					if (fs[1] == null){
						log("storageCategory.removePreference");
						storageCategory.removePreference(downloadLocationPreference);		
					}
					else{
						log("storageCategory.removePreference");
						storageCategory.removePreference(downloadLocation);
					}
				}
			}			
		}
		else{
			log("NOT lollipop version check storage");
			storageCategory.removePreference(downloadLocationPreference);
		}
		
		advancedFeaturesCache = findPreference(KEY_CACHE);	
		advancedFeaturesCache.setOnPreferenceClickListener(this);
		advancedFeaturesOffline = findPreference(KEY_OFFLINE);
		advancedFeaturesOffline.setOnPreferenceClickListener(this);
		
		aboutPrivacy = findPreference(KEY_ABOUT_PRIVACY_POLICY);
		aboutPrivacy.setOnPreferenceClickListener(this);
		
		aboutTOS = findPreference(KEY_ABOUT_TOS);
		aboutTOS.setOnPreferenceClickListener(this);

		aboutApp = findPreference(KEY_ABOUT_APP_VERSION);
		aboutApp.setOnPreferenceClickListener(this);
		aboutSDK = findPreference(KEY_ABOUT_SDK_VERSION);
		aboutSDK.setOnPreferenceClickListener(this);

		codeLink = findPreference(KEY_ABOUT_CODE_LINK);
		codeLink.setOnPreferenceClickListener(this);

		if (prefs == null){
			log("pref is NULL");
			dbH.setStorageAskAlways(false);
			
			File defaultDownloadLocation = null;
			if (Environment.getExternalStorageDirectory() != null){
				defaultDownloadLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.downloadDIR + "/");
			}
			else{
				defaultDownloadLocation = context.getFilesDir();
			}
			
			defaultDownloadLocation.mkdirs();
			
			dbH.setStorageDownloadLocation(defaultDownloadLocation.getAbsolutePath());
			
			dbH.setFirstTime(false);
			dbH.setCamSyncEnabled(false);
			dbH.setSecondaryUploadEnabled(false);
			dbH.setPinLockEnabled(false);
			dbH.setPinLockCode("");
			dbH.setStorageAdvancedDevices(false);
			cameraUpload = false;
			charging = true;
			fileNames = false;
			pinLock = false;
			askMe = true;
		}
		else{
			if (prefs.getCamSyncEnabled() == null){
				dbH.setCamSyncEnabled(false);
				cameraUpload = false;	
				charging = true;
				fileNames = false;
			}
			else{
				cameraUpload = Boolean.parseBoolean(prefs.getCamSyncEnabled());
				camSyncLocalPath = prefs.getCamSyncLocalPath();
				if (prefs.getCameraFolderExternalSDCard() != null){
					isExternalSDCard = Boolean.parseBoolean(prefs.getCameraFolderExternalSDCard());
				}
				String tempHandle = prefs.getCamSyncHandle();
				if(tempHandle!=null){
					camSyncHandle = Long.valueOf(tempHandle);
					if(camSyncHandle!=-1){						
						camSyncMegaNode = megaApi.getNodeByHandle(camSyncHandle);
						if(camSyncMegaNode!=null){
							camSyncMegaPath = camSyncMegaNode.getName();
						}	
						else
						{
							//The node for the Camera Sync no longer exists...
							dbH.setCamSyncHandle(-1);
							camSyncHandle = (long) -1;
							//Meanwhile is not created, set just the name
							camSyncMegaPath = CameraSyncService.CAMERA_UPLOADS;
						}
					}
					else{
						//Meanwhile is not created, set just the name
						camSyncMegaPath = CameraSyncService.CAMERA_UPLOADS;
					}
				}		
				else{
					dbH.setCamSyncHandle(-1);
					camSyncHandle = (long) -1;
					//Meanwhile is not created, set just the name
					camSyncMegaPath = CameraSyncService.CAMERA_UPLOADS;
				}				
				
				if (prefs.getCamSyncFileUpload() == null){
					dbH.setCamSyncFileUpload(MegaPreferences.ONLY_PHOTOS);
					fileUpload = getString(R.string.settings_camera_upload_only_photos);
				}
				else{
					switch(Integer.parseInt(prefs.getCamSyncFileUpload())){
						case MegaPreferences.ONLY_PHOTOS:{
							fileUpload = getString(R.string.settings_camera_upload_only_photos);
							cameraUploadWhat.setValueIndex(0);
							break;
						}
						case MegaPreferences.ONLY_VIDEOS:{
							fileUpload = getString(R.string.settings_camera_upload_only_videos);
							cameraUploadWhat.setValueIndex(1);
							break;
						}
						case MegaPreferences.PHOTOS_AND_VIDEOS:{
							fileUpload = getString(R.string.settings_camera_upload_photos_and_videos);
							cameraUploadWhat.setValueIndex(2);
							break;
						}
						default:{
							fileUpload = getString(R.string.settings_camera_upload_only_photos);
							cameraUploadWhat.setValueIndex(0);
							break;
						}
					}
				}
				
				if (Boolean.parseBoolean(prefs.getCamSyncWifi())){
					wifi = getString(R.string.cam_sync_wifi);
					cameraUploadHow.setValueIndex(1);
				}
				else{
					wifi = getString(R.string.cam_sync_data);
					cameraUploadHow.setValueIndex(0);
				}	
				
				if (prefs.getCamSyncCharging() == null){
					log("Charging NULLL");
					dbH.setCamSyncCharging(true);
					charging = true;
				}
				else{
					charging = Boolean.parseBoolean(prefs.getCamSyncCharging());
					log("Charging: "+charging);
				}
				
				if (prefs.getKeepFileNames() == null){
					dbH.setKeepFileNames(false);
					fileNames = false;
				}
				else{
					fileNames = Boolean.parseBoolean(prefs.getKeepFileNames());
				}
				
				if (camSyncLocalPath == null){
					File cameraDownloadLocation = null;
					if (Environment.getExternalStorageDirectory() != null){
						cameraDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					}
					
					cameraDownloadLocation.mkdirs();
					
					dbH.setCamSyncLocalPath(cameraDownloadLocation.getAbsolutePath());
					dbH.setCameraFolderExternalSDCard(false);
					
					camSyncLocalPath = cameraDownloadLocation.getAbsolutePath();
				}
				else{
					if (camSyncLocalPath.compareTo("") == 0){
						File cameraDownloadLocation = null;
						if (Environment.getExternalStorageDirectory() != null){
							cameraDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						}
						
						cameraDownloadLocation.mkdirs();
						
						dbH.setCamSyncLocalPath(cameraDownloadLocation.getAbsolutePath());
						dbH.setCameraFolderExternalSDCard(false);
						
						camSyncLocalPath = cameraDownloadLocation.getAbsolutePath();
					}
					else{
						File camFolder = new File(camSyncLocalPath);
						if (!isExternalSDCard){
							if(!camFolder.exists()){
								File cameraDownloadLocation = null;
								if (Environment.getExternalStorageDirectory() != null){
									cameraDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
								}
								
								cameraDownloadLocation.mkdirs();
								
								dbH.setCamSyncLocalPath(cameraDownloadLocation.getAbsolutePath());
								dbH.setCameraFolderExternalSDCard(false);
								
								camSyncLocalPath = cameraDownloadLocation.getAbsolutePath();
							}
						}
					}
				}
				
				//Check if the secondary sync is enabled
				if (prefs.getSecondaryMediaFolderEnabled() == null){
					dbH.setSecondaryUploadEnabled(false);
					secondaryUpload = false;						
				}
				else{
					secondaryUpload = Boolean.parseBoolean(prefs.getSecondaryMediaFolderEnabled());
					log("onCreate, secondary is: "+secondaryUpload);
					
					if(secondaryUpload){
						secondaryUpload=true;						
					}
					else{
						secondaryUpload=false;
					}
				}
			}
			
			if (prefs.getPinLockEnabled() == null){
				dbH.setPinLockEnabled(false);
				dbH.setPinLockCode("");
				pinLock = false;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					pinLockEnableSwitch.setChecked(pinLock);
				}
				else{
					pinLockEnableCheck.setChecked(pinLock);
				}
			}
			else{
				pinLock = Boolean.parseBoolean(prefs.getPinLockEnabled());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					pinLockEnableSwitch.setChecked(pinLock);
				}
				else{
					pinLockEnableCheck.setChecked(pinLock);
				}
				pinLockCodeTxt = prefs.getPinLockCode();
				if (pinLockCodeTxt == null){
					pinLockCodeTxt = "";
					dbH.setPinLockCode(pinLockCodeTxt);
				}
			}
			
			if (prefs.getStorageAskAlways() == null){
				dbH.setStorageAskAlways(false);
								
				File defaultDownloadLocation = null;
				if (Environment.getExternalStorageDirectory() != null){
					defaultDownloadLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.downloadDIR + "/");
				}
				else{
					defaultDownloadLocation = context.getFilesDir();
				}
				
				defaultDownloadLocation.mkdirs();
				
				dbH.setStorageDownloadLocation(defaultDownloadLocation.getAbsolutePath());
				
				askMe = false;
				downloadLocationPath = defaultDownloadLocation.getAbsolutePath();
				
				if (downloadLocation != null){
					downloadLocation.setSummary(downloadLocationPath);
				}
				if (downloadLocationPreference != null){
					downloadLocationPreference.setSummary(downloadLocationPath);
				}
			}
			else{
				askMe = Boolean.parseBoolean(prefs.getStorageAskAlways());
				if (prefs.getStorageDownloadLocation() == null){
					File defaultDownloadLocation = null;
					if (Environment.getExternalStorageDirectory() != null){
						defaultDownloadLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.downloadDIR + "/");
					}
					else{
						defaultDownloadLocation = context.getFilesDir();
					}
					
					defaultDownloadLocation.mkdirs();
					
					dbH.setStorageDownloadLocation(defaultDownloadLocation.getAbsolutePath());
					
					downloadLocationPath = defaultDownloadLocation.getAbsolutePath();
					
					if (downloadLocation != null){
						downloadLocation.setSummary(downloadLocationPath);
					}
					if (downloadLocationPreference != null){
						downloadLocationPreference.setSummary(downloadLocationPath);
					}
				}
				else{
					downloadLocationPath = prefs.getStorageDownloadLocation();
					
					if (downloadLocationPath.compareTo("") == 0){
						File defaultDownloadLocation = null;
						if (Environment.getExternalStorageDirectory() != null){
							defaultDownloadLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.downloadDIR + "/");
						}
						else{
							defaultDownloadLocation = context.getFilesDir();
						}
						
						defaultDownloadLocation.mkdirs();
						
						dbH.setStorageDownloadLocation(defaultDownloadLocation.getAbsolutePath());
						
						downloadLocationPath = defaultDownloadLocation.getAbsolutePath();
						
						if (downloadLocation != null){
							downloadLocation.setSummary(downloadLocationPath);
						}
						if (downloadLocationPreference != null){
							downloadLocationPreference.setSummary(downloadLocationPath);
						}
					}
				}
			}
			
			if (prefs.getStorageAdvancedDevices() == null){
				dbH.setStorageAdvancedDevices(false);
			}
			else{
				if(askMe){
					advancedDevices = Boolean.parseBoolean(prefs.getStorageAdvancedDevices());
				}
				else{
					advancedDevices = false;
					dbH.setStorageAdvancedDevices(false);
				}
			}
		}		

		advancedFeaturesCache.setSummary(getString(R.string.settings_advanced_features_calculating));
		advancedFeaturesOffline.setSummary(getString(R.string.settings_advanced_features_calculating));
		
		taskGetSizeCache();
		taskGetSizeOffline();

		if (cameraUpload){
			cameraUploadOn.setTitle(getString(R.string.settings_camera_upload_off));
			cameraUploadHow.setSummary(wifi);
			localCameraUploadFolder.setSummary(camSyncLocalPath);
			localCameraUploadFolderSDCard.setSummary(camSyncLocalPath);
			megaCameraFolder.setSummary(camSyncMegaPath);
			localSecondaryFolder.setSummary(localSecondaryFolderPath);
			megaSecondaryFolder.setSummary(megaPathSecMediaFolder);
			cameraUploadWhat.setSummary(fileUpload);
			downloadLocation.setSummary(downloadLocationPath);
			downloadLocationPreference.setSummary(downloadLocationPath);
			cameraUploadCharging.setChecked(charging);
			keepFileNames.setChecked(fileNames);
			cameraUploadCategory.addPreference(cameraUploadHow);
			cameraUploadCategory.addPreference(cameraUploadWhat);
			cameraUploadCategory.addPreference(cameraUploadCharging);
			cameraUploadCategory.addPreference(keepFileNames);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				File[] fs = context.getExternalFilesDirs(null);
				if (fs.length == 1){
					cameraUploadCategory.addPreference(localCameraUploadFolder);
					cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
				}
				else{
					if (fs.length > 1){
						if (fs[1] == null){
							cameraUploadCategory.addPreference(localCameraUploadFolder);
							cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
						}
						else{
							cameraUploadCategory.removePreference(localCameraUploadFolder);
							cameraUploadCategory.addPreference(localCameraUploadFolderSDCard);
						}
					}
				}			
			}
			else{
				cameraUploadCategory.addPreference(localCameraUploadFolder);
				cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
			}
			
			if(secondaryUpload){
				
				//Check if the node exists in MEGA
				String secHandle = prefs.getMegaHandleSecondaryFolder();
				if(secHandle!=null){
					if (secHandle.compareTo("") != 0){
						log("handleSecondaryMediaFolder NOT empty");
						handleSecondaryMediaFolder = Long.valueOf(secHandle);
						if(handleSecondaryMediaFolder!=null && handleSecondaryMediaFolder!=-1){
							megaNodeSecondaryMediaFolder = megaApi.getNodeByHandle(handleSecondaryMediaFolder);	
							if(megaNodeSecondaryMediaFolder!=null){
								megaPathSecMediaFolder = megaNodeSecondaryMediaFolder.getName();
							}
							else{
								megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
							}
						}
						else{
							megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
						}
					}
					else{
						log("handleSecondaryMediaFolder empty string");
						megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
					}	
					
				}
				else{
					log("handleSecondaryMediaFolder Null");
					dbH.setSecondaryFolderHandle(-1);
					handleSecondaryMediaFolder = (long) -1;
					megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
				}
				
				//check if the local secondary folder exists				
				localSecondaryFolderPath = prefs.getLocalPathSecondaryFolder();
				if(localSecondaryFolderPath==null || localSecondaryFolderPath.equals("-1")){
					log("secondary ON: invalid localSecondaryFolderPath");
					localSecondaryFolderPath = getString(R.string.settings_empty_folder);
					Toast.makeText(context, getString(R.string.secondary_media_service_error_local_folder), Toast.LENGTH_SHORT).show();
				}
				else
				{
					File checkSecondaryFile = new File(localSecondaryFolderPath);
					if(!checkSecondaryFile.exists()){
						log("secondary ON: the local folder does not exist");
						dbH.setSecondaryFolderPath("-1");
						//If the secondary folder does not exist
						Toast.makeText(context, getString(R.string.secondary_media_service_error_local_folder), Toast.LENGTH_SHORT).show();
						localSecondaryFolderPath = getString(R.string.settings_empty_folder);					

					}
				}
				
				megaSecondaryFolder.setSummary(megaPathSecMediaFolder);
				localSecondaryFolder.setSummary(localSecondaryFolderPath);
				secondaryMediaFolderOn.setTitle(getString(R.string.settings_secondary_upload_off));
				cameraUploadCategory.addPreference(localSecondaryFolder);
				cameraUploadCategory.addPreference(megaSecondaryFolder);
				
			}
			else{
				secondaryMediaFolderOn.setTitle(getString(R.string.settings_secondary_upload_on));
				cameraUploadCategory.removePreference(localSecondaryFolder);
				cameraUploadCategory.removePreference(megaSecondaryFolder);
			}
		}
		else{
			cameraUploadOn.setTitle(getString(R.string.settings_camera_upload_on));
			cameraUploadHow.setSummary("");
			localCameraUploadFolder.setSummary("");
			localCameraUploadFolderSDCard.setSummary("");
			megaCameraFolder.setSummary("");
			localSecondaryFolder.setSummary("");
			megaSecondaryFolder.setSummary("");
			cameraUploadWhat.setSummary("");
			cameraUploadCategory.removePreference(localCameraUploadFolder);
			cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
			cameraUploadCategory.removePreference(cameraUploadCharging);
			cameraUploadCategory.removePreference(keepFileNames);
			cameraUploadCategory.removePreference(megaCameraFolder);
			cameraUploadCategory.removePreference(cameraUploadHow);
			cameraUploadCategory.removePreference(cameraUploadWhat);
			
			//Remove Secondary Folder			
			cameraUploadCategory.removePreference(secondaryMediaFolderOn);
			cameraUploadCategory.removePreference(localSecondaryFolder);
			cameraUploadCategory.removePreference(megaSecondaryFolder);
		}
		
		if (pinLock){
//			pinLockEnableSwitch.setTitle(getString(R.string.settings_pin_lock_off));
			ast = "";
			if (pinLockCodeTxt.compareTo("") == 0){
				ast = getString(R.string.settings_pin_lock_code_not_set);
			}
			else{
				for (int i=0;i<pinLockCodeTxt.length();i++){
					ast = ast + "*";
				}
			}
			pinLockCode.setSummary(ast);
			pinLockCategory.addPreference(pinLockCode);
		}
		else{
//			pinLockEnableSwitch.setTitle(getString(R.string.settings_pin_lock_on));
			pinLockCategory.removePreference(pinLockCode);
		}
		
		storageAskMeAlways.setChecked(askMe);

		if (storageAskMeAlways.isChecked()){
			if (downloadLocation != null){
				downloadLocation.setEnabled(false);
				downloadLocation.setSummary("");
			}
			if (downloadLocationPreference != null){
				downloadLocationPreference.setEnabled(false);
				downloadLocationPreference.setSummary("");
			}
			storageAdvancedDevices.setChecked(advancedDevices);
		}
		else{
			if (downloadLocation != null){
				downloadLocation.setEnabled(true);
				downloadLocation.setSummary(downloadLocationPath);
			}
			if (downloadLocationPreference != null){
				downloadLocationPreference.setEnabled(true);
				downloadLocationPreference.setSummary(downloadLocationPath);
			}
			storageAdvancedDevices.setEnabled(false);
			storageAdvancedDevices.setChecked(false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if(v != null) {
			ListView lv = (ListView) v.findViewById(android.R.id.list);
			lv.setPadding(0, 0, 0, 0);
		}
		return v;
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		log("onPreferenceChange");
		
		prefs = dbH.getPreferences();
		if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_HOW_TO) == 0){
			switch (Integer.parseInt((String)newValue)){
				case CAMERA_UPLOAD_WIFI:{
					dbH.setCamSyncWifi(true);
					wifi = getString(R.string.cam_sync_wifi);
					cameraUploadHow.setValueIndex(1);
					break;
				}
				case CAMERA_UPLOAD_WIFI_OR_DATA_PLAN:{
					dbH.setCamSyncWifi(false);
					wifi = getString(R.string.cam_sync_data);
					cameraUploadHow.setValueIndex(0);
					break;
				}
			}
			cameraUploadHow.setSummary(wifi);
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					log("Now I start the service");
					context.startService(new Intent(context, CameraSyncService.class));		
				}
			}, 30 * 1000);
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_WHAT_TO) == 0){
			switch(Integer.parseInt((String)newValue)){
				case CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS:{
					dbH.setCamSyncFileUpload(MegaPreferences.ONLY_PHOTOS);
					fileUpload = getString(R.string.settings_camera_upload_only_photos);
					cameraUploadWhat.setValueIndex(0);
					break;
				}
				case CAMERA_UPLOAD_FILE_UPLOAD_VIDEOS:{
					dbH.setCamSyncFileUpload(MegaPreferences.ONLY_VIDEOS);
					fileUpload = getString(R.string.settings_camera_upload_only_videos);
					cameraUploadWhat.setValueIndex(1);
					break;
				}
				case CAMERA_UPLOAD_FILE_UPLOAD_PHOTOS_AND_VIDEOS:{
					dbH.setCamSyncFileUpload(MegaPreferences.PHOTOS_AND_VIDEOS);
					fileUpload = getString(R.string.settings_camera_upload_photos_and_videos);
					cameraUploadWhat.setValueIndex(2);
					break;
				}
			}
			cameraUploadWhat.setSummary(fileUpload);
			
			Intent photosVideosIntent = null;
			photosVideosIntent = new Intent(context, CameraSyncService.class);
			photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
			context.startService(photosVideosIntent);
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					log("Now I start the service");
					context.startService(new Intent(context, CameraSyncService.class));		
				}
			}, 30 * 1000);
		}
		else if (preference.getKey().compareTo(KEY_PIN_LOCK_CODE) == 0){
			pinLockCodeTxt = (String) newValue;
			dbH.setPinLockCode(pinLockCodeTxt);
			
			ast = "";
			if (pinLockCodeTxt.compareTo("") == 0){
				ast = getString(R.string.settings_pin_lock_code_not_set);
			}
			else{
				for (int i=0;i<pinLockCodeTxt.length();i++){
					ast = ast + "*";
				}
			}
			pinLockCode.setSummary(ast);
			
			pinLockCode.setSummary(ast);
			log("Object: " + newValue);
		}
		
		return true;
	}
	
	public void setCacheSize(String size){
		advancedFeaturesCache.setSummary(getString(R.string.settings_advanced_features_size, size));
	}
	
	public void setOfflineSize(String size){
		advancedFeaturesOffline.setSummary(getString(R.string.settings_advanced_features_size, size));
	}


	@Override
	public boolean onPreferenceClick(Preference preference) {
		log("onPreferenceClick");
		prefs = dbH.getPreferences();
		log("KEY = " + preference.getKey());
		if (preference.getKey().compareTo(KEY_ABOUT_SDK_VERSION) == 0){
			log("KEY_ABOUT_SDK_VERSION pressed");
			numberOfClicksSDK++;
			if (numberOfClicksSDK == 5){
				MegaAttributes attrs = dbH.getAttributes();
				if (attrs.getFileLogger() != null){
					try {
						if (Boolean.parseBoolean(attrs.getFileLogger()) == false) {
							dbH.setFileLogger(true);
							Util.setFileLogger(true);
							numberOfClicksSDK = 0;
							MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
							Toast.makeText(context, getString(R.string.settings_enable_logs), Toast.LENGTH_LONG).show();
							log("App Version: " + Util.getVersion(context));
						}
						else{
							dbH.setFileLogger(false);
							Util.setFileLogger(false);
							numberOfClicksSDK = 0;
							MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_FATAL);
							Toast.makeText(context, getString(R.string.settings_disable_logs), Toast.LENGTH_LONG).show();
						}
					}
					catch(Exception e){
						dbH.setFileLogger(true);
						Util.setFileLogger(true);
						numberOfClicksSDK = 0;
						Toast.makeText(context, getString(R.string.settings_enable_logs), Toast.LENGTH_LONG).show();
					}
				}
				else{
					dbH.setFileLogger(true);
					Util.setFileLogger(true);
					numberOfClicksSDK = 0;
					Toast.makeText(context, getString(R.string.settings_enable_logs), Toast.LENGTH_LONG).show();
				}
			}
		}
		else{
			numberOfClicksSDK = 0;
		}

		if (preference.getKey().compareTo(KEY_STORAGE_DOWNLOAD_LOCATION) == 0){
			log("KEY_STORAGE_DOWNLOAD_LOCATION pressed");
			Intent intent = new Intent(context, FileStorageActivityLollipop.class);
			intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
			intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
			startActivityForResult(intent, REQUEST_DOWNLOAD_FOLDER);
		}
		else if (preference.getKey().compareTo(KEY_STORAGE_DOWNLOAD_LOCATION_SD_CARD_PREFERENCE) == 0){
			log("KEY_STORAGE_DOWNLOAD_LOCATION_SD_CARD_PREFERENCE pressed");
			Dialog downloadLocationDialog;
			String[] sdCardOptions = getResources().getStringArray(R.array.settings_storage_download_location_array);
	        AlertDialog.Builder b=new AlertDialog.Builder(context);

			b.setTitle(getResources().getString(R.string.settings_storage_download_location));
			b.setItems(sdCardOptions, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					log("onClick");
					switch(which){
						case 0:{
							log("intent to FileStorageActivityLollipop");
							Intent intent = new Intent(context, FileStorageActivityLollipop.class);
							intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
							intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
							startActivityForResult(intent, REQUEST_DOWNLOAD_FOLDER);
							break;
						}
						case 1:{
							log("get External Files");
							File[] fs = context.getExternalFilesDirs(null);
							if (fs.length > 1){
								log("more than one");
								if (fs[1] != null){
									log("external not NULL");
									String path = fs[1].getAbsolutePath();
									dbH.setStorageDownloadLocation(path);
									if (downloadLocation != null){
										downloadLocation.setSummary(path);
									}
									if (downloadLocationPreference != null){
										downloadLocationPreference.setSummary(path);
									}
								}
								else{
									log("external NULL -- intent to FileStorageActivityLollipop");
									Intent intent = new Intent(context, FileStorageActivityLollipop.class);
									intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
									intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
									startActivityForResult(intent, REQUEST_DOWNLOAD_FOLDER);
								}
							}
							break;
						}
					}
				}
			});
			b.setNegativeButton(getResources().getString(R.string.general_cancel), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					log("Cancel dialog");
					dialog.cancel();
				}
			});
			downloadLocationDialog = b.create();
			downloadLocationDialog.show();
			log("downloadLocationDialog shown");
		}
		else if (preference.getKey().compareTo(KEY_CACHE) == 0){
			log("Clear Cache!");

			ClearCacheTask clearCacheTask = new ClearCacheTask(context);
			clearCacheTask.execute();
		}
		else if (preference.getKey().compareTo(KEY_OFFLINE) == 0){
			log("Clear Offline!");

			ClearOfflineTask clearOfflineTask = new ClearOfflineTask(context);
			clearOfflineTask.execute();
		}
		else if (preference.getKey().compareTo(KEY_SECONDARY_MEDIA_FOLDER_ON) == 0){
			log("Changing the secondary uploads");
			dbH.setSecSyncTimeStamp(0);			
			secondaryUpload = !secondaryUpload;
			if (secondaryUpload){
				dbH.setSecondaryUploadEnabled(true);
				secondaryMediaFolderOn.setTitle(getString(R.string.settings_secondary_upload_off));
				//Check MEGA folder
				if(handleSecondaryMediaFolder!=null){
					if(handleSecondaryMediaFolder==-1){
						megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
					}
				}		
				else{
					megaPathSecMediaFolder = CameraSyncService.SECONDARY_UPLOADS;
				}
				
				megaSecondaryFolder.setSummary(megaPathSecMediaFolder);			
				
				prefs = dbH.getPreferences();
				localSecondaryFolderPath = prefs.getLocalPathSecondaryFolder();
				
				//Check local folder				
				if(localSecondaryFolderPath!=null){
					log("Secondary folder in database: "+localSecondaryFolderPath);
					File checkSecondaryFile = new File(localSecondaryFolderPath);
					if(!checkSecondaryFile.exists()){					
						dbH.setSecondaryFolderPath("-1");
						//If the secondary folder does not exist any more
						Toast.makeText(context, getString(R.string.secondary_media_service_error_local_folder), Toast.LENGTH_SHORT).show();
						
						if(localSecondaryFolderPath==null || localSecondaryFolderPath.equals("-1")){
							localSecondaryFolderPath = getString(R.string.settings_empty_folder);						
						}					
					}
				}
				else{
					dbH.setSecondaryFolderPath("-1");
					//If the secondary folder does not exist any more
					Toast.makeText(context, getString(R.string.secondary_media_service_error_local_folder), Toast.LENGTH_SHORT).show();
					localSecondaryFolderPath = getString(R.string.settings_empty_folder);
				}

				localSecondaryFolder.setSummary(localSecondaryFolderPath);
				
				cameraUploadCategory.addPreference(localSecondaryFolder);
				cameraUploadCategory.addPreference(megaSecondaryFolder);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						log("Now I start the service");
						context.startService(new Intent(context, CameraSyncService.class));		
					}
				}, 5 * 1000);
				
			}
			else{				

				dbH.setSecondaryUploadEnabled(false);
				
				secondaryMediaFolderOn.setTitle(getString(R.string.settings_secondary_upload_on));
				cameraUploadCategory.removePreference(localSecondaryFolder);
				cameraUploadCategory.removePreference(megaSecondaryFolder);
			}			
		}
		else if (preference.getKey().compareTo(KEY_STORAGE_ADVANCED_DEVICES) == 0){
			log("Changing the advances devices preference");
			advancedDevices = !advancedDevices;
			if(advancedDevices){
				if(Util.getExternalCardPath()==null){
					Toast.makeText(context, getString(R.string.no_external_SD_card_detected), Toast.LENGTH_SHORT).show();
					storageAdvancedDevices.setChecked(false);
					advancedDevices = !advancedDevices;
				}
			}
			else{
				log("No advanced devices");
			}
			
			dbH.setStorageAdvancedDevices(advancedDevices);
		}
		else if (preference.getKey().compareTo(KEY_LOCAL_SECONDARY_MEDIA_FOLDER) == 0){
			log("Changing the local folder for secondary uploads");
			Intent intent = new Intent(context, FileStorageActivityLollipop.class);
			intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
			intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
			startActivityForResult(intent, REQUEST_LOCAL_SECONDARY_MEDIA_FOLDER);
		}
		else if (preference.getKey().compareTo(KEY_MEGA_SECONDARY_MEDIA_FOLDER) == 0){
			log("Changing the MEGA folder for secondary uploads");
			Intent intent = new Intent(context, FileExplorerActivityLollipop.class);
			intent.setAction(FileExplorerActivityLollipop.ACTION_CHOOSE_MEGA_FOLDER_SYNC);
			startActivityForResult(intent, REQUEST_MEGA_SECONDARY_MEDIA_FOLDER);
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_ON) == 0){
			log("Changing camera upload");
			dbH.setCamSyncTimeStamp(0);			
			cameraUpload = !cameraUpload;			
			
			if (cameraUpload){
				log("Camera ON");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					log("Lollipop version");
					boolean hasStoragePermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
					if (!hasStoragePermission) {
						log("No storage permission");
						ActivityCompat.requestPermissions((ManagerActivityLollipop)context,
				                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
					}
					
					boolean hasCameraPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
	        		if (!hasCameraPermission){
						log("No camera permission");
	        			ActivityCompat.requestPermissions((ManagerActivityLollipop)context,
				                new String[]{Manifest.permission.CAMERA},
								Constants.REQUEST_CAMERA);
	        		}
				}
				
				if (camSyncLocalPath!=null){
					File checkFile = new File(camSyncLocalPath);
					if(!checkFile.exists()){
						//Local path does not exist, then Camera folder by default
						log("local path not exist, default camera folder");
						File cameraDownloadLocation = null;
						if (Environment.getExternalStorageDirectory() != null){
							cameraDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						}
						
						cameraDownloadLocation.mkdirs();
						
						dbH.setCamSyncLocalPath(cameraDownloadLocation.getAbsolutePath());
						dbH.setCameraFolderExternalSDCard(false);
						
						camSyncLocalPath = cameraDownloadLocation.getAbsolutePath();
						
					} 	
				}
				else{
					log("local parh is NULL");
					//Local path not valid = null, then Camera folder by default
					File cameraDownloadLocation = null;
					if (Environment.getExternalStorageDirectory() != null){
						cameraDownloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					}
					
					cameraDownloadLocation.mkdirs();
					
					dbH.setCamSyncLocalPath(cameraDownloadLocation.getAbsolutePath());
					dbH.setCameraFolderExternalSDCard(false);
					
					camSyncLocalPath = cameraDownloadLocation.getAbsolutePath();
				}

				if(camSyncHandle!=null){
					if(camSyncHandle==-1){
						camSyncMegaPath = CameraSyncService.CAMERA_UPLOADS;
					}
				}		
				else{
					camSyncMegaPath = CameraSyncService.CAMERA_UPLOADS;
				}
				
				megaCameraFolder.setSummary(camSyncMegaPath);
					
				dbH.setCamSyncFileUpload(MegaPreferences.ONLY_PHOTOS);
				fileUpload = getString(R.string.settings_camera_upload_only_photos);
				cameraUploadWhat.setValueIndex(0);
				
				dbH.setCamSyncWifi(true);
				wifi = getString(R.string.cam_sync_wifi);
				cameraUploadHow.setValueIndex(1);
				
				dbH.setCamSyncCharging(true);
				charging = true;
				cameraUploadCharging.setChecked(charging);
				
				dbH.setCamSyncEnabled(true);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						log("Now I start the service");
						context.startService(new Intent(context, CameraSyncService.class));		
					}
				}, 5 * 1000);
				
				cameraUploadOn.setTitle(getString(R.string.settings_camera_upload_off));
				cameraUploadHow.setSummary(wifi);
				localCameraUploadFolder.setSummary(camSyncLocalPath);
				localCameraUploadFolderSDCard.setSummary(camSyncLocalPath);
					
				cameraUploadWhat.setSummary(fileUpload);
				cameraUploadCategory.addPreference(cameraUploadHow);
				cameraUploadCategory.addPreference(cameraUploadWhat);
				cameraUploadCategory.addPreference(cameraUploadCharging);
				cameraUploadCategory.addPreference(keepFileNames);
				cameraUploadCategory.addPreference(megaCameraFolder);								
				cameraUploadCategory.addPreference(secondaryMediaFolderOn);
				cameraUploadCategory.removePreference(localSecondaryFolder);
				cameraUploadCategory.removePreference(megaSecondaryFolder);		
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					File[] fs = context.getExternalFilesDirs(null);
					if (fs.length == 1){
						cameraUploadCategory.addPreference(localCameraUploadFolder);
						cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
					}
					else{
						if (fs.length > 1){
							if (fs[1] == null){
								cameraUploadCategory.addPreference(localCameraUploadFolder);
								cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
							}
							else{
								cameraUploadCategory.removePreference(localCameraUploadFolder);
								cameraUploadCategory.addPreference(localCameraUploadFolderSDCard);
							}
						}
					}			
				}
				else{
					cameraUploadCategory.addPreference(localCameraUploadFolder);
					cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
				}

			}
			else{
				log("Camera OFF");
				dbH.setCamSyncEnabled(false);
				dbH.setSecondaryUploadEnabled(false);
				secondaryUpload = false;
				Intent stopIntent = null;
				stopIntent = new Intent(context, CameraSyncService.class);
				stopIntent.setAction(CameraSyncService.ACTION_STOP);
				context.startService(stopIntent);
				
				cameraUploadOn.setTitle(getString(R.string.settings_camera_upload_on));
				secondaryMediaFolderOn.setTitle(getString(R.string.settings_secondary_upload_on));
				cameraUploadCategory.removePreference(cameraUploadHow);
				cameraUploadCategory.removePreference(cameraUploadWhat);
				cameraUploadCategory.removePreference(localCameraUploadFolder);
				cameraUploadCategory.removePreference(localCameraUploadFolderSDCard);
				cameraUploadCategory.removePreference(cameraUploadCharging);
				cameraUploadCategory.removePreference(keepFileNames);
				cameraUploadCategory.removePreference(megaCameraFolder);
				cameraUploadCategory.removePreference(secondaryMediaFolderOn);
				cameraUploadCategory.removePreference(localSecondaryFolder);
				cameraUploadCategory.removePreference(megaSecondaryFolder);
			}			
		}
		else if (preference.getKey().compareTo(KEY_PIN_LOCK_ENABLE) == 0){
			log("KEY_PIN_LOCK_ENABLE");
			pinLock = !pinLock;
			if (pinLock){
				//Intent to set the PIN
				log("call to showPAnelSetPinLock");
				((ManagerActivityLollipop)getActivity()).showPanelSetPinLock();
			}
			else{
				dbH.setPinLockEnabled(false);
				dbH.setPinLockCode("");
//				pinLockEnableSwitch.setTitle(getString(R.string.settings_pin_lock_on));
				pinLockCategory.removePreference(pinLockCode);
			}
		}
		else if (preference.getKey().compareTo(KEY_PIN_LOCK_CODE) == 0){
			//Intent to reset the PIN
			log("KEY_PIN_LOCK_CODE");
			resetPinLock();
		}
		else if (preference.getKey().compareTo(KEY_STORAGE_ASK_ME_ALWAYS) == 0){
			log("KEY_STORAGE_ASK_ME_ALWAYS");
			askMe = storageAskMeAlways.isChecked();
			dbH.setStorageAskAlways(askMe);
			if (storageAskMeAlways.isChecked()){
				log("storageAskMeAlways is checked!");
				if (downloadLocation != null){
					downloadLocation.setEnabled(false);
					downloadLocation.setSummary("");
				}
				if (downloadLocationPreference != null){
					downloadLocationPreference.setEnabled(false);
					downloadLocationPreference.setSummary("");
				}
				storageAdvancedDevices.setEnabled(true);
			}
			else{
				log("storageAskMeAlways NOT checked!");
				if (downloadLocation != null){
					downloadLocation.setEnabled(true);
					downloadLocation.setSummary(downloadLocationPath);
				}
				if (downloadLocationPreference != null){
					downloadLocationPreference.setEnabled(true);
					downloadLocationPreference.setSummary(downloadLocationPath);
				}
				storageAdvancedDevices.setEnabled(false);
			}
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_CHARGING) == 0){
			log("KEY_CAMERA_UPLOAD_CHARGING");
			charging = cameraUploadCharging.isChecked();
			dbH.setCamSyncCharging(charging);
		}
		else if(preference.getKey().compareTo(KEY_KEEP_FILE_NAMES) == 0){
			log("KEY_KEEP_FILE_NAMES");
			fileNames = keepFileNames.isChecked();
			dbH.setKeepFileNames(fileNames);
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_CAMERA_FOLDER) == 0){
			log("Changing the LOCAL folder for camera uploads");
			Intent intent = new Intent(context, FileStorageActivityLollipop.class);
			intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
			intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
			intent.putExtra(FileStorageActivityLollipop.EXTRA_CAMERA_FOLDER,true);
			startActivityForResult(intent, REQUEST_CAMERA_FOLDER);
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_CAMERA_FOLDER_SDCARD) == 0){
			log("KEY_CAMERA_UPLOAD_CAMERA_FOLDER_SDCARD");
			Dialog localCameraDialog;
			String[] sdCardOptions = getResources().getStringArray(R.array.settings_storage_download_location_array);
	        AlertDialog.Builder b=new AlertDialog.Builder(context);

			b.setTitle(getResources().getString(R.string.settings_storage_download_location));
			b.setItems(sdCardOptions, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which){
						case 0:{
							Intent intent = new Intent(context, FileStorageActivityLollipop.class);
							intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
							intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
							intent.putExtra(FileStorageActivityLollipop.EXTRA_CAMERA_FOLDER, true);
							startActivityForResult(intent, REQUEST_CAMERA_FOLDER);
							break;
						}
						case 1:{
							File[] fs = context.getExternalFilesDirs(null);
							if (fs.length > 1){		
								if (fs[1] != null){
									Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
									startActivityForResult(intent, REQUEST_CODE_TREE_LOCAL_CAMERA);
								}
								else{
									Intent intent = new Intent(context, FileStorageActivityLollipop.class);
									intent.setAction(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
									intent.putExtra(FileStorageActivityLollipop.EXTRA_FROM_SETTINGS, true);
									intent.putExtra(FileStorageActivityLollipop.EXTRA_CAMERA_FOLDER, true);
									startActivityForResult(intent, REQUEST_CAMERA_FOLDER);
								}
							}
							break;
						}
					}
				}
			});
			b.setNegativeButton(getResources().getString(R.string.general_cancel), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			localCameraDialog = b.create();
			localCameraDialog.show();
		}
		else if (preference.getKey().compareTo(KEY_CAMERA_UPLOAD_MEGA_FOLDER) == 0){
			log("Changing the MEGA folder for camera uploads");
			Intent intent = new Intent(context, FileExplorerActivityLollipop.class);
			intent.setAction(FileExplorerActivityLollipop.ACTION_CHOOSE_MEGA_FOLDER_SYNC);
			startActivityForResult(intent, REQUEST_MEGA_CAMERA_FOLDER);
		}
		else if (preference.getKey().compareTo(KEY_ABOUT_PRIVACY_POLICY) == 0){
			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
			viewIntent.setData(Uri.parse("https://mega.nz/mobile_privacy.html"));
			startActivity(viewIntent);
		}
		else if (preference.getKey().compareTo(KEY_ABOUT_TOS) == 0){
			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
			viewIntent.setData(Uri.parse("https://mega.nz/mobile_terms.html"));
			startActivity(viewIntent);
		}
		else if(preference.getKey().compareTo(KEY_ABOUT_CODE_LINK) == 0){
			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
			viewIntent.setData(Uri.parse("https://github.com/meganz/android"));
			startActivity(viewIntent);
		}
		
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log("onActivityResult");
		
		prefs = dbH.getPreferences();
		log("REQUEST CODE: " + requestCode + "___RESULT CODE: " + resultCode);
		if (requestCode == REQUEST_CODE_TREE_LOCAL_CAMERA && resultCode == Activity.RESULT_OK){
			if (intent == null){
				log("intent NULL");
				return;
			}
			
			Uri treeUri = intent.getData();
			
			if (dbH == null){
				dbH = DatabaseHandler.getDbHandler(context);
			}
			
			dbH.setUriExternalSDCard(treeUri.toString());
			dbH.setCameraFolderExternalSDCard(true);
			
			DocumentFile pickedDir = DocumentFile.fromTreeUri(context, treeUri);
			String pickedDirName = pickedDir.getName();
			if(pickedDirName!=null){
				dbH.setCamSyncLocalPath(pickedDir.getName());
				localCameraUploadFolder.setSummary(pickedDir.getName());
				localCameraUploadFolderSDCard.setSummary(pickedDir.getName());

				dbH.setCamSyncLocalPath(pickedDir.getName());
				localCameraUploadFolder.setSummary(pickedDir.getName());
				localCameraUploadFolderSDCard.setSummary(pickedDir.getName());
			}
			else{
				log("pickedDirNAme NULL");
			}

			dbH.setCamSyncTimeStamp(0);
			
			Intent photosVideosIntent = null;
			photosVideosIntent = new Intent(context, CameraSyncService.class);
			photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
			context.startService(photosVideosIntent);
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					log("Now I start the service");
					context.startService(new Intent(context, CameraSyncService.class));		
				}
			}, 5 * 1000);
		}
		else if(requestCode == Constants.SET_PIN){
			if(resultCode == Activity.RESULT_OK) {
				log("Set PIN Ok");

				afterSetPinLock();
			}
			else{
				log("Set PIN ERROR");
			}
		}
		else if (requestCode == REQUEST_DOWNLOAD_FOLDER && resultCode == Activity.RESULT_CANCELED && intent != null){
			log("REQUEST_DOWNLOAD_FOLDER - canceled");
		}
		else if (requestCode == REQUEST_DOWNLOAD_FOLDER && resultCode == Activity.RESULT_OK && intent != null) {
			String path = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			dbH.setStorageDownloadLocation(path);
			if (downloadLocation != null){
				downloadLocation.setSummary(path);
			}
			if (downloadLocationPreference != null){
				downloadLocationPreference.setSummary(path);
			}
		}		
		else if (requestCode == REQUEST_CAMERA_FOLDER && resultCode == Activity.RESULT_OK && intent != null){
			//Local folder to sync
			String cameraPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			dbH.setCamSyncLocalPath(cameraPath);
			dbH.setCameraFolderExternalSDCard(false);
			localCameraUploadFolder.setSummary(cameraPath);
			localCameraUploadFolderSDCard.setSummary(cameraPath);
			dbH.setCamSyncTimeStamp(0);
			
			Intent photosVideosIntent = null;
			photosVideosIntent = new Intent(context, CameraSyncService.class);
			photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
			context.startService(photosVideosIntent);
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					log("Now I start the service");
					context.startService(new Intent(context, CameraSyncService.class));		
				}
			}, 5 * 1000);
		}
		else if (requestCode == REQUEST_LOCAL_SECONDARY_MEDIA_FOLDER && resultCode == Activity.RESULT_OK && intent != null){
			//Local folder to sync
			String secondaryPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			
			dbH.setSecondaryFolderPath(secondaryPath);
			localSecondaryFolder.setSummary(secondaryPath);
			dbH.setSecSyncTimeStamp(0);
			
			Intent photosVideosIntent = null;
			photosVideosIntent = new Intent(context, CameraSyncService.class);
			photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
			context.startService(photosVideosIntent);
			
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					log("Now I start the service");
					context.startService(new Intent(context, CameraSyncService.class));		
				}
			}, 5 * 1000);
		}		
		else if (requestCode == REQUEST_MEGA_SECONDARY_MEDIA_FOLDER && resultCode == Activity.RESULT_OK && intent != null){
			//Mega folder to sync
			
			Long handle = intent.getLongExtra("SELECT_MEGA_FOLDER",-1);
			if(handle!=-1){
				dbH.setSecondaryFolderHandle(handle);						
				
				handleSecondaryMediaFolder = handle;
				megaNodeSecondaryMediaFolder = megaApi.getNodeByHandle(handleSecondaryMediaFolder);
				megaPathSecMediaFolder = megaNodeSecondaryMediaFolder.getName();
				
				megaSecondaryFolder.setSummary(megaPathSecMediaFolder);
				dbH.setSecSyncTimeStamp(0);
				
				Intent photosVideosIntent = null;
				photosVideosIntent = new Intent(context, CameraSyncService.class);
				photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
				context.startService(photosVideosIntent);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						log("Now I start the service");
						context.startService(new Intent(context, CameraSyncService.class));		
					}
				}, 5 * 1000);
				
				log("Mega folder to secondary uploads change!!");
			}
			else{
				log("Error choosing the secondary uploads");
			}
			
		}
		else if (requestCode == REQUEST_MEGA_CAMERA_FOLDER && resultCode == Activity.RESULT_OK && intent != null){
			//Mega folder to sync
			
			Long handle = intent.getLongExtra("SELECT_MEGA_FOLDER",-1);
			if(handle!=-1){
				dbH.setCamSyncHandle(handle);
				
				camSyncHandle = handle;
				camSyncMegaNode = megaApi.getNodeByHandle(camSyncHandle);	
				camSyncMegaPath = camSyncMegaNode.getName();
				
				megaCameraFolder.setSummary(camSyncMegaPath);
				dbH.setCamSyncTimeStamp(0);
				
				Intent photosVideosIntent = null;
				photosVideosIntent = new Intent(context, CameraSyncService.class);
				photosVideosIntent.setAction(CameraSyncService.ACTION_LIST_PHOTOS_VIDEOS_NEW_FOLDER);
				context.startService(photosVideosIntent);
				
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						log("Now I start the service");
						context.startService(new Intent(context, CameraSyncService.class));		
					}
				}, 5 * 1000);
				
				log("Mega folder to sync the Camera CHANGED!!");
			}
			else{
				log("Error choosing the Mega folder to sync the Camera");
			}
			
		}
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    log("onResume");
	    prefs=dbH.getPreferences();
	    
	    if (prefs.getPinLockEnabled() == null){
			dbH.setPinLockEnabled(false);
			dbH.setPinLockCode("");
			pinLock = false;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				pinLockEnableSwitch.setChecked(pinLock);
			}
			else{
				pinLockEnableCheck.setChecked(pinLock);
			}
		}
		else{
			pinLock = Boolean.parseBoolean(prefs.getPinLockEnabled());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				pinLockEnableSwitch.setChecked(pinLock);
			}
			else{
				pinLockEnableCheck.setChecked(pinLock);
			}
			pinLockCodeTxt = prefs.getPinLockCode();
			if (pinLockCodeTxt == null){
				pinLockCodeTxt = "";
				dbH.setPinLockCode(pinLockCodeTxt);
			}
		}	    

		taskGetSizeCache();
		taskGetSizeOffline();
	}
	
	public void afterSetPinLock(){
		log("afterSetPinLock");

		prefs=dbH.getPreferences();
		pinLockCodeTxt = prefs.getPinLockCode();
		if (pinLockCodeTxt == null){
			pinLockCodeTxt = "";
			dbH.setPinLockCode(pinLockCodeTxt);

		}
//		pinLockEnableSwitch.setTitle(getString(R.string.settings_pin_lock_off));
		ast = "";
		if (pinLockCodeTxt.compareTo("") == 0){
			ast = getString(R.string.settings_pin_lock_code_not_set);
		}
		else{
			for (int i=0;i<pinLockCodeTxt.length();i++){
				ast = ast + "*";
			}
		}
		pinLockCode.setSummary(ast);
		pinLockCategory.addPreference(pinLockCode);
		dbH.setPinLockEnabled(true);
	}

	public void taskGetSizeCache (){
		log("taskGetSizeCache");
		GetCacheSizeTask getCacheSizeTask = new GetCacheSizeTask(context);
		getCacheSizeTask.execute();
	}

	public void taskGetSizeOffline (){
		log("taskGetSizeOffline");
		GetOfflineSizeTask getOfflineSizeTask = new GetOfflineSizeTask(context);
		getOfflineSizeTask.execute();
	}

	public void intentToPinLock(){
		log("intentToPinLock");
		Intent intent = new Intent(context, PinLockActivityLollipop.class);
		intent.setAction(PinLockActivityLollipop.ACTION_SET_PIN_LOCK);
		this.startActivityForResult(intent, Constants.SET_PIN);
	}

	public void resetPinLock(){
		log("resetPinLock");
		Intent intent = new Intent(context, PinLockActivityLollipop.class);
		intent.setAction(PinLockActivityLollipop.ACTION_RESET_PIN_LOCK);
		this.startActivity(intent);
	}

	private static void log(String log) {
		Util.log("SettingsFragmentLollipop", log);
	}
}
