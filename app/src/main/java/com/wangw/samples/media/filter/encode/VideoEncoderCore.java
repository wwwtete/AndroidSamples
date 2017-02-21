/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wangw.samples.media.filter.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class wraps up the core components used for surface-input video encoding.
 * <p>
 * Once created, frames are fed to the input surface.  Remember to provide the presentation
 * time stamp, and always call drainEncoder() before swapBuffers() to ensure that the
 * producer side doesn't get backed up.
 * <p>
 * This class is not thread-safe, with one exception: it is valid to use the input surface
 * on one thread, and drain the output on a different thread.
 */
public class VideoEncoderCore {
    private static final String TAG = "zl";
    private static final boolean VERBOSE = true;

    // TODO: these ought to be configurable as well
    private static final String VIDEO_MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int VIDEO_FRAME_RATE = 30;                // 30fps
    private static final int VIDEO_IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    private static final int VIDEO_BIT_RATE = 2000000; // 2Mbps
    private int frameRate;

    private Surface mInputSurface;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    /*private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;*/

    private MediaMuxerWrapper mMediaMuxerWrapper;


    private OnEncodingCompleteCallback mOnEncodingCompleteCallback;
    public interface OnEncodingCompleteCallback{
        void onEncodingComplete();
    }

    public void setOnEncodingCompleteCallback(OnEncodingCompleteCallback callback){
        mOnEncodingCompleteCallback = callback;
    }


    public VideoEncoderCore(String inputFilePath, MediaMuxerWrapper muxerWrapper) throws IOException {

        mVideoBufferInfo = new MediaCodec.BufferInfo();
        mMediaMuxerWrapper = muxerWrapper;

        MediaExtractor mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(inputFilePath);
        int videoInputTrack = getAndSelectVideoTrackIndex(mMediaExtractor);
        MediaFormat videoInputFormat = mMediaExtractor.getTrackFormat(videoInputTrack);
        if (VERBOSE) Log.d(TAG, "video inputFormat: " + videoInputFormat);

        String videoMimeType = videoInputFormat.getString(MediaFormat.KEY_MIME);
        int width = videoInputFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = videoInputFormat.getInteger(MediaFormat.KEY_HEIGHT);
//      int bitRate = inputFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        frameRate = videoInputFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
//      int iFrameInterval = inputFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
        if (VERBOSE) {
            Log.d(TAG, "Video size is " + width + "x" + height + ",frameRate: " + frameRate + ",videoMimeType: " + videoMimeType);
        }

        MediaFormat outputVideoFormat = MediaFormat.createVideoFormat(videoMimeType, width, height);
        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE);
        outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        if (VERBOSE) Log.d(TAG, "outputVideoFormat: " + outputVideoFormat);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        mVideoEncoder = MediaCodec.createEncoderByType(videoMimeType);
        mVideoEncoder.configure(outputVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    public VideoEncoderCore(int width, int height, int bitRate, MediaMuxerWrapper muxerWrapper)
            throws IOException {

        mVideoBufferInfo = new MediaCodec.BufferInfo();

        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        mMediaMuxerWrapper = muxerWrapper;

        MediaFormat videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        if (VERBOSE) Log.d(TAG, "format: " + videoFormat);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();
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

    private static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    /**
     * Releases encoder resources.
     */
    public void release() {
        if (VERBOSE) Log.d(TAG, "releasing mVideoEncoder objects");
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
//        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to VideoPlayEncoderTask");
            mVideoEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMediaMuxerWrapper.isMuxerStart()) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                Log.e(TAG, "video encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mMediaMuxerWrapper.addTrackIndex(MediaMuxerWrapper.TRACK_VIDEO, newFormat);
                if (!mMediaMuxerWrapper.startMuxer()) {
                    // we should wait until muxer is ready
                    synchronized (mMediaMuxerWrapper.getLock()) {
                        while (!mMediaMuxerWrapper.isMuxerStart())
                            try {
                                mMediaMuxerWrapper.getLock().wait(100);
                            } catch (final InterruptedException e) {
                                break;
                            }
                    }
                }
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mVideoBufferInfo.size = 0;
                }

                if (mVideoBufferInfo.size != 0) {
                    if (!mMediaMuxerWrapper.isMuxerStart()) {
                        // muxer is not ready...this will prrograming failure.
                        throw new RuntimeException("VideoPlayEncoderTask hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mVideoBufferInfo.offset);
                    encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
//                    mMuxer.writeSampleData(mTrackIndex, encodedData, mVideoBufferInfo);
                    mMediaMuxerWrapper.writeSampleData(MediaMuxerWrapper.TRACK_VIDEO, encodedData, mVideoBufferInfo);
                    /*if (VERBOSE) {
                        Log.d(TAG, "sent " + mVideoBufferInfo.size + " bytes to muxer, ts=" + mVideoBufferInfo.presentationTimeUs);
                    }*/
                }

                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached video");
                    }
                    if(mOnEncodingCompleteCallback != null){
                        mOnEncodingCompleteCallback.onEncodingComplete();
                    }

                    break;      // out of while
                }
            }
        }
    }
}
