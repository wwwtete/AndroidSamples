package com.wangw.samples.exoplayer.player;

/**
 * Created by wangw on 2016/6/2.
 */

public interface RendererBuilder {

    void buildRenderers(SuperPlayer player);

    void cancel();

}
