{:title "Interpolating Polygons"
 :layout :post
 :draft? true
 :tags  ["clojure" "math" "interactive" "visual"]}

Note for all blog posts: Make preamble hideable
NOTE: Take rect from other project

I could edit the polygon and add points, but I hate doing tedious things. I'd much rather write a program to do that for me.

<div class="language-klipse">
```clojure

(def animation-duration 1000)
(def pre-pause 500)
(def post-pause 500)

(defn get-time [] (js/Date.now))

(def canvas (atom 0))
(def animations (atom {}))
(defn get-canvas [id]
  (js/document.getElementById (str "canvas-" @canvas)))

(defn render-polygon! 
 ([polygon] (render-polygon! @canvas polygon))
 ([canvas-id polygon]
  (let [canvas (get-canvas canvas-id)
        [[initial-x initial-y] & rest] polygon
        context (.getContext canvas "2d")]
        (doto context
          (.clearRect 0 0 (.-width canvas) 
                          (.-height canvas))
          (.beginPath)
          (.moveTo context initial-x initial-y))
        (doseq [[x y] rest]
          (.lineTo context x y))
        (doto context
          (.lineTo initial-x initial-y)
          (.fill)))
  polygon))

(defn animate
  ([initial final interpolation-fn] 
    ; Mark animation for canvas 
    (swap! animations assoc @canvas [initial final interpolation-fn])
    (render-polygon! (interpolation-fn initial final 0))
    (animate initial final interpolation-fn (get-time) @canvas)
    initial)
  ([initial final interpolation-fn start-time canvas]
    (let [total-time (+ pre-pause post-pause animation-duration)
          percent 
          (-> (get-time) (- start-time) 
              (mod total-time)
              (- pre-pause)
              (/ animation-duration)
              (max 0) (min 1))]
    (render-polygon! (get @animations [initial final interpolation-fn])
      (interpolation-fn initial final percent))
    ; Do not repeat animation if a new one has been started
    (if (= (get @animations canvas) [initial final interpolation-fn])
      (js/requestAnimationFrame 
        (fn [_] (animate initial final interpolation-fn start-time canvas)))))))

(defn rect 
  ([width height] (rect 0 0 width height))
  ([x y width height]
    [[x y]          
    [(+ x width) y] 
    [(+ x width) (+ y height)] 
    [x (+ y height)]]))
```
</div>

<div class="language-klipse" data-preamble="(reset! canvas 4)">
```clojure
(render-polygon! (rect 10 10 30 30))
```
</div>
<canvas width="400" height="200" id="canvas-4"></canvas>


<div class="language-klipse">
```clojure
(defn regular-ngon 
  ([n] (regular-ngon n 100))
  ([n radius] (regular-ngon n radius radius radius))
  ([n x y radius]
    (mapv (partial to-rect [x y])
      (mapv (fn [theta] [theta radius]) 
            (range 0 (* 2 js/Math.PI) (/ (* 2 js/Math.PI) n))))))

(regular-ngon 5)
```
</div>

<div class="language-klipse" data-preamble="(reset! canvas 2)" >
```clojure
(render-polygon! (regular-ngon 4))
```
</div>
<canvas width="400px" height="200" id="canvas-2"></canvas>

<div class="language-klipse">
```clojure
(defn weighted-average [percent v1 v2]
  (+ (* (- 1 percent) v1) (* percent v2)))

(defn average-polygons [initial final percent]
  (map (partial map #(weighted-average percent %1 %2)) initial final))

(defn copy-line [points n]
  (->> (repeat points)
       (mapcat identity)
       (take n)))

(defn copy-points [points new-size]
  (let [size (count points)
        points-needed (- size new-size)
        additional-indices (range 0 new-size (/ size points-needed))
        original-point-indices (range 0 (count points))
        all-indices (sort (concat original-point-indices additional-indices))]
    (mapv (fn [index] (nth points (js/Math.floor index)))
          all-indices)))

(average-polygons (rect 100 200) (rect 200 100) 0.5)
```
</div>

; How about instead of throwing away information we take advange of it.
Partial index access

<div class="language-klipse">
```clojure

(defn copy-last-point [points n]
  (take n (concat points (repeat (last points)))))

(defn interpolation-fn [fix]
  (fn [initial final percent]
    (let [size (max (count initial) (count final))]
      (average-polygons (fix initial size)
                        (fix final   size)
                        percent))))

(def post-interpolate (interpolation-fn copy-last-point))
(def copy-inter (interpolation-fn copy-line))
(def pre-interpolate (interpolation-fn copy-last-point))
```
</div>

<div class="language-klipse" data-preamble="(reset! canvas 5)" >
```clojure
(animate (rect 100 200) (rect 200 100) copy-inter)
```
</div>
<canvas width="400px" height="200" id="canvas-5"></canvas>

PS

Interpolating polygons
----------------------
[[] [] [] []]
[[] [] []] 

Copy last point n times where n = point differenial.
Find closest point along polygon and add point.


<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> 
</link>



<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>

