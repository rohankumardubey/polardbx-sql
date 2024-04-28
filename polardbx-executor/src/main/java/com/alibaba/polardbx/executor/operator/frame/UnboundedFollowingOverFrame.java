package com.alibaba.polardbx.executor.operator.frame;

import com.alibaba.polardbx.executor.operator.util.ChunksIndex;
import com.alibaba.polardbx.optimizer.core.expression.calc.Aggregator;

import java.util.List;

/**
 * The UnboundedFollowing window frame.
 */
public abstract class UnboundedFollowingOverFrame implements OverWindowFrame {

    protected List<Aggregator> aggregators;
    protected ChunksIndex chunksIndex;

    public UnboundedFollowingOverFrame(List<Aggregator> aggregators) {
        this.aggregators = aggregators;
    }

    @Override
    public void resetChunks(ChunksIndex chunksIndex) {
        this.chunksIndex = chunksIndex;
    }

    @Override
    public List<Aggregator> getAggregators() {
        return aggregators;
    }
}