package com.wangw.samples.media.filter.encode;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * 从源视频中copy音频信息并输出
 */
public class AudioCloneCore {
    private static final String TAG = "zl";
    private static final boolean VERBOSE = false;    // TODO set false on release

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private static final int SAMPLE_RATE = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 64 * 1024;
    public static final int SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 30;    // AAC, frame/buffer/sec
    private static final int CHANNEL_COUNT = 1; // Must match the input stream.

    private AudioDecoderThread mAudioDecoderThread;
    private AudioEncoderThread mAudioEncoderThread;

    private MediaExtractor mAudioExtractor;
    private MediaCodec mAudioDecoder, mAudioEncoder;
    private MediaMuxerWrapper mMediaMuxerWrapper;
    private MediaFormat mAudioDecoderFormat, mAudioEncoderFormat;

    private OnEncodingCompleteCallback mOnEncodingCompleteCallback;
    public interface OnEncodingCompleteCallback{
        void onEncodingComplete();
    }

    public void setOnEncodingCompleteCallback(OnEncodingCompleteCallback callback){
        mOnEncodingCompleteCallback = callback;
    }

    public AudioCloneCore(String inputFilePath, MediaMuxerWrapper muxerWrapper) throws IOException {
        mMediaMuxerWrapper = muxerWrapper;

        mAudioExtractor = new MediaExtractor();
        mAudioExtractor.setDataSource(inputFilePath);
        int audioInputTrack = getAndSelectAudioTrackIndex(mAudioExtractor);
        mAudioDecoderFormat = mAudioExtractor.getTrackFormat(audioInputTrack);
        if (VERBOSE) Log.d(TAG, "audio inputFormat: " + mAudioDecoderFormat);

        String audioMimeType = mAudioDecoderFormat.getString(MediaFormat.KEY_MIME);
        mAudioDecoder = MediaCodec.createDecoderByType(audioMimeType);
        mAudioDecoder.configure(mAudioDecoderFormat, null, null, 0);
        mAudioDecoder.start();

        int sampleRate = mAudioDecoderFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = mAudioDecoderFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int aacProfile = mAudioDecoderFormat.getInteger(MediaFormat.KEY_AAC_PROFILE);
        if (VERBOSE) {
            Log.d(TAG, "audio info,sampleRate: " + sampleRate + ",channelCount: " + channelCount + ",audioMimeType: " + audioMimeType);
        }
        mAudioEncoderFormat = MediaFormat.createAudioFormat(audioMimeType, sampleRate, channelCount);
        mAudioEncoderFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile);
        mAudioEncoderFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        mAudioEncoderFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        Log.d(TAG, "outputAudioFormat: " + mAudioEncoderFormat);

        mAudioEncoder = MediaCodec.createEncoderByType(audioMimeType);
        mAudioEncoder.configure(mAudioEncoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();
    }

    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is " + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    public void startEncoding() {
        // create and execute audio capturing thread using internal mic
        if (mAudioDecoderThread == null || mAudioEncoderThread == null) {
            synchronized (this) {
                if (mAudioDecoderThread == null) {
                    Log.i(TAG, "audio decoder task start...");
                    mAudioDecoderThread = new AudioDecoderThread();
                    mAudioDecoderThread.start();
                }

                if (mAudioEncoderThread == null) {
                    Log.i(TAG, "audio encoder task start...");
                    mAudioEncoderThread = new AudioEncoderThread();
                    mAudioEncoderThread.start();
                }
            }
        }
    }

    public void release() {
        if (VERBOSE) Log.d(TAG, "start releasing audio objects");
        if (mAudioExtractor != null) {
            try {
                mAudioExtractor.release();
                mAudioExtractor = null;
            } catch (Exception e) {
                Log.e(TAG, "failed releasing audioExtractor err", e);
            }
        }
        if (mAudioDecoder != null) {
            try {
                mAudioDecoder.stop();
                mAudioDecoder.release();
                mAudioDecoder = null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "failed releasing audio decoder err", e);
            }
        }
        if (mAudioEncoder != null) {
            try {
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mAudioEncoder = null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "failed releasing audio encoder err", e);
            }
        }

        if (mAudioDecoderDone) {
            mAudioDecoderDone = false;
            mAudioDecoderThread = null;
        }
        if (mAudioEncoderDone) {
            mAudioEncoderDone = false;
            mAudioEncoderThread = null;
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

        if (mPendingAudioEncoderOutputBufferInfos != null) {
            mPendingAudioEncoderOutputBufferInfos.clear();
            mPendingAudioEncoderOutputBufferInfos = null;
        }
        if (mPendingAudioEncoderOutputBufferIndices != null) {
            mPendingAudioEncoderOutputBufferIndices.clear();
            mPendingAudioEncoderOutputBufferIndices = null;
        }
    }


    protected void signalEndOfInputStream() {
        if (VERBOSE) Log.d(TAG, "sending EOS to audio AudioEncoder");
        mAudioDecoderDone = false;
        mAudioEncoderDone = false;
    }


    private static final long TIMEOUT_USEC = 10000;
    private boolean mAudioEncoderDone = false;
    private int mAudioExtractedFrameCount = 0;
    private int mAudioDecodedFrameCount = 0;
    private int mAudioEncodedFrameCount = 0;

    private boolean mAudioExtractorDone = false;
    private boolean mAudioDecoderDone = false;

    private LinkedList<Integer> mPendingAudioDecoderOutputBufferIndices = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioDecoderOutputBufferInfos = new LinkedList<>();
    private LinkedList<Integer> mPendingAudioEncoderInputBufferIndices = new LinkedList<>();

    private LinkedList<Integer> mPendingAudioEncoderOutputBufferIndices = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderOutputBufferInfos = new LinkedList<>();


    private class AudioDecoderThread extends Thread {
        @Override
        public void run() {
            super.run();
            ByteBuffer[] decoderInputBuffers = mAudioDecoder.getInputBuffers();
            ByteBuffer[] decoderOutputBuffers = mAudioDecoder.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            //audio decode process start
            while (!mAudioDecoderDone) {
                int decoderInStatus = -1;
                try {
                    //等待时mAudioDecoderDone有可能被置为true,decoder相关信息被释放导致异常(直接break即可)
                    decoderInStatus = mAudioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
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
                        if (VERBOSE) {
                            Log.d(TAG, "audio extractor: returned buffer of size " + size);
                            Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                        }
                        if (size >= 0) {
                            mAudioDecoder.queueInputBuffer(
                                    decoderInStatus,
                                    0,
                                    size,
                                    presentationTime,
                                    mAudioExtractor.getSampleFlags());
                        }
                        mAudioExtractorDone = !mAudioExtractor.advance();
                        if (mAudioExtractorDone) {
                            if (VERBOSE) Log.d(TAG, "audio extractor: EOS");
                            mAudioDecoder.queueInputBuffer(
                                    decoderInStatus,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }
                        mAudioExtractedFrameCount++;
                        if (VERBOSE)
                            Log.i(TAG, "audio decoder: mAudioExtractedFrameCount: " + mAudioExtractedFrameCount);
                        if (size >= 0) {
                            break;
                        }
                    }
                }

                int decoderOutStatus = mAudioDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                if (decoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (mAudioDecoderDone) {
                        break;      // out of while
                    } else {
                        if (VERBOSE)
                            Log.d(TAG, "audio decoder: no output available, spinning to await EOS");
                    }
                } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    mAudioDecoderFormat = mAudioDecoder.getOutputFormat();
                    if (VERBOSE) {
                        Log.d(TAG, "audio decoder: output format changed: " + mAudioDecoderFormat);
                    }
                } else if (decoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    decoderOutputBuffers = mAudioDecoder.getOutputBuffers();
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
                        mAudioDecoder.releaseOutputBuffer(decoderOutStatus, false);
                    }
                    if (VERBOSE) {
                        Log.d(TAG, "audio decoder: returned buffer for time " + bufferInfo.presentationTimeUs);
                    }
                    mPendingAudioDecoderOutputBufferIndices.add(decoderOutStatus);
                    mPendingAudioDecoderOutputBufferInfos.add(bufferInfo);
                    mAudioDecodedFrameCount++;
                    if (VERBOSE)
                        Log.i(TAG, "audio decoder: mAudioDecodedFrameCount: " + mAudioDecodedFrameCount);
                    tryEncodeAudio();
                }
            }
            Log.d(TAG, "audio decoder task complete...");
        }
    }

    private void tryEncodeAudio() {
        if (mPendingAudioEncoderInputBufferIndices.size() == 0 ||
                mPendingAudioDecoderOutputBufferIndices.size() == 0)
            return;
        int decoderIndex = mPendingAudioDecoderOutputBufferIndices.poll();
        int encoderIndex = mPendingAudioEncoderInputBufferIndices.poll();
        MediaCodec.BufferInfo decodeBufferInfo = mPendingAudioDecoderOutputBufferInfos.poll();

        ByteBuffer[] encoderInputBuffers = mAudioEncoder.getInputBuffers();
        ByteBuffer encoderInputBuffer = encoderInputBuffers[encoderIndex];
        int size = decodeBufferInfo.size;
        long presentationTime = decodeBufferInfo.presentationTimeUs;
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: processing pending buffer: " + decoderIndex);
        }
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: pending buffer of size " + size);
            Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
        }
        if (size >= 0) {
            ByteBuffer[] decoderOutputBuffers = mAudioDecoder.getOutputBuffers();
            ByteBuffer decoderOutputBuffer = decoderOutputBuffers[decoderIndex].duplicate();
            decoderOutputBuffer.position(decodeBufferInfo.offset);
            decoderOutputBuffer.limit(decodeBufferInfo.offset + size);
            encoderInputBuffer.position(0);
            encoderInputBuffer.put(decoderOutputBuffer);

            mAudioEncoder.queueInputBuffer(encoderIndex, 0, size, presentationTime, decodeBufferInfo.flags);
        }
        mAudioDecoder.releaseOutputBuffer(decoderIndex, false);
        if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mAudioDecoderDone = true;
            Log.d(TAG, "audio decoder: EOS");
        }
    }


    private class AudioEncoderThread extends Thread {
        @Override
        public void run() {
            super.run();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] encoderInputBuffers = mAudioEncoder.getInputBuffers();
            ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
            while (!mAudioEncoderDone) {
                int encodeInStatus = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (encodeInStatus >= 0) {
                    if (VERBOSE)
                        Log.d(TAG, "audio encoder: returned input buffer: " + encodeInStatus);
                    mPendingAudioEncoderInputBufferIndices.add(encodeInStatus);
                    tryEncodeAudio();
                }

                int encoderOutStatus = mAudioEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                if (encoderOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (mAudioEncoderDone) {
                        break;      // out of while
                    } else {
                        if (VERBOSE)
                            Log.d(TAG, "audio encoder: no output available, spinning to await EOS");
                    }
                } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    /*if (mMediaMuxerWrapper.isMuxerStart()) {
                        throw new RuntimeException("format changed twice");
                    }*/
                    mAudioEncoderFormat = mAudioEncoder.getOutputFormat();
                    Log.e(TAG, "audio encoder: output format changed: " + mAudioEncoderFormat);

                    // now that we have the Magic Goodies, start the muxer
                    mMediaMuxerWrapper.addTrackIndex(MediaMuxerWrapper.TRACK_AUDIO, mAudioEncoderFormat);
                    if (!mMediaMuxerWrapper.startMuxer()) {
                        // we should wait until muxer is ready
                        synchronized (mMediaMuxerWrapper.getLock()) {
                            while (!mMediaMuxerWrapper.isMuxerStart()) {
                                try {
                                    mMediaMuxerWrapper.getLock().wait(100);
                                } catch (final InterruptedException e) {
                                    break;
                                }
                            }
                        }
                    }

                    MediaCodec.BufferInfo infoIndices;
                    while ((infoIndices = mPendingAudioEncoderOutputBufferInfos.poll()) != null) {
                        Log.e(TAG, "audio encoder: mPendingAudioEncoderOutputBufferInfos not null... ");
                        int index = mPendingAudioEncoderOutputBufferIndices.poll().intValue();
                        muxAudio(index, infoIndices);
                    }
                } else if (encoderOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
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
            Log.d(TAG, "audio encoder task complete...");
        }
    }

    private void muxAudio(int encoderOutStatus, MediaCodec.BufferInfo bufferInfo) {
        if (!mMediaMuxerWrapper.isMuxerStart()) {
            Log.e(TAG, "audio encoder: muxAudio mediamuxer not start... ");
            mPendingAudioEncoderOutputBufferIndices.add(new Integer(encoderOutStatus));
            mPendingAudioEncoderOutputBufferInfos.add(bufferInfo);
            return;
        }
        ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mAudioEncoder.releaseOutputBuffer(encoderOutStatus, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "audio encoder: returned buffer for time " + bufferInfo.presentationTimeUs);
        }

        if (bufferInfo.size != 0) {
            if (!mMediaMuxerWrapper.isMuxerStart()) {
                // muxer is not ready...this will prrograming failure.
                throw new RuntimeException("VideoPlayEncoderTask hasn't started");
            }
            mMediaMuxerWrapper.writeSampleData(MediaMuxerWrapper.TRACK_AUDIO, encoderOutputBuffers[encoderOutStatus], bufferInfo);
            if (VERBOSE) {
                Log.d(TAG, "sent " + bufferInfo.size + " bytes to muxer, ts=" + bufferInfo.presentationTimeUs);
            }
        }
        mAudioEncoder.releaseOutputBuffer(encoderOutStatus, false);
        mAudioEncodedFrameCount++;
        if (VERBOSE)
            Log.d(TAG, "audio encoder: mAudioEncodedFrameCount: " + mAudioEncodedFrameCount);
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            mAudioEncoderDone = true;
            Log.d(TAG, "audio encoder: EOS");
            if(mAudioEncoderDone && mOnEncodingCompleteCallback != null){
                mOnEncodingCompleteCallback.onEncodingComplete();
            }
        }
    }
}
