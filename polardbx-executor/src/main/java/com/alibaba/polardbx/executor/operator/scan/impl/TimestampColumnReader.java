package com.alibaba.polardbx.executor.operator.scan.impl;

import com.alibaba.polardbx.common.utils.time.MySQLTimeConverter;
import com.alibaba.polardbx.common.utils.time.core.MySQLTimeVal;
import com.alibaba.polardbx.common.utils.time.core.MysqlDateTime;
import com.alibaba.polardbx.common.utils.time.core.TimeStorage;
import com.alibaba.polardbx.executor.chunk.Block;
import com.alibaba.polardbx.executor.chunk.RandomAccessBlock;
import com.alibaba.polardbx.executor.chunk.TimestampBlock;
import com.alibaba.polardbx.executor.operator.scan.StripeLoader;
import com.alibaba.polardbx.executor.operator.scan.metrics.RuntimeMetrics;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.utils.TimestampUtils;
import com.alibaba.polardbx.rpc.result.XResultUtil;
import com.google.common.base.Preconditions;
import org.apache.orc.OrcProto;
import org.apache.orc.impl.OrcIndex;

import java.io.IOException;
import java.time.ZoneId;

import static com.alibaba.polardbx.rpc.result.XResultUtil.ZERO_TIMESTAMP_LONG_VAL;

public class TimestampColumnReader extends AbstractLongColumnReader {

    private final ZoneId zoneId;

    public TimestampColumnReader(int columnId, boolean isPrimaryKey,
                                 StripeLoader stripeLoader,
                                 OrcIndex orcIndex,
                                 RuntimeMetrics metrics,
                                 OrcProto.ColumnEncoding.Kind kind, int indexStride,
                                 boolean enableMetrics, ExecutionContext context) {
        super(columnId, isPrimaryKey, stripeLoader, orcIndex, metrics, kind, indexStride, enableMetrics);
        this.zoneId = TimestampUtils.getZoneId(context);
    }

    @Override
    public void next(RandomAccessBlock randomAccessBlock, int positionCount) throws IOException {
        Preconditions.checkArgument(isOpened.get());
        Preconditions.checkArgument(!openFailed.get());
        Preconditions.checkArgument(randomAccessBlock instanceof TimestampBlock);
        init();

        long start = System.nanoTime();

        long[] packed = ((TimestampBlock) randomAccessBlock).getPacked();
        boolean[] nulls = randomAccessBlock.nulls();

        if (present == null) {
            randomAccessBlock.setHasNull(false);
            for (int i = 0; i < positionCount; i++) {
                // no null value.
                long longVal = data.next();
                long datetime = parseTimestamp(longVal, zoneId);
                packed[i] = datetime;
                lastPosition++;
            }

            // destroy null array to save the memory.
            ((Block) randomAccessBlock).destroyNulls(true);
        } else {
            randomAccessBlock.setHasNull(true);
            // there are some null values
            for (int i = 0; i < positionCount; i++) {
                if (present.next() != 1) {
                    // for present
                    nulls[i] = true;
                    packed[i] = 0;
                } else {
                    // if not null
                    long longVal = data.next();
                    long datetime = parseTimestamp(longVal, zoneId);
                    packed[i] = datetime;
                }
                lastPosition++;
            }
        }

        // metrics
        if (enableMetrics) {
            parseTimer.inc(System.nanoTime() - start);
        }
    }

    private static long parseTimestamp(long timestamp, ZoneId zoneId) {
        if (timestamp == ZERO_TIMESTAMP_LONG_VAL) {
            return TimeStorage.writeTimestamp(MysqlDateTime.zeroDateTime());
        } else {
            MySQLTimeVal mySQLTimeVal = XResultUtil.longToTimeValue(timestamp);
            MysqlDateTime mysqlDateTime =
                MySQLTimeConverter.convertTimestampToDatetime(mySQLTimeVal, zoneId);
            return TimeStorage.writeTimestamp(mysqlDateTime);
        }
    }
}