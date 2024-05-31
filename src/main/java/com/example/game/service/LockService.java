package com.example.game.service;

import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service for managing distributed locks using Curator's InterProcessMutex.
 */
@Service
@RequiredArgsConstructor
public class LockService {

    private final CuratorFramework curatorFramework;
    private static final long LOCK_WAIT_TIME = 2L;

    /**
     * Executes a supplier within a distributed lock.
     *
     * @param lockPath the path of the lock
     * @param supplier the supplier to execute within the lock
     * @param <T>      the type of the result returned by the supplier
     * @return the result of the supplier
     * @throws RuntimeException if unable to acquire or release the lock
     */
    public <T> T executeWithLockSupplier(String lockPath, Supplier<T> supplier) {
        InterProcessMutex lock = createLock(lockPath);
        try {
            if (lock.acquire(LOCK_WAIT_TIME, TimeUnit.SECONDS)) {
                return supplier.get();
            } else {
                throw new RuntimeException("Unable to acquire lock for path: " + lockPath);
            }
        } catch (Exception e) {
            throw createLockException("acquire", lockPath, e);
        } finally {
            releaseLock(lock, lockPath);
        }
    }

    /**
     * Executes a runnable within a distributed lock.
     *
     * @param lockPath the path of the lock
     * @param runnable the runnable to execute within the lock
     * @throws RuntimeException if unable to acquire or release the lock
     */
    public void executeWithLock(String lockPath, Runnable runnable) {
        InterProcessMutex lock = createLock(lockPath);
        try {
            if (lock.acquire(LOCK_WAIT_TIME, TimeUnit.SECONDS)) {
                runnable.run();
            } else {
                throw new RuntimeException("Unable to acquire lock for path: " + lockPath);
            }
        } catch (Exception e) {
            throw createLockException("acquire", lockPath, e);
        } finally {
            releaseLock(lock, lockPath);
        }
    }

    InterProcessMutex createLock(String lockPath) {
        return new InterProcessMutex(curatorFramework, lockPath);
    }

    private void releaseLock(InterProcessMutex lock, String lockPath) {
        try {
            lock.release();
        } catch (Exception e) {
            throw createLockException("release", lockPath, e);
        }
    }

    private RuntimeException createLockException(String action, String lockPath, Exception e) {
        return new RuntimeException("Lock " + action + " interrupted for path: " + lockPath, e);
    }
}