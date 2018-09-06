package org.gearvrf.widgetlib.widget.animation;

import android.util.Pair;

import org.gearvrf.widgetlib.log.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.gearvrf.utility.Threads.spawn;

public final class SimpleAnimationTracker {
    private final ConcurrentHashMap<GVRSceneObject, Pair<GVRAnimation, GVROnFinish>> tracker;
    private final LinkedBlockingQueue<AnimationRequest> requestQueue;

    public final Runnable clearTracker = new Runnable() {

        @Override
        public void run() {
            clear();
        }
    };

    /**
     * Creates SimpleAnimationTracker
     */
    public SimpleAnimationTracker(GVRContext gvrContext) {
        tracker = new ConcurrentHashMap<GVRSceneObject, Pair<GVRAnimation, GVROnFinish>>();
        requestQueue = new LinkedBlockingQueue<AnimationRequest>();
        // start animation request worker thread
        spawn(new AnimationWorker(requestQueue));
    }


    public void clear() {
        if (tracker != null) {
            tracker.clear();
        }
        if (requestQueue != null) {
            requestQueue.clear();
        }
    }

    public void interrupt(final GVRSceneObject target) {
        stop(target, tracker.remove(target));
    }


    public boolean interruptAll() {
        boolean ret = false;
        for (GVRSceneObject target: tracker.keySet()) {
            interrupt(target);
            ret = true;
        }
        return ret;
    }

    public boolean inProgress(final GVRSceneObject target) {
        return tracker.containsKey(target);
    }

    private void stop(final GVRSceneObject target, final Pair<GVRAnimation, GVROnFinish> pair) {
        if (null != pair) {
            target.getGVRContext().getAnimationEngine().stop(pair.first);
            Log.v(TAG, "stopping running animation for target %s",
                  target.getName());
            runUserFinisher(pair);
        }
    }

    public void track(final GVRSceneObject target, final GVRAnimation anim) {
        track(target, anim, null, null);
    }

    public void track(final GVRSceneObject target, final GVRAnimation anim, final GVROnFinish finisher) {
        track(target, anim, null, finisher);
    }

    public void track(final GVRSceneObject target, final GVRAnimation anim, final Runnable starter, final GVROnFinish finisher) {
        if (target == null || anim == null) {
            return;
        }
        // add request to the queue
        try {
            requestQueue.put(new AnimationRequest(target, anim, starter, finisher));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, e, "track()");
        }
    }

    class AnimationRequest {
        private GVRAnimation anim;
        private GVROnFinish finisher;
        private Runnable starter;
        private GVRSceneObject target;
        AnimationRequest(final GVRSceneObject target, final GVRAnimation anim, final Runnable starter, final GVROnFinish finisher) {
            this.target = target;
            this.anim = anim;
            this.finisher = finisher;
            this.starter = starter;
        }

        void process() {
            final Pair<GVRAnimation, GVROnFinish> pair;
            pair = tracker
                        .put(target, new Pair<GVRAnimation, GVROnFinish>(
                                anim, finisher));

            stop(target, pair);

            anim.setOnFinish(new GVROnFinish() {
                @Override
                public final void finished(final GVRAnimation animation) {
                    final Pair<GVRAnimation, GVROnFinish> pair;
                    pair = tracker.remove(target);
                    if (null != pair) {
                        runUserFinisher(pair);
                    }
                }
            });

            if (starter != null) {
                starter.run();
            }
            anim.start(target.getGVRContext().getAnimationEngine());
        }
    }

    private static class AnimationWorker implements Runnable {
        private final LinkedBlockingQueue<AnimationRequest> queue;

        public AnimationWorker(LinkedBlockingQueue<AnimationRequest> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Get the next animation request item off of the queue
                    AnimationRequest request = queue.take();

                    // Process animation request
                    if (request != null) {
                        request.process();
                    }
                }
                catch ( InterruptedException ie ) {
                    break;
                }
            }
        }
    }

    private void runUserFinisher(final Pair<GVRAnimation, GVROnFinish> pair) {
        if (null != pair.second) {
            try {
                pair.second.finished(pair.first);
            } catch (final Exception e) {
                Log.e(TAG, "exception in finisher", e);
            }
        }
    }

    private static final String TAG = "SimpleAnimationTracker";
}
