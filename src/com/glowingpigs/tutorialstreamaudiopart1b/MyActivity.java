package com.glowingpigs.tutorialstreamaudiopart1b;

//import com.example.audioplayer1.R;

//import com.example.audioplayer1.R;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.glowingpigs.tutorialstreamaudiopart1b.myPlayService.LocalBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

/* This file contains the source code for examples discussed in Tutorials 1-9 of developerglowingpigs YouTube channel.
 *  The source code is for your convenience purposes only. The source code is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

//---Implement OnSeekBarChangeListener to keep track of seek bar changes ---
public class MyActivity extends Activity implements OnSeekBarChangeListener {
	Intent serviceIntent;
	private Button buttonPlayStop;

	private LinearLayout list;
	private LinearLayout linearLayout;
	private Button list_Button;
	private Button play_Button; // PLAY 버튼
	private Button pause_Button; // PAUSE 버튼
	private Button back_Button;
	private Button home_Button;

	//
	private Cursor audiocursor;
	private int count;
	private ListView PhoneAideoList;
	private int audio_column_index;
	private TextView artist_View;
	private TextView album_View;
	private TextView year_View;
	private TextView filesize_View;
	private TextView position_View;
	private ImageView image_View;
	

	//
	private myPlayService mService = null;
	boolean mBound = false;
	private ServiceConnection mConnection;

	// -- PUT THE NAME OF YOUR AUDIO FILE HERE...URL GOES IN THE SERVICE

	private String strAudioLink = null;

	private boolean isOnline;
	private boolean boolMusicPlaying = false;
	TelephonyManager telephonyManager;
	PhoneStateListener listener;

	private boolean once = false;
	// --Seekbar variables --
	private SeekBar seekBar;
	private int seekMax;
	private static int songEnded = 0;
	boolean mBroadcastIsRegistered;

	// --Set up constant ID for broadcast of seekbar position--
	public static final String BROADCAST_SEEKBAR = "com.glowingpigs.tutorialstreamaudiopart1b.sendseekbar";
	Intent intent;

	// Progress dialogue and broadcast receiver variables
	boolean mBufferBroadcastIsRegistered;
	private ProgressDialog pdBuff = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {

				LocalBinder binder = (LocalBinder) service;
				mService = binder.getService();
				if(mService != null){
					mBound = true;
					Log.i("wefwefwefwef", "no instance");
				}
				
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				mBound = false;
			}
		};

		list = (LinearLayout) findViewById(R.id.List);
		linearLayout = (LinearLayout) findViewById(R.id.Right_Container);
		list_Button = (Button) findViewById(R.id.list);
		home_Button = (Button) findViewById(R.id.home);

		Button service_end = (Button) findViewById(R.id.rw);
		Button service_ff = (Button) findViewById(R.id.ff);
		Button service_start = (Button) findViewById(R.id.play);
		// Button service_pause = (Button) findViewById(R.id.pause);

		artist_View = (TextView) findViewById(R.id.Artist_View);
		album_View = (TextView) findViewById(R.id.Album_View);
		year_View = (TextView) findViewById(R.id.Year_View);
		filesize_View = (TextView) findViewById(R.id.Filesize_View);
		position_View = (TextView) findViewById(R.id.Position_View);
		image_View = (ImageView) findViewById(R.id.Image_View);

		linearLayout.setVisibility(View.VISIBLE);
		list.setVisibility(View.INVISIBLE);

		home_Button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub

				Intent showOptions = new Intent(Intent.ACTION_MAIN);

				showOptions.addCategory(Intent.CATEGORY_HOME);

				startActivity(showOptions);

			}

		});

		list_Button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				// list_Button.setSelected(true);

				// list_Button.setPressed(true);

				init_phone_audio_grid();

			}
		});

		try {
			serviceIntent = new Intent(this, myPlayService.class);

			// --- set up seekbar intent for broadcasting new position to
			// service ---
			intent = new Intent(BROADCAST_SEEKBAR);

			initViews();
			setListeners();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					e.getClass().getName() + " " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	// 0625 수정한 소스 시작
	//album art 가져오는 것
	
	/*
	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");

	
	public Bitmap getBitmapImage(int id, int w, int h) {
		ContentResolver res = mContext.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, id);
		image_View = (ImageView)findViewById(R.id.Image_View);
		image_View.setImageURI(art_uri);
		if (uri != null) {
			ParcelFileDescriptor fd = null;
			try {
				fd = res.openFileDescriptor(uri, "r");
				int sampleSize = 1;

				sBitmapOptionsCache.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
						null, sBitmapOptionsCache);
				int nextWidth = sBitmapOptionsCache.outWidth >> 1;
				int nextHeight = sBitmapOptionsCache.outHeight >> 1;
				while (nextWidth > w && nextHeight > h) {
					sampleSize <<= 1;
					nextWidth >>= 1;
					nextHeight >>= 1;
				}
				sBitmapOptionsCache.inSampleSize = sampleSize;
				sBitmapOptionsCache.inJustDecodeBounds = false;
				Bitmap b = BitmapFactory.decodeFileDescriptor(
						fd.getFileDescriptor(), null, sBitmapOptionsCache);
				if (b != null) {
					if (sBitmapOptionsCache.outWidth != w
							|| sBitmapOptionsCache.outHeight != h) {
						Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
						b.recycle();
						b = tmp;
					}
				}
				return b;
			} catch (FileNotFoundException e) {
				return null;
			} catch (Exception e) {
				return null;
			} finally {
				try {
					if (fd != null)
						fd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
*/
	// 0625수정한 소스 끝

	// -- Broadcast Receiver to update position of seekbar from service --
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent serviceIntent) {
			updateUI(serviceIntent);
		}
	};

	private void updateUI(Intent serviceIntent) {
		String counter = serviceIntent.getStringExtra("counter");
		String mediamax = serviceIntent.getStringExtra("mediamax");
		String strSongEnded = serviceIntent.getStringExtra("song_ended");
		int seekProgress = Integer.parseInt(counter);
		seekMax = Integer.parseInt(mediamax);
		songEnded = Integer.parseInt(strSongEnded);
		seekBar.setMax(seekMax);
		seekBar.setProgress(seekProgress);
		if (songEnded == 1) {
			buttonPlayStop.setBackgroundResource(R.drawable.play_button);
		}
	}

	// --End of seekbar update code--

	// --- Set up initial screen ---
	private void initViews() {
		buttonPlayStop = (Button) findViewById(R.id.play);
		buttonPlayStop.setBackgroundResource(R.drawable.play_button);

		// --Reference seekbar in main.xml
		seekBar = (SeekBar) findViewById(R.id.progress);
	}

	// --- Set up listeners ---
	private void setListeners() {
		buttonPlayStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonPlayStopClick();
			}
		});
		seekBar.setOnSeekBarChangeListener(this);

	}

	// --- invoked from ButtonPlayStop listener above ----
	private void buttonPlayStopClick() {
		if (!boolMusicPlaying) {
			buttonPlayStop.setBackgroundResource(R.drawable.pause_button);
			playAudio();
			boolMusicPlaying = true;
		} else {
			if (boolMusicPlaying) {
				buttonPlayStop.setBackgroundResource(R.drawable.play_button);
				stopMyPlayService();
				//mService.pauseMedia();
				boolMusicPlaying = false;
			}
		}
	}

	// --- Stop service (and music) ---
	private void stopMyPlayService() {
		// mService.pauseMedia();

		// --Unregister broadcastReceiver for seekbar
		/*
		if (mBroadcastIsRegistered) {
			try {
				unregisterReceiver(broadcastReceiver);
				mBroadcastIsRegistered = false;
			} catch (Exception e) {
				// Log.e(TAG, "Error in Activity", e);
				// TODO Auto-generated catch block

				e.printStackTrace();
				Toast.makeText(

				getApplicationContext(),

				e.getClass().getName() + " " + e.getMessage(),

				Toast.LENGTH_LONG).show();
			}
		}*/

		try {
			 //stopService(serviceIntent);
			mService.pauseMedia();
			boolMusicPlaying = false;

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
					e.getClass().getName() + " " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		boolMusicPlaying = false;
	}

	// --- Start service and play music ---
	private void playAudio() {

		//checkConnectivity();
		//if (isOnline) {
		if(once==false){
			
			stopMyPlayService();
			once = true;
			
			

			serviceIntent.putExtra("sentAudioLink", strAudioLink);

			try {
				startService(serviceIntent);
				bindService(serviceIntent, mConnection,
						Context.BIND_AUTO_CREATE);
			} catch (Exception e) {

				e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						e.getClass().getName() + " " + e.getMessage(),

						Toast.LENGTH_LONG).show();
			}

			// -- Register receiver for seekbar--
			registerReceiver(broadcastReceiver, new IntentFilter(
					myPlayService.BROADCAST_ACTION));
			;
			mBroadcastIsRegistered = true;
		}else{
			mService.playMedia();
		}

		//} 
		/*
		else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Network Not Connected...");
			alertDialog.setMessage("Please connect to a network and try again");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// here you can add functions
				}
			});
			alertDialog.setIcon(R.drawable.icon);
			buttonPlayStop.setBackgroundResource(R.drawable.play_button);
			alertDialog.show();
		}*/
	}

	// Handle progress dialogue for buffering...
	private void showPD(Intent bufferIntent) {
		String bufferValue = bufferIntent.getStringExtra("buffering");
		int bufferIntValue = Integer.parseInt(bufferValue);

		// When the broadcasted "buffering" value is 1, show "Buffering"
		// progress dialogue.
		// When the broadcasted "buffering" value is 0, dismiss the progress
		// dialogue.

		switch (bufferIntValue) {
		case 0:
			// Log.v(TAG, "BufferIntValue=0 RemoveBufferDialogue");
			// txtBuffer.setText("");
			if (pdBuff != null) {
				pdBuff.dismiss();
			}
			break;

		case 1:
			BufferDialogue();
			break;

		// Listen for "2" to reset the button to a play button
		case 2:
			buttonPlayStop.setBackgroundResource(R.drawable.play_button);
			break;

		}
	}

	// Progress dialogue...
	private void BufferDialogue() {

		pdBuff = ProgressDialog.show(MyActivity.this, "Buffering...",
				"Acquiring song...", true);
	}

	// Set up broadcast receiver
	private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent bufferIntent) {
			showPD(bufferIntent);
		}
	};

	private void checkConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnectedOrConnecting()
				|| cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting())
			isOnline = true;
		else
			isOnline = false;
	}

	// -- onPause, unregister broadcast receiver. To improve, also save screen
	// data ---
	@Override
	protected void onPause() {
		// Unregister broadcast receiver
		if (mBufferBroadcastIsRegistered) {
			unregisterReceiver(broadcastBufferReceiver);
			mBufferBroadcastIsRegistered = false;
		}
		super.onPause();
	}

	// -- onResume register broadcast receiver. To improve, retrieve saved
	// screen data ---
	@Override
	protected void onResume() {
		// Register broadcast receiver
		if (!mBufferBroadcastIsRegistered) {
			registerReceiver(broadcastBufferReceiver, new IntentFilter(
					myPlayService.BROADCAST_BUFFER));
			mBufferBroadcastIsRegistered = true;
		}
		super.onResume();
	}

	// --- When user manually moves seekbar, broadcast new position to service
	// ---
	@Override
	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		if (fromUser) {
			int seekPos = sb.getProgress();
			intent.putExtra("seekpos", seekPos);
			sendBroadcast(intent);
		}
	}

	// --- The following two methods are alternatives to track seekbar if moved.
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	static class ViewHolder {
		TextView txtTitle;
		TextView txtSize;
		ImageView thumbImage;
		TextView runningTime;
	}

	@SuppressWarnings("deprecation")
	private void init_phone_audio_grid() {
		linearLayout.setVisibility(View.INVISIBLE);
		list.setVisibility(View.VISIBLE);

		System.gc();
		String[] proj = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.YEAR, MediaStore.Audio.Media.SIZE };
		audiocursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				proj, null, null, null);
		count = audiocursor.getCount();
		PhoneAideoList = (ListView) findViewById(R.id.PhoneAideoList);
		PhoneAideoList.setAdapter(new audioAdapter(getApplicationContext()));
		PhoneAideoList.setOnItemClickListener(audiogridlistener);
	}

	private OnItemClickListener audiogridlistener = new OnItemClickListener() {
		public void onItemClick(AdapterView parent, View v, int position,
				long id) {

			System.gc();
			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
			position_View.setText(audiocursor.getString(audio_column_index));
			audiocursor.moveToPosition(position);

			// filename = audiocursor.getString(audio_column_index);
			strAudioLink = audiocursor.getString(audio_column_index);

			// audioview.setaudioPath(filename);
			audiocursor.moveToPosition(position);

			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
			// movie_Name.setText(audiocursor.getString(audio_column_index));
			audiocursor.moveToPosition(position);

			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
			String iduration = audiocursor.getString(audio_column_index);
			// audiocursor.moveToPosition(position);

			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
			// audiocursor.moveToPosition(position);
			artist_View.setText(audiocursor.getString(audio_column_index));

			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
			// audiocursor.moveToPosition(position);
			album_View.setText(audiocursor.getString(audio_column_index));

			audio_column_index = audiocursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
			// audiocursor.moveToPosition(position);
			year_View.setText(audiocursor.getString(audio_column_index));

			long timeInmillisec = Long.parseLong(iduration);
			long duration = timeInmillisec / 1000;
			int hours = (int) (duration / 3600);
			int minutes = (int) ((duration - hours * 3600) / 60);
			int seconds = (int) (duration - (hours * 3600 + minutes * 60));

			String time = String.format("%02d", hours) + ":"
					+ String.format("%02d", minutes) + ":"
					+ String.format("%02d", seconds);
			// timeText2.setText(time);

			// audioview.requestFocus();

			list.setVisibility(View.INVISIBLE);
			linearLayout.setVisibility(View.VISIBLE);

			buttonPlayStop.setBackgroundResource(R.drawable.pause_button);
			boolMusicPlaying = true;
			playAudio();
		}
	};

	public class audioAdapter extends BaseAdapter {
		private Context vContext;

		public audioAdapter(Context c) {
			vContext = c;
		}

		public int getCount() {
			return count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			System.gc();
			ViewHolder holder;
			String id = null;

			convertView = null;
			if (convertView == null) {
				//
				convertView = LayoutInflater.from(vContext).inflate(
						R.layout.listitem, parent, false);
				holder = new ViewHolder();
				holder.txtTitle = (TextView) convertView
						.findViewById(R.id.txtTitle);
				holder.txtSize = (TextView) convertView
						.findViewById(R.id.txtSize);
				holder.runningTime = (TextView) convertView
						.findViewById(R.id.runningTime);
				holder.thumbImage = (ImageView) convertView
						.findViewById(R.id.imgIcon);

				// ListView
				audio_column_index = audiocursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
				audiocursor.moveToPosition(position);
				id = audiocursor.getString(audio_column_index);
				holder.txtTitle.setText(id);

				// ListView
				audio_column_index = audiocursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
				audiocursor.moveToPosition(position);
				String size = audiocursor.getString(audio_column_index);

				long lsize = Long.parseLong(size);
				double dsize = lsize / 1000000;
				String ssize = String.format("%4.1f", dsize);
				holder.txtSize.setText(ssize + "Mb" + " | ");

				filesize_View.setText(ssize + "Mb");

				// ListView
				audio_column_index = audiocursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
				audiocursor.moveToPosition(position);
				String iduration = audiocursor.getString(audio_column_index);

				String time;

				long timeInmillisec = Long.parseLong(iduration);

				long duration = timeInmillisec / 1000;
				int hours = (int) (duration / 3600);
				int minutes = (int) ((duration - hours * 3600) / 60);
				int seconds = (int) (duration - (hours * 3600 + minutes * 60));

				time = String.format("%02d", hours) + ":"
						+ String.format("%02d", minutes) + ":"
						+ String.format("%02d", seconds);

				holder.runningTime.setText(time);
				audiocursor.moveToPosition(position);

				String[] proj = { MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.YEAR,
						MediaStore.Audio.Media.SIZE };
				@SuppressWarnings("deprecation")
				Cursor cursor = managedQuery(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
						MediaStore.Audio.Media.DISPLAY_NAME + "=?",
						new String[] { id }, null);
				cursor.moveToFirst();
				long ids = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media._ID));

				//
				long albumId = ids;
				Uri sArtworkUri = Uri
						.parse("content://media/external/audio/albumart");
				Uri sAlbumArtUri = ContentUris.withAppendedId(sArtworkUri,
						albumId);
				image_View.setImageURI(sAlbumArtUri);
				//

				ContentResolver crThumb = getContentResolver();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				// MediaStore.Audio.
				// Bitmap curThumb = MediaStore.Images.Thumbnails(
				// crThumb, ids,Images.Thumbnails.MICRO_KIND.null);
				// Bitmap sizingBmp = Bitmap.createScaledBitmap(curThumb, 140,
				// 90, true);
				// holder.thumbImage.setImageBitmap(curThumb);
				holder.thumbImage.setImageBitmap(null);
				// curThumb = null;
			}
			return convertView;
		}
	}

}