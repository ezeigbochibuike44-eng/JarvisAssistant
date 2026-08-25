package com.jarvis.assistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Legitimate, user-visible UI automation only. Performs actions strictly in response to a
 * command the user issued in this session - it never acts autonomously and never reads
 * password/secure-entry fields.
 */
class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No autonomous behavior: J.A.R.V.I.S. does not act on events by itself.
        // This callback exists only so the service can maintain window-state context
        // (e.g. "what's on screen right now") when a command explicitly asks for it.
    }

    override fun onInterrupt() {}

    fun performGoHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun performGoBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    /** Finds the first clickable node whose text or content description matches [label]. */
    fun clickNodeByLabel(label: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val match = findNode(root) { node ->
            !node.isPassword &&
                (node.text?.toString()?.contains(label, ignoreCase = true) == true ||
                    node.contentDescription?.toString()?.contains(label, ignoreCase = true) == true)
        } ?: return false

        return match.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun performScroll(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return performSwipeScroll(forward)
        val scrollable = findNode(root) { it.isScrollable } ?: return performSwipeScroll(forward)
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return scrollable.performAction(action)
    }

    /** Fallback gesture-based scroll for views that don't expose ACTION_SCROLL_*. */
    private fun performSwipeScroll(forward: Boolean): Boolean {
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2f
        val startY = if (forward) displayMetrics.heightPixels * 0.7f else displayMetrics.heightPixels * 0.3f
        val endY = if (forward) displayMetrics.heightPixels * 0.3f else displayMetrics.heightPixels * 0.7f

        val path = Path().apply {
            moveTo(centerX, startY)
            lineTo(centerX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    private fun findNode(
        root: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (predicate(root)) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findNode(child, predicate)
            if (found != null) return found
        }
        return null
    }
}
