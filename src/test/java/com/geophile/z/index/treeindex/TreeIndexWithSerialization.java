/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.treeindex;

import com.geophile.z.DuplicateSpatialObjectException;
import com.geophile.z.Index;
import com.geophile.z.Serializer;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.SpatialObjectKey;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

// Like TreeIndex, but with serialization of spatial objects

public class TreeIndexWithSerialization implements Index
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface


    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public void add(long z, SpatialObject spatialObject)
    {
        ByteBuffer replaced = tree.put(key(z, spatialObject.id()), serialize(spatialObject));
        if (replaced != null) {
            throw new DuplicateSpatialObjectException(deserialize(replaced));
        }
    }

    @Override
    public boolean remove(long z, long soid)
    {
        boolean removed = false;
        Iterator<Map.Entry<SpatialObjectKey, ByteBuffer>> zScan =
            tree.tailMap(key(z, soid)).entrySet().iterator();
        if (zScan.hasNext()) {
            Map.Entry<SpatialObjectKey, ByteBuffer> entry = zScan.next();
            if (entry.getKey().z() == z) {
                SpatialObjectKey key = entry.getKey();
                assert key.z() == z : key;
                if (key.soid() == soid) {
                    zScan.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public Cursor cursor(long z)
    {
        return new TreeIndexWithSerializationCursor(serializer, tree, key(z));
    }

    @Override
    public SpatialObjectKey key(long z)
    {
        return SpatialObjectKey.keyLowerBound(z);
    }

    @Override
    public SpatialObjectKey key(long z, long soid)
    {
        return SpatialObjectKey.key(z, soid);
    }

    // TreeIndex

    public TreeIndexWithSerialization(Serializer serializer)
    {
        this.serializer = serializer;
    }

    // For use by this class

    private ByteBuffer serialize(SpatialObject spatialObject)
    {
        ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        boolean serialized = false;
        do {
            try {
                serializer.serialize(spatialObject, buffer);
                buffer.flip();
                serialized = true;
            } catch (BufferOverflowException e) {
                buffer = ByteBuffer.allocate(buffer.capacity() * 2);
            }
        } while (!serialized);
        return buffer;
    }

    private SpatialObject deserialize(ByteBuffer buffer)
    {
        return serializer.deserialize(buffer);
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final int INITIAL_BUFFER_SIZE = 100;

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final Serializer serializer;
    private final TreeMap<SpatialObjectKey, ByteBuffer> tree = new TreeMap<>();
}
