{:title "Interactive Sierpinski Triangles"
 :layout :post
 :tags  ["clojure" "graphics" "fractals"]}

Some of my earliest memories of the internet are getting lost on endless wikipedia articles, usually some sort of mathematics. One of my fondest memories of this time was the week I discovered fractals. I dove around through each style of fractal with such excitement. This was the first moment I can remember finding joy in mathematics. Little did I know I was learning the ideas of recursive I'd be putting to use later in my career. To pay homage here is a small tribute, some interactive clojure code of one of the simplest fractals, The sierpinksi triangle.

**Code samples are live and edibtable enabled by [Klipse](http://github.com/viebel/klipse)**

<div class="language-klipse "> 
```clojure
(defn mid-point [p1 p2]
  (map #(/ (+ % %2) 2) p1 p2))

(defn next-triangles [[top left right]]
  [[top (mid-point top left) (mid-point top right)]
   [(mid-point left top) left (mid-point left right)]
   [(mid-point right top) (mid-point left right) right]])

(def canvas (js/document.getElementById "render"))
(doto (.getContext canvas "2d")
  (aset "strokeStyle" "black")
  (.clearRect 0 0 (.-width canvas) (.-height canvas)))

(defn draw-triangle [canvas [[x1 y1] [x2 y2] [x3 y3]]]
  (doto (.getContext canvas "2d")
    (.beginPath)
    (.moveTo x1 y1)
    (.lineTo x2 y2)
    (.lineTo x3 y3)
    (.lineTo x1 y1)
    (.stroke)
    (.closePath)))

(defn draw-sierp [canvas initial n]
  (when (> n 0)
    (doseq [triangle (next-triangles initial)]
      (draw-triangle canvas triangle)
      (draw-sierp canvas triangle (dec n)))))

(draw-sierp canvas [[0 500] [250 0] [500 500]] 6)
```
</div>
<canvas width="500" height="500" id="render"></canvas>

<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> 
</link>

<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>
