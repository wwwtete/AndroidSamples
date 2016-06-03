package com.wangw.samples.exoplayer;

import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import com.exlogcat.L;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;
import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimpleExoPlayerActivity extends BaseActivity implements ExoPlayer.Listener, BandwidthMeter.EventListener, MediaCodecVideoTrackRenderer.EventListener, MediaCodecAudioTrackRenderer.EventListener {


    //video
    public static final String VIDEO_URL = "http://covertness.qiniudn.com/android_zaixianyingyinbofangqi_test_baseline.mp4";

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    @Bind(R.id.surface_view)
    SurfaceView mSurfaceView;
    @Bind(R.id.btn_play)
    Button mBtnPlay;
    @Bind(R.id.tv_info)
    TextView mTvInfo;

    private ExoPlayer mPlayer;
    private TrackRenderer mVideoRender;
    private TrackRenderer mAudioRender;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            L.d("[handleMessage] msg.what = %s | msg.value = %s",msg.what,msg.arg1);
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_exo_player);
        ButterKnife.bind(this);
        onInitPlayer();
    }

    private void onInitPlayer() {
        //1.通过ExoPlayer的Factory创建一个ExoPlayer
        mPlayer = ExoPlayer.Factory.newInstance(2, 1000, 5000);
        mPlayer.addListener(this);

        //2.初始化DataSource
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mHandler, this);
        DataSource dataSource = new DefaultUriDataSource(this, bandwidthMeter, Util.getUserAgent(this, "AndroidSamples"));
        //3.初始化SampleSource
        Uri uri = Uri.parse(VIDEO_URL);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        //4.初始化TrackRender
        TrackRenderer videoRender = new MediaCodecVideoTrackRenderer(this, sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, mHandler, this, 50);
        TrackRenderer audioRender = new MediaCodecAudioTrackRenderer(sampleSource, null, true, mHandler, this, AudioCapabilities.getCapabilities(this));
        //3.将渲染器注入到Player中
        mPlayer.prepare(videoRender, audioRender);
        mPlayer.sendMessage(videoRender, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, mSurfaceView.getHolder().getSurface());
        mPlayer.setPlayWhenReady(true);
    }

    @OnClick(R.id.btn_play)
    public void onClickPlayBtn(){
            mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
        if(mPlayer.getPlayWhenReady())
            mBtnPlay.setText("Pause");
        else {
            mBtnPlay.setText("Play");
        }
    }

    private void outputInfo(String msg){
        String txt = mTvInfo.getText().toString();
        if (!TextUtils.isEmpty(txt)){
            txt += "\n";
        }
        mTvInfo.setText(txt+msg);


    }

    @Override
    public String getSampleName() {
        return "ExoPlayer简单运用";
    }

    /**
     * Invoked when the value returned from either {@link ExoPlayer#getPlayWhenReady()} or
     * {@link ExoPlayer#getPlaybackState()} changes.
     *
     * @param playWhenReady Whether playback will proceed when ready.
     * @param playbackState One of the {@code STATE} constants defined in the {@link ExoPlayer}
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String stage ="other";
        switch (playbackState){
            case ExoPlayer.STATE_BUFFERING:
                stage = "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                stage = "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                stage = "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                stage = "ready";
                break;
            case ExoPlayer.STATE_READY:
                stage = "ready";
                break;
        }
        outputInfo("[onPlayerStateChanged] playWhenReady="+playWhenReady+" | playbackState="+stage);
    }

    /**
     * Invoked when the current value of {@link ExoPlayer#getPlayWhenReady()} has been reflected
     * by the internal playback thread.
     * <p>
     * An invocation of this method will shortly follow any call to
     * {@link ExoPlayer#setPlayWhenReady(boolean)} that changes the state. If multiple calls are
     * made in rapid succession, then this method will be invoked only once, after the final state
     * has been reflected.
     */
    @Override
    public void onPlayWhenReadyCommitted() {
        outputInfo("[onPlayWhenReadyCommitted]");
    }

    /**
     * Invoked when an error occurs. The playback state will transition to
     * {@link ExoPlayer#STATE_IDLE} immediately after this method is invoked. The player instance
     * can still be used, and {@link ExoPlayer#release()} must still be called on the player should
     * it no longer be required.
     *
     * @param error The error.
     */
    @Override
    public void onPlayerError(ExoPlaybackException error) {
        outputInfo("[onPlayerError] error = "+error.getMessage());
    }

    /**
     * Invoked periodically to indicate that bytes have been transferred.
     *
     * @param elapsedMs The time taken to transfer the bytes, in milliseconds.
     * @param bytes     The number of bytes transferred.
     * @param bitrate   The estimated bitrate in bits/sec, or {@link #NO_ESTIMATE} if no estimate
     *                  is available. Note that this estimate is typically derived from more information than
     *                  {@code bytes} and {@code elapsedMs}.
     */
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
        outputInfo("[onBandwidthSample]elapsedMs="+elapsedMs+" | bytes = "+bytes+" | bitrate="+bitrate);
    }

    /**
     * Invoked to report the number of frames dropped by the renderer. Dropped frames are reported
     * whenever the renderer is stopped having dropped frames, and optionally, whenever the count
     * reaches a specified threshold whilst the renderer is started.
     *
     * @param count   The number of dropped frames.
     * @param elapsed The duration in milliseconds over which the frames were dropped. This
     *                duration is timed from when the renderer was started or from when dropped frames were
     *                last reported (whichever was more recent), and not from when the first of the reported
     */
    @Override
    public void onDroppedFrames(int count, long elapsed) {
        outputInfo("[onDroppedFrames]");
    }

    /**
     * Invoked each time there's a change in the size of the video being rendered.
     *
     * @param width                    The video width in pixels.
     * @param height                   The video height in pixels.
     * @param unappliedRotationDegrees For videos that require a rotation, this is the clockwise
     *                                 rotation in degrees that the application should apply for the video for it to be rendered
     *                                 in the correct orientation. This value will always be zero on API levels 21 and above,
     *                                 since the renderer will apply all necessary rotations internally. On earlier API levels
     *                                 this is not possible. Applications that use {@link TextureView} can apply the rotation by
     *                                 calling {@link TextureView#setTransform}. Applications that do not expect to encounter
     *                                 rotated videos can safely ignore this parameter.
     * @param pixelWidthHeightRatio    The width to height ratio of each pixel. For the normal case
     *                                 of square pixels this will be equal to 1.0. Different values are indicative of anamorphic
     */
    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    /**
     * Invoked when a frame is rendered to a surface for the first time following that surface
     * having been set as the target for the renderer.
     *
     * @param surface The surface to which a first frame has been rendered.
     */
    @Override
    public void onDrawnToSurface(Surface surface) {

    }

    /**
     * Invoked when a decoder fails to initialize.
     *
     * @param e The corresponding exception.
     */
    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {

    }

    /**
     * Invoked when a decoder operation raises a {@link CryptoException}.
     *
     * @param e The corresponding exception.
     */
    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {

    }

    /**
     * Invoked when a decoder is successfully created.
     *
     * @param decoderName              The decoder that was configured and created.
     * @param elapsedRealtimeMs        {@code elapsedRealtime} timestamp of when the initialization
     *                                 finished.
     * @param initializationDurationMs Amount of time taken to initialize the decoder.
     */
    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {

    }

    /**
     * Invoked when an {@link AudioTrack} fails to initialize.
     *
     * @param e The corresponding exception.
     */
    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {

    }

    /**
     * Invoked when an {@link AudioTrack} write fails.
     *
     * @param e The corresponding exception.
     */
    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {

    }
}
