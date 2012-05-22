//
// Flump - Copyright 2012 Three Rings Design

package flump.display;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLUtils;

import flump.mold.AtlasMold;
import flump.mold.AtlasTextureMold;

public class TextureDisplayObject extends DisplayObject
{
    public TextureDisplayObject (AtlasMold atlas, AtlasTextureMold texture)
    {
        _atlas = atlas;
        _texture = texture;

        float w = _texture.rect.width();
        float h = _texture.rect.height();

        // Compute stuffs
        _verts = makeFloatBuffer(new float[] {
            0, 0, 0,
            w, 0, 0,
            w, h, 0,
            0, 0, 0,
            w, h, 0,
            0, h, 0
        });

        float bmpW = _atlas.bitmap.getWidth();
        float bmpH = _atlas.bitmap.getHeight();
        float l = _texture.rect.left / bmpW;
        float r = _texture.rect.right / bmpW;
        float t = _texture.rect.top / bmpH;
        float b = _texture.rect.bottom / bmpH;

        _tex = makeFloatBuffer(new float[] {
            l, t,
            r, t,
            r, b,
            l, t,
            r, b,
            l, b
        });
    }

    @Override
    public void bindTextures (GL10 gl)
    {
        if (_atlas.textureId == -1) {
            int[] textureId = new int[1];
            gl.glGenTextures(1, textureId, 0);
            _atlas.textureId = textureId[0];
            gl.glBindTexture(GL10.GL_TEXTURE_2D, _atlas.textureId);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, _atlas.bitmap, 0);
        }
    }

    @Override
    protected void doDraw (GL10 gl)
    {
        gl.glPushMatrix();

        gl.glTranslatef(_texture.offset.x, _texture.offset.y, 0);
        gl.glColor4f(1, 1, 1, 1);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, _tex);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, _atlas.textureId);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _verts);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 6);

        gl.glPopMatrix();
    }

    protected static FloatBuffer makeFloatBuffer (float[] values)
    {
        FloatBuffer buf = ByteBuffer.allocateDirect(4 * values.length).order(
            ByteOrder.nativeOrder()).asFloatBuffer();
        buf.put(values);
        buf.rewind();
        return buf;
    }

    protected AtlasMold _atlas;
    protected AtlasTextureMold _texture;

    protected FloatBuffer _verts, _tex;
}
