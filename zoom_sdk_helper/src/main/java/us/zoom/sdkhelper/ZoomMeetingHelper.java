package us.zoom.sdkhelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import us.zoom.sdk.ZoomUIDelegate;
import us.zoom.sdk.IMeetingInviteMenuItem;
import us.zoom.sdk.VideoScene;
import java.util.List;
import us.zoom.sdk.InMeetingServiceListener;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;
import us.zoom.sdk.ZoomSDKRawDataMemoryMode;

public class ZoomMeetingHelper {
    private static final String TAG = "ZoomMeetingHelper";
    private static ZoomMeetingHelper instance;
    private ZoomSDK mZoomSDK;

    private ZoomMeetingHelper() {
        mZoomSDK = ZoomSDK.getInstance();
    }

    public static synchronized ZoomMeetingHelper getInstance() {
        if (instance == null) {
            instance = new ZoomMeetingHelper();
        }
        return instance;
    }

    public void initSDK(Context context, String clientId, String clientSecret, final ZoomSDKInitializeListener listener) {
        if (!mZoomSDK.isInitialized()) {
            ZoomSDKInitParams initParams = new ZoomSDKInitParams();
            initParams.jwtToken = JwtUtils.createJWTToken(clientId, clientSecret);
            initParams.domain = "zoom.us";
            initParams.enableLog = true;
            initParams.enableGenerateDump = true;
            initParams.logSize = 5;
            initParams.videoRawDataMemoryMode = ZoomSDKRawDataMemoryMode.ZoomSDKRawDataMemoryModeStack;
            mZoomSDK.initialize(context, new ZoomSDKInitializeListener() {
                @Override
                public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
                    if (errorCode == 0) {
                        // Enable Mini Meeting and PIP
                        mZoomSDK.getZoomUIService().enableMinimizeMeeting(true);
                        mZoomSDK.getZoomUIService().disablePIPMode(false);
                        
                        mZoomSDK.getMeetingSettingsHelper().enable720p(false);
                        mZoomSDK.getMeetingSettingsHelper().enableShowMyMeetingElapseTime(true);
                        
                        // Ensure back button in meeting shows leave dialog and works on waiting screen
                        mZoomSDK.getMeetingSettingsHelper().setNoLeaveMeetingButtonForHostEnabled(false);
                        mZoomSDK.getMeetingSettingsHelper().disableLeaveMeetingWhenTaskRemoved(false);
                        mZoomSDK.getMeetingSettingsHelper().disableAutoShowSelectJoinAudioDlgWhenJoinMeeting(false);

                        // Set UI Delegate to handle minimize
                        mZoomSDK.getZoomUIService().setZoomUIDelegate(new ZoomUIDelegate() {
                            @Override
                            public boolean onClickInviteButton(Context context, List<IMeetingInviteMenuItem> list) {
                                return false;
                            }

                            @Override
                            public void afterMeetingMinimized(Activity activity) {
                                Log.d(TAG, "Meeting Minimized");
                                // Ensure the app activity is brought to front
                                try {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setPackage(activity.getPackageName());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error bringing app to front", e);
                                }
                            }

                            @Override
                            public boolean onClickEndButton() {
                                return false;
                            }

                            @Override
                            public boolean onClickAudioButton() {
                                return false;
                            }

                            @Override
                            public boolean onClickVideoButton() {
                                return false;
                            }

                            @Override
                            public boolean onClickShareButton() {
                                return false;
                            }

                            @Override
                            public boolean onClickMoreButton() {
                                return false;
                            }

                            @Override
                            public boolean onClickParticipantsButton() {
                                return false;
                            }

                            @Override
                            public void onVideoSceneChanged(VideoScene videoScene, VideoScene videoScene1) {

                            }
                        });
                    }
                    if (listener != null) {
                        listener.onZoomSDKInitializeResult(errorCode, internalErrorCode);
                    }
                }

                @Override
                public void onZoomAuthIdentityExpired() {
                    if (listener != null) {
                        listener.onZoomAuthIdentityExpired();
                    }
                }
            }, initParams);
        } else if (listener != null) {
            listener.onZoomSDKInitializeResult(0, 0);
        }
    }

    /**
     * Checks if the overlay permission (Display over other apps) is granted.
     * Required for Zoom's "Mini Meeting" (floating window) window.
     */
    public boolean checkOverlayPermission(Activity activity) {
        if (Settings.canDrawOverlays(activity)) {
            return true;
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, 1001);
            Toast.makeText(activity, "Please enable 'Display over other apps' to use Mini Meeting window", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public int joinMeeting(Context context, String meetingId, String passcode, String displayName, String customerKey) {
        if (!mZoomSDK.isInitialized()) {
            Log.e(TAG, "SDK not initialized. Call initSDK first.");
            return -1;
        }

        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.content.ContextWrapper) {
            Context baseContext = ((android.content.ContextWrapper) context).getBaseContext();
            if (baseContext instanceof Activity) {
                activity = (Activity) baseContext;
            }
        }

        if (activity != null) {
            checkOverlayPermission(activity);
        }

        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = meetingId;
        params.displayName = displayName;
        params.password = passcode;

        JoinMeetingOptions options = new JoinMeetingOptions();
        options.customer_key = customerKey;
        options.no_meeting_end_message = false; // Ensure leave dialog shows
        // You can customize options here (e.g. options.no_audio = true)

        Log.d(TAG, "Joining meeting: " + meetingId + " as " + displayName);
        return mZoomSDK.getMeetingService().joinMeetingWithParams(context, params, options);
    }

    public boolean isInitialized() {
        return mZoomSDK.isInitialized();
    }

    public boolean isMeetingOngoing() {
        if (!isInitialized()) return false;
        MeetingStatus status = mZoomSDK.getMeetingService().getMeetingStatus();
        return status != MeetingStatus.MEETING_STATUS_IDLE && status != MeetingStatus.MEETING_STATUS_FAILED;
    }

    public void leaveMeeting(boolean endIfHost) {
        if (isInitialized()) {
            mZoomSDK.getMeetingService().leaveCurrentMeeting(endIfHost);
        }
    }

    public void showMiniMeetingWindow() {
        if (isInitialized()) {
            mZoomSDK.getZoomUIService().showMiniMeetingWindow();
        }
    }

    public void hideMiniMeetingWindow() {
        if (isInitialized()) {
            mZoomSDK.getZoomUIService().hideMiniMeetingWindow();
        }
    }
}
