//
// Flump - Copyright 2012 Three Rings Design

package flump.display;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class DisplayObjectContainer extends DisplayObject
{
    public void addChild (DisplayObject child)
    {
        _children.add(child);
    }

    public void addChildAt (DisplayObject child, int index)
    {
        _children.add(index, child);
    }

    public void removeChildAt (int index)
    {
        _children.remove(index);
    }

    public DisplayObject getChildAt (int index)
    {
        return _children.get(index);
    }

    public int numChildren ()
    {
        return _children.size();
    }

    @Override
    protected void doDraw (GL10 gl)
    {
        if (alpha != 1) {
            // Oh craps
        }

        for (DisplayObject child : _children) {
            child.draw(gl);
        }
    }

    @Override
    public void tick (float dt)
    {
        for (DisplayObject child : _children) {
            child.tick(dt);
        }
    }

    @Override
    public void bindTextures (GL10 gl)
    {
        for (DisplayObject child : _children) {
            child.bindTextures(gl);
        }
    }

    protected List<DisplayObject> _children = new ArrayList<DisplayObject>();
}
