package org.usfirst.frc.team4322.motion


class Path {

    class KdTree {

        data class RectHV(var xmin: Double = 0.0, var ymin: Double = 0.0, var xmax: Double = 0.0, var ymax: Double = 0.0) {

            fun distanceSquaredTo(p: Waypoint): Double {
                var dx = 0.0
                var dy = 0.0
                if (p.x < xmin)
                    dx = p.x - xmin
                else if (p.x > xmax) dx = p.x - xmax
                if (p.y < ymin)
                    dy = p.y - ymin
                else if (p.y > ymax) dy = p.y - ymax
                return dx * dx + dy * dy
            }

            operator fun contains(p: Waypoint): Boolean {
                return (p.x in xmin..xmax && p.y in ymin..ymax)
            }

            fun intersects(that: RectHV): Boolean {
                return (this.xmax >= that.xmin && this.ymax >= that.ymin
                        && that.xmax >= this.xmin && that.ymax >= this.ymin)
            }
        }

        private var size: Int = 0
        private var root: KdNode? = null

        inner class KdNode(val p: Waypoint, val r: RectHV) {
            var left: KdNode? = null  // left is also down
            var right: KdNode? = null // right is also up
        }

        init {
            size = 0
            root = null
        }

        fun insert(p: Waypoint) {
            root = insert(root, p, 0.0, 0.0, 1.0, 1.0, true)
        }

        operator fun contains(p: Waypoint): Boolean {
            return contains(root, p, true)
        }

        fun nearest(p: Waypoint): Waypoint? {
            return if (root == null) null else nearest(root, p, root!!.p, true)
        }

        private fun insert(node: KdNode?, p: Waypoint, x0: Double, y0: Double,
                           x1: Double, y1: Double, xcmp: Boolean): KdNode {
            if (node == null) {
                size++
                val r = RectHV(x0, y0, x1, y1)
                return KdNode(p, r)
            } else if (node.p == p) return node
            if (xcmp) {
                val cmp = p.x - node.p.x
                if (cmp < 0)
                    node.left = insert(node.left, p, x0, y0, node.p.x, y1, !xcmp)
                else
                    node.right = insert(node.right, p, node.p.x, y0, x1, y1, !xcmp)
            } else {
                val cmp = p.y - node.p.y
                if (cmp < 0)
                    node.left = insert(node.left, p, x0, y0, x1, node.p.y, !xcmp)
                else
                    node.right = insert(node.right, p, x0, node.p.y, x1, y1, !xcmp)
            }
            return node
        }

        private fun range(node: KdNode?, rect: RectHV, q: MutableList<Waypoint>) {
            if (node == null) return
            if (rect.contains(node.p)) {
                q.add(node.p)
            }
            if (rect.intersects(node.r)) {
                range(node.left, rect, q)
                range(node.right, rect, q)
            }
        }

        private fun contains(node: KdNode?, p: Waypoint, xcmp: Boolean): Boolean {
            when {
                node == null -> return false
                node.p == p -> return true
                else -> return if (xcmp) {
                    val cmp = p.x - node.p.x
                    if (cmp < 0)
                        contains(node.left, p, !xcmp)
                    else
                        contains(node.right, p, !xcmp)
                } else {
                    val cmp = p.y - node.p.y
                    if (cmp < 0)
                        contains(node.left, p, !xcmp)
                    else
                        contains(node.right, p, !xcmp)
                }
            }
        }

        private fun nearest(node: KdNode?, p: Waypoint, c: Waypoint, xcmp: Boolean): Waypoint {
            var closest = c
            if (node == null) return closest
            if (node.p.distanceSquaredTo(p) < closest.distanceSquaredTo(p))
                closest = node.p
            if (node.r.distanceSquaredTo(p) < closest.distanceSquaredTo(p)) {
                val near: KdNode?
                val far: KdNode?
                if (xcmp && p.x < node.p.x || !xcmp && p.y < node.p.y) {
                    near = node.left
                    far = node.right
                } else {
                    near = node.right
                    far = node.left
                }
                closest = nearest(near, p, closest, !xcmp)
                closest = nearest(far, p, closest, !xcmp)
            }
            return closest
        }
    }

    data class Waypoint(var x: Double = 0.0, var y: Double = 0.0, var heading: Double = 0.0) {
        var currentCurvature: Double = 0.0
        val distanceFromStart: Double
            get() = Math.sqrt(x * x + y * y)
        var nextWaypoint: Waypoint? = null

        fun distanceSquaredTo(other: Waypoint): Double {
            val dx = (other.x - x)
            val dy = (other.y - y)
            return dx * dx + dy * dy
        }
    }


}