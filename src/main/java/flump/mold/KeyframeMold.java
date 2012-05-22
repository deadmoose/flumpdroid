//
// Flump - Copyright 2012 Three Rings Design

package flump.mold;

public class KeyframeMold
{
    public int index;

    /** The length of this keyframe in frames. */
    public int duration;

    /**
     * The symbol of the texture or movie in this keyframe, or null if there is nothing in it.
     * For flipbook frames, this will be a name constructed out of the movie and frame index.
     */
    public String ref;

    /** The label on this keyframe, or null if there isn't one */
    public String label;

    /** Exploded values from matrix */
    public float x = 0, y = 0, scaleX = 1, scaleY = 1, skewX = 0, skewY = 0;

    /** Transformation point */
    public float pivotX = 0, pivotY = 0;

    public float alpha = 1;

    public boolean visible = true;

    public float ease = 0;
}
