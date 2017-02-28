package com.wangw.videocache.file;

import java.io.File;
import java.io.IOException;

/**
 *
 * Created by wangw on 2017/2/25.
 */

public interface DiskUsage {

    void touch(File file)  throws IOException;
}
