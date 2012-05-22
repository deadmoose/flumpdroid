//
// Flump - Copyright 2012 Three Rings Design

package flump.mold;

import java.util.List;

public class LibraryMold
{
    // The MD5 of the published library SWF
    public String md5;

    public List<MovieMold> movies;
    public List<AtlasMold> atlases;

    public List<TextureGroupMold> textureGroups;

    public void useRetinaAtlas (boolean retina)
    {
        for (TextureGroupMold group : textureGroups) {
            if (group.retina == retina) {
                atlases = group.atlases;
                return;
            }
        }

        // Not really IAE, but... what?
        throw new IllegalArgumentException("Library did not have desired retina mode: " + retina);
    }
}
