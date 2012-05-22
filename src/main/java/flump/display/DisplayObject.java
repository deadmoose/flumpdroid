//
// Flump - Copyright 2012 Three Rings Design

package flump.display;

import javax.microedition.khronos.opengles.GL10;

public abstract class DisplayObject
{
    public float x, y;

    public float scaleX = 1, scaleY = 1;

    public float skewX, skewY;
    public float pivotX, pivotY;
    public float alpha = 1;

    public boolean visible = true;

    public void tick (float dt)
    {
        // By default, nothing
    }

    public void draw (GL10 gl)
    {
        gl.glPushMatrix();

        try {
            if (x != 0 || y != 0) {
                gl.glTranslatef(x, y, 0);
            }
            if (skewX != 0 || skewY != 0) {
                gl.glRotatef(90, 0, 0, 1);
                gl.glMultMatrixf(new float[] {
                    (float)Math.cos(skewX), (float)Math.sin(skewX), 0, 0,
                    -(float)Math.sin(skewY), (float)Math.cos(skewY), 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1,
                }, 0);
                gl.glRotatef(-90, 0, 0, 1);
            }
            if (scaleX != 1 || scaleY != 0) {
                gl.glScalef(scaleX, scaleY, 1);
            }
            if (pivotX != 0 || pivotY != 0) {
                gl.glTranslatef(-pivotX, -pivotY, 0);
            }

            doDraw(gl);

        } catch (Exception e) {
            // TODO: log
        }

        gl.glPopMatrix();
    }

    public void bindTextures (GL10 gl)
    {
        // By default, nothing
    }

    abstract protected void doDraw (GL10 gl);
}
