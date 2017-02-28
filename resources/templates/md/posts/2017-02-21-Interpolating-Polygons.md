{:title "Interpolating Polygons"
 :layout :post
 :tags  ["clojure" "math" "interactive" "visual"]}

**Code samples are live and edibtable enabled by [Klipse](http://github.com/viebel/klipse)**

In writing a library for animation I stumbled upon the problem of how to animate polygons. Trying to create animations like this:

<canvas width="200" height="200" id="canvas-0"></canvas>

Generating an animation given an initial and final value relies on [interpolation](http://en.wikipedia.org/wiki/Interpolation) to find the intermediary values. Interpolation is simple when working with simple values like numbers, but becomes more complex as the data being interpolated becomes more complex. Polygons contain some features which make interpolating them a little interesting.


<pre style="display:none">
<div class="language-klipse">
```clojure
(def canvas (atom 0))
(defn get-canvas [id]
  (js/document.getElementById (str "canvas-" id)))

(defn clear-canvas! [canvas-id]
  (let [canvas (get-canvas canvas-id)
        context (.getContext canvas "2d")]
    (.clearRect context 0 0 (.-width canvas) 
                            (.-height canvas))))

(defn reset-canvas! [canvas-id]
  (reset! canvas canvas-id)
  (clear-canvas! canvas-id))

(defn render-polygon! 
 ([polygon] (render-polygon! @canvas polygon {}))
 ([polygon opts] (render-polygon! @canvas polygon opts))
 ([canvas-id polygon {:keys [color] :or {color "blue"}}]
  (let [canvas (get-canvas canvas-id)
        [[initial-x initial-y] & rest] polygon
        context (.getContext canvas "2d")]
        (doto context
          (aset "fillStyle" color)
          (.beginPath)
          (.moveTo context initial-x initial-y))
        (doseq [[x y] rest]
          (.lineTo context x y))
        (doto context
          (.lineTo initial-x initial-y)
          (.fill)))))

(def animation-duration 1000)
(def pre-pause 500)
(def post-pause 500)

(defn get-time [] (js/Date.now))

(def animations (atom {}))

(defn animate
  ([initial final interpolation-fn] 
    ; Mark animation for canvas 

    (swap! animations assoc @canvas [initial final interpolation-fn])
    (render-polygon! @canvas (interpolation-fn 0 initial final) {})
    (animate initial final interpolation-fn (get-time) @canvas)
    initial)
  ([initial final interpolation-fn start-time canvas-id]
    (let [total-time (+ pre-pause post-pause animation-duration)
          p 
          (-> (get-time) (- start-time) 
              (mod total-time)
              (- pre-pause)
              (/ animation-duration)
              (max 0) (min 1))]
    (clear-canvas! canvas-id)
    (render-polygon! canvas-id (interpolation-fn p initial final) {})
    ; Do not repeat animation if a new one has been started
    (if (= (get @animations canvas-id) [initial final interpolation-fn])
      (js/requestAnimationFrame 
        (fn [_] (animate initial final interpolation-fn start-time canvas-id)))))))

(defn to-rect 
  ([[theta radius]] (to-rect [0 0] [theta radius]))
  ([[x y] [theta radius]]
    [(+ x (* radius (js/Math.cos theta)))
     (+ y (* radius (js/Math.sin theta)))]))

(defn rect 
  ([width height] (rect 0 0 width height))
  ([x y width height]
    [[x y]          
    [(+ x width) y] 
    [(+ x width) (+ y height)] 
    [x (+ y height)]]))

(defn random-polar-polygon [edges radius]
  (let [radius-damper 0.34
        angle-damper  1.22
        angle-increment (/ (* 2 js/Math.PI) edges)]
    (mapv 
      (fn [edge] 
        [(+ (* angle-increment edge) 
            (* (js/Math.pow (rand) angle-damper) angle-increment))
         (* (js/Math.pow (rand) radius-damper) radius)])
      (range edges))))

(defn random-polygon 
  ([n] (random-polygon n 100))
  ([n radius] (random-polygon n [radius radius] radius))
  ([n center radius] 
    (mapv (partial to-rect center) (random-polar-polygon n radius))))

```
</div>
</pre>

An interpolation function will have the arguments `initial`, `final`, and `p`. Where `p` indicates the position of the value being returned. `p` is the distance from our intial value of the value being returned. Where `p=0` returns the intial value, and `p=1` indicates the final value. In mathmatical notation that is: 

`V = Vinitial*(1-p) + Vfinal*p`

Starting with the simplest case, consisting of two equal sized polygons. The problem can be broken down into interpolating a series of points. Each point can be broken down into interpolating two numbers.

<div class="language-klipse">
```clojure
(defn interpolate-number  [p initial final]
  (+ (* initial (- 1 p)) (* final p)))

(defn interpolate-points [p initial-point final-point]
  (mapv (partial interpolate-number p)
    initial-point final-point))

(interpolate-points 0.5 [0 0] [10 10])
```
</div>

Mapping over polygons gives the interpolation function as demonstrated with a quick test using some rectangles.

<div class="language-klipse">
```clojure
(defn interpolate-polygons [p initial final]
  (mapv (partial interpolate-points p) initial final))

(interpolate-polygons 0.5 (rect 0 0 10 10) (rect 20 0 10 10))
```
</div>

Along with a rendering to make things more clear. With each polygon as a different color to distinguish them from each other. Notice the polygon is in between the other two by size and position.

<div class="language-klipse" data-preamble="(reset! canvas 4)">
```clojure
(let [rect-1 (rect 0 0 30 30)
      rect-2 (rect 200 0 60 60)]
(render-polygon! rect-1 {:color "red"})
(render-polygon! (interpolate-polygons 0.5 rect-1 rect-2) {:color "green"})
(render-polygon! rect-2 {:color "blue"}))

```
</div>
<canvas width="400" height="50" id="canvas-4"></canvas>

Now let's look at this in the context of an animation. The animate function will render interpolated polygons for values `p = [0,1]`.
<div class="language-klipse" data-preamble="(reset-canvas! 6)" data-eval-idle-msec="500">
```clojure
(animate (rect 0 0 100 100) (rect 0 0 200 200) interpolate-polygons)
```
</div>
<canvas width="400" height="200" id="canvas-6"></canvas>

Another example making use of a random polygon function written in an [earlier blog post](http://lambdafunk.com/posts-output/2016-02-16-Random-Polygons).
<div class="language-klipse" data-preamble="(reset-canvas! 7)" data-eval-idle-msec="500">
```clojure
(animate (random-polygon 5) (random-polygon 5) interpolate-polygons)
```
</div>
<canvas width="400" height="200" id="canvas-7"></canvas>

As shown, interpolating polygons with the same number of points is straightforward. More interesting is developing a method for polygons of unequal sides. Adding points to the polygon with the lesser points allows reuse of the previous function. As long as the point added to the polygon are added are on the edges it will not change the shape of the original polygon until it becomes animated.

Using a higher-order function we can test this interpolation with a number of different approachs to resizing a polygon. Starting with a very simple approach of copying the last point as many times as needed.

<div class="language-klipse" data-preamble="(reset-canvas! 8)" data-eval-idle-msec="500">
```clojure
(defn fixed-interpolation-fn [resize]
  (fn [p initial final]
    (let [size (max (count initial) (count final))]
      (interpolate-polygons p 
        (resize initial size) 
        (resize final   size)))))

(defn copy-last-point [points n]
  (take n (concat points (repeat (last points)))))

(animate (random-polygon 7)
         (random-polygon 13)
         (fixed-interpolation-fn copy-last-point))

```
</div>
<canvas width="400" height="250" id="canvas-8"></canvas>

An interesting looking effect, but the points unfurling from one location doesn't have a natural animation feel. What does it look like if we repeat the points circularly and take as many as we need?

<div class="language-klipse" data-preamble="(reset-canvas! 9)" data-eval-idle-msec="500">
```clojure

(defn circular-resize [points n]
  (->> (repeat points)
       (mapcat identity)
       (take n)))

(animate (random-polygon 5)
         (random-polygon 13)
         (fixed-interpolation-fn circular-resize))
```
</div>
<canvas width="400" height="250" id="canvas-9"></canvas>

Even more interesting, it unfurls and crosses inside itself, but it looks even less organic. What's needed is a method to distribute the points across the polygon evenly. This can be done by creating a new polygon and getting the value for each point by accessing the corresponding index of the old polygon.

<div class="language-klipse" data-preamble="(reset-canvas! 10)" data-eval-idle-msec="500">
```clojure
(defn dispersed-resize [points new-size]
  (let [old-size (count points)
        new-polygon-indices (range 0 old-size (/ old-size new-size))]
    (mapv #(get points (mod (js/Math.round %) old-size)) 
      new-polygon-indices)))

(animate (random-polygon 5)
         (random-polygon 13)
         (fixed-interpolation-fn dispersed-resize))
```
</div>
<canvas width="400" height="250" id="canvas-10"></canvas>

Much better, the only problem remaining is we are still copying points and not adding new ones. This leads to the visual effect of points growing out of each other. Much nicer would be if the new points were placed somewhere between the old points, somewhere on the edge. Looking at the original function the culprit is using `round`. It is throwing away data that could be used. Using the non-integer value to find a new point interpolated between its neighbors should help accomplish this goal.

<div class="language-klipse" data-preamble="(reset-canvas! 11)" data-eval-idle-msec="500">
```clojure
(defn get-interpolated [points index] 
  (interpolate-points (mod index 1) 
    (get points (js/Math.floor index))
    (get points (mod (js/Math.ceil index) (count points)) 
         (last points))))

(defn resize-polygon [points final-size]
  (let [initial-size (count points)
        initial-indices (range initial-size)
        additional-points (- final-size initial-size)
        new-indices 
        (take additional-points
        (range (/ initial-size final-size) 
               js/Infinity (/ initial-size additional-points)))]
        (->> initial-indices
             (concat new-indices)
             (sort)
             (mapv (partial get-interpolated points)))))

(animate (random-polygon 4)
         (random-polygon 7)
         (fixed-interpolation-fn resize-polygon))

```
</div>
<canvas width="400" height="250" id="canvas-11"></canvas>

Looks good! There is definitely more you could do with the algorithm, but for now that's enough. Hope you enjoyed.

<pre style="display:none">
<div class="language-klipse" data-preamble="(reset-canvas! 0)" data-eval-idle-msec="500">
```clojure
(animate (random-polygon 3)
         (random-polygon 5)
         (fixed-interpolation-fn resize-polygon))
```
</div>
</pre>

<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> 
</link>

<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>

