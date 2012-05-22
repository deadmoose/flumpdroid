//
// Flump - Copyright 2012 Three Rings Design

package flump.simplexml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import android.graphics.PointF;
import android.graphics.Rect;
import flump.display.Movie;
import flump.mold.AtlasMold;
import flump.mold.AtlasTextureMold;
import flump.mold.KeyframeMold;
import flump.mold.LayerMold;
import flump.mold.LibraryMold;
import flump.mold.MovieMold;
import flump.mold.TextureGroupMold;

/**
 * Uses Simple (http://simple.sourceforge.net/) to read (but not write) Flump XML.
 *
 * Of course, one of the big perks of that library is the ability to just annotate your data
 * and have it happen automagically. I've moved away from that, though, to declutter my data classes
 * plus cope with the handful of less-than-trivial classes, so it might be worth ditching that
 * dependency.
 */
public class FlumpMoldRegistry extends Registry
{
    public FlumpMoldRegistry ()
        throws Exception
    {
        super();

        bind(AtlasMold.class, new FlumpConverter<AtlasMold>() {
            @Override
            public AtlasMold read (InputNode node) throws Exception {
                AtlasMold mold = new AtlasMold();

                mold.file = requireString(node, "file");
                mold.textures = loadChildren(node, "texture", AtlasTextureMold.class);

                return mold;
            }
        });

        bind(AtlasTextureMold.class, new FlumpConverter<AtlasTextureMold>() {
            @Override
            public AtlasTextureMold read (InputNode node) throws Exception {
                AtlasTextureMold mold = new AtlasTextureMold();

                mold.symbol = requireString(node, "name"); // Doesn't match the XML. GRAH.
                mold.rect = requireRect(node, "rect");
                mold.offset = requirePointF(node, "offset");
                mold.md5 = requireString(node, "md5");

                return mold;
            }
        });

        bind(KeyframeMold.class, new FlumpConverter<KeyframeMold>() {
            @Override
            public KeyframeMold read (InputNode node) throws Exception {
                KeyframeMold mold = new KeyframeMold();

                mold.duration = requireInt(node, "duration");
                mold.ref = getString(node, "ref", null);
                mold.label = getString(node, "label", null);

                float[] floats = getFloatArray(node, "loc", null, 2);
                if (floats != null) {
                    mold.x = floats[0];
                    mold.y = floats[1];
                }

                floats = getFloatArray(node, "scale", null, 2);
                if (floats != null) {
                    mold.scaleX = floats[0];
                    mold.scaleY = floats[1];
                }

                floats = getFloatArray(node, "skew", null, 2);
                if (floats != null) {
                    mold.skewX = floats[0];
                    mold.skewY = floats[1];
                }

                floats = getFloatArray(node, "pivot", null, 2);
                if (floats != null) {
                    mold.pivotX = floats[0];
                    mold.pivotY = floats[1];
                }

                mold.alpha = getFloat(node, "alpha", 1);
                mold.visible = getBoolean(node, "visible", true);
                mold.ease = getFloat(node, "ease", 0);

                return mold;
            }
        });

        bind(LayerMold.class, new FlumpConverter<LayerMold>() {
            @Override
            public LayerMold read (InputNode node) throws Exception {
                LayerMold mold = new LayerMold();

                mold.name = requireString(node, "name");
                mold.keyframes = loadChildren(node, "kf", KeyframeMold.class);
                mold.flipbook = getBoolean(node, "flipbook", false);

                mold.frames = 0;
                for (KeyframeMold kf : mold.keyframes) {
                    kf.index = mold.frames;
                    mold.frames += kf.duration;
                }

                return mold;
            }
        });

        bind(LibraryMold.class, new FlumpConverter<LibraryMold>() {
            @Override
            public LibraryMold read (InputNode node) throws Exception {
                LibraryMold mold = new LibraryMold();

                mold.md5 = requireString(node, "md5");
                mold.movies = loadChildren(node, "movie", MovieMold.class);

                // Load all the textureGroups
                mold.textureGroups = loadChildren(node.getNext("textureGroups"),
                    "textureGroup", TextureGroupMold.class);
                // Default to whichever was first
                mold.atlases = mold.textureGroups.get(0).atlases;

                return mold;
            }
        });

        bind(MovieMold.class, new FlumpConverter<MovieMold>() {
            @Override
            public MovieMold read (InputNode node) throws Exception {
                MovieMold mold = new MovieMold();

                mold.id = requireString(node, "name"); // Doesn't match the XML. GRAH.
                mold.layers = loadChildren(node, "layer", LayerMold.class);
                mold.md5 = requireString(node, "md5");
                mold.frameRate = requireFloat(node, "frameRate");

                mold.frames = 0;
                for (LayerMold layer : mold.layers) {
                    mold.frames = Math.max(mold.frames, layer.frames);
                }
                mold.flipbook = mold.layers.get(0).flipbook;

                mold.labels = new ArrayList<Set<String>>(mold.frames);
                // Prepopulate with nulls
                for (int ii = 0; ii < mold.frames; ii++) {
                    mold.labels.add(null);
                }

                // Fill in the canned ones
                mold.labels.set(0, new HashSet<String>());
                mold.labels.get(0).add(Movie.FIRST_FRAME);
                mold.labels.set(mold.frames - 1, new HashSet<String>());
                mold.labels.get(mold.frames - 1).add(Movie.LAST_FRAME);

                // Fill in the rest
                for (LayerMold layer : mold.layers) {
                    for (KeyframeMold kf : layer.keyframes) {
                        if (kf.label == null) {
                            continue;
                        }

                        if (mold.labels.get(kf.index) == null) {
                            mold.labels.set(kf.index, new HashSet<String>());
                        }
                        mold.labels.get(kf.index).add(kf.label);
                    }
                }

                return mold;
            }
        });

        bind(TextureGroupMold.class, new FlumpConverter<TextureGroupMold>() {
            @Override
            public TextureGroupMold read (InputNode node) throws Exception {
                TextureGroupMold mold = new TextureGroupMold();

                mold.retina = requireBoolean(node, "retina");
                mold.atlases = loadChildren(node, "atlas", AtlasMold.class);

                return mold;
            }

        });
    }

    protected static String requireString (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }

        return getString(node, name, null);
    }

    protected static String getString (InputNode node, String name, String defaultValue)
        throws Exception
    {
        InputNode attr = node.getAttribute(name);
        if (attr != null) {
            return attr.getValue();
        }

        return defaultValue;
    }

    protected static String[] getStringArray (
        InputNode node, String name, String defaultValue, int length)
        throws Exception
    {
        String val = getString(node, name, defaultValue);
        if (val == null) {
            return null;
        }

        String[] vals = getString(node, name, defaultValue).split(",");
        if (length > 0 && vals.length != length) {
            throw new Exception("Invalid length array in attribute '" + name + "'. " +
                "Got " + vals.length + ", expected " + length);
        }

        return vals;
    }

    protected static PointF requirePointF (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }

        return getPointF(node, name, null);
    }

    protected static PointF getPointF (InputNode node, String name, String defaultValue)
        throws Exception
    {
        String[] split = getStringArray(node, name, defaultValue, 2);
        return new PointF(
            Float.parseFloat(split[0]),
            Float.parseFloat(split[1])
        );
    }

    protected static Rect requireRect (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }

        return getRect(node, name, null);
    }

    protected static Rect getRect (InputNode node, String name, String defaultValue)
        throws Exception
    {
        String[] split = getString(node, name, defaultValue).split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int w = Integer.parseInt(split[2]);
        int h = Integer.parseInt(split[3]);
        return new Rect(x, y, x + w, y + h);
    }

    protected static int requireInt (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }
        return getInt(node, name, null);
    }

    protected static int getInt (InputNode node, String name, int defaultValue)
        throws Exception
    {
        // FIXME: I hate converting to string for this...
        return getInt(node, name, Integer.toString(defaultValue));
    }

    protected static int getInt (InputNode node, String name, String defaultValue)
        throws Exception
    {
        return Integer.parseInt(getString(node, name, defaultValue));
    }

    protected static float requireFloat (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }
        return getFloat(node, name, null);
    }

    protected static float getFloat (InputNode node, String name, float defaultValue)
        throws Exception
    {
        // FIXME: I hate converting to string for this...
        return getFloat(node, name, Float.toString(defaultValue));
    }

    protected static float getFloat (InputNode node, String name, String defaultValue)
        throws Exception
    {
        return Float.parseFloat(getString(node, name, defaultValue));
    }

    protected static boolean requireBoolean (InputNode node, String name)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }
        return getBoolean(node, name, null);
    }

    protected static boolean getBoolean (InputNode node, String name, boolean defaultValue)
        throws Exception
    {
        // FIXME: I hate converting to string for this...
        return getBoolean(node, name, Boolean.toString(defaultValue));
    }

    protected static boolean getBoolean (InputNode node, String name, String defaultValue)
        throws Exception
    {
        return Boolean.parseBoolean(getString(node, name, defaultValue));
    }

    protected static float[] requireFloatArray (InputNode node, String name, int length)
        throws Exception
    {
        if (node.getAttribute(name) == null) {
            throw new Exception("Missing attribute: " + name);
        }

        return getFloatArray(node, name, null, length);
    }

    protected static float[] getFloatArray (
        InputNode node, String name, String defaultValue, int length)
        throws Exception
    {
        String[] strs = getStringArray(node, name, defaultValue, length);
        float[] floats = null;

        if (strs != null) {
            floats = new float[strs.length];
            for (int ii = 0, jj = strs.length; ii < jj; ii++) {
                floats[ii] = Float.parseFloat(strs[ii]);
            }
        }

        return floats;
    }

    protected <T> ArrayList<T> loadChildren (InputNode node, String name, Class<T> klass)
        throws Exception
    {
        ArrayList<T> values = new ArrayList<T>();

        @SuppressWarnings("unchecked")
        Converter<T> converter = lookup(klass);

        InputNode listNode;
        while ((listNode = node.getNext(name)) != null) {
            values.add(converter.read(listNode));
        }

        return values;
    }

    protected static abstract class FlumpConverter<T> implements Converter<T> {
        abstract public T read (InputNode node) throws Exception;

        public void write (OutputNode node, T mold) throws Exception {
            throw new UnsupportedOperationException();
        }
    };
}
