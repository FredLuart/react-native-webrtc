package com.oney.WebRTCModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;

public class ScreenCaptureController extends AbstractVideoCaptureController {
    /**
     * The {@link Log} tag with which {@code ScreenCaptureController} is to log.
     */
    private static final String TAG = ScreenCaptureController.class.getSimpleName();

    private static final int DEFAULT_FPS = 30;

    private final Intent mediaProjectionPermissionResultData;

    private final OrientationEventListener orientationListener;

    private final Context context;
    private final int savedWidth;
    private final int savedHeight;

    public ScreenCaptureController(Context context, int width, int height, Intent mediaProjectionPermissionResultData) {
        super(width, height, DEFAULT_FPS);

        this.mediaProjectionPermissionResultData = mediaProjectionPermissionResultData;

        this.context = context;
        this.savedWidth = width;
        this.savedHeight = height;

        this.orientationListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {


//A voir ici car l'ancien code
//permet de gérer l'évolution de la taille de l'écran au changement d'orientation
//TODO vérifier si on récup les bons width et height après rotation écran
//Il faudrait savoir si les valeurs viennent des contraintes ou non



                // Pivot to the executor thread because videoCapturer.changeCaptureFormat runs in the main
                // thread and may deadlock.
                ThreadUtils.runOnExecutor(() -> {
                    try {
                        videoCapturer.changeCaptureFormat(savedWidth, savedHeight, DEFAULT_FPS);
                    } catch (Exception ex) {
                        // We ignore exceptions here. The video capturer runs on its own
                        // thread and we cannot synchronize with it.
                    }
                });
            }
        };

        if (this.orientationListener.canDetectOrientation()) {
            this.orientationListener.enable();
        }
    }

    @Override
    public String getDeviceId() {
        return "screen-capture";
    }

    @Override
    public void dispose() {
        MediaProjectionService.abort(context);
        super.dispose();
    }

    @Override
    protected VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer =
                new ScreenCapturerAndroid(mediaProjectionPermissionResultData, new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        Log.w(TAG, "Media projection stopped.");
                        orientationListener.disable();
                        stopCapture();

                        if (capturerEventsListener != null) {
                            capturerEventsListener.onCapturerEnded();
                        }
                    }
                });

        return videoCapturer;
    }
}
