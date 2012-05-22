//
// Flump - Copyright 2012 Three Rings Design

package flump.simplexml;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import flump.mold.AtlasMold;
import flump.mold.LibraryMold;
import flump.mold.TextureGroupMold;

public class FlumpParser
{
    public FlumpParser (AssetManager assets, String basePath)
    {
        _assets = assets;
        _basePath = basePath;

        try {
            _serial = new Persister(new RegistryStrategy(new FlumpMoldRegistry()));
        } catch (Exception e) {
            // OH CRAP
        }
    }

    public String getBasePath ()
    {
        return _basePath;
    }

    public LibraryMold loadLibrary (String name)
        throws Exception
    {
        // Parse the XML
        LibraryMold library = _serial.read(LibraryMold.class,
            _assets.open(_basePath + "/" + name + "/resources.xml"));

        // Load the texture atlases
        // FIXME: Don't load them all yet since we likely only want one scale
        for (TextureGroupMold group : library.textureGroups) {
            for (AtlasMold atlas : group.atlases) {
                atlas.bitmap = BitmapFactory.decodeStream(_assets.open(atlas.file));
            }
        }

        return library;
    }

    protected AssetManager _assets;
    protected String _basePath;
    protected Serializer _serial;
}
