package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class DeadlineEngineImpl implements DeadlineEngine {
    private final TreeMap<Long, Set<Long>> tasks = new TreeMap<>();
    private final HashMap<Long, Long> requestIdDeadLineMap = new HashMap<>();
    private long requestIdCounter = 0;

    @Override
    public long schedule(long deadlineMs) {
        tasks.computeIfAbsent(deadlineMs, k -> new HashSet<>()).add(++requestIdCounter);
        requestIdDeadLineMap.put(requestIdCounter, deadlineMs);
        return requestIdCounter;
    }

    @Override
    public boolean cancel(long requestId) {
        boolean isCancelled = false;
        Long deadLine = requestIdDeadLineMap.remove(requestId);
        if (deadLine != null) {
            Set<Long> requestIdSet = tasks.get(deadLine);
            if (requestIdSet != null) {
                isCancelled = requestIdSet
                        .remove(requestId);

                if (requestIdSet.isEmpty())
                    tasks.remove(deadLine);
            }
        }
        return isCancelled;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        int triggeredCount = 0;

        NavigableMap<Long, Set<Long>> triggeredMap = tasks.headMap(nowMs, true);

        if (maxPoll > 0) {
            for (Map.Entry<Long, Set<Long>> entry : triggeredMap.entrySet()) {
                Set<Long> requestIdSet = entry.getValue();
                Iterator<Long> iterator = requestIdSet.iterator();

                while (iterator.hasNext() && --maxPoll >= 0) {
                    handler.accept(iterator.next());
                    iterator.remove();
                    triggeredCount++;
                }

                if (requestIdSet.isEmpty())
                    triggeredMap.remove(entry.getKey());

                if (maxPoll <= 0)
                    break;
            }
        }

        return triggeredCount;
    }

    @Override
    public int size() {
        return requestIdDeadLineMap.size();
    }
}
