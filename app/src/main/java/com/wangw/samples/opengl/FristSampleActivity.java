package com.wangw.samples.opengl;

import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import com.wangw.samples.BaseActivity;
import com.wangw.samples.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wangw on 2017/4/7.
 */
@SuppressLint("NewApi")
public class FristSampleActivity extends BaseActivity {

    @Bind(R.id.glview)
    GLSurfaceView mGlview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opengl_frist_sample);
        ButterKnife.bind(this);

        //设置GLES版本号
        mGlview.setEGLContextClientVersion(2);
        //设置渲染器
        mGlview.setRenderer(new MyRenderer2());
        //设置渲染Mode
        //渲染模型分两种：
        //GLSurfaceView.RENDERMODE_WHEN_DIRTY:懒惰的，需要手动调用requestRender()方法
        //GLSurfaceView.RENDERMODE_CONTINUOUSLY:不停渲染
        mGlview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    public String getSampleName() {
        return "第一个Demo";
    }

   static class MyRenderer2 implements GLSurfaceView.Renderer {
        private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n"
                + "uniform mat4 uMVPMatrix;\n"
                + "void main() {\n"
                + "  gl_Position = uMVPMatrix * vPosition;\n"
                + "}";
        private static final String FRAGMENT_SHADER = "precision mediump float;\n"
                + "void main() {\n"
                + "  gl_FragColor = vec4(0.5,0,0,1);\n"
                + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                0, 1, 0.0f, // top
                -0.5f, -1, 0.0f, // bottom left
                1f, -1, 0.0f,  // bottom right
        };

        private int mProgram;
       private int mPostionHandler;
       private final FloatBuffer mVertexBuffer;
       private int mMatrixHandler;
       private final float[] mProjectionMatrix = new float[16];
       private final float[] mCameraMatrix = new float[16];
       private final float[] mMVPMatrix = new float[16];

       MyRenderer2(){
           mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length*4)
                   .order(ByteOrder.nativeOrder())
                   .asFloatBuffer()
                   .put(VERTEX);
           mVertexBuffer.position(0);
       }

       @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //创建GLSL程序
            mProgram = GLES20.glCreateProgram();
            //加载Shader代码
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT_SHADER);
            //Attatch share代码
            GLES20.glAttachShader(mProgram,vertexShader);
            GLES20.glAttachShader(mProgram,fragmentShader);
            //链接shader代码
            GLES20.glLinkProgram(mProgram);
            //链接Shader代码中的索引
            mPostionHandler = GLES20.glGetAttribLocation(mProgram,"vPosition");
            mMatrixHandler = GLES20.glGetUniformLocation(mProgram,"uMVPMatrix");

            float ratio = (float)width/height;

        }

       private int loadShader(int type, String source) {
           int shader = GLES20.glCreateShader(type);
           GLES20.glShaderSource(type,source);
           GLES20.glCompileShader(shader);
           return shader;
       }

       @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
           GLES20.glUseProgram(mProgram);
           GLES20.glEnableVertexAttribArray(mPostionHandler);
           GLES20.glVertexAttribPointer(mPostionHandler,3,GLES20.GL_FLOAT,false,12,mVertexBuffer);
           GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,3);
           GLES20.glDisableVertexAttribArray(mPostionHandler);
        }
    }


    static class MyRenderer implements GLSurfaceView.Renderer {
        private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n"
                + "uniform mat4 uMVPMatrix;\n"
                + "void main() {\n"
                + "  gl_Position = uMVPMatrix * vPosition;\n"
                + "}";
        private static final String FRAGMENT_SHADER = "precision mediump float;\n"
                + "void main() {\n"
                + "  gl_FragColor = vec4(0.5,0,0,1);\n"
                + "}";
        private static final float[] VERTEX = {   // in counterclockwise order:
                0, 1, 0.0f, // top
                -0.5f, -1, 0.0f, // bottom left
                1f, -1, 0.0f,  // bottom right
        };

        private final FloatBuffer mVertexBuffer;
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];

        private int mProgram;
        private int mPositionHandle;
        private int mMatrixHandle;

        MyRenderer() {
            mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(VERTEX);
            mVertexBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            mProgram = GLES20.glCreateProgram();
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            float ratio = (float) height / width;
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
            Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12,
                    mVertexBuffer);

            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

        static int loadShader(int type, String shaderCode) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }
    }

}
