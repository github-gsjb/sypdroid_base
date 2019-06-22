package com.wificamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoStream;

import java.io.IOException;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class CameraManager {
    private final static String TAG = "CameraManager";
    public Camera mCamera;
    private SurfaceHolder mHoder;
    private OnFrameCallback onFrameCallback;
    private SurfaceView mSurfaceView = null;
    private Boolean mPreviewStarted ;
    /**
     * 初始化界面
     */
    private CameraManager () {}

    private static CameraManager cameraManager = null;


    public static CameraManager getInstance() {
        if (cameraManager == null) {
            synchronized (CameraManager.class) {
                if (cameraManager == null) {
                    cameraManager = new CameraManager();
                }
            }
        }
        return cameraManager;
    }

    public CameraManager setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        return this;
    }

    /**
     * 设置帧数据回调
     *
     * @param onFrameCallback
     */
    public void setOnFrameCallback(OnFrameCallback onFrameCallback) {
        this.onFrameCallback = onFrameCallback;
    }

    /**
     * 初始化预览界面
     *
     */
    public void initSurface() {
        mHoder = mSurfaceView.getHolder();
        mHoder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHoder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                initCamera();

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.d("ggh", "预览摄像头");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//                destroy();
            }
        });
    }


    /**
     * 初始化摄像头
     */
    public void initCamera() {
        if (mCamera == null) {
            //摄像头设置，预览视频,实例化摄像头类对象  0为后置 1为前置
            mCamera = Camera.open(0);
            //视频旋转90度
//            mCamera.setDisplayOrientation(90);
            //将摄像头参数传入p中
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode("off");

            Camera.Parameters camParams = mCamera.getParameters();

            //设置预览视频的尺寸，CIF格式352×288
            p.setPreviewSize(WiFiCamera_Constant.Camera_mWidth, WiFiCamera_Constant.Camera_mHeight);
            //设置预览的帧率，15帧/秒
            p.setPreviewFrameRate(CameraConfig.framerate);
            p.set("rotation", 90);
//            p.setPreviewFormat(ImageFormat.NV21);
            //设置参数
            mCamera.setParameters(p);
//            byte[] rawBuf = new byte[1400];
//            mCamera.addCallbackBuffer(rawBuf);
            try {
                //预览的视频显示到指定窗口
                mCamera.setPreviewDisplay(mHoder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
            //获取帧
            //预览的回调函数在开始预览的时候以中断方式被调用，每秒调用15次，回调函数在预览的同时调出正在播放的帧
//            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    if(data != null){
////                        onFrameCallback.onCameraFrame(data);
//                    }
//
//                }
//            });

        }
    }

    public void startPreview() {
        //开始预览
        mCamera.startPreview();
    }


    /**
     * 销毁
     */
    public void destroy() {
        if (mCamera != null) {
            //停止回调函数
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放资源
            mCamera.release();
            //重新初始化
            mCamera = null;
        }
    }

    public interface OnFrameCallback {
        void onCameraFrame(byte[] data);
    }

    /**
     * 拍照
     */
    public void doTakePicture() {

//		mCamera.takePicture((Camera.ShutterCallback) mShutterCallback, null, null);

        if (mCamera == null){
            Log.i(TAG, "相机为空");
            //调用正在使用的摄像头拍照 . 正在使用的摄像头是推流的摄像头
            if(VideoStream.mCamera !=null){
                VideoStream.mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        Log.i(TAG, "myShutterCallback:onShutter...");
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {

                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "myJpegCallback:onPictureTaken...");
                        Bitmap b = null;
                        if(null != data){
                            b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
//                            mCamera.stopPreview();
//                            mPreviewStarted = false;
                        }
                        //保存图片到sdcard
                        if(null != b)
                        {
                            //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                            //图片竟然不能旋转了，故这里要旋转下
                            Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
                            FileUtil.saveBitmap(rotaBitmap);
                        }

                        //再次进入预览
                        VideoStream.mCamera.startPreview();
//                        mPreviewStarted = true;
                    }

                });

            }



            return ;
        }

		mCamera.takePicture(new Camera.ShutterCallback() {
			@Override
			public void onShutter() {
				Log.i(TAG, "myShutterCallback:onShutter...");
			}
		}, new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

			}
		}, new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				Log.i(TAG, "myJpegCallback:onPictureTaken...");
				Bitmap b = null;
				if(null != data){
					b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
					mCamera.stopPreview();
					mPreviewStarted = false;
				}
				//保存图片到sdcard
				if(null != b)
				{
					//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
					//图片竟然不能旋转了，故这里要旋转下
					Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
					FileUtil.saveBitmap(rotaBitmap);
				}

				//再次进入预览
				mCamera.startPreview();
				mPreviewStarted = true;
			}

		});

//        mCamera = Camera.open(0);




//        if (mCamera == null) {
//            openCamera();
//        }
//        if (mCamera != null) {
//            mCamera.takePicture(new Camera.ShutterCallback() {//按下快门
//                @Override
//                public void onShutter() {
//                    //按下快门瞬间的操作
//                }
//            }, new Camera.PictureCallback() {
//                @Override
//                public void onPictureTaken(byte[] data, Camera camera) {//是否保存原始图片的信息
//
//                }
//            }, mJpegPictureCallback);
//
//            Log.i(TAG, "拍照成功");
//        }


//		if(mPreviewStarted && (mCamera != null)){
//			mCamera.takePicture(new Camera.ShutterCallback() {
//				@Override
//				public void onShutter() {
//					Log.i(TAG, "myShutterCallback:onShutter...");
//				}
//			}, null, new Camera.PictureCallback() {
//				@Override
//				public void onPictureTaken(byte[] data, Camera camera) {
//					// TODO Auto-generated method stub
//					Log.i(TAG, "myJpegCallback:onPictureTaken...");
//					Bitmap b = null;
//					if(null != data){
//						b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
//						mCamera.stopPreview();
//						mPreviewStarted = false;
//					}
//					//保存图片到sdcard
//					if(null != b)
//					{
//						//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
//						//图片竟然不能旋转了，故这里要旋转下
//						Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
//						FileUtil.saveBitmap(rotaBitmap);
//					}
//					//再次进入预览
//					mCamera.startPreview();
//					mPreviewStarted = true;
//				}
//
//			});
//		}
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
//    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
//            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
//    {
//        public void onShutter() {
//            // TODO Auto-generated method stub
//            Log.i(TAG, "myShutterCallback:onShutter...");
//        }
//    };
//    CameraSource.PictureCallback mRawCallback = new CameraSource.PictureCallback()
//            // 拍摄的未压缩原数据的回调,可以为null
//    {
//
//        @Override
//        public void onPictureTaken(byte[] bytes) {
//
//        }
//
//    };
}
