package com.wangw.samples.media.filter.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.wangw.samples.media.filter.filter.FilterManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by xingliao_zgl on 16/8/17.
 */
public class MediaCodecHelper implements IMediaCodec {

    private static final String TAG = "zl";
    private static final boolean VERBOSE = false; // lots of logging

    /**
     * How long to wait for the next buffer to become available.
     */
    private static final int TIMEOUT_USEC = 10000;

    // parameters for the video encoder
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 2000000; // 2Mbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 25; // 15fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 5; // 5 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT =
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    // parameters for the audio encoder
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
    private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
    private static final int OUTPUT_AUDIO_BIT_RATE = 64 * 1024;
    private static final int OUTPUT_AUDIO_AAC_PROFILE =
            MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.

    /**
     * Width of the output frames.
     */
    private int mWidth = 720;
    /**
     * Height of the output frames.
     */
    private int mHeight = 1280;

    /**
     * Whether to copy the video from the test video.
     */
    private boolean mCopyVideo = true;
    /**
     * Whether to copy the audio from the test video.
     */
    private boolean mCopyAudio = true;


    private MediaExtractor mVideoExtractor = null;
    private MediaExtractor mAudioExtractor = null;
    private WindowSurface mInputSurface = null;
    private OutputSurface mOutputSurface = null;
    private MediaCodec mVideoDecoder = null;
    private MediaCodec mAudioDecoder = null;
    private MediaCodec mVideoEncoder = null;
    private MediaCodec mAudioEncoder = null;
    private MediaMuxer mMuxer = null;


    private static final int THREAD_POOL_SIZE = 5;
    private ExecutorService mFixedThreadPool;
    private OnVideoProcessCallback mOnVideoProcessCallback;


    public MediaCodecHelper() {
        init(true, true);
    }

    public MediaCodecHelper(boolean copyVideo, boolean copyAudio) {
        init(copyVideo, copyAudio);
    }

    private void init(boolean copyVideo, boolean copyAudio) {
        this.mCopyVideo = copyVideo;
        this.mCopyAudio = copyAudio;
        mFixedThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    public void setOnVideoProcessCallback(OnVideoProcessCallback callback) {
        mOnVideoProcessCallback = callback;
    }

    @Override
    public void startProcessVideo(final Context context, final CodecConfig config) {
        mFixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                processVideo(context, config.inputVideoFilePath, config.inputAudioFilePath, config.filterType, config.outputFilePath);
            }
        });
    }
    @Override
    public void cancelProcessVideo(){
        mVideoExtractorDone = false;
        mVideoDecoderDone = false;
        mVideoEncoderDone = false;
        mAudioExtractorDone = false;
        mAudioDecoderDone = false;
        mAudioEncoderDone = false;
    }

    /**
     * Tests encoding and subsequently decoding video from frames generated into a buffer.
     * <p>
     * We encode several frames of a video test pattern using MediaCodec, then decode the output
     * with MediaCodec and do some simple checks.
     */
    private void processVideo(Context context, String inputVideoFilePath, String inputAudioFilePath, FilterManager.FilterType filterType, String outputFilePath) {
        // Exception that may be thrown during release.
        Exception exception = null;

        mDecoderOutputVideoFormat = null;
        mDecoderOutputAudioFormat = null;
        mEncoderOutputVideoFormat = null;
        mEncoderOutputAudioFormat = null;

        mOutputVideoTrack = -1;
        mOutputAudioTrack = -1;
        mVideoExtractorDone = false;
        mVideoDecoderDone = false;
        mVideoEncoderDone = false;
        mAudioExtractorDone = false;
        mAudioDecoderDone = false;
        mAudioEncoderDone = false;
        mPendingAudioDecoderOutputBufferIndices = new LinkedList<>();
        mPendingAudioDecoderOutputBufferInfos = new LinkedList<>();
        mPendingAudioEncoderInputBufferIndices = new LinkedList<>();
        mPendingVideoEncoderOutputBufferIndices = new LinkedList<>();
        mPendingVideoEncoderOutputBufferInfos = new LinkedList<>();
        mPendingAudioEncoderOutputBufferIndices = new LinkedList<>();
        mPendingAudioEncoderOutputBufferInfos = new LinkedList<>();
        mMuxing = false;
        mVideoExtractedFrameCount = 0;
        mVideoDecodedFrameCount = 0;
        mVideoEncodedFrameCount = 0;
        mAudioExtractedFrameCount = 0;
        mAudioDecodedFrameCount = 0;
        mAudioEncodedFrameCount = 0;

        MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
        if (videoCodecInfo == null) {
            // Don't fail CTS if they don't have an AVC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_VIDEO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "video found codec: " + videoCodecInfo.getName());

        MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
        if (audioCodecInfo == null) {
            // Don't fail CTS if they don't have an AAC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_AUDIO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "audio found codec: " + audioCodecInfo.getName());

        try {
            // Creates a muxer but do not start or add tracks just yet.
            mMuxer = createMuxer(outputFilePath);

            //process video format start
            if (mCopyVideo) {
                mVideoExtractor = createExtractor(inputVideoFilePath);
                int videoInputTrack = getAndSelectVideoTrackIndex(mVideoExtractor);
//            assertTrue("missing video track in test video", videoInputTrack != -1);
                MediaFormat videoInputFormat = mVideoExtractor.getTrackFormat(videoInputTrack);
                if (VERBOSE) Log.d(TAG, "video inputFormat: " + videoInputFormat);

                String videoMimeType = videoInputFormat.getString(MediaFormat.KEY_MIME);
                int width = videoInputFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = videoInputFormat.getInteger(MediaFormat.KEY_HEIGHT);
//                int bitRate = inputFormat.getInteger(MediaFormat.KEY_BIT_RATE);
//                int frameRate = videoInputFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
//                int iFrameInterval = inputFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
                if (VERBOSE) {
                    Log.d(TAG, "Video size is " + width + "x" + height + "videoMimeType: " + videoMimeType);
                }
                // We avoid the device-specific limitations on width and height by using values
                // that are multiples of 16, which all tested devices seem to be able to handle.
                MediaFormat outputVideoFormat = MediaFormat.createVideoFormat(videoMimeType, mWidth, mHeight);

                // Set some properties. Failing to specify some of these can cause the MediaCodec
                // configure() call to throw an unhelpful exception.
                outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT);
                outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
                outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
                outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);
                if (VERBOSE) Log.d(TAG, "video format: " + outputVideoFormat);

                // Create a MediaCodec for the desired codec, then configure it as an encoder with
                // our desired properties. Request a Surface to use for input.
                AtomicReference<Surface> inputSurfaceReference = new AtomicReference<>();
                mVideoEncoder = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
                EglCore mEglCore = new EglCore(EGL14.eglGetCurrentContext(), EglCore.FLAG_RECORDABLE);
                mInputSurface = new WindowSurface(mEglCore, inputSurfaceReference.get(), true);
                mInputSurface.makeCurrent();


                // Create a MediaCodec for the decoder, based on the extractor's format.
                mOutputSurface = new OutputSurface(context, filterType);
//                mOutputSurface.changeFragmentShader(fragmentShader);
                mVideoDecoder = createVideoDecoder(videoInputFormat, mOutputSurface.getSurface());
                mInputSurface.releaseEglContext();

                processVideoDecoderData(mVideoDecoder);
                processVideoEncoderData(mVideoEncoder);
            }
            //process video format end

            //process audio format start
            if (mCopyAudio) {
                mAudioExtractor = createExtractor(inputAudioFilePath);
                int audioInputTrack = getAndSelectAudioTrackIndex(mAudioExtractor);
                MediaFormat audioInputFormat = mAudioExtractor.getTrackFormat(audioInputTrack);
                if (VERBOSE) Log.d(TAG, "audio inputFormat: " + audioInputFormat);

                String audioMimeType = audioInputFormat.getString(MediaFormat.KEY_MIME);
                int sampleRate = audioInputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = audioInputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                if (VERBOSE) {
                    Log.d(TAG, "audio info,sampleRate: " + sampleRate + ",channelCount: " + channelCount + ",audioMimeType: " + audioMimeType);
                }
                MediaFormat outputAudioFormat = MediaFormat.createAudioFormat(audioMimeType, sampleRate, channelCount);
                outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_AUDIO_BIT_RATE);
                outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, OUTPUT_AUDIO_AAC_PROFILE);

                // Create a MediaCodec for the desired codec, then configure it as an encoder with
                // our desired properties. Request a Surface to use for input.
                mAudioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
                // Create a MediaCodec for the decoder, based on the extractor's format.
                mAudioDecoder = createAudioDecoder(audioInputFormat);

                processAudioDecoderData(mAudioDecoder);
                processAudioEncoderData(mAudioEncoder);
            }
            //process audio format end

            awaitEncode();

        } catch (IOException e) {
            e.printStackTrace();
            if (mOnVideoProcessCallback != null) {
                mOnVideoProcessCallback.onVideoProcessFailed(e != null ? e.getMessage() : "");
            }

        } finally {
            Log.d(TAG, "releasing extractor, decoder, encoder, and muxer");
            try {
                if (mVideoExtractor != null) {
                    mVideoExtractor.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoExtractor", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioExtractor != null) {
                    mAudioExtractor.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioExtractor", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mVideoDecoder != null) {
                    mVideoDecoder.stop();
                    mVideoDecoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoDecoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mOutputSurface != null) {
                    mOutputSurface.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing outputSurface", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mVideoEncoder != null) {
                    mVideoEncoder.stop();
                    mVideoEncoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing videoEncoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioDecoder != null) {
                    mAudioDecoder.stop();
                    mAudioDecoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioDecoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mAudioEncoder != null) {
                    mAudioEncoder.stop();
                    mAudioEncoder.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing audioEncoder", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mMuxer != null) {
                    mMuxer.stop();
                    mMuxer.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing muxer", e);
                if (exception == null) {
                    exception = e;
                }
            }
            try {
                if (mInputSurface != null) {
                    mInputSurface.release();
                }
            } catch (Exception e) {
                Log.e(TAG, "error while releasing inputSurface", e);
                if (exception == null) {
                    exception = e;
                }
            }
            if (mPendingAudioDecoderOutputBufferIndices != null) {
                mPendingAudioDecoderOutputBufferIndices.clear();
                mPendingAudioDecoderOutputBufferIndices = null;
            }
            if (mPendingAudioDecoderOutputBufferInfos != null) {
                mPendingAudioDecoderOutputBufferInfos.clear();
                mPendingAudioDecoderOutputBufferInfos = null;
            }
            if (mPendingAudioEncoderInputBufferIndices != null) {
                mPendingAudioEncoderInputBufferIndices.clear();
                mPendingAudioEncoderInputBufferIndices = null;
            }
            if (mPendingVideoEncoderOutputBufferIndices != null) {
                mPendingVideoEncoderOutputBufferIndices.clear();
                mPendingVideoEncoderOutputBufferIndices = null;
            }

            if (mPendingVideoEncoderOutputBufferInfos != null) {
                mPendingVideoEncoderOutputBufferInfos.clear();
                mPendingVideoEncoderOutputBufferInfos = null;
            }
            if (mPendingAudioEncoderOutputBufferIndices != null) {
                mPendingAudioEncoderOutputBufferIndices.clear();
                mPendingAudioEncoderOutputBufferIndices = null;
            }
            if (mPendingAudioEncoderOutputBufferInfos != null) {
                mPendingAudioEncoderOutputBufferInfos.clear();
                mPendingAudioEncoderOutputBufferInfos = null;
            }

            mVideoExtractor = null;
            mAudioExtractor = null;
            mOutputSurface = null;
            mInputSurface = null;
            mVideoDecoder = null;
            mAudioDecoder = null;
            mVideoEncoder = null;
            mAudioEncoder = null;
            mMuxer = null;
            if (mVideoDecoderHandlerThread != null) {
                mVideoDecoderHandlerThread.quitSafely();
            }
            mVideoDecoderHandlerThread = null;

            if (mOnVideoProcessCallback != null) {
                if (exception != null) {
                    mOnVideoProcessCallback.onVideoProcessFailed(exception.getMessage());
                } else {
                    mOnVideoProcessCallback.onVideoProcessSuccess();
                }
                mOnVideoProcessCallback.onVideoProcessComplete();
            }
        }
    }

    /**
     * Creates an extractor that reads its frames from sdcard file
     *
     * @param inputFile
     * @return
     * @throws IOException
     */
    private MediaExtractor createExtractor(String inputFile) throws IOException {
        MediaExtractor extractor;
        extractor = new MediaExtractor();
        extractor.setDataSource(inputFile);
        return extractor;
    }


    private HandlerThread mVideoDecoderHandlerThread;
    private CallbackHandler mVideoDecoderHandler;

    static class CallbackHandler extends Handler {
        CallbackHandler(Looper l) {
            super(l);
        }

        private MediaCodec mCodec;
        private boolean mEncoder;
        private String mMime;
        private boolean mSetDone;

        @Override
        public void handleMessage(Message msg) {
            try {
                mCodec = mEncoder ? MediaCodec.createEncoderByType(mMime) : MediaCodec.createDecoderByType(mMime);
            } catch (IOException ioe) {
            }
            synchronized (this) {
                mSetDone = true;
                notifyAll();
            }
        }

        void create(boolean encoder, String mime) {
            mEncoder = encoder;
            mMime = mime;
            mSetDone = false;
            sendEmptyMessage(0);
            synchronized (this) {
                while (!mSetDone) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        MediaCodec getCodec() {
            return mCodec;
        }
    }


    /**
     * Creates a decoder for the given format, which outputs to the given surface.
     *
     * @param inputFormat the format of the stream to decode
     * @param surface     into which to decode the frames
     */
    private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
        // Create the decoder on a different thread, in order to have the callbacks there.
        // This makes sure that the blocking waiting and rendering in onOutputBufferAvailable
        // won't block other callbacks (e.g. blocking encoder output callbacks), which
        // would otherwise lead to the transcoding pipeline to lock up.

        // Since API 23, we could just do setCallback(callback, mVideoDecoderHandler) instead
        // of using a custom Handler and passing a message to create the MediaCodec there.

        // When the callbacks are received on a different thread, the updating of the variables
        // that are used for state logging (mVideoExtractedFrameCount, mVideoDecodedFrameCount,
        // mVideoExtractorDone and mVideoDecoderDone) should ideally be synchronized properly
        // against accesses from other threads, but that is left out for brevity since it's
        // not essential to the actual transcoding.
//        mVideoDecoderHandler.create(false, getMimeTypeFor(inputFormat), callback);
//        MediaCodec decoder = mVideoDecoderHandler.getCodec();

        mVideoDecoderHandlerThread = new HandlerThread("DecoderThread");
        mVideoDecoderHandlerThread.start();
        mVideoDecoderHandler = new CallbackHandler(mVideoDecoderHandlerThread.getLooper());
        mVideoDecoderHandler.create(false, getMimeTypeFor(inputFormat));
        MediaCodec decoder = mVideoDecoderHandler.getCodec();
//        final MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.configure(inputFormat, surface, null, 0);
        decoder.start();
        return decoder;
    }

    private void processVideoDecoderData(final MediaCodec decoder) {
        mFixedThreadPool.execute(new Runnable() {
                                     @Override
                                     public void run() {
                                         MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                         ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
                                         ByteBuffer[] decoderOutputBuffers = decoder.getOutputBuffers();
                                         while (!mVideoDecoderDone) {
                                             int decoderInStatus = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                                             if (decoderInStatus >= 0) {
                                                 Loop:
                                                 while (!mVideoExtractorDone) {
                                                     ByteBuffer buffer = decoderInputBuffers[decoderInStatus];
                                                     int size = mVideoExtractor.readSampleData(buffer, 0);
                                                     long presentationTime = mVideoExtractor.getSampleTime();
                                                     int sampleFlags = mVideoExtractor.getSampleFlags();
                                                     if (VERBOSE) {
                                                         Log.d(TAG, "video extractor: returned buffer of size " + size);
                                                         Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
                                                         Log.d(TAG, "video extractor: returned buffer for sampleFlags " + sampleFlags);
                                                     }
                                                     if (size >= 0) {
                                                         decoder.queueInputBuffer(
                                                                 decoderInStatus,
                                                                 0,
                                                                 size,
                                                                 presentationTime,
                                                                 sampleFlags);
                                                     }
                                                     mVideoExtractorDone = !mVideoExtractor.advance();
                                                     if (mVideoExtractorDone) {
                                                         Log.d(TAG, "video extractor: EOS");
                                                         decoder.queueInputBuffer(
                                                                 decoderInStatus,
                                                                 0,
                                                                 0,
                                                                 0,
                                                                 MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                                     }
                                                     mVideoExtractedFrameCount++;
                                                     logState();
                                                     if (size >= 0) {
                                                         break Loop;
                                                     }
                                                 }
                                             }

                                             int decoderOutStatus = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                                             if (decoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                                 // no output available yet
                                                 if (mVideoDecoderDone) {
                                                     break;      // out of while
                                                 } else {
                                                     if (VERBOSE)
                                                         Log.d(TAG, "video decoder: no output available, spinning to await EOS");
                                                 }
                                             } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                                 mDecoderOutputVideoFormat = decoder.getOutputFormat();
                                                 if (VERBOSE) {
                                                     Log.d(TAG, "video decoder: output format changed: " + mDecoderOutputVideoFormat);
                                                 }
                                             } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                                 decoderOutputBuffers = decoder.getOutputBuffers();
                                             } else if (decoderOutStatus < 0) {
                                                 Log.w(TAG, "video decoder: unexpected result from encoder.dequeueOutputBuffer: " + decoderOutStatus);
                                                 // let's ignore it
                                             } else {
                                                 if (VERBOSE) {
                                                     Log.d(TAG, "video decoder: returned output buffer: " + decoderOutStatus);
                                                     Log.d(TAG, "video decoder: returned buffer of size " + bufferInfo.size);
                                                 }
                                                 if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                                     if (VERBOSE) Log.d(TAG, "video decoder: codec config buffer");
                                                     decoder.releaseOutputBuffer(decoderOutStatus, false);
                                                 }
                                                 if (VERBOSE) {
                                                     Log.d(TAG, "video decoder: returned buffer for time " + bufferInfo.presentationTimeUs);
                                                 }
                                                 boolean render = bufferInfo.size != 0;
                                                 decoder.releaseOutputBuffer(decoderOutStatus, render);
                                                 if (render) {
                                                     mInputSurface.makeCurrent();
                                                     if (VERBOSE) Log.d(TAG, "output surface: await new image");
                                                     mOutputSurface.awaitNewImage();
                                                     // Edit the frame and send it to the encoder.
                                                     if (VERBOSE) Log.d(TAG, "output surface: draw image");
                                                     mOutputSurface.drawImage();
                                                     mInputSurface.setPresentationTime(
                                                             bufferInfo.presentationTimeUs * 1000);
                                                     if (VERBOSE) Log.d(TAG, "input surface: swap buffers");
                                                     mInputSurface.swapBuffers();
                                                     if (VERBOSE) Log.d(TAG, "video encoder: notified of new frame");
                                                     mInputSurface.releaseEglContext();
                                                 }
                                                 if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                                     Log.d(TAG, "video decoder: EOS");
                                                     mVideoDecoderDone = true;
                                                     mVideoEncoder.signalEndOfInputStream();
                                                 }
                                                 mVideoDecodedFrameCount++;
                                                 if (VERBOSE)
                                                     Log.d(TAG, "video decoder: mVideoDecodedFrameCount: " + mVideoDecodedFrameCount);
                                                 logState();
                                             }
                                         }
                                     }
                                 }

        );
    }

    /**
     * Creates an encoder for the given format using the specified codec, taking input from a
     * surface.
     * <p>
     * <p>The surface to use as input is stored in the given reference.
     *
     * @param codecInfo        of the codec to use
     * @param format           of the stream to be produced
     * @param surfaceReference to store the surface to use as input
     */
    private MediaCodec createVideoEncoder(
            MediaCodecInfo codecInfo,
            MediaFormat format,
            AtomicReference<Surface> surfaceReference) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // Must be called before start() is.
        surfaceReference.set(encoder.createInputSurface());
        encoder.start();
        return encoder;
    }

    private void processVideoEncoderData(final MediaCodec encoder) {
        mFixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
                ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
                while (!mVideoEncoderDone) {
                    //err: MediaCodec: dequeueInputBuffer can't be used with input surface
                    /*int encodeInStatus = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (encodeInStatus >= 0) {
                        Log.d(TAG, "video encoder: returned input buffer: " + encodeInStatus);
                    }*/
                    int encoderOutStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (encoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (mVideoEncoderDone) {
                            break;      // out of while
                        } else {
                            if (VERBOSE)
                                Log.d(TAG, "video encoder: no output available, spinning to await EOS");
                        }
                    } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (VERBOSE) Log.d(TAG, "video encoder: output format changed");
                        if (mOutputVideoTrack >= 0) {
                            Log.e(TAG, "video encoder: changed its output format again?");
                        }
                        mEncoderOutputVideoFormat = encoder.getOutputFormat();
                        setupMuxer();
                    } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not expected for an encoder
                        encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
                    } else if (encoderOutStatus < 0) {
                        Log.w(TAG, "video encoder: unexpected result from encoder.dequeueOutputBuffer: " + encoderOutStatus);
                        // let's ignore itm
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "video encoder: returned output buffer: " + encoderOutStatus);
                            Log.d(TAG, "video encoder: returned buffer of size " + bufferInfo.size);
                        }
                        muxVideo(encoderOutStatus, bufferInfo);
                    }
                }
            }
        });
    }


    /**
     * Creates a decoder for the given format.
     *
     * @param inputFormat the format of the stream to decode
     */
    private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
        MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        return decoder;
    }

    private void processAudioDecoderData(final MediaCodec decoder) {
        mFixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
                ByteBuffer[] decoderOutputBuffers = decoder.getOutputBuffers();
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                while (!mAudioDecoderDone) {
                    int decoderInStatus = -1;
                    try {
                        //等待时mAudioDecoderDone有可能被置为true,decoder相关信息被释放导致异常(直接break即可)
                        decoderInStatus = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (VERBOSE) {
                            Log.d(TAG, "audio dequeueInputBuffer error: " + e);
                        }
                        break;
                    }
                    if (decoderInStatus >= 0) {
                        while (!mAudioExtractorDone) {
                            ByteBuffer buffer = decoderInputBuffers[decoderInStatus];
                            int size = mAudioExtractor.readSampleData(buffer, 0);
                            long presentationTime = mAudioExtractor.getSampleTime();
                            int sampleFlags = mAudioExtractor.getSampleFlags();
                            if (VERBOSE) {
                                Log.d(TAG, "audio extractor: returned buffer of size " + size);
                                Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                                Log.d(TAG, "audio extractor: returned buffer for sampleFlags " + sampleFlags);
                            }
                            if (size >= 0) {
                                decoder.queueInputBuffer(
                                        decoderInStatus,
                                        0,
                                        size,
                                        presentationTime,
                                        sampleFlags);
                            }
                            mAudioExtractorDone = !mAudioExtractor.advance();
                            if (mAudioExtractorDone) {
                                Log.d(TAG, "audio extractor: EOS");
                                decoder.queueInputBuffer(
                                        decoderInStatus,
                                        0,
                                        0,
                                        0,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            }
                            mAudioExtractedFrameCount++;
                            logState();
                            if (size >= 0) {
                                break;
                            }
                        }
                    }

                    int decoderOutStatus = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (decoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (mAudioDecoderDone) {
                            break;      // out of while
                        } else {
                            if (VERBOSE)
                                Log.d(TAG, "audio decoder: no output available, spinning to await EOS");
                        }
                    } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        mDecoderOutputAudioFormat = decoder.getOutputFormat();
                        if (VERBOSE) {
                            Log.d(TAG, "audio decoder: output format changed: " + mDecoderOutputAudioFormat);
                        }
                    } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        decoderOutputBuffers = decoder.getOutputBuffers();
                    } else if (decoderOutStatus < 0) {
                        Log.w(TAG, "audio decoder: unexpected result from encoder.dequeueOutputBuffer: " + decoderOutStatus);
                        // let's ignore it
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "audio decoder: returned output buffer: " + decoderOutStatus);
                        }
                        if (VERBOSE) {
                            Log.d(TAG, "audio decoder: returned buffer of size " + bufferInfo.size);
                        }
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            if (VERBOSE) Log.d(TAG, "audio decoder: codec config buffer");
                            decoder.releaseOutputBuffer(decoderOutStatus, false);
                        }
                        if (VERBOSE) {
                            Log.d(TAG, "audio decoder: returned buffer for time " + bufferInfo.presentationTimeUs);
                        }
                        mPendingAudioDecoderOutputBufferIndices.add(decoderOutStatus);
                        mPendingAudioDecoderOutputBufferInfos.add(bufferInfo);
                        mAudioDecodedFrameCount++;
                        logState();
                        tryEncodeAudio();
                    }
                }
            }
        });
    }

    /**
     * Creates an encoder for the given format using the specified codec.
     *
     * @param codecInfo of the codec to use
     * @param format    of the stream to be produced
     */
    private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        return encoder;
    }

    private void processAudioEncoderData(final MediaCodec encoder) {
        mFixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer[] encoderInputBuffers = encoder.getInputBuffers();
                ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
                while (!mAudioEncoderDone) {
                    int encodeInStatus = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (encodeInStatus >= 0) {
                        if(VERBOSE) Log.d(TAG, "audio encoder: returned input buffer: " + encodeInStatus);
                        mPendingAudioEncoderInputBufferIndices.add(encodeInStatus);
                        tryEncodeAudio();
                    }

                    int encoderOutStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                    if (encoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (mAudioEncoderDone) {
                            break;      // out of while
                        } else {
                            if (VERBOSE)
                                Log.d(TAG, "audio encoder: no output available, spinning to await EOS");
                        }
                    } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (VERBOSE) Log.d(TAG, "audio encoder: output format changed");
                        if (mOutputAudioTrack >= 0) {
                            Log.e(TAG, "audio encoder: changed its output format again?");
                        }

                        mEncoderOutputAudioFormat = encoder.getOutputFormat();
                        setupMuxer();
                    } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        encoderOutputBuffers = encoder.getOutputBuffers();
                    } else if (encoderOutStatus < 0) {
                        Log.w(TAG, "audio encoder: unexpected result from encoder.dequeueOutputBuffer: " + encoderOutStatus);
                        // let's ignore it
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "audio encoder: returned output buffer: " + encoderOutStatus);
                            Log.d(TAG, "audio encoder: returned buffer of size " + bufferInfo.size);
                        }
                        muxAudio(encoderOutStatus, bufferInfo);
                    }
                }
            }
        });
    }

    // No need to have synchronization around this, since both audio encoder and
    // decoder callbacks are on the same thread.
    private void tryEncodeAudio() {
        if (mPendingAudioEncoderInputBufferIndices.size() == 0 ||
                mPendingAudioDecoderOutputBufferIndices.size() == 0)
            return;
        int decoderOutIndex = mPendingAudioDecoderOutputBufferIndices.poll();
        int encoderInIndex = mPendingAudioEncoderInputBufferIndices.poll();
        MediaCodec.BufferInfo info = mPendingAudioDecoderOutputBufferInfos.poll();

        ByteBuffer[] encoderInputBuffers = mAudioEncoder.getInputBuffers();
        int size = info.size;
        long presentationTime = info.presentationTimeUs;
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: processing pending buffer: "
                    + decoderOutIndex);
        }
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: pending buffer of size " + size);
            Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
        }
        if (size >= 0) {
            ByteBuffer decoderOutputBuffer = mAudioDecoder.getOutputBuffers()[decoderOutIndex].duplicate();
            decoderOutputBuffer.position(info.offset);
            decoderOutputBuffer.limit(info.offset + size);
            encoderInputBuffers[encoderInIndex].position(0);
            encoderInputBuffers[encoderInIndex].put(decoderOutputBuffer);

            mAudioEncoder.queueInputBuffer(
                    encoderInIndex,
                    0,
                    size,
                    presentationTime,
                    info.flags);
        }
        mAudioDecoder.releaseOutputBuffer(decoderOutIndex, false);
        if ((info.flags
                & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "audio decoder: EOS");
            mAudioDecoderDone = true;
        }
        logState();
    }

    private void setupMuxer() {
        if (!mMuxing
                && (!mCopyAudio || mEncoderOutputAudioFormat != null)
                && (!mCopyVideo || mEncoderOutputVideoFormat != null)) {
            if (mCopyVideo) {
                Log.d(TAG, "muxer: adding video track.mEncoderOutputVideoFormat: " + mEncoderOutputVideoFormat);
                mOutputVideoTrack = mMuxer.addTrack(mEncoderOutputVideoFormat);
            }
            if (mCopyAudio) {
                Log.d(TAG, "muxer: adding audio track.mEncoderOutputAudioFormat: " + mEncoderOutputAudioFormat);
                mOutputAudioTrack = mMuxer.addTrack(mEncoderOutputAudioFormat);
            }
            Log.d(TAG, "muxer: starting");
            mMuxer.start();
            mMuxing = true;

            MediaCodec.BufferInfo info;
            while ((info = mPendingVideoEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingVideoEncoderOutputBufferIndices.poll().intValue();
                muxVideo(index, info);
            }
            while ((info = mPendingAudioEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderOutputBufferIndices.poll().intValue();
                muxAudio(index, info);
            }
        }
    }

    private void muxVideo(int index, MediaCodec.BufferInfo info) {

        if((mCopyAudio && !mAudioEncoderDone)){
            Log.i(TAG,"video encoder: waiting auido encoder complete...");
        }
        synchronized (this) {
            while ((mCopyAudio && !mAudioEncoderDone)) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!mMuxing) {
            mPendingVideoEncoderOutputBufferIndices.add(new Integer(index));
            mPendingVideoEncoderOutputBufferInfos.add(info);
            return;
        }
        ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "video encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mVideoEncoder.releaseOutputBuffer(index, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "video encoder: returned buffer for time "
                    + info.presentationTimeUs);
        }
        if (info.size != 0) {
            if(VERBOSE) Log.i(TAG, "video encoder: info.flags " + info.flags);
            mMuxer.writeSampleData(mOutputVideoTrack, encoderOutputBuffers[index], info);
        }
        mVideoEncoder.releaseOutputBuffer(index, false);
        mVideoEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "video encoder: EOS");
            synchronized (this) {
                mVideoEncoderDone = true;
                notifyAll();
            }
        }
        logState();
    }

    private void muxAudio(int index, MediaCodec.BufferInfo info) {
        if (!mMuxing) {
            mPendingAudioEncoderOutputBufferIndices.add(new Integer(index));
            mPendingAudioEncoderOutputBufferInfos.add(info);
            return;
        }
        ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mAudioEncoder.releaseOutputBuffer(index, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "audio encoder: returned buffer for time " + info.presentationTimeUs);
        }
        if (info.size != 0) {
            if(VERBOSE) Log.i(TAG, "audio encoder: info.flags " + info.flags);
            mMuxer.writeSampleData(
                    mOutputAudioTrack, encoderOutputBuffers[index], info);
        }
        mAudioEncoder.releaseOutputBuffer(index, false);
        mAudioEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "audio encoder: EOS");
            synchronized (this) {
                mAudioEncoderDone = true;
                notifyAll();
            }
        }
        logState();
    }

    /**
     * Creates a muxer to write the encoded frames.
     * <p>
     * <p>The muxer is not started as it needs to be started only after all streams have been added.
     */
    private MediaMuxer createMuxer(String outputFilePath) throws IOException {
        /*File mOutputFile = new File(outputFilePath);
        if(mOutputFile.exists()){
            mOutputFile.delete();
        }*/
        return new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    // We will get these from the decoders when notified of a format change.
    private MediaFormat mDecoderOutputVideoFormat = null;
    private MediaFormat mDecoderOutputAudioFormat = null;
    // We will get these from the encoders when notified of a format change.
    private MediaFormat mEncoderOutputVideoFormat = null;
    private MediaFormat mEncoderOutputAudioFormat = null;

    // We will determine these once we have the output format.
    private int mOutputVideoTrack = -1;
    private int mOutputAudioTrack = -1;
    // Whether things are done on the video side.
    private boolean mVideoExtractorDone = false;
    private boolean mVideoDecoderDone = false;
    private boolean mVideoEncoderDone = false;
    // Whether things are done on the audio side.
    private boolean mAudioExtractorDone = false;
    private boolean mAudioDecoderDone = false;
    private boolean mAudioEncoderDone = false;
    private LinkedList<Integer> mPendingAudioDecoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioDecoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderInputBufferIndices;

    private LinkedList<Integer> mPendingVideoEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderOutputBufferInfos;

    private boolean mMuxing = false;

    private int mVideoExtractedFrameCount = 0;
    private int mVideoDecodedFrameCount = 0;
    private int mVideoEncodedFrameCount = 0;

    private int mAudioExtractedFrameCount = 0;
    private int mAudioDecodedFrameCount = 0;
    private int mAudioEncodedFrameCount = 0;

    private void logState() {
        if (VERBOSE) {
            Log.d(TAG, String.format(
                    "loop: "
                            + "video extracted:%d(done:%b) "
                            + "video decoded:%d(done:%b) "
                            + "video encoded:%d(done:%b)} "

                            + "audio extracted:%d(done:%b) "
                            + "audio decoded:%d(done:%b) "
                            + "audio encoded:%d(done:%b) "

                            + "muxing:%b(V:%d,A:%d)",

                    mVideoExtractedFrameCount, mVideoExtractorDone,
                    mVideoDecodedFrameCount, mVideoDecoderDone,
                    mVideoEncodedFrameCount, mVideoEncoderDone,

                    mAudioExtractedFrameCount, mAudioExtractorDone,
                    mAudioDecodedFrameCount, mAudioDecoderDone,
                    mAudioEncodedFrameCount, mAudioEncoderDone,

                    mMuxing, mOutputVideoTrack, mOutputAudioTrack));
        }
    }

    private void awaitEncode() {
        synchronized (this) {
            while ((mCopyVideo && !mVideoEncoderDone) || (mCopyAudio && !mAudioEncoderDone)) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        // Basic sanity checks.
        /*if (mCopyVideo) {
            assertEquals("encoded and decoded video frame counts should match",
                    mVideoDecodedFrameCount, mVideoEncodedFrameCount);
            assertTrue("decoded frame count should be less than extracted frame count",
                    mVideoDecodedFrameCount <= mVideoExtractedFrameCount);
        }
        if (mCopyAudio) {
            assertEquals("no frame should be pending", 0, mPendingAudioDecoderOutputBufferIndices.size());
        }*/

        // TODO: Check the generated output file.
    }

    private static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }

    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
}
