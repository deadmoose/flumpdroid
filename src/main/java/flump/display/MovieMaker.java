//
// Flump - Copyright 2012 Three Rings Design

package flump.display;

import flump.mold.AtlasMold;
import flump.mold.AtlasTextureMold;
import flump.mold.LibraryMold;
import flump.mold.MovieMold;

/**
 * Creates displayable movies from a LibraryMold.
 */
public class MovieMaker
{
    public MovieMaker (LibraryMold library)
    {
        _library = library;
    }

    public DisplayObject getDisplayObject (String name)
    {
        // TODO: Index all this

        for (MovieMold movie : _library.movies) {
            if (name.equals(movie.id)) {
                return new Movie(movie, movie.frameRate, this);
            }
        }

        for (AtlasMold atlas : _library.atlases) {
            for (AtlasTextureMold texture : atlas.textures) {
                if (texture.symbol.equals(name)) {
                    return new TextureDisplayObject(atlas, texture);
                }
            }
        }

        return null;
    }

    protected LibraryMold _library;
}
