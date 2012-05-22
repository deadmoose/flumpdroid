//
// Flump - Copyright 2012 Three Rings Design

package com.threerings.flump;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

public class FlumpView extends GLSurfaceView
{
    public FlumpView (Context context)
    {
        super(context);

        init(context);
    }

    public FlumpView (Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    protected void init (Context context)
    {
        _scaleDetector = new ScaleGestureDetector(context, new SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale (ScaleGestureDetector detector) {
                final float scale = detector.getScaleFactor();

                if (!Float.isInfinite(scale) && !Float.isNaN(scale)) {
                    _renderer.updateScale(scale);
                }

                return true;
            }
        });

        _renderer = new FlumpRenderer();
        setRenderer(_renderer);
    }

    public FlumpRenderer getRenderer ()
    {
        return _renderer;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event)
    {
        return _scaleDetector.onTouchEvent(event);
    }

    protected FlumpRenderer _renderer;
    protected ScaleGestureDetector _scaleDetector;
}
