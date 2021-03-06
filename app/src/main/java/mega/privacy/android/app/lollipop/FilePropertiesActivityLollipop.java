package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nirhart.parallaxscroll.views.ParallaxScrollView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContact;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.EditTextCursorWatcher;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop.Mode;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.PreviewUtils;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;


@SuppressLint("NewApi")
public class FilePropertiesActivityLollipop extends PinActivityLollipop implements OnClickListener, MegaRequestListenerInterface, OnCheckedChangeListener, MegaGlobalListenerInterface, OnItemClickListener, DatePickerDialog.OnDateSetListener{
	
	static int TYPE_EXPORT_GET = 0;
	static int TYPE_EXPORT_REMOVE = 1;
	static int TYPE_EXPORT_MANAGE = 2;
	static int FROM_FILE_BROWSER = 13;
	static public int FROM_INCOMING_SHARES= 14;
	static int FROM_OFFLINE= 15;
	static public int FROM_INBOX= 16;
	
	RelativeLayout imageLayout;
	ImageView toolbarBack;
	ImageView toolbarOverflow;
	ImageView toolbarDownload;
	ImageView toolbarRubbishBin;

	int accountType;

	private android.support.v7.app.AlertDialog getLinkDialog;
	Button expiryDateButton;
	SwitchCompat switchGetLink;
	private boolean isExpiredDateLink = false;
	private boolean isGetLink = false;

	float scaleText;
	
	RelativeLayout overflowMenuLayout;
	ListView overflowMenuList;
	
	RelativeLayout optionsBackLayout;
	ParallaxScrollView sV;
	RelativeLayout container;
	ImageView imageView;
	LinearLayout optionsLayout;
	TextView nameView;
	LinearLayout availableOfflineLayout;

	RelativeLayout sizeLayout;
	RelativeLayout contentLayout;
	RelativeLayout addedLayout;
	RelativeLayout modifiedLayout;
	ImageView shareIcon;
	ImageView infoIcon;
	ImageView contentIcon;
	ImageView addedIcon;
	ImageView modifiedIcon;
	RelativeLayout sharedLayout;
	TextView usersSharedWithText;
	View dividerSharedLayout;
	
	TextView availableOfflineView;
	
	ImageView publicLinkIcon;
	
//	ImageView publicLinkImage;
	SwitchCompat offlineSwitch;
	
	TextView sizeTextView;
	TextView sizeTitleTextView;
	
	TextView contentTextView;
	TextView contentTitleTextView;
	
	TextView addedTextView;
	TextView modifiedTextView;
	
	RelativeLayout permissionsLayout;
	TextView permissionLabel;
	TextView permissionInfo;
	ImageView permissionsIcon;
	
	boolean owner= true;
	int typeExport = -1;
	
	ArrayList<MegaShare> sl;
	MegaOffline mOffDelete;
	
	RelativeLayout ownerLayout;
	TextView ownerLabel;
	TextView ownerInfo;	
	ImageView ownerIcon;

//	ArrayList<MegaNode> dTreeList = null;
	
	MegaNode node;
	long handle;
	
	boolean availableOfflineBoolean = false;
	
	private MegaApiAndroid megaApi = null;
	int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
	
	public FilePropertiesActivityLollipop filePropertiesActivity;
	
	ProgressDialog statusDialog;
	boolean publicLink=false;
	
	private static int EDIT_TEXT_ID = 1;
	private Handler handler;
	
	private AlertDialog renameDialog;

	boolean moveToRubbish = false;
	
	public static int REQUEST_CODE_SELECT_CONTACT = 1000;
	public static int REQUEST_CODE_SELECT_MOVE_FOLDER = 1001;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
//	public static String DB_FILE = "0";
//	public static String DB_FOLDER = "1";
	
	Display display;
	DisplayMetrics outMetrics;
	float density;
	float scaleW;
	float scaleH;

	boolean shareIt = true;
	int imageId;
	int from;
	
	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;
	
	AlertDialog permissionsDialog;
	
	public int getStatusBarHeight() { 
	      int result = 0;
	      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	      if (resourceId > 0) {
	          result = getResources().getDimensionPixelSize(resourceId);
	      } 
	      return result;
	} 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {			
	
		super.onCreate(savedInstanceState);
		log("onCreate");
		if (megaApi == null){
			MegaApplication app = (MegaApplication)getApplication();
			megaApi = app.getMegaApi();
		}
		
		megaApi.addGlobalListener(this);
		
		filePropertiesActivity = this;
		handler = new Handler();

//		dbH = new DatabaseHandler(getApplicationContext());
		dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		
		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
		
	    scaleW = Util.getScaleW(outMetrics, density);
	    scaleH = Util.getScaleH(outMetrics, density);

		if (scaleH < scaleW){
			scaleText = scaleH;
		}
		else{
			scaleText = scaleW;
		}
	    
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			imageId = extras.getInt("imageId");
			from = extras.getInt("from");
//			String name = extras.getString("name");
			accountType = extras.getInt("typeAccount", MegaAccountDetails.ACCOUNT_TYPE_FREE);
			handle = extras.getLong("handle", -1);
			log("Handle of the selected node: "+handle);
			node = megaApi.getNodeByHandle(handle);
			if (node == null){  
				log("Node is NULL");
				finish();
				return;
			}
			
			String name = node.getName();
					
			setContentView(R.layout.activity_file_properties);
			sV = (ParallaxScrollView) findViewById(R.id.file_properties_scroll_view);
			sV.post(new Runnable() { 
		        public void run() { 
		             sV.scrollTo(0, outMetrics.heightPixels/3);
		        } 
			});
						
			container = (RelativeLayout) findViewById(R.id.file_properties_main_layout);
			container.setOnClickListener(this);
			imageLayout = (RelativeLayout) findViewById(R.id.file_properties_image_layout);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageLayout.getLayoutParams();
			params.setMargins(0, -getStatusBarHeight(), 0, 0);
			imageLayout.setLayoutParams(params);
			
			optionsBackLayout = (RelativeLayout) findViewById(R.id.file_properties_toolbar_back_options_layout);
			params = (RelativeLayout.LayoutParams) optionsBackLayout.getLayoutParams();

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				params.setMargins(0,  0, 0, Util.scaleHeightPx(100, outMetrics));
			}
			else{
				params.setMargins(0, getStatusBarHeight(), 0, Util.scaleHeightPx(100, outMetrics));
			}
			optionsBackLayout.setLayoutParams(params);
			
			toolbarBack = (ImageView) findViewById(R.id.file_properties_toolbar_back);
			params = (RelativeLayout.LayoutParams) toolbarBack.getLayoutParams();
//			int leftMarginBack = getResources().getDimensionPixelSize(R.dimen.left_margin_back_arrow);
			int leftMarginBack = Util.scaleWidthPx(2, outMetrics);
			params.setMargins(leftMarginBack, 0, 0, 0);
			toolbarBack.setLayoutParams(params);
			toolbarBack.setOnClickListener(this);
			
			toolbarOverflow = (ImageView) findViewById(R.id.file_properties_toolbar_overflow);
			params = (RelativeLayout.LayoutParams) toolbarOverflow.getLayoutParams();
			params.setMargins(0, 0, leftMarginBack, 0);
			toolbarOverflow.setLayoutParams(params);
			toolbarOverflow.setOnClickListener(this);
			
			toolbarDownload = (ImageView) findViewById(R.id.file_properties_toolbar_download);
			toolbarDownload.setOnClickListener(this);
			
			toolbarRubbishBin = (ImageView) findViewById(R.id.file_properties_toolbar_rubbish_bin);
			toolbarRubbishBin.setOnClickListener(this);
			
			overflowMenuLayout = (RelativeLayout) findViewById(R.id.file_properties_overflow_menu_layout);
			params = (RelativeLayout.LayoutParams) overflowMenuLayout.getLayoutParams();
			params.setMargins(0, getStatusBarHeight() + Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(5, outMetrics), 0);
			overflowMenuLayout.setLayoutParams(params);
			overflowMenuList = (ListView) findViewById(R.id.file_properties_overflow_menu_list);
			overflowMenuLayout.setVisibility(View.GONE);
			
			createOverflowMenu(overflowMenuList);
			overflowMenuList.setOnItemClickListener(this);
			
			imageView = (ImageView) findViewById(R.id.file_properties_toolbar_image);
			imageView.setImageResource(imageId);
		
			float scaleText;
			if (scaleH < scaleW){
				scaleText = scaleH;
			}
			else{
				scaleText = scaleW;
			}
			
			//NAME Title
			
			nameView = (TextView) findViewById(R.id.file_properties_name);
			nameView.setText(name);
			nameView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			nameView.setSingleLine();
			nameView.setTypeface(null, Typeface.BOLD);	
			
			nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (20*scaleText));
			publicLinkIcon = (ImageView) findViewById(R.id.file_properties_public_link_image);
			RelativeLayout.LayoutParams lpPL = new RelativeLayout.LayoutParams(publicLinkIcon.getLayoutParams());
			lpPL.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			publicLinkIcon.setLayoutParams(lpPL);			
			
			//Available Offline Layout
			
			availableOfflineLayout = (LinearLayout) findViewById(R.id.available_offline_layout);
			availableOfflineLayout.setVisibility(View.VISIBLE);	
			
			availableOfflineView = (TextView) findViewById(R.id.file_properties_available_offline_text);
			LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) availableOfflineView.getLayoutParams();
			params4.leftMargin = Util.scaleWidthPx(55, outMetrics);
			params4.topMargin = Util.scaleHeightPx(15, outMetrics);
			params4.bottomMargin = Util.scaleHeightPx(15, outMetrics);
			availableOfflineView.setLayoutParams(params4);			
			
			offlineSwitch = (SwitchCompat) findViewById(R.id.file_properties_switch);
			offlineSwitch.setOnCheckedChangeListener(this);
			
			//Share with Layout
			
			sharedLayout = (RelativeLayout) findViewById(R.id.file_properties_shared_layout);
			sharedLayout.setOnClickListener(this);
			
			shareIcon = (ImageView) findViewById(R.id.file_properties_shared_image);
			RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(shareIcon.getLayoutParams());
			lp1.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			shareIcon.setLayoutParams(lp1);
			
			usersSharedWithText = (TextView) findViewById(R.id.file_properties_shared_info);
			RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) usersSharedWithText.getLayoutParams();
			params1.rightMargin = Util.scaleWidthPx(10, outMetrics);
			usersSharedWithText.setLayoutParams(params1);
			
			dividerSharedLayout = (View) findViewById(R.id.divider_shared_layout);
			LinearLayout.LayoutParams paramsDivider = (LinearLayout.LayoutParams) dividerSharedLayout.getLayoutParams();
			paramsDivider.leftMargin = Util.scaleWidthPx(55, outMetrics);
			dividerSharedLayout.setLayoutParams(paramsDivider);
			
			//OPTIONS LAYOUT			
			optionsLayout = (LinearLayout) findViewById(R.id.file_properties_options);
			
			//Permissions Layout
			permissionsLayout = (RelativeLayout) findViewById(R.id.file_properties_permissions_layout);
			permissionsLayout.setVisibility(View.GONE);
			
		    permissionsIcon = (ImageView) findViewById(R.id.file_properties_permissions_image);
		    RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(permissionsIcon.getLayoutParams());
			lp3.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			permissionsIcon.setLayoutParams(lp3); 
			
			permissionLabel = (TextView) findViewById(R.id.file_properties_permission_label);				
			permissionInfo = (TextView) findViewById(R.id.file_properties_permission_info);
			
			//Owner Layout
			ownerLayout = (RelativeLayout) findViewById(R.id.file_properties_owner_layout);
			
		    ownerIcon = (ImageView) findViewById(R.id.file_properties_owner_image);
		    RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(ownerIcon.getLayoutParams());
			lp4.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			ownerIcon.setLayoutParams(lp4); 
			
			ownerLabel =  (TextView) findViewById(R.id.file_properties_owner_label);
			ownerInfo = (TextView) findViewById(R.id.file_properties_owner_info);
			ownerLayout.setVisibility(View.GONE);	
			
			//Info Layout
			
		    infoIcon = (ImageView) findViewById(R.id.file_properties_size_image);
		    RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(infoIcon.getLayoutParams());
			lp2.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			infoIcon.setLayoutParams(lp2); 			
			
			sizeLayout = (RelativeLayout) findViewById(R.id.file_properties_size_layout);
			sizeTitleTextView  = (TextView) findViewById(R.id.file_properties_info_menu_size);
			sizeTextView = (TextView) findViewById(R.id.file_properties_info_data_size);
			RelativeLayout.LayoutParams params5 = (RelativeLayout.LayoutParams) sizeTextView.getLayoutParams();
			params5.rightMargin = Util.scaleWidthPx(10, outMetrics);
			sizeTextView.setLayoutParams(params5);
			
			//Content Layout
			contentLayout = (RelativeLayout) findViewById(R.id.file_properties_content_layout);
			
			contentIcon = (ImageView) findViewById(R.id.file_properties_content_image);
		    RelativeLayout.LayoutParams lpContent = new RelativeLayout.LayoutParams(contentIcon.getLayoutParams());
		    lpContent.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
			contentIcon.setLayoutParams(lpContent);			
			
			contentTitleTextView  = (TextView) findViewById(R.id.file_properties_info_menu_content);
			contentTextView = (TextView) findViewById(R.id.file_properties_info_data_content);
			
			//Added Layout
			
			addedLayout = (RelativeLayout) findViewById(R.id.file_properties_added_layout);
			
			addedIcon = (ImageView) findViewById(R.id.file_properties_added_image);
		    RelativeLayout.LayoutParams lpAdded = new RelativeLayout.LayoutParams(addedIcon.getLayoutParams());
		    lpAdded.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
		    addedIcon.setLayoutParams(lpAdded); 		    
		    addedTextView = (TextView) findViewById(R.id.file_properties_info_data_added);

		    
		    //Modified Layout		    
		    modifiedLayout = (RelativeLayout) findViewById(R.id.file_properties_modified_layout);
		    
		    modifiedIcon = (ImageView) findViewById(R.id.file_properties_modified_image);
		    RelativeLayout.LayoutParams lpModified = new RelativeLayout.LayoutParams(modifiedIcon.getLayoutParams());
		    lpModified.setMargins(Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(3, outMetrics), Util.scaleHeightPx(3, outMetrics));
		    modifiedIcon.setLayoutParams(lpModified); 
			modifiedTextView = (TextView) findViewById(R.id.file_properties_info_data_modified);
			
		}
		else{
			log("Extras is NULL");
		}
		refreshProperties();
	}
	
	private void createOverflowMenu(ListView list){
		log("createOverflowMenu");
		ArrayList<String> menuOptions = new ArrayList<String>();
		
		MegaNode parent = megaApi.getNodeByHandle(handle);
		while (megaApi.getParentNode(parent) != null){
			parent = megaApi.getParentNode(parent);
		}
		
		if (parent.getHandle() == megaApi.getRubbishNode().getHandle()){
			toolbarDownload.setVisibility(View.GONE);
			menuOptions.add(getString(R.string.context_delete));
			menuOptions.add(getString(R.string.context_move));
		}
		else{
			toolbarDownload.setVisibility(View.VISIBLE);
			if(from==FROM_INCOMING_SHARES){
				menuOptions.add(getString(R.string.context_download));
		    	if (node.isFolder() && (megaApi.getParentNode(node) == null)){
		    		menuOptions.add(getString(R.string.context_leave_menu));
		    	}
		    	
		    	int accessLevel= megaApi.getAccess(node);
				log("Node: "+node.getName());
				
				switch(accessLevel){
				
					case MegaShare.ACCESS_OWNER:
					case MegaShare.ACCESS_FULL:{
						menuOptions.add(getString(R.string.context_rename));
						menuOptions.add(getString(R.string.context_move));
						menuOptions.add(getString(R.string.context_copy));
						menuOptions.add(getString(R.string.context_move_to_trash));
						break;
					}
					case MegaShare.ACCESS_READ:{
						menuOptions.add(getString(R.string.context_copy));
						break;
					}
					case MegaShare.ACCESS_READWRITE:{
						menuOptions.add(getString(R.string.context_copy));
						break;
					}
				}
		    }
			else{
				menuOptions.add(getString(R.string.context_download));
				if (node.isFolder()){
					menuOptions.add(getString(R.string.context_share_folder));
				}
				
				if (node.isExported()){
					log("node Exported");
					menuOptions.add(getString(R.string.context_manage_link_menu));
					menuOptions.add(getString(R.string.context_remove_link_menu));
				}
				else{
					log("node NOT Exported");
					menuOptions.add(getString(R.string.context_get_link_menu));
				}
				menuOptions.add(getString(R.string.context_rename));
				menuOptions.add(getString(R.string.context_move));
				menuOptions.add(getString(R.string.context_copy));	
				menuOptions.add(getString(R.string.context_move_to_trash));
			}
		}
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuOptions);
		if (list.getAdapter() != null){
			ArrayAdapter<String> ad = (ArrayAdapter<String>) list.getAdapter();
			ad.clear();
			ad.addAll(menuOptions);
			ad.notifyDataSetChanged();
		}
		else{
			list.setAdapter(arrayAdapter);
		}
	}
	
	private void refreshProperties(){		
		log("refreshProperties");
	
		boolean result=true;
		
		if(node.isExported()){
			publicLink=true;
			publicLinkIcon.setVisibility(View.VISIBLE);
		}	
		else{
			publicLink=false;
			publicLinkIcon.setVisibility(View.INVISIBLE);
		}
		
		if (node.isFile()){				
			log("onCreate node is FILE");
			sharedLayout.setVisibility(View.GONE);				
			dividerSharedLayout.setVisibility(View.GONE);		
			sizeTitleTextView.setText(getString(R.string.file_properties_info_size_file));
			
			sizeTextView.setText(Formatter.formatFileSize(this, node.getSize()));
			
			contentLayout.setVisibility(View.GONE);	

			if (node.getCreationTime() != 0){
				try {addedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{addedTextView.setText("");}

				if (node.getModificationTime() != 0){
					try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getModificationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}
				}
				else{
					try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}	
				}
			}
			else{
				addedTextView.setText("");
				modifiedTextView.setText("");
			}
			
			Bitmap thumb = null;
			Bitmap preview = null;
			thumb = ThumbnailUtils.getThumbnailFromCache(node);
			if (thumb != null){
				imageView.setImageBitmap(thumb);
			}
			else{
				thumb = ThumbnailUtils.getThumbnailFromFolder(node, this);
				if (thumb != null){
					imageView.setImageBitmap(thumb);
				}
			}
			preview = PreviewUtils.getPreviewFromCache(node);
			if (preview != null){
				PreviewUtils.previewCache.put(node.getHandle(), preview);
				imageView.setImageBitmap(preview);
			}
			else{
				preview = PreviewUtils.getPreviewFromFolder(node, this);
				if (preview != null){
					PreviewUtils.previewCache.put(node.getHandle(), preview);
					imageView.setImageBitmap(preview);	
				}
				else{
					if (node.hasPreview()){	
						File previewFile = new File(PreviewUtils.getPreviewFolder(this), node.getBase64Handle()+".jpg");
						megaApi.getPreview(node, previewFile.getAbsolutePath(), this);
					}
				}
			}
		}
		else{ //Folder
			
			contentTextView.setVisibility(View.VISIBLE);
			contentTitleTextView.setVisibility(View.VISIBLE);
			
			contentTextView.setText(MegaApiUtils.getInfoFolder(node, this));
			
			long sizeFile=megaApi.getSize(node);				
			sizeTextView.setText(Formatter.formatFileSize(this, sizeFile));				

			//OLD APPROACH, the folder is only shown as offline if all the children are saved offline
			
//			if(dbH.exists(node.getHandle())){
//
//				ArrayList<MegaNode> childrenList=megaApi.getChildren(node);
//				if(childrenList.size()>0){
//
//					result=checkChildrenStatus(childrenList);
//
//				}
//
//				if(!result){
//					log("false checkChildrenStatus: "+result);
//					availableOfflineBoolean = false;
//					offlineSwitch.setChecked(false);
//				}
//				else{
//					log("true checkChildrenStatus: "+result);
//					availableOfflineBoolean = true;
//					offlineSwitch.setChecked(true);
//				}
//
//			}
//			else{
//				availableOfflineBoolean = false;
//				offlineSwitch.setChecked(false);
//			}
			
			imageView.setImageResource(imageId);

			if(from==FROM_INCOMING_SHARES){
				//Show who is the owner
				ArrayList<MegaShare> sharesIncoming = megaApi.getInSharesList();
				for(int j=0; j<sharesIncoming.size();j++){
					MegaShare mS = sharesIncoming.get(j);
					if(mS.getNodeHandle()==node.getHandle()){						
						MegaUser user= megaApi.getContact(mS.getUser());
						if(user!=null){
							MegaContact contactDB = dbH.findContactByHandle(String.valueOf(user.getHandle()));
							if(contactDB!=null){
								if(!contactDB.getName().equals("")){
									ownerInfo.setText(contactDB.getName()+" "+contactDB.getLastName());
								}
								else{
									ownerInfo.setText(user.getEmail());
								}
							}
							else{
								log("The contactDB is null: ");
								ownerInfo.setText(user.getEmail());
							}		
						}
						else{
							ownerInfo.setText(mS.getUser());
						}
						ownerLayout.setVisibility(View.VISIBLE);
					}				
				}
			}
			
			sl = megaApi.getOutShares(node);		

			if (sl != null){

				if (sl.size() == 0){						
					sharedLayout.setVisibility(View.GONE);
					dividerSharedLayout.setVisibility(View.GONE);	
//					If I am the owner
					if (megaApi.checkAccess(node, MegaShare.ACCESS_OWNER).getErrorCode() == MegaError.API_OK){
						
//						permissionLabel.setVisibility(View.GONE);
//						permissionInfo.setVisibility(View.GONE);
						permissionsLayout.setVisibility(View.GONE);
						//permissionInfo.setText(getResources().getString(R.string.file_properties_owner));
						
					}
					else{	
						
						owner = false;
						//If I am not the owner
						permissionsLayout.setVisibility(View.VISIBLE);
//						permissionLabel.setVisibility(View.VISIBLE);
//						permissionInfo.setVisibility(View.VISIBLE);
						
						int accessLevel= megaApi.getAccess(node);
						log("Node: "+node.getName());
																				
						switch(accessLevel){
							case MegaShare.ACCESS_OWNER:
							case MegaShare.ACCESS_FULL:{
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_full_access));	
								break;
							}
							case MegaShare.ACCESS_READ:{
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_read_only));
								break;
							}						
							case MegaShare.ACCESS_READWRITE:{								
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_read_write));
								break;
							}
						}
					}
					
				}
				else{		
					if(publicLink){
//						publicLinkTextView.setText(getResources().getString(R.string.file_properties_shared_folder_public_link));							
//
//						publicLinkTextView.setVisibility(View.VISIBLE);
						
						if(sl.size()>1){
							//It is public and shared
							imageView.setImageResource(R.drawable.folder_shared_mime);
							sharedLayout.setVisibility(View.VISIBLE);
							dividerSharedLayout.setVisibility(View.VISIBLE);	
							usersSharedWithText.setText(sl.size()+" "+getResources().getQuantityString(R.plurals.general_num_users,sl.size()));
						}
						else{
							//It is just public
							imageView.setImageResource(R.drawable.folder_mime);
							sharedLayout.setVisibility(View.GONE);
							dividerSharedLayout.setVisibility(View.GONE);	
//							sharedWithButton.setText(R.string.file_properties_shared_folder_public_link);
						}
						
					}
					else{
//						publicLinkTextView.setText(getResources().getString(R.string.file_properties_shared_folder_private_folder));
						//It is private and shared
						imageView.setImageResource(R.drawable.folder_shared_mime);
						sharedLayout.setVisibility(View.VISIBLE);
						dividerSharedLayout.setVisibility(View.VISIBLE);	
						usersSharedWithText.setText(sl.size()+" "+getResources().getQuantityString(R.plurals.general_num_users,sl.size()));
					}						
				}					
				
				if (node.getCreationTime() != 0){
					try {addedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{addedTextView.setText("");}

					if (node.getModificationTime() != 0){
						try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getModificationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}
					}
					else{
						try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}	
					}
				}
				else{
					addedTextView.setText("");
					modifiedTextView.setText("");
				}
			}
			else{
				sharedLayout.setVisibility(View.GONE);
				dividerSharedLayout.setVisibility(View.GONE);	
			}
		}

		//Choose the button offlineSwitch

		File offlineFile = null;

		if(dbH.exists(node.getHandle())) {
			log("Exists OFFLINE in the DB!!!");

			MegaOffline offlineNode = dbH.findByHandle(node.getHandle());
			if (offlineNode != null) {
				log("YESS FOUND: " + node.getName());
				if (from == FROM_INCOMING_SHARES) {
					log("FROM_INCOMING_SHARES");
					//Find in the filesystem
					if (Environment.getExternalStorageDirectory() != null) {
						offlineFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + offlineNode.getHandleIncoming() + offlineNode.getPath());
						log("offline File INCOMING: " + offlineFile.getAbsolutePath());
					} else {
						offlineFile = this.getFilesDir();
					}

				} else {
					log("NOT INCOMING");
					//Find in the filesystem
					//Path MEGA
					String pathMega = megaApi.getNodePath(node);
					if(from==FROM_INBOX){
						pathMega = pathMega.replace("/in", "");
					}
					if (Environment.getExternalStorageDirectory() != null) {
						offlineFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + pathMega);
						log("offline File: " + offlineFile.getAbsolutePath());
					} else {
						offlineFile = this.getFilesDir();
					}
				}

				if (offlineFile != null) {
					if (offlineFile.exists()) {
						log("FOUND!!!: " + node.getHandle() + " " + node.getName());
						availableOfflineBoolean = true;
						offlineSwitch.setChecked(true);
					} else {
						log("Not found: " + node.getHandle() + " " + node.getName());
						availableOfflineBoolean = false;
						offlineSwitch.setChecked(false);
					}
				} else {
					log("Not found offLineFile is NULL");
					availableOfflineBoolean = false;
					offlineSwitch.setChecked(false);
				}
			}
			else{
				log("offLineNode is NULL");
				availableOfflineBoolean = false;
				offlineSwitch.setChecked(false);
			}

		}
		else{
			log("NOT Exists OFFLINE: setChecket FALSE");
			availableOfflineBoolean = false;
			offlineSwitch.setChecked(false);
		}
	}
		
//	private boolean checkChildrenStatus(ArrayList<MegaNode> childrenList){
//
//		boolean children = true;
//		ArrayList<MegaNode> childrenListRec;
//
//		if(childrenList.size()>0){
//			for(int i=0;i<childrenList.size();i++){
//
//				if(!dbH.exists(childrenList.get(i).getHandle())){
//					children=false;
//					return children;
//				}
//				else{
//					if(childrenList.get(i).isFolder()){
//
//						childrenListRec=megaApi.getChildren(childrenList.get(i));
//
//						if(childrenListRec.size()>0){
//							boolean result=checkChildrenStatus(childrenListRec);
//							if(!result){
//								children=false;
//								return children;
//							}
//						}
//					}
//					else{
//
//						if(!dbH.exists(childrenList.get(i).getHandle())){
//							children=false;
//							return children;
//						}
//
//					}
//				}
//			}
//		}
//		return children;
//	}
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
			case R.id.file_properties_main_layout:{
				if (overflowMenuLayout != null){
					if (overflowMenuLayout.getVisibility() == View.VISIBLE){
						overflowMenuLayout.setVisibility(View.GONE);
						return;
					}
				}
				break;
			}
			case R.id.file_properties_shared_layout:{
				Intent i = new Intent(this, FileContactListActivityLollipop.class);
				i.putExtra("name", node.getHandle());
				startActivity(i);				
				break;
			}
			case R.id.file_properties_toolbar_back:{
				finish();
				break;
			}
			case R.id.file_properties_toolbar_overflow:{
				overflowMenuLayout.setVisibility(View.VISIBLE);
				break;
			}
			case R.id.file_properties_toolbar_download:{
				if (!availableOfflineBoolean){
			    	ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(node.getHandle());
					downloadNode(handleList);
		    	}
		    	else{
		    		
		    		File destination = null;
					File offlineFile = null;
					if (Environment.getExternalStorageDirectory() != null){
						destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/"+MegaApiUtils.createStringTree(node, this));
					}
					else{
						destination = new File(getFilesDir(), node.getHandle()+"");
					}
					
					if (destination.exists() && destination.isDirectory()){
						offlineFile = new File(destination, node.getName());
						if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getName())){ //This means that is already available offline
							availableOfflineBoolean = true;
							offlineSwitch.setChecked(true);
						}
						else{
							availableOfflineBoolean = false;
							offlineSwitch.setChecked(false);
							mOffDelete = dbH.findByHandle(node.getHandle());
							removeOffline(mOffDelete);							
							supportInvalidateOptionsMenu();
						}
					}
					else{
						availableOfflineBoolean = false;
						offlineSwitch.setChecked(false);
						mOffDelete = dbH.findByHandle(node.getHandle());
						removeOffline(mOffDelete);		
						supportInvalidateOptionsMenu();
					}
		    		Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(offlineFile), MimeTypeList.typeForName(offlineFile.getName()).getType());
					if (MegaApiUtils.isIntentAvailable(this, intent)){
						startActivity(intent);
					}
					else{
						Snackbar.make(container, getString(R.string.intent_not_available), Snackbar.LENGTH_LONG).show();
					}
		    	}
				break;
			}
			case R.id.file_properties_toolbar_rubbish_bin:{
				moveToTrash();
				break;
			}
//			case R.id.file_properties_content_table:{			
//				Intent i = new Intent(this, FileContactListActivityLollipop.class);
//				i.putExtra("name", node.getHandle());
//				startActivity(i);
//				finish();
//				break;
//			}
//			
		}
	}	

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		log("onCheckedChanged");
		
		if(owner){
			log("Owner: me");
			if (!isChecked){
				log("isChecked");
				availableOfflineBoolean = false;
				offlineSwitch.setChecked(false);	
				mOffDelete = dbH.findByHandle(node.getHandle());
				removeOffline(mOffDelete);			
				supportInvalidateOptionsMenu();
			}
			else{		
				log("NOT Checked");
				availableOfflineBoolean = true;
				offlineSwitch.setChecked(true);	
				
				log("Path destination: "+Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/"+MegaApiUtils.createStringTree(node, this));
				
				File destination = null;
				if (Environment.getExternalStorageDirectory() != null){
					destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/"+MegaApiUtils.createStringTree(node, this));
				}
				else{
					destination = getFilesDir();
				}

				if (destination.exists() && destination.isDirectory()){
					File offlineFile = new File(destination, node.getName());
					if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getName())){ //This means that is already available offline
						return;
					}
				}
				
				saveOffline(destination);
				
				supportInvalidateOptionsMenu();
			}	
		}
		else{
			
			log("not owner");

			if (!isChecked){
				availableOfflineBoolean = false;
				offlineSwitch.setChecked(false);
				mOffDelete = dbH.findByHandle(node.getHandle());
				removeOffline(mOffDelete);			
				supportInvalidateOptionsMenu();
			}
			else{										
				availableOfflineBoolean = true;
				offlineSwitch.setChecked(true);
				
				supportInvalidateOptionsMenu();
				
				log("Comprobando el node"+node.getName());
				
				//check the parent
				long result = -1;
				result=findIncomingParentHandle(node);
				log("IncomingParentHandle: "+result);
				if(result!=-1){
					MegaNode megaNode = megaApi.getNodeByHandle(result);
					if(megaNode!=null){
						log("ParentHandleIncoming: "+megaNode.getName());
					}
					String handleString = Long.toString(result);
					String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + handleString + "/"+MegaApiUtils.createStringTree(node, this);
					log("Not owner path destination: "+destinationPath);
					
					File destination = null;
					if (Environment.getExternalStorageDirectory() != null){
						destination = new File(destinationPath);
					}
					else{
						destination = getFilesDir();
					}

					if (destination.exists() && destination.isDirectory()){
						File offlineFile = new File(destination, node.getName());
						if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getName())){ //This means that is already available offline
							return;
						}
					}				
					saveOffline(destination);
				}
				else{
					log("result=findIncomingParentHandle NOT result!");
				}
			}
		}
	}
	
	public long findIncomingParentHandle(MegaNode nodeToFind){
		log("findIncomingParentHandle");
		
		MegaNode parentNodeI = megaApi.getParentNode(nodeToFind);
		long result=-1;
		if(parentNodeI==null){
			log("findIncomingParentHandle A: "+nodeToFind.getHandle());
			return nodeToFind.getHandle();
		}
		else{
			result=findIncomingParentHandle(parentNodeI);
			while(result==-1){
				result=findIncomingParentHandle(parentNodeI);
			}	
			log("findIncomingParentHandle B: "+nodeToFind.getHandle());
			return result;
		}
	}
	
	public void saveOffline (File destination){
		log("saveOffline");
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}
		}

		destination.mkdirs();
		
		log ("DESTINATION!!!!!: " + destination.getAbsolutePath());

		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(destination.getAbsolutePath());
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}
		
		Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
		if (node.getType() == MegaNode.TYPE_FOLDER) {
			log("saveOffline:isFolder");
			getDlList(dlFiles, node, new File(destination, new String(node.getName())));
		} else {
			log("saveOffline:isFile");
			dlFiles.put(node, destination.getAbsolutePath());			
		}

		for (MegaNode document : dlFiles.keySet()) {

			String path = dlFiles.get(document);	
			
			if(availableFreeSpace <document.getSize()){
				Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space) + " (" + new String(document.getName()) + ")", false, this);
				continue;
			}
	
			String url = null;
			Intent service = new Intent(this, DownloadService.class);
			service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
			service.putExtra(DownloadService.EXTRA_URL, url);
			service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
			service.putExtra(DownloadService.EXTRA_PATH, path);
			service.putExtra(DownloadService.EXTRA_OFFLINE, true);
			startService(service);					
		}
	}

	public void removeOffline(MegaOffline mOffDelete){
	
		if (mOffDelete == null){
			return;
		}
		
		log("removeOffline - file(type): "+mOffDelete.getName()+"("+mOffDelete.getType()+")");	
		//mOffDelete = node;
		MegaOffline parentNode = null;	
		ArrayList<MegaOffline> mOffListParent=new ArrayList<MegaOffline>();
		ArrayList<MegaOffline> mOffListChildren=new ArrayList<MegaOffline>();			
				
		if(mOffDelete.getType().equals(MegaOffline.FOLDER)){
			log("Finding children... ");
			//Delete in DB
			//Delete children
			mOffListChildren=dbH.findByParentId(mOffDelete.getId());
			if(mOffListChildren.size()>0){
				log("Children: "+mOffListChildren.size());
				//The node have childrens, delete
				deleteChildrenDB(mOffListChildren);			
			}
		}
		else{
			log("NOT children... ");
		}
		
		int parentId = mOffDelete.getParentId();
		log("Finding parents... "+parentId);
		//Delete parents
		if(parentId!=-1){
			mOffListParent=dbH.findByParentId(parentId);
						
			if(mOffListParent.size()<1){
				//No more node with the same parent, keep deleting				
				parentNode = dbH.findById(parentId);
				if(parentNode != null){
					removeOffline(mOffDelete);						
				}	
			}			
		}		
		
		//Remove the node physically
		File destination = null;
		if(mOffDelete.isIncoming()){			
			if (Environment.getExternalStorageDirectory() != null){
				destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR);
			}
			else{
				destination = new File(getFilesDir(), mOffDelete.getHandle()+"");
			}
			
			log("Remove incoming: "+destination.getAbsolutePath());
			
			try{
				File offlineFile = new File(destination,  mOffDelete.getHandleIncoming());
				Util.deleteFolderAndSubfolders(this, offlineFile);				
			}
			catch(Exception e){
				log("EXCEPTION: removeOffline - file");
			};	
			
			dbH.removeById(mOffDelete.getId());		
		}
		else
		{			
			if (Environment.getExternalStorageDirectory() != null){
				destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + mOffDelete.getPath());
			}
			else{
				destination = new File(getFilesDir(), mOffDelete.getHandle()+"");
			}
			
			log("Remove node: "+destination.getAbsolutePath());
			
			try{
				File offlineFile = new File(destination, mOffDelete.getName());
				Util.deleteFolderAndSubfolders(this, offlineFile);
			}
			catch(Exception e){
				log("EXCEPTION: removeOffline - file");
			};	
			
			dbH.removeById(mOffDelete.getId());		
		}
	}	
	
		
	private void deleteChildrenDB(ArrayList<MegaOffline> mOffList){
	
		log("deleteChildenDB");
		MegaOffline mOffDelete=null;
	
		for(int i=0; i< mOffList.size(); i++){
			
			mOffDelete=mOffList.get(i);
			ArrayList<MegaOffline> mOffListChildren2=dbH.findByParentId(mOffDelete.getId());
			if(mOffList.size()>0){
				//The node have children, delete
				deleteChildrenDB(mOffListChildren2);
				
			}			
			dbH.removeById(mOffDelete.getId());			
		}
	}
	
	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}
	
	public void leaveIncomingShare (){
		log("leaveIncomingShare");
			
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	//TODO remove the incoming shares		        	
		    		megaApi.remove(node,filePropertiesActivity);		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
		builder.setTitle(getResources().getString(R.string.alert_leave_share));
		String message= getResources().getString(R.string.confirmation_leave_share_folder);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
	    	.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}
	
	public void showCopy(){
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(node.getHandle());
		
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_COPY_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("COPY_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_COPY_FOLDER);
	}
	
	public void showMove(){
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(node.getHandle());
		
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_MOVE_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("MOVE_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_MOVE_FOLDER);
	}

	public void downloadNode(ArrayList<Long> handleList){
		
		long size = 0;
		long[] hashes = new long[handleList.size()];
		for (int i=0;i<handleList.size();i++){
			hashes[i] = handleList.get(i);
			size += megaApi.getNodeByHandle(hashes[i]).getSize();
		}
		
		if (dbH == null){
//			dbH = new DatabaseHandler(getApplicationContext());
			dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		}
		
		boolean askMe = true;
		String downloadLocationDefaultPath = "";
		prefs = dbH.getPreferences();		
		if (prefs != null){
			if (prefs.getStorageAskAlways() != null){
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							askMe = false;
							downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
						}
					}
				}
			}
		}		
			
		if (askMe){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				File[] fs = getExternalFilesDirs(null);
				if (fs.length > 1){
					if (fs[1] == null){
						Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
						intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
						intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, size);
						intent.setClass(this, FileStorageActivityLollipop.class);
						intent.putExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES, hashes);
						startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
					}
					else{
						Dialog downloadLocationDialog;
						String[] sdCardOptions = getResources().getStringArray(R.array.settings_storage_download_location_array);
				        AlertDialog.Builder b=new AlertDialog.Builder(this);
	
						b.setTitle(getResources().getString(R.string.settings_storage_download_location));
						final long sizeFinal = size;
						final long[] hashesFinal = new long[hashes.length];
						for (int i=0; i< hashes.length; i++){
							hashesFinal[i] = hashes[i];
						}
						
						b.setItems(sdCardOptions, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch(which){
									case 0:{
										Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
										intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
										intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, sizeFinal);
										intent.setClass(getApplicationContext(), FileStorageActivityLollipop.class);
										intent.putExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES, hashesFinal);
										startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
										break;
									}
									case 1:{
										File[] fs = getExternalFilesDirs(null);
										if (fs.length > 1){
											String path = fs[1].getAbsolutePath();
											File defaultPathF = new File(path);
											defaultPathF.mkdirs();
											Toast.makeText(getApplicationContext(), getString(R.string.general_download) + ": "  + defaultPathF.getAbsolutePath() , Toast.LENGTH_LONG).show();
											downloadTo(path, null, sizeFinal, hashesFinal);
										}
										break;
									}
								}
							}
						});
						b.setNegativeButton(getResources().getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						downloadLocationDialog = b.create();
						downloadLocationDialog.show();
					}
				}
				else{
					Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
					intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
					intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, size);
					intent.setClass(this, FileStorageActivityLollipop.class);
					intent.putExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES, hashes);
					startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
				}
			}
			else{
				Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
				intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
				intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, size);
				intent.setClass(this, FileStorageActivityLollipop.class);
				intent.putExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES, hashes);
				startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
			}
		}
		else{
			downloadTo(downloadLocationDefaultPath, null, size, hashes);
		}
	}
	
	public void moveToTrash(){
		log("moveToTrash");
		
		final long handle = node.getHandle();
		moveToRubbish = false;
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		final MegaNode rubbishNode = megaApi.getRubbishNode();
		
		MegaNode parent = megaApi.getNodeByHandle(handle);
		while (megaApi.getParentNode(parent) != null){
			parent = megaApi.getParentNode(parent);
		}
		
		if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
			moveToRubbish = true;			
		}
		else{
			moveToRubbish = false;
		}
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	//TODO remove the outgoing shares
		    		//Check if the node is not yet in the rubbish bin (if so, remove it)			
		    		
		    		if (moveToRubbish){
		    			megaApi.moveNode(megaApi.getNodeByHandle(handle), rubbishNode, filePropertiesActivity);
		    			ProgressDialog temp = null;
		    			try{
		    				temp = new ProgressDialog(filePropertiesActivity);
		    				temp.setMessage(getString(R.string.context_move_to_trash));
		    				temp.show();
		    			}
		    			catch(Exception e){
		    				return;
		    			}
		    			statusDialog = temp;
		    		}
		    		else{
		    			megaApi.remove(megaApi.getNodeByHandle(handle), filePropertiesActivity);
		    			ProgressDialog temp = null;
		    			try{
		    				temp = new ProgressDialog(filePropertiesActivity);
		    				temp.setMessage(getString(R.string.context_delete_from_mega));
		    				temp.show();
		    			}
		    			catch(Exception e){
		    				return;
		    			}
		    			statusDialog = temp;
		    		}
		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};
		
		if (moveToRubbish){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.confirmation_alert));
			String message= getResources().getString(R.string.confirmation_move_to_rubbish);
			builder.setMessage(message).setPositiveButton(R.string.general_move_to, dialogClickListener)
		    	.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.confirmation_alert));
			String message= getResources().getString(R.string.confirmation_delete_from_mega);
			builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
		    	.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
		}
	}
	
	public void showRenameDialog(){
		
		final EditTextCursorWatcher input = new EditTextCursorWatcher(this, node.isFolder());
//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setText(node.getName());

		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		input.setImeActionLabel(getString(R.string.context_rename),
				KeyEvent.KEYCODE_ENTER);
		
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if (hasFocus) {
					if (node.isFolder()){
						input.setSelection(0, input.getText().length());
					}
					else{
						String [] s = node.getName().split("\\.");
						if (s != null){
							int numParts = s.length;
							int lastSelectedPos = 0;
							if (numParts == 1){
								input.setSelection(0, input.getText().length());
							}
							else if (numParts > 1){
								for (int i=0; i<(numParts-1);i++){
									lastSelectedPos += s[i].length(); 
									lastSelectedPos++;
								}
								lastSelectedPos--; //The last point should not be selected)
								input.setSelection(0, lastSelectedPos);
							}
						}
						showKeyboardDelayed(v);
					}
				}
			}
		});

		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.context_rename) + " "	+ new String(node.getName()), null, input);
		builder.setPositiveButton(getString(R.string.context_rename),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						rename(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		renameDialog = builder.create();
		renameDialog.show();
		
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					renameDialog.dismiss();
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					rename(value);
					return true;
				}
				return false;
			}
		});
	}
	
	private void rename(String newName){
		if (newName.equals(node.getName())) {
			return;
		}
		
		if(!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if (isFinishing()){
			return;
		}
		
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.context_renaming));
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
		
		log("renaming " + node.getName() + " to " + newName);
		
		megaApi.renameNode(node, newName, this);
	}
	
	public void getPublicLinkAndShareIt(){
	
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.context_creating_link));
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;

		if(node.isExported()){
			log("node is already exported: "+node.getName());
			log("node link: "+node.getPublicLink());
			showGetLinkPanel(node.getPublicLink(), node.getExpirationTime());
		}
		else{
			NodeController nC = new NodeController(filePropertiesActivity);
			log("Export link for Node: "+node.getName());
			nC.exportLink(node);
		}
	}

	public void showGetLinkPanel(final String link, long expirationTimestamp){
		log("showGetLinkPanel: "+link);

		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		final DatePickerDialog datePickerDialog = new DatePickerDialog(filePropertiesActivity, this, year, month, day);
		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

		builder.setTitle(getString(R.string.context_get_link_menu));

		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.panel_get_link, null);

		final CheckedTextView linkWithoutKeyCheck = (CheckedTextView) dialoglayout.findViewById(R.id.link_without_key);
		linkWithoutKeyCheck.setChecked(true);
		linkWithoutKeyCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
		linkWithoutKeyCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
		ViewGroup.MarginLayoutParams linkWOK = (ViewGroup.MarginLayoutParams) linkWithoutKeyCheck.getLayoutParams();
		linkWOK.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(14, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

		final CheckedTextView linkDecryptionKeyCheck = (CheckedTextView) dialoglayout.findViewById(R.id.link_decryption_key);
		linkDecryptionKeyCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
		linkDecryptionKeyCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
		ViewGroup.MarginLayoutParams linkDecry = (ViewGroup.MarginLayoutParams) linkDecryptionKeyCheck.getLayoutParams();
		linkDecry.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

		final CheckedTextView linkWithKeyCheck = (CheckedTextView) dialoglayout.findViewById(R.id.link_with_key);
		linkWithKeyCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
		linkWithKeyCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
		ViewGroup.MarginLayoutParams linkWK = (ViewGroup.MarginLayoutParams) linkWithKeyCheck.getLayoutParams();
		linkWK.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

		RelativeLayout expiryDateLayout = (RelativeLayout) dialoglayout.findViewById(R.id.expiry_date_layout);
		LinearLayout.LayoutParams paramsDateLayout = (LinearLayout.LayoutParams)expiryDateLayout.getLayoutParams();
		paramsDateLayout.setMargins(Util.scaleWidthPx(26, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, 0);
		expiryDateLayout.setLayoutParams(paramsDateLayout);

		TextView expiryDateTitle = (TextView) dialoglayout.findViewById(R.id.title_set_expiry_date);
		expiryDateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));

		TextView expiryDateSubtitle = (TextView) dialoglayout.findViewById(R.id.subtitle_set_expiry_date);
		expiryDateSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleText));

		expiryDateButton = (Button) dialoglayout.findViewById(R.id.expiry_date);
		LinearLayout.LayoutParams paramsExpiryDate = (LinearLayout.LayoutParams)expiryDateButton.getLayoutParams();
		paramsExpiryDate.setMargins(Util.scaleWidthPx(20, outMetrics), 0, 0, 0);
		expiryDateButton.setLayoutParams(paramsExpiryDate);

		final TextView linkText = (TextView) dialoglayout.findViewById(R.id.link);
		linkText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleText));
		LinearLayout.LayoutParams paramsLink = (LinearLayout.LayoutParams)linkText.getLayoutParams();
		paramsLink.setMargins(Util.scaleWidthPx(26, outMetrics), Util.scaleHeightPx(3, outMetrics), Util.scaleWidthPx(16, outMetrics), Util.scaleHeightPx(6, outMetrics));
		linkText.setLayoutParams(paramsLink);

		switchGetLink = (SwitchCompat) dialoglayout.findViewById(R.id.switch_set_expiry_date);
		RelativeLayout.LayoutParams paramsSwitch = (RelativeLayout.LayoutParams)switchGetLink.getLayoutParams();
		paramsSwitch.setMargins(0, 0, Util.scaleWidthPx(16, outMetrics), 0);
		switchGetLink.setLayoutParams(paramsSwitch);

		linkWithoutKeyCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				linkWithoutKeyCheck.setChecked(true);
				linkDecryptionKeyCheck.setChecked(false);
				linkWithKeyCheck.setChecked(false);
				String urlString="";
				String [] s = link.split("!");
				if (s.length == 3){
					urlString = s[0] + "!" + s[1];
				}
				linkText.setText(urlString);
			}
		});

		linkDecryptionKeyCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				linkWithoutKeyCheck.setChecked(false);
				linkDecryptionKeyCheck.setChecked(true);
				linkWithKeyCheck.setChecked(false);
				String keyString="!";
				String [] s = link.split("!");
				if (s.length == 3){
					keyString = keyString+s[2];
				}
				linkText.setText(keyString);
			}
		});

		linkWithKeyCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				linkWithoutKeyCheck.setChecked(false);
				linkDecryptionKeyCheck.setChecked(false);
				linkWithKeyCheck.setChecked(true);
				linkText.setText(link);
			}
		});

		datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_NEGATIVE) {
					log("Negative button of DatePicker clicked");
					switchGetLink.setChecked(false);
					expiryDateButton.setVisibility(View.INVISIBLE);
				}
			}
		});
		//Set by default, link without key
		String urlString="";
		String [] s = link.split("!");
		if (s.length == 3){
			urlString = s[0] + "!" + s[1];
		}
		linkText.setText(urlString);
		linkWithoutKeyCheck.setChecked(true);

		builder.setView(dialoglayout);
//
		builder.setPositiveButton(getString(R.string.context_send), new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, linkText.getText());
				startActivity(Intent.createChooser(intent, getString(R.string.context_get_link)));
			}
		});

		builder.setNegativeButton(getString(R.string.context_copy), new android.content.DialogInterface.OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(link);
				} else {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", linkText.getText());
					clipboard.setPrimaryClip(clip);
				}
				Snackbar.make(container, getString(R.string.file_properties_get_link), Snackbar.LENGTH_LONG).show();
			}
		});

		getLinkDialog = builder.create();

		expiryDateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				datePickerDialog.show();
			}
		});

		if(accountType> MegaAccountDetails.ACCOUNT_TYPE_FREE){
			log("The user is PRO - enable expiration date");

			if(expirationTimestamp<=0){
				switchGetLink.setChecked(false);
				expiryDateButton.setVisibility(View.INVISIBLE);
			}
			else{
				switchGetLink.setChecked(true);
				java.text.DateFormat df = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, Locale.getDefault());
				Calendar cal = Util.calculateDateFromTimestamp(expirationTimestamp);
				TimeZone tz = cal.getTimeZone();
				df.setTimeZone(tz);
				Date date = cal.getTime();
				String formattedDate = df.format(date);
				expiryDateButton.setText(formattedDate);
				expiryDateButton.setVisibility(View.VISIBLE);
			}

			switchGetLink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(switchGetLink.isChecked()){
						datePickerDialog.show();
					}
					else{
						isExpiredDateLink=true;
						NodeController nC = new NodeController(filePropertiesActivity);
						nC.exportLink(node);
					}
				}
			});
		}
		else{
			log("The is user is not PRO");
			switchGetLink.setEnabled(false);
			expiryDateButton.setVisibility(View.INVISIBLE);
		}

		log("show getLinkDialog");
		getLinkDialog.show();
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		log("onDateSet: "+year+monthOfYear+dayOfMonth);

		Calendar cal = Calendar.getInstance();
		cal.set(year, monthOfYear, dayOfMonth);
		Date date = cal.getTime();
		SimpleDateFormat dfTimestamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		String dateString = dfTimestamp.format(date);
		dateString = dateString + "2359";
		log("the date string is: "+dateString);
		int timestamp = (int) Util.calculateTimestamp(dateString);
		log("the TIMESTAMP is: "+timestamp);
		isExpiredDateLink=true;
		NodeController nC = new NodeController(this);
		log("Export node: "+node.getName());
		nC.exportLinkTimestamp(node, timestamp);
	}

	public void setIsGetLink(boolean value){
		this.isGetLink = value;
	}

	public void setExpiredDateLink(boolean expiredDateLink) {
		isExpiredDateLink = expiredDateLink;
	}

	public boolean isExpiredDateLink() {
		return isExpiredDateLink;
	}


	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		
		node = megaApi.getNodeByHandle(request.getNodeHandle());
		
		log("onRequestFinish: "+request.getType() + "__" + request.getRequestString());
		
		if (request.getType() == MegaRequest.TYPE_GET_ATTR_FILE){
			if (e.getErrorCode() == MegaError.API_OK){
				File previewDir = PreviewUtils.getPreviewFolder(this);
				File preview = new File(previewDir, node.getBase64Handle()+".jpg");
				if (preview.exists()) {
					if (preview.length() > 0) {
						Bitmap bitmap = PreviewUtils.getBitmapForCache(preview, this);
						PreviewUtils.previewCache.put(handle, bitmap);	
						if (imageView != null){
							imageView.setImageBitmap(bitmap);
							sV.post(new Runnable() { 
						        public void run() { 
						             sV.scrollTo(0, outMetrics.heightPixels/3);
						        } 
							});
						}
					}
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_EXPORT){
			log("MegaRequest.TYPE_EXPORT");
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				
				if((typeExport==TYPE_EXPORT_GET) || (typeExport == TYPE_EXPORT_MANAGE)){
					log("typeExport==TYPE_EXPORT_GET or typeExport == TYPE_EXPORT_MANAGE");

					if (isGetLink){
						final String link = request.getLink();
						MegaNode node = megaApi.getNodeByHandle(request.getNodeHandle());
						log("EXPIRATION DATE: "+node.getExpirationTime());
						if(isExpiredDateLink){
							log("change the expiration date");

							if(node.getExpirationTime()<=0){
								switchGetLink.setChecked(false);
								expiryDateButton.setVisibility(View.INVISIBLE);
							}
							else{
								switchGetLink.setChecked(true);
								java.text.DateFormat df = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, Locale.getDefault());
								Calendar cal = Util.calculateDateFromTimestamp(node.getExpirationTime());
								TimeZone tz = cal.getTimeZone();
								df.setTimeZone(tz);
								Date date = cal.getTime();
								String formattedDate = df.format(date);
								expiryDateButton.setText(formattedDate);
								expiryDateButton.setVisibility(View.VISIBLE);
							}
						}
						else{
							showGetLinkPanel(link, node.getExpirationTime());
						}
					}
					log("link: "+request.getLink());
				}
				else if(typeExport==TYPE_EXPORT_REMOVE)
				{		
					log("TYPE_EXPORT_REMOVE");
					Snackbar.make(container, getString(R.string.file_properties_remove_link), Snackbar.LENGTH_LONG).show();
				}					
				
			}
			else{
				Snackbar.make(container, getString(R.string.context_no_link), Snackbar.LENGTH_LONG).show();
				log("Error code: "+e.getErrorCode()+" "+e.getErrorString());
			}
			isGetLink=false;
			isExpiredDateLink=false;
			log("export request finished");
			createOverflowMenu(overflowMenuList);
			log("export request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_RENAME){
			
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Snackbar.make(container, getString(R.string.context_correctly_renamed), Snackbar.LENGTH_LONG).show();
				nameView.setText(megaApi.getNodeByHandle(request.getNodeHandle()).getName());
			}			
			else{
				Snackbar.make(container, getString(R.string.context_no_renamed), Snackbar.LENGTH_LONG).show();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (moveToRubbish){
				if (e.getErrorCode() == MegaError.API_OK){
					Snackbar.make(container, getString(R.string.context_correctly_moved), Snackbar.LENGTH_LONG).show();
					finish();
				}
				else{
					Snackbar.make(container, getString(R.string.context_no_moved), Snackbar.LENGTH_LONG).show();
				}
				moveToRubbish = false;
				log("move to rubbish request finished");
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					Snackbar.make(container, getString(R.string.context_correctly_moved), Snackbar.LENGTH_LONG).show();
					finish();
				}
				else{
					Snackbar.make(container, getString(R.string.context_no_moved), Snackbar.LENGTH_LONG).show();
				}
				log("move nodes request finished");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			
			log("remove request finished");
			if (e.getErrorCode() == MegaError.API_OK){				
				finish();				
			}
			else{
				Snackbar.make(container, getString(R.string.context_no_removed), Snackbar.LENGTH_LONG).show();
			}			
			
		}
		else if (request.getType() == MegaRequest.TYPE_COPY){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				if (request.getEmail() != null){
					Snackbar.make(container, getString(R.string.context_correctly_copied_contact), Snackbar.LENGTH_LONG).show();
				}
				else{
					Snackbar.make(container, getString(R.string.context_correctly_copied), Snackbar.LENGTH_LONG).show();
				}
			}
			else{
				Snackbar.make(container, getString(R.string.context_no_copied), Snackbar.LENGTH_LONG).show();
			}
			log("copy nodes request finished");
		}
		if (request.getType() == MegaRequest.TYPE_SHARE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			if (e.getErrorCode() == MegaError.API_OK){
				Snackbar.make(container, getString(R.string.context_correctly_shared), Snackbar.LENGTH_LONG).show();
				ArrayList<MegaShare> sl = megaApi.getOutShares(node);
			}
			else{
				Snackbar.make(container, getString(R.string.context_no_shared), Snackbar.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getName());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (intent == null) {
			return;
		}
		
		if (requestCode == REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
			log("local folder selected");
			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			String url = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_URL);
			long size = intent.getLongExtra(FileStorageActivityLollipop.EXTRA_SIZE, 0);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES);
			log("URL: " + url + "___SIZE: " + size);

			
			downloadTo (parentPath, url, size, hashes);
			Util.showToast(this, R.string.download_began);
		}
		else if (requestCode == REQUEST_CODE_SELECT_MOVE_FOLDER && resultCode == RESULT_OK) {
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
			final long toHandle = intent.getLongExtra("MOVE_TO", 0);
			final int totalMoves = moveHandles.length;
			
			MegaNode parent = megaApi.getNodeByHandle(toHandle);
			moveToRubbish = false;
			
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_moving));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
			
			for(int i=0; i<moveHandles.length;i++){
				megaApi.moveNode(megaApi.getNodeByHandle(moveHandles[i]), parent, this);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_COPY_FOLDER && resultCode == RESULT_OK){
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
			final long toHandle = intent.getLongExtra("COPY_TO", 0);
			final int totalCopy = copyHandles.length;
			
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_copying));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
			
			MegaNode parent = megaApi.getNodeByHandle(toHandle);
			for(int i=0; i<copyHandles.length;i++){
				megaApi.copyNode(megaApi.getNodeByHandle(copyHandles[i]), parent, this);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_CONTACT && resultCode == RESULT_OK){
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final ArrayList<String> contactsData = intent.getStringArrayListExtra(ContactsExplorerActivityLollipop.EXTRA_CONTACTS);
			final long nodeHandle = intent.getLongExtra(ContactsExplorerActivityLollipop.EXTRA_NODE_HANDLE, -1);
			final boolean megaContacts = intent.getBooleanExtra(ContactsExplorerActivityLollipop.EXTRA_MEGA_CONTACTS, true);
			
			if (megaContacts){
				if (node.isFolder()){
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
					dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
					final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
					dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							ProgressDialog temp = null;
							try{
								temp = new ProgressDialog(filePropertiesActivity);
								temp.setMessage(getString(R.string.context_sharing_folder));
								temp.show();
							}
							catch(Exception e){
								return;
							}
							statusDialog = temp;
							permissionsDialog.dismiss();
							
							switch(item) {
			                    case 0:{
			                    	for (int i=0;i<contactsData.size();i++){
			                    		MegaUser u = megaApi.getContact(contactsData.get(i));
			                    		megaApi.share(node, u, MegaShare.ACCESS_READ, filePropertiesActivity);
			                    	}
			                    	break;
			                    }
			                    case 1:{
			                    	for (int i=0;i<contactsData.size();i++){
			                    		MegaUser u = megaApi.getContact(contactsData.get(i));
			                    		megaApi.share(node, u, MegaShare.ACCESS_READWRITE, filePropertiesActivity);
			                    	}
			                        break;
			                    }
			                    case 2:{
			                    	for (int i=0;i<contactsData.size();i++){
			                    		MegaUser u = megaApi.getContact(contactsData.get(i));
			                    		megaApi.share(node, u, MegaShare.ACCESS_FULL, filePropertiesActivity);
			                    	}		                    	
			                        break;
			                    }
			                }
						}
					});
					permissionsDialog = dialogBuilder.create();
					permissionsDialog.show();
					Resources resources = permissionsDialog.getContext().getResources();
					int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
					TextView alertTitle = (TextView) permissionsDialog.getWindow().getDecorView().findViewById(alertTitleId);
			        alertTitle.setTextColor(resources.getColor(R.color.mega));
					/*int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
					View titleDivider = permissionsDialog.getWindow().getDecorView().findViewById(titleDividerId);
					titleDivider.setBackgroundColor(resources.getColor(R.color.mega));*/
				}
				else{ 
					for (int i=0;i<contactsData.size();i++){
						MegaUser u = megaApi.getContact(contactsData.get(i));
						megaApi.sendFileToUser(node, u, filePropertiesActivity);
					}
				}
			}
			else{
				if (node.isFolder()){
					for (int i=0; i < contactsData.size();i++){
						String type = contactsData.get(i);
						if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_EMAIL) == 0){
							i++;
							Snackbar.make(container, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
						}
						else if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_PHONE) == 0){
							i++;
							Snackbar.make(container, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
						}
					}
				}
				else{
					for (int i=0; i < contactsData.size();i++){
						String type = contactsData.get(i);
						if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_EMAIL) == 0){
							i++;
							Snackbar.make(container, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
						}
						else if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_PHONE) == 0){
							i++;
							Snackbar.make(container, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
						}
					}
				}
			}			
		}
	}
	
	/*
	 * Get list of all child files
	 */
	private void getDlList(Map<MegaNode, String> dlFiles, MegaNode parent, File folder) {
		
		if (megaApi.getRootNode() == null)
			return;
		
		folder.mkdir();
		ArrayList<MegaNode> nodeList = megaApi.getChildren(parent);
		for(int i=0; i<nodeList.size(); i++){
			MegaNode document = nodeList.get(i);
			if (document.getType() == MegaNode.TYPE_FOLDER) {
				File subfolder = new File(folder, new String(document.getName()));
				getDlList(dlFiles, document, subfolder);
			} 
			else {
				dlFiles.put(document, folder.getAbsolutePath());
			}
		}
	}

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		log("onUsersUpdate");		
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodes) {
		log("onNodesUpdate");
		
		boolean thisNode = false;
		if(nodes==null){
			return;
		}
		
		Iterator<MegaNode> it = nodes.iterator();
		while (it.hasNext()){
			MegaNode n = it.next();
			if (n != null){
				if (n.getHandle() == handle){
					thisNode = true;
				}
			}
		}
		
		if (!thisNode){
			log("exit onNodesUpdate - Not related to this node");
			return;
		}
		
		if (handle != -1){
			log("node updated");
			node = megaApi.getNodeByHandle(handle);
		}
		supportInvalidateOptionsMenu();

		if (node == null){
			return;
		}

		if(node.isExported()){
			log("Node HAS public link");
			publicLink=true;
			publicLinkIcon.setVisibility(View.VISIBLE);
		}	
		else{
			log("Node NOT public link");
			publicLink=false;
			publicLinkIcon.setVisibility(View.INVISIBLE);
		}
		
		if (overflowMenuList != null){
			log("overflowMenuList != null");
			createOverflowMenu(overflowMenuList);
		}
		else{
			log("overflowMenuList == null");
		}
		
		if (node.isFolder()){
			imageView.setImageResource(imageId);
			sl = megaApi.getOutShares(node);
			if (sl != null){

				if (sl.size() == 0){
					log("sl.size==0");
					sharedLayout.setVisibility(View.GONE);
					dividerSharedLayout.setVisibility(View.GONE);	
					imageView.setImageResource(R.drawable.folder_mime);
					
//					If I am the owner
					if (megaApi.checkAccess(node, MegaShare.ACCESS_OWNER).getErrorCode() == MegaError.API_OK){
						
//						permissionLabel.setVisibility(View.GONE);
//						permissionInfo.setVisibility(View.GONE);
						permissionsLayout.setVisibility(View.GONE);
						//permissionInfo.setText(getResources().getString(R.string.file_properties_owner));
						
					}
					else{	
						
						//If I am not the owner
						owner = false;
						permissionsLayout.setVisibility(View.VISIBLE);
//						permissionLabel.setVisibility(View.VISIBLE);
//						permissionInfo.setVisibility(View.VISIBLE);
						
						int accessLevel= megaApi.getAccess(node);
						log("Node: "+node.getName());
						
						switch(accessLevel){
							case MegaShare.ACCESS_OWNER:
							case MegaShare.ACCESS_FULL:{
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_full_access));								
								break;
							}
							case MegaShare.ACCESS_READ:{
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_read_only));								
								break;
							}						
							case MegaShare.ACCESS_READWRITE:{								
								permissionInfo.setText(getResources().getString(R.string.file_properties_shared_folder_read_write));
								break;
							}
						}
					}
					
				}
				else{	
					if(publicLink){
//						publicLinkTextView.setText(getResources().getString(R.string.file_properties_shared_folder_public_link));							
//
//						publicLinkTextView.setVisibility(View.VISIBLE);
						
						if(sl.size()>1){
							//It is public and shared
							imageView.setImageResource(R.drawable.folder_shared_mime);
							sharedLayout.setVisibility(View.VISIBLE);
							dividerSharedLayout.setVisibility(View.VISIBLE);	
							usersSharedWithText.setText((sl.size()-1)+" "+getResources().getQuantityString(R.plurals.general_num_users,(sl.size()-1)));
						}
						else{
							//It is just public
							imageView.setImageResource(R.drawable.folder_mime);
							sharedLayout.setVisibility(View.GONE);
							dividerSharedLayout.setVisibility(View.GONE);	
							usersSharedWithText.setText(R.string.file_properties_shared_folder_public_link);
						}
						
					}
					else{
//						publicLinkTextView.setText(getResources().getString(R.string.file_properties_shared_folder_private_folder));
						//It is private and shared
						imageView.setImageResource(R.drawable.folder_shared_mime);
						sharedLayout.setVisibility(View.VISIBLE);
						dividerSharedLayout.setVisibility(View.VISIBLE);	
						usersSharedWithText.setText(sl.size()+" "+getResources().getQuantityString(R.plurals.general_num_users,sl.size()));
					}					
				}


				if (node.getCreationTime() != 0){
					try {addedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{addedTextView.setText("");}

					if (node.getModificationTime() != 0){
						try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getModificationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}
					}
					else{
						try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}	
					}
				}
				else{
					addedTextView.setText("");
					modifiedTextView.setText("");
				}
			}

//			iconView.setImageResource(imageId);
		}
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		log("onReloadNeeded");
	}
	
	@Override
	protected void onDestroy(){
    	super.onDestroy();
    	
    	if(megaApi != null)
    	{
    		megaApi.removeGlobalListener(this);
    		megaApi.removeRequestListener(this);
    	}
    }

	public static void log(String message) {
		Util.log("FilePropertiesActivityLollipop", message);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	protected void onResume() {
		log("onResume-FilePropertiesActivityLollipop");
		super.onResume();		
		
		refreshProperties();
	}
	
	public void downloadTo(String parentPath, String url, long size, long [] hashes){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}
		}
		
		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(parentPath);
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}
		
		
		if (hashes == null){
			if(url != null) {
				if(availableFreeSpace < size) {
					Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
					return;
				}
				
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_URL, url);
				service.putExtra(DownloadService.EXTRA_SIZE, size);
				service.putExtra(DownloadService.EXTRA_PATH, parentPath);
				startService(service);
			}
		}
		else{
			if(hashes.length == 1){
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					log("ISFILE");
					String localPath = Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
					if(localPath != null){	
						try { 
							Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName())); 
						}
						catch(Exception e) {}
						
						Intent viewIntent = new Intent(Intent.ACTION_VIEW);
						viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
						if (MegaApiUtils.isIntentAvailable(this, viewIntent))
							startActivity(viewIntent);
						else{
							Intent intentShare = new Intent(Intent.ACTION_SEND);
							intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							if (MegaApiUtils.isIntentAvailable(this, intentShare))
								startActivity(intentShare);
							String toastMessage = getString(R.string.general_already_downloaded) + ": " + localPath;
							Snackbar.make(container, toastMessage, Snackbar.LENGTH_LONG).show();
						}								
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						
						if(availableFreeSpace < document.getSize()){
							Snackbar.make(container, getString(R.string.error_not_enough_free_space), Snackbar.LENGTH_LONG).show();
							continue;
						}
						
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						startService(service);
					}
				}
				else if(url != null) {
					if(availableFreeSpace < size) {
						Snackbar.make(container, getString(R.string.error_not_enough_free_space), Snackbar.LENGTH_LONG).show();
						continue;
					}
					
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					startService(service);
				}
				else {
					log("node not found");
				}
			}
		}
	}


	@Override
	public void onAccountUpdate(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onContactRequestsUpdate(MegaApiJava api,
			ArrayList<MegaContactRequest> requests) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		overflowMenuLayout.setVisibility(View.GONE);
		String itemText = (String) parent.getItemAtPosition(position);
		if (itemText.compareTo(getString(R.string.context_download)) == 0){
			if (!availableOfflineBoolean){
		    	ArrayList<Long> handleList = new ArrayList<Long>();
				handleList.add(node.getHandle());
				downloadNode(handleList);
	    	}
	    	else{
	    		
	    		File destination = null;
				File offlineFile = null;
				if (Environment.getExternalStorageDirectory() != null){
					destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/"+MegaApiUtils.createStringTree(node, this));
				}
				else{
					destination = new File(getFilesDir(), node.getHandle()+"");
				}
				
				if (destination.exists() && destination.isDirectory()){
					offlineFile = new File(destination, node.getName());
					if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getName())){ //This means that is already available offline
						availableOfflineBoolean = true;
						offlineSwitch.setChecked(true);
					}
					else{
						availableOfflineBoolean = false;
						offlineSwitch.setChecked(false);
						mOffDelete = dbH.findByHandle(node.getHandle());
						removeOffline(mOffDelete);							
						supportInvalidateOptionsMenu();
					}
				}
				else{
					availableOfflineBoolean = false;
					offlineSwitch.setChecked(false);
					mOffDelete = dbH.findByHandle(node.getHandle());
					removeOffline(mOffDelete);		
					supportInvalidateOptionsMenu();
				}
	    		Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(offlineFile), MimeTypeList.typeForName(offlineFile.getName()).getType());
				if (MegaApiUtils.isIntentAvailable(this, intent)){
					startActivity(intent);
				}
				else{
					Snackbar.make(container, getString(R.string.intent_not_available), Snackbar.LENGTH_LONG).show();
				}
	    	}
		}
		else if (itemText.compareTo(getString(R.string.context_share_folder)) == 0){
			Intent intent = new Intent(ContactsExplorerActivityLollipop.ACTION_PICK_CONTACT_SHARE_FOLDER);
	    	intent.setClass(this, ContactsExplorerActivityLollipop.class);
	    	intent.putExtra(ContactsExplorerActivityLollipop.EXTRA_NODE_HANDLE, node.getHandle());
	    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
		}
		else if (itemText.compareTo(getString(R.string.context_get_link_menu)) == 0){
	    	shareIt = false;
	    	typeExport=TYPE_EXPORT_GET;
	    	getPublicLinkAndShareIt();
		}
		else if (itemText.compareTo(getString(R.string.context_manage_link_menu)) == 0){
	    	shareIt = false;
	    	typeExport=TYPE_EXPORT_MANAGE;
	    	getPublicLinkAndShareIt();	    
		}
		else if (itemText.compareTo(getString(R.string.context_remove_link_menu)) == 0){
	    	shareIt = false;
	    	AlertDialog removeLinkDialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

			LayoutInflater inflater = getLayoutInflater();
			View dialoglayout = inflater.inflate(R.layout.dialog_link, null);
			TextView url = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_url);
			TextView key = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_key);
			TextView symbol = (TextView) dialoglayout.findViewById(R.id.dialog_link_symbol);
			TextView removeText = (TextView) dialoglayout.findViewById(R.id.dialog_link_text_remove);
			
			((RelativeLayout.LayoutParams) removeText.getLayoutParams()).setMargins(Util.scaleWidthPx(25, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(10, outMetrics), 0);
			
			url.setVisibility(View.GONE);
			key.setVisibility(View.GONE);
			symbol.setVisibility(View.GONE);
			removeText.setVisibility(View.VISIBLE);
			
			removeText.setText(getString(R.string.context_remove_link_warning_text));
	    	
			Display display = getWindowManager().getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			float density = getResources().getDisplayMetrics().density;

			float scaleW = Util.getScaleW(outMetrics, density);
			float scaleH = Util.getScaleH(outMetrics, density);
			
			removeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (18*scaleW));
			
			builder.setView(dialoglayout);
			
			builder.setPositiveButton(getString(R.string.context_remove), new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					typeExport=TYPE_EXPORT_REMOVE;
			    	megaApi.disableExport(node, filePropertiesActivity);
				}
			});
			
			builder.setNegativeButton(getString(R.string.general_cancel), new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			
			removeLinkDialog = builder.create();
			removeLinkDialog.show();
	    }
		else if (itemText.compareTo(getString(R.string.context_rename)) == 0){
	    	showRenameDialog();
		}
		else if (itemText.compareTo(getString(R.string.context_move)) == 0){
	    	showMove();
		}
		else if (itemText.compareTo(getString(R.string.context_copy)) == 0){
	    	showCopy();	
		}
		else if (itemText.compareTo(getString(R.string.context_send_link)) == 0){
	    	shareIt = true;
	    	getPublicLinkAndShareIt();
		}
		else if (itemText.compareTo(getString(R.string.context_leave_menu)) == 0){
		    leaveIncomingShare();
		}
		else if (itemText.compareTo(getString(R.string.context_move_to_trash)) == 0){
	    	moveToTrash();
		}
		else if (itemText.compareTo(getString(R.string.context_delete)) == 0){
	    	moveToTrash();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (overflowMenuLayout != null){
			if (overflowMenuLayout.getVisibility() == View.VISIBLE){
				overflowMenuLayout.setVisibility(View.GONE);
				return;
			}
		}
		super.onBackPressed();
	}

	public void showSnackbar(String s){
		log("showSnackbar");
		Snackbar snackbar = Snackbar.make(container, s, Snackbar.LENGTH_LONG);
		TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarTextView.setMaxLines(5);
		snackbar.show();
	}
}
