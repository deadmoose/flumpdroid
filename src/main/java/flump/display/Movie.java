//
// Flump - Copyright 2012 Three Rings Design

package flump.display;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import react.Signal;
import flump.mold.KeyframeMold;
import flump.mold.LayerMold;
import flump.mold.MovieMold;

public class Movie extends DisplayObjectContainer
{
    /** A label fired by all movies when entering their first frame. */
    public static final String FIRST_FRAME = "flump.movie.FIRST_FRAME";

    /** A label fired by all movies when entering their last frame. */
    public static final String LAST_FRAME = "flump.movie.LAST_FRAME";

    /** Fires the label string whenever it's passed in playing. */
    public Signal<String> labelPassed = Signal.create();

    public Movie (MovieMold src, float frameRate, MovieMaker maker)
    {
        _name = src.id;
        _labels = src.labels;
        _frameRate = frameRate;
        if (src.flipbook) {
            _layers = new Layer[1];
            _layers[0] = new Layer(this, src.layers.get(0), maker, true);
            _frames = src.layers.get(0).frames;
        } else {
            _layers = new Layer[src.layers.size()];
            for (int ii = 0, jj = _layers.length; ii < jj; ii++) {
                _layers[ii] = new Layer(this, src.layers.get(ii), maker, false);
                _frames = Math.max(_frames, src.layers.get(ii).frames);
            }
        }
        _duration = _frames / _frameRate;
        updateFrame(0, /*fromSkip=*/true, /*overDuration=*/false);
    }

    @Override
    public void draw (GL10 gl)
    {
        // Top level movie handles flipping the rest. Except shit, is this going to break when
        // there are refs to movies?
        gl.glPushMatrix();
        gl.glScalef(1, -1, 1);

        super.draw(gl);

        gl.glPopMatrix();
    }

    /** The frame being displayed. */
    public int getFrame () { return _frame; }

    /** The number of frames in the movie. */
    public int getFrames () { return _frames; }

    /** If the movie is playing currently. */
    public boolean isPlaying () { return _playing; }

    /** Starts playing if not already doing so, and continues to do so indefinitely.  */
    public Movie loop ()
    {
        _playing = true;
        _stopFrame = NO_FRAME;
        return this;
    }

    /**
     * Moves to the given String label or int frame. Doesn't alter playing status or stop frame.
     * If there are labels at the given position, they're fired as part of the goto, even if the
     * current frame is equal to the destination. Labels between the current frame and the
     * destination frame are not fired.
     *
     * @param position the int frame or String label to goto.
     *
     * @return this movie for chaining
     *
     * @throws Error if position isn't an int or String, or if it is a String and that String isn't
     * a label on this movie
     */
    public Movie gotoPosition (Object position)
    {
        final int frame = extractFrame(position);
        updateFrame(frame, /*fromSkip=*/true, /*overDuration=*/false);
        return this;
    }

    /**
     * Starts playing if not already doing so, and continues to do so to the last frame in the
     * movie.
     */
    public Movie play () { return playTo(LAST_FRAME); }

   /**
    * Starts playing if not already doing so, and continues to do so to the given stop label or
    * frame.
    *
    * @param position to int frame or String label to stop at
    *
    * @return this movie for chaining
    *
    * @throws Error if position isn't an int or String, or if it is a String and that String isn't
    * a label on this movie.
    */
   public Movie playTo (Object position)
   {
       _stopFrame = extractFrame(position);
       _playing = true;
       return this;
   }

   /** Stops playback if it's currently active. Doesn't alter the current frame or stop frame. */
    public Movie stop ()
    {
        _playing = false;
        return this;
    }

    /** @private */
    protected int extractFrame (Object position)
    {
        if (position instanceof Integer) {
            return (Integer)position;
        }
        if (!(position instanceof String)) {
            throw new IllegalArgumentException("Movie position must be an int frame or String label");
        }
        final String label = (String)position;
        for (int ii = 0; ii < _labels.size(); ii++) {
            if (_labels.get(ii) != null && _labels.get(ii).contains(label)) {
                return ii;
            }
        }
        throw new IllegalArgumentException("No such label '" + label + "'");
    }

    @Override
    public void tick (float dt)
    {
        if (!_playing) {
            return;
        }

        super.tick(dt);

        _playTime += dt;
        float actualPlaytime = _playTime;
        if (_playTime >= _duration) {
            _playTime = _playTime % _duration;
        }
        int newFrame = (int)(_playTime * _frameRate);
        final boolean overDuration = dt >= _duration;
        // If the update crosses or goes to the stopFrame, go to the stop frame, stop the movie and
        // clear it
        if (_stopFrame != NO_FRAME) {
            // how many frames remain to the stopframe?
            int framesRemaining =
                (_frame <= _stopFrame ? _stopFrame - _frame : _frames - _frame + _stopFrame);
            int framesElapsed = (int)(actualPlaytime * _frameRate) - _frame;
            if (framesElapsed >= framesRemaining) {
                _playing = false;
                newFrame = _stopFrame;
                _stopFrame = NO_FRAME;
            }
        }
        updateFrame(newFrame, false, overDuration);
    }

    /** @private */
    protected void updateFrame (int newFrame, boolean fromSkip, boolean overDuration)
    {
        if (newFrame >= _frames) {
            throw new Error("Asked to go to frame " + newFrame + " past the last frame, " +
                (_frames - 1));
        }
        if (_goingToFrame) {
            _pendingFrame = newFrame;
            return;
        }
        _goingToFrame = true;
        final boolean differentFrame = newFrame != _frame;
        final boolean wrapped = newFrame < _frame;
        if (differentFrame) {
            if (wrapped) {
                for (Layer layer : _layers) {
                    layer.changedKeyframe = true;
                    layer.keyframeIdx = 0;
                }
            }
            for (Layer layer : _layers) {
                layer.drawFrame(newFrame);
            }
        }

        // Update the frame before firing, so if firing changes the frame, it sticks.
        final int oldFrame = _frame;
        _frame = newFrame;
        if (fromSkip) {
            fireLabels(newFrame, newFrame);
            _playTime = newFrame/_frameRate;
        } else if (overDuration) {
            fireLabels(oldFrame + 1, _frames - 1);
            fireLabels(0, _frame);
        } else if (differentFrame) {
            if (wrapped) {
                fireLabels(oldFrame + 1, _frames - 1);
                fireLabels(0, _frame);
            } else fireLabels(oldFrame + 1, _frame);
        }
        _goingToFrame = false;
        if (_pendingFrame != NO_FRAME) {
            newFrame = _pendingFrame;
            _pendingFrame = NO_FRAME;
            updateFrame(newFrame, true, false);
        }
    }

    protected void fireLabels (int startFrame, int endFrame)
    {
        for (int ii = startFrame; ii <= endFrame; ii++) {
            if (_labels.get(ii) == null) {
                continue;
            }
            for (String label : _labels.get(ii)) {
                labelPassed.emit(label);
            }
        }
    }

    protected class Layer {
        public int keyframeIdx;// The index of the last keyframe drawn in drawFrame
        public int layerIdx;// This layer's index in the movie
        public List<KeyframeMold> keyframes;
        // Only created if there are multiple items on this layer. If it does exist, the appropriate display is swapped in at keyframe changes. If it doesn't, the display is only added to the parent on layer creation
        public Vector<DisplayObject> displays;
        public Movie movie; // The movie this layer belongs to
        // If the keyframe has changed since the last drawFrame
        public boolean changedKeyframe;

        public Layer (Movie movie, LayerMold src, MovieMaker maker, boolean flipbook) {
            keyframes = src.keyframes;
            this.movie = movie;
            String lastItem = null;
            for (int ii = 0; ii < keyframes.size() && lastItem == null; ii++) {
                lastItem = keyframes.get(ii).ref;
            }
            if (!flipbook && lastItem == null) {
                movie.addChild(new DisplayObjectContainer());// Label only layer
            } else {
                boolean multipleItems = flipbook;
                for (int ii = 0; ii < keyframes.size() && !multipleItems; ii++) {
                    multipleItems = keyframes.get(ii).ref != lastItem;
                }
                if (!multipleItems) {
                    movie.addChild(maker.getDisplayObject(lastItem));
                } else {
                    displays = new Vector<DisplayObject>();
                    for (KeyframeMold kf : keyframes) {
                        DisplayObject display = kf.ref == null ?
                            new DisplayObjectContainer() : maker.getDisplayObject(kf.ref);
                        displays.add(display);
                        //display.name = src.name;
                    }
                    movie.addChild(displays.get(0));
                }
            }
            layerIdx = movie.numChildren() - 1;
            //movie.getChildAt(layerIdx).name = src.name;
        }

        public void drawFrame (int frame) {
            while (keyframeIdx < keyframes.size() - 1 && keyframes.get(keyframeIdx + 1).index <= frame) {
                keyframeIdx++;
                changedKeyframe = true;
            }
            // We've got multiple items. Swap in the one for this kf
            if (changedKeyframe && displays != null) {
                movie.removeChildAt(layerIdx);
                movie.addChildAt(displays.get(keyframeIdx), layerIdx);
            }
            changedKeyframe = false;

            final KeyframeMold kf = keyframes.get(keyframeIdx);
            final DisplayObject layer = movie.getChildAt(layerIdx);
            if (keyframeIdx == keyframes.size() - 1 || kf.index == frame) {
                layer.x = kf.x;
                layer.y = kf.y;
                layer.scaleX = kf.scaleX;
                layer.scaleY = kf.scaleY;
                layer.skewX = kf.skewX;
                layer.skewY = kf.skewY;
                layer.alpha = kf.alpha;
            } else {
                float interped = (float)(frame - kf.index)/kf.duration;
                //interped = 0;
                float ease = kf.ease;
                if (ease != 0) {
                    float t;
                    if (ease < 0) {
                        // Ease in
                        float inv = 1 - interped;
                        t = 1 - inv*inv;
                        ease = -ease;
                    } else {
                        // Ease out
                        t = interped*interped;
                    }
                    interped = ease*t + (1 - ease)*interped;
                }

                final KeyframeMold nextKf = keyframes.get(keyframeIdx + 1);

                layer.x = kf.x + (nextKf.x - kf.x) * interped;
                layer.y = kf.y + (nextKf.y - kf.y) * interped;
                layer.scaleX = kf.scaleX + (nextKf.scaleX - kf.scaleX) * interped;
                layer.scaleY = kf.scaleY + (nextKf.scaleY - kf.scaleY) * interped;
                layer.skewX = kf.skewX + (nextKf.skewX - kf.skewX) * interped;
                layer.skewY = kf.skewY + (nextKf.skewY - kf.skewY) * interped;
                layer.alpha = kf.alpha + (nextKf.alpha - kf.alpha) * interped;
            }

            layer.pivotX = kf.pivotX;
            layer.pivotY = kf.pivotY;
            layer.visible = kf.visible;
        }
    }

    protected String _name;
    protected boolean _goingToFrame;
    protected int _pendingFrame = NO_FRAME;
    protected int _frame = NO_FRAME, _stopFrame = NO_FRAME;
    protected boolean _playing = true;
    protected float _playTime, _duration;
    protected Layer[] _layers;
    protected int _frames;
    protected float _frameRate;
    protected List<Set<String>> _labels;

    protected static final int NO_FRAME = -1;
}
