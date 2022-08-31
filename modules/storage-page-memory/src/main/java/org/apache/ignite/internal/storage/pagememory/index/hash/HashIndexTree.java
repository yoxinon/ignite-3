/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.storage.pagememory.index.hash;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.internal.pagememory.PageMemory;
import org.apache.ignite.internal.pagememory.datapage.DataPageReader;
import org.apache.ignite.internal.pagememory.reuse.ReuseList;
import org.apache.ignite.internal.pagememory.tree.BplusTree;
import org.apache.ignite.internal.pagememory.tree.io.BplusIo;
import org.apache.ignite.internal.pagememory.util.PageLockListener;
import org.apache.ignite.internal.storage.pagememory.index.hash.io.HashIndexTreeInnerIo;
import org.apache.ignite.internal.storage.pagememory.index.hash.io.HashIndexTreeIo;
import org.apache.ignite.internal.storage.pagememory.index.hash.io.HashIndexTreeLeafIo;
import org.apache.ignite.internal.storage.pagememory.index.hash.io.HashIndexTreeMetaIo;
import org.apache.ignite.lang.IgniteInternalCheckedException;
import org.jetbrains.annotations.Nullable;

/**
 * {@link BplusTree} implementation for storing {@link HashIndexRow}.
 */
public class HashIndexTree extends BplusTree<HashIndexRowKey, HashIndexRow> {
    /** Data page reader instance to read payload from data pages. */
    private final DataPageReader dataPageReader;

    /**
     * Constructor.
     *
     * @param grpId Group ID.
     * @param grpName Group name.
     * @param partId Partition ID.
     * @param pageMem Page memory.
     * @param lockLsnr Page lock listener.
     * @param globalRmvId Remove ID.
     * @param metaPageId Meta page ID.
     * @param reuseList Reuse list.
     * @param initNew {@code True} if new tree should be created.
     * @throws IgniteInternalCheckedException If failed.
     */
    public HashIndexTree(
            int grpId,
            @Nullable String grpName,
            int partId,
            PageMemory pageMem,
            PageLockListener lockLsnr,
            AtomicLong globalRmvId,
            long metaPageId,
            @Nullable ReuseList reuseList,
            boolean initNew
    ) throws IgniteInternalCheckedException {
        super("HashIndexTree_" + grpId, grpId, grpName, partId, pageMem, lockLsnr, globalRmvId, metaPageId, reuseList);

        setIos(HashIndexTreeInnerIo.VERSIONS, HashIndexTreeLeafIo.VERSIONS, HashIndexTreeMetaIo.VERSIONS);

        dataPageReader = new DataPageReader(pageMem, grpId, statisticsHolder());

        initTree(initNew);
    }

    /**
     * Returns a partition id.
     */
    public int partitionId() {
        return partId;
    }

    /**
     * Returns a data page reader instance to read payload from data pages.
     */
    public DataPageReader dataPageReader() {
        return dataPageReader;
    }

    @Override
    protected int compare(BplusIo<HashIndexRowKey> io, long pageAddr, int idx, HashIndexRowKey row) throws IgniteInternalCheckedException {
        HashIndexTreeIo hashIndexTreeIo = (HashIndexTreeIo) io;

        return hashIndexTreeIo.compare(dataPageReader, partId, pageAddr, idx, row);
    }

    @Override
    public HashIndexRow getRow(BplusIo<HashIndexRowKey> io, long pageAddr, int idx, Object x) throws IgniteInternalCheckedException {
        HashIndexTreeIo hashIndexTreeIo = (HashIndexTreeIo) io;

        return hashIndexTreeIo.getRow(dataPageReader, partId, pageAddr, idx);
    }
}
