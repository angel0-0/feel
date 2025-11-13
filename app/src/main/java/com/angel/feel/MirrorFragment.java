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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MirrorFragment extends Fragment {

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Size previewSize;

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
        return inflater.inflate(R.layout.fragment_mirror, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textureView = view.findViewById(R.id.camera_preview);
        // This line is CRITICAL to prevent the crash. My sincere apologies for my previous errors.
        textureView.setOpaque(false);
        loadYouPhrase(view);
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

    private void openCameraWithPermissionCheck() {
        if (!isAdded()) return; // Prevent crash if fragment is detached
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        if (!isAdded()) return; // Prevent crash
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
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

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void transformTextureView(int viewWidth, int viewHeight) {
        if (textureView == null || previewSize == null || viewWidth == 0 || viewHeight == 0 || !isAdded()) return;

        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        // The camera sensor is landscape. The preview size is landscape (e.g., 1280x720).
        // The view is portrait. We must rotate the preview to match.
        float previewWidth = previewSize.getWidth();
        float previewHeight = previewSize.getHeight();

        RectF bufferRect = new RectF(0, 0, previewWidth, previewHeight);

        // The view is portrait, so we need to rotate the buffer's rectangle for calculation
        RectF rotatedBufferRect = new RectF(0, 0, previewHeight, previewWidth); // Swapped dimensions
        rotatedBufferRect.offset(centerX - rotatedBufferRect.centerX(), centerY - rotatedBufferRect.centerY());

        // Use `setRectToRect` to map the portrait-oriented buffer rectangle to the view rectangle
        // Using `ScaleToFit.CENTER` to match the ImageView's `fitCenter` scaleType. This is the fix.
        matrix.setRectToRect(viewRect, rotatedBufferRect, Matrix.ScaleToFit.CENTER);
        
        // Apply the necessary rotation since the buffer is actually landscape
        matrix.postRotate(-90, centerX, centerY);

        // Mirror the view for the front camera
        matrix.postScale(-1, 1, centerX, centerY);

        textureView.setTransform(matrix);
    }


    private Size chooseOptimalPreviewSize(Size[] choices, int viewWidth, int viewHeight) {
        float targetRatio = (float) viewHeight / viewWidth; // Portrait aspect ratio
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : choices) {
            // The camera sizes are landscape, so we compare width/height to the view's height/width
            float ratio = (float) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > 0.05) continue;
            if (Math.abs(size.getHeight() - viewHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - viewHeight);
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
