package services.fetcher;

import app.E_Report;
import models.UserSession;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base fetcher that handles:
 * - Auto-refresh every 5 minutes
 * - Background-thread fetching (no UI freeze)
 * - Listener pattern for panels to react to data changes
 * - Filter-state memory so auto-refresh respects active filters
 *
 * Subclasses implement performFetch() with role-specific queries.
 */
public abstract class AbstractDashboardFetcher {
    protected final E_Report app;
    protected final UserSession us;

    private final Timer autoRefreshTimer;
    private final List<Runnable> dataChangeListeners = new CopyOnWriteArrayList<>();

    private volatile boolean isFetching = false;
    private volatile boolean pendingRefresh = false;

    public AbstractDashboardFetcher(E_Report app) {
        this.app = app;
        this.us = app.getUserSession();
        this.autoRefreshTimer = new Timer(300_000, e -> refreshNow()); // 5 min

        // Initial fetch after subclass constructor finishes, then start timer
        SwingUtilities.invokeLater(() -> {
            refreshNow();
            autoRefreshTimer.start();
        });
    }

    /** Force an immediate refresh. Safe to call from EDT (e.g. on filter apply). */
    public void refreshNow() {
        if (isFetching) {
            pendingRefresh = true;
            return;
        }
        isFetching = true;

        new Thread(() -> {
            try {
                performFetch();
                SwingUtilities.invokeLater(() -> {
                    isFetching = false;
                    fireDataChanged();
                    if (pendingRefresh) {
                        pendingRefresh = false;
                        refreshNow();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                isFetching = false;
            }
        }).start();
    }

    /**
     * Role-specific DB logic. Runs on a background thread — do NOT touch Swing
     * components here.
     */
    protected abstract void performFetch();

    /** Stop auto-refresh. Call this when the panel is disposed / user logs out. */
    public void stopAutoRefresh() {
        autoRefreshTimer.stop();
    }

    public void addDataChangeListener(Runnable listener) {
        dataChangeListeners.add(listener);
    }

    public void removeDataChangeListener(Runnable listener) {
        dataChangeListeners.remove(listener);
    }

    protected void fireDataChanged() {
        for (Runnable listener : dataChangeListeners) {
            try {
                listener.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected boolean isCurrentlyFetching() {
        return isFetching;
    }
}