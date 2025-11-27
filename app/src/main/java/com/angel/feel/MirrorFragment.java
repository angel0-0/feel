package com.angel.feel;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.angel.feel.ShakeListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MirrorFragment extends Fragment implements ShakeListener {

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Size previewSize;
    private int sensorOrientation;
    private View mainView;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    if (isAdded()) Toast.makeText(getContext(), "Camera permission is required.", Toast.LENGTH_LONG).show();
                }
            });

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCameraWithPermissionCheck();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            transformTextureView(width, height);
        }

        @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) { return true; }
        @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); }
        @Override public void onError(@NonNull CameraDevice camera, int error) {
            if (cameraDevice != null) cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_mirror, container, false);
        return mainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textureView = view.findViewById(R.id.camera_preview);
        textureView.setOpaque(false);
        loadYouPhrase(view);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCameraWithPermissionCheck();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onShakeAndColorChange(int color) {
        if (mainView != null) {
            mainView.setBackgroundColor(color);
        }
    }

    private void openCameraWithPermissionCheck() {
        if (!isAdded()) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        if (!isAdded()) return;
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert map != null;
                    previewSize = chooseOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());
                    if (isAdded()) {
                        textureView.post(() -> transformTextureView(textureView.getWidth(), textureView.getHeight()));
                    }
                    break;
                }
            }
            if (cameraId == null) return;
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        if (cameraDevice == null || !textureView.isAvailable() || previewSize == null) return;
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            final CaptureRequest.Builder previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        cameraCaptureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void transformTextureView(int viewWidth, int viewHeight) {
        if (textureView == null || previewSize == null || viewWidth == 0 || viewHeight == 0 || !isAdded()) {
            return;
        }

        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        float bufferWidth = previewSize.getWidth();
        float bufferHeight = previewSize.getHeight();

        int displayRotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        int totalRotation = (sensorOrientation - (displayRotation * 90) + 360) % 360;

        float previewRotatedWidth, previewRotatedHeight;
        if (totalRotation == 90 || totalRotation == 270) {
            previewRotatedWidth = bufferHeight;
            previewRotatedHeight = bufferWidth;
        } else {
            previewRotatedWidth = bufferWidth;
            previewRotatedHeight = bufferHeight;
        }

        float scale = Math.max(
                (float) viewWidth / previewRotatedWidth,
                (float) viewHeight / previewRotatedHeight);

        matrix.postScale(scale, scale, centerX, centerY);
        matrix.postRotate(totalRotation, centerX, centerY);
        matrix.postScale(-1, 1, centerX, centerY);
        
        textureView.setTransform(matrix);
    }


    private Size chooseOptimalPreviewSize(Size[] choices, int viewWidth, int viewHeight) {
        if (viewWidth == 0 || viewHeight == 0) return choices[0];
        float targetRatio = (float) viewWidth / viewHeight;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : choices) {
            float ratio = (float) size.getWidth() / (float) size.getHeight();
            if (Math.abs(ratio - targetRatio) < 0.1) { 
                if (Math.abs(size.getHeight() - viewHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - viewHeight);
                }
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : choices) {
                if (Math.abs(size.getHeight() - viewHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - viewHeight);
                }
            }
        }
        return optimalSize;
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) { cameraCaptureSession.close(); cameraCaptureSession = null; }
        if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadYouPhrase(View view) {
        if (!isAdded()) return;
        TextView phraseTextView = view.findViewById(R.id.you_phrase_text);
        SharedPreferences prefs = requireContext().getSharedPreferences("user_phrases", Context.MODE_PRIVATE);
        Set<String> userPhrases = prefs.getStringSet("you_phrases", new HashSet<>());
        if (userPhrases.isEmpty() || userPhrases.size() == 0) {
            phraseTextView.setText("it's still you");
        } else {
            int randomIndex = new Random().nextInt(userPhrases.size());
            String randomPhrase = (String) userPhrases.toArray()[randomIndex];
            phraseTextView.setText(randomPhrase);
        }
    }
}
