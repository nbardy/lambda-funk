{:title "Random Polygons"
 :layout :post
 :tags  ["clojure" "interactive" "visual"]}

**Code samples are live and edibtable enabled by [Klipse](http://github.com/viebel/klipse)**

I was working on another blog post involving polygons and realized how nice it would be to have some random polygons to test with. Turns out its a interesting little problem and now its become a post of its own.
<pre style="display:none">
<div class="language-klipse">
```clojure
(def canvas (atom 0))
(defn get-canvas [id]
  (js/document.getElementById (str "canvas-" @canvas)))

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
```
</div>
</pre>
Let's start with generating a regular polygon. This is a perfect problem for polar coordinates. A polygon can be defined as a set of polar points with equal radius and equally divided angles.

<div class="language-klipse">
```clojure
(defn polar-polygon [edges radius]
  (mapv #(vector (* % (/ (* 2 js/Math.PI) edges)) radius)
    (range edges)))

(polar-polygon 5 70)
```
</div>

It works, but most graphics programs work with rectangular coordinates. Let's write a quick function to transform polar polygons to rectangular polygons with their center at point `[x y]`.
<div class="language-klipse">
```clojure
(defn to-rect 
  ([[theta radius]] (to-rect [0 0] [theta radius]))
  ([[x y] [theta radius]]
    [(+ x (* radius (js/Math.cos theta)))
     (+ y (* radius (js/Math.sin theta)))]))

(defn regular-polygon 
  ([edges] (regular-polygon edges 100))
  ([edges radius] (regular-polygon edges [radius radius] radius))
  ([edges center radius] 
    (mapv (partial to-rect center) (polar-polygon edges radius))))

(regular-polygon 5)
```
</div>

And render it.

<div class="language-klipse" data-preamble="(reset-canvas! 4)">
```clojure
(render-polygon! (regular-polygon 7))
```
</div>
<canvas width="400" height="200" id="canvas-4"></canvas>

Or render a few!

<div class="language-klipse" data-preamble="(reset-canvas! 5)">
```clojure
(render-polygon! (regular-polygon 9)              {:color "green"})
(render-polygon! (regular-polygon 5 [260 50]  50) {:color "red"})
(render-polygon! (regular-polygon 3 [310 130] 40) {:color "blue"})
```
</div>
<canvas width="400" height="200" id="canvas-5"></canvas>

Now to make a random polygon. Take that regular one and add some random noise. We'll modify the original function to add some randomness to the radius and theta. But only enough so the points don't overlap avoiding [complex](https://en.wikipedia.org/wiki/Complex_polygon) polygons.

<div class="language-klipse" data-preamble="(reset-canvas! 6)">
```clojure
(defn random-polar-polygon [edges radius]
  (let [angle-increment (/ (* 2 js/Math.PI) edges)]
    (mapv 
      (fn [edge] 
        [(+ (* angle-increment edge) (* (rand) angle-increment))
         (* (rand) radius)])
      (range edges))))

(defn random-polygon 
  ([n] (random-polygon n 100))
  ([n radius] (random-polygon n [radius radius] radius))
  ([n center radius] 
    (mapv (partial to-rect center) (random-polar-polygon n radius))))

(render-polygon! (random-polygon 9)               {:color "green"})
(render-polygon! (random-polygon 7  [260 50]  50) {:color "red"})
(render-polygon! (random-polygon 13 [310 130] 40) {:color "blue"})
```
</div>
<canvas width="400" height="200" id="canvas-6"></canvas>

Hmm... Too pointy. It would be better if the shapes were a little closer to their regular self. Adjusting the distribution and not the range will allow us to continue generating a wide set of polygons, but make it more likely to generate polygons seen in the real world. The `pow` function map values `[0,1] => [0,1]` when applied to the uniform random distribution provides a skew. Using this we can skew our random noise to generate a more standard looking set of polygons.

<div class="language-klipse" data-preamble="(reset-canvas! 7)">
```clojure
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

(render-polygon! (random-polygon 9)               {:color "green"})
(render-polygon! (random-polygon 7  [260 50]  50) {:color "red"})
(render-polygon! (random-polygon 13 [310 130] 40) {:color "blue"})
```
</div>
<canvas width="400" height="200" id="canvas-7"></canvas>

Much better. With a little bit of math and we've got some nice looking random polygons.

<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> 
</link>

<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>

