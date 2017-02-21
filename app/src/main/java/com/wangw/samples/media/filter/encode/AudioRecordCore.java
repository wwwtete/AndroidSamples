package com.wangw.samples.media.filter.encode;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaAudioEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioRecordCore {
    private static final String TAG = "zl";
    private static final boolean VERBOSE = true;    // TODO set false on release

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private static final int SAMPLE_RATE = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 64 * 1024;
    public static final int SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 30;    // AAC, frame/buffer/sec
    private static final int CHANNEL_COUNT = 1; // Must match the input stream.

    protected boolean mIsEOS;
    private MediaCodec mAudioEncoder;

    private AudioThread mAudioThread = null;

    private MediaCodec.BufferInfo mAudioBufferInfo;
    private MediaMuxerWrapper mMediaMuxerWrapper;

    public AudioRecordCore(MediaMuxerWrapper muxerWrapper) throws IOException {
        mMediaMuxerWrapper = muxerWrapper;
        mAudioBufferInfo = new MediaCodec.BufferInfo();

        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, AAC_PROFILE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        if (VERBOSE) Log.i(TAG, "audioFormat: " + audioFormat);
        mAudioEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();
    }

    public void startEncoding() {
        // create and execute audio capturing thread using internal mic
        if (mAudioThread == null) {
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }
    }

    public void release() {
        mAudioThread = null;
        if (mAudioEncoder != null) {
            if (VERBOSE) Log.d(TAG, "releasing mAudioEncoder objects");
            try {
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mAudioEncoder = null;
            } catch (Exception e) {
                Log.e(TAG, "failed releasing mAudioEncoder", e);
            }
        }
        /*if (mMediaMuxerWrapper.isMuxerStart()) {
            mMediaMuxerWrapper.stopMuxer();
        }*/
    }


    protected void signalEndOfInputStream() {
        if (VERBOSE) Log.d(TAG, "sending EOS to audio AudioEncoder");
        offerAudioEncoder(null, 0, getPTSUs());
//        drainEncoder();
    }

    private static final int[] AUDIO_SOURCES = new int[]{
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private class AudioThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                final int min_buffer_size = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
                if (buffer_size < min_buffer_size) {
                    buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
                }
                AudioRecord audioRecord = null;
                for (int source : AUDIO_SOURCES) {
                    try {
                        Log.i(TAG,"AudioThread buffer_size: " + buffer_size);
                        audioRecord = new AudioRecord(source, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                            audioRecord = null;
                        }
                    } catch (Exception e) {
                        audioRecord = null;
                    }
                    if (audioRecord != null) {
                        break;
                    }
                }
                Log.v(TAG, "audioRecord: " + audioRecord);
                if (audioRecord != null) {
                    try {
                        if (VERBOSE) Log.v(TAG, "AudioThread:start audio recording");
                        ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                        int readBytes;
                        audioRecord.startRecording();
                        try {
                            while (!mIsEOS) {
                                // read audio data from internal mic
                                buf.clear();
                                readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                                if (readBytes > 0) {
                                    // set audio data to encoder
                                    buf.position(readBytes);
                                    buf.flip();
                                    drainEncoder();
                                    offerAudioEncoder(buf, readBytes, getPTSUs());
                                }
                            }
                        } finally {
                            audioRecord.stop();
                            Log.v(TAG, "audioRecord.stop()");
                        }
                    } finally {
                        audioRecord.release();
                        Log.v(TAG, "audioRecord.release()");
                    }
                } else {
                    Log.e(TAG, "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Log.e(TAG, "AudioThread#run", e);
            }
            if (VERBOSE) Log.v(TAG, "AudioThread:finished");
        }
    }


    /**
     * Method to set byte array to the MediaCodec encoder
     *
     * @param buffer
     * @param length             　length of byte array, zero means EOS.
     * @param presentationTimeUs
     */
    protected void offerAudioEncoder(ByteBuffer buffer, int length, long presentationTimeUs) {
        final int TIMEOUT_USEC = 10000;
        final ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    // send EOS
                    mIsEOS = true;
                    if (VERBOSE) Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                    mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, 0);
                }
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
                // no output available yet
                if (mIsEOS) {
                    break;      // out of while
                } else {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
            }
        }
    }

    private void drainEncoder() {
         /*获取解码后的数据*/
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!mIsEOS) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMediaMuxerWrapper.isMuxerStart()) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mAudioEncoder.getOutputFormat();
                Log.d(TAG, "audio encoder output format changed: " + newFormat);
                // now that we have the Magic Goodies, start the muxer
                mMediaMuxerWrapper.addTrackIndex(MediaMuxerWrapper.TRACK_AUDIO, newFormat);
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
                Log.w(TAG, "audio unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mAudioBufferInfo.size = 0;
                }

                if (mAudioBufferInfo.size != 0) {
                    if (!mMediaMuxerWrapper.isMuxerStart()) {
                        // muxer is not ready...this will prrograming failure.
                        throw new RuntimeException("AudioEncoder hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mAudioBufferInfo.offset);
                    encodedData.limit(mAudioBufferInfo.offset + mAudioBufferInfo.size);
                    mMediaMuxerWrapper.writeSampleData(MediaMuxerWrapper.TRACK_AUDIO, encodedData, mAudioBufferInfo);
                    prevOutputPTSUs = mAudioBufferInfo.presentationTimeUs;
                }
                mAudioEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!mIsEOS) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached audio");
                    }
                    break;      // out of while
                }

            }
        }
    }


    /**
     * previous presentationTimeUs for writing
     */
    private long prevOutputPTSUs = 0;

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs) {
            result = (prevOutputPTSUs - result) + result;
        }
        return result;
    }

}
