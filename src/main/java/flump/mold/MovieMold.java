//
// Flump - Copyright 2012 Three Rings Design

package flump.mold;

import java.util.List;
import java.util.Set;

public class MovieMold
{
    public String id;
    public List<LayerMold> layers;
    public List<Set<String>> labels;

    // The hash of the XML file for this symbol in the library
    public String md5;

    public int frames;
    public boolean flipbook;

    public float frameRate;
}
