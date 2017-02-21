package com.wangw.samples.media.filter.video;


import com.wangw.samples.media.filter.filter.FilterManager;

/**
 * Created by xingliao_zgl on 16/8/26.
 */
public class CodecConfig {
    public String inputVideoFilePath;
    public String inputAudioFilePath;
    public String outputFilePath;
    public FilterManager.FilterType filterType;

    /**
     *
     * @param inputVideoFilePath 输入视频文件路径
     * @param inputAudioFilePath 输入音频文件路径
     * @param outputFilePath 输出文件路径
     * @param filterType 滤镜类型
     */
    public CodecConfig(String inputVideoFilePath, String inputAudioFilePath, String outputFilePath, FilterManager.FilterType filterType) {
        this.inputVideoFilePath = inputVideoFilePath;
        this.inputAudioFilePath = inputAudioFilePath;
        this.outputFilePath = outputFilePath;
        this.filterType = filterType;
    }
}
