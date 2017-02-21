//#extension GL_OES_EGL_image_external : require
//uniform samplerExternalOES uTexture;

precision mediump float; //指定默认精度

varying vec2 vTextureCoord;

void main() {
    gl_FragColor = texture2D(uTexture, vTextureCoord);
}