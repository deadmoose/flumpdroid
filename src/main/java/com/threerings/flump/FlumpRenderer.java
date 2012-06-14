//
// Flump - Copyright 2012 Three Rings Design

package com.threerings.flump;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import flump.display.DisplayObject;

public class FlumpRenderer
    implements Renderer
{
    public void addDisplayObject (DisplayObject displayObject)
    {
        synchronized (_displayObjects) {
            _displayObjects.add(displayObject);
        }
    }

    public void clearDisplayObjects ()
    {
        synchronized (_displayObjects) {
            _displayObjects.clear();
        }
    }

    public void updateScale (float factor)
    {
        _scale = Math.min(5f, Math.max(0.5f, _scale * factor));
    }

    public void resetScale ()
    {
        _scale = 1;
    }

    @Override
    public void onDrawFrame (GL10 gl)
    {
        long stamp = System.currentTimeMillis();
        if (_lastFrame == 0) {
            _lastFrame = stamp;
        }

        float dt = (stamp - _lastFrame) / 1000f;
        _lastFrame = stamp;

        updateViewport(gl);

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnable (GL10.GL_BLEND);
        gl.glBlendFunc (GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

        synchronized (_displayObjects) {
            for (DisplayObject displayObject : _displayObjects) {
                displayObject.bindTextures(gl);
                displayObject.tick(dt);
                displayObject.draw(gl);
            }
        }

        gl.glDisable(GL10.GL_TEXTURE_2D);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    protected void updateViewport (GL10 gl)
    {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float scale = _scale;
        gl.glOrthof(-_width/scale, _width/scale, -_height/scale, _height/scale, -1, 1);
    }

    @Override
    public void onSurfaceChanged (GL10 gl, int width, int height)
    {
        bindTextures(gl);

        _width = width;
        _height = height;

        gl.glViewport(0, 0, width, height);

        updateViewport(gl);
    }

    @Override
    public void onSurfaceCreated (GL10 gl, EGLConfig config)
    {
        gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glDisable(GL10.GL_DITHER);
        gl.glDisable(GL10.GL_LIGHTING);

        bindTextures(gl);
    }

    protected void bindTextures (GL10 gl)
    {
        // TODO: Purge existing ones

        for (DisplayObject displayObject : _displayObjects) {
            displayObject.bindTextures(gl);
        }
    }

    protected int _width, _height;
    protected List<DisplayObject> _displayObjects = new ArrayList<DisplayObject>();
    protected long _lastFrame = 0;
    protected float _scale = 1f;
}
