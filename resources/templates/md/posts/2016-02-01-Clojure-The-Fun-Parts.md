{:title "Clojure: The Fun Parts"
 :layout :post
 :draft? true
 :tags  ["clojure"]}

Clojure, the fun parts
======================

Why this tutorial?
As the title of the most famous clojure book, [The Joy of Clojure](http://www.amazon.com/The-Joy-Clojure-Michael-Fogus/dp/1617291412/), states. The Clojure programming language can and does bring joy. It is one of the few languages that has causes me to smile not only because of what I create, but also how I create it. However, often this joy doesn't come until you have invested much time in the languages. So here are the fun parts.


We need to cover just a bit of clojure before we begin.
This is a hash-map {:a 2}
This is a vector [1 2 3]
parenthesis go on the outside in Clojure (+ 1 2)

(nth (filter prime? (range) 9)

<div class="language-klipse" 
data-external-libs="https://raw.githubusercontent.com/master/clojure/core.async/src/main/clojure/cljs"
style="visibility: hidden;"> 
    (ns example.blog
      (:require-macros [cljs.core.async.macros :refer [go]])
      (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]))
    (def dom (atom -1))
    (def thread-group (atom -))
    (defn reset-threads! [group-id]
     (reset! thread-group group-id)
     (aset js/window (str "tg" group-id) (js/Date.now)))
    (defn new-thread! []
      (let [started (aget js/window (str "tg" @thread-group))]
        (fn [] 
          (js/console.log 
           (aget js/window (str "tg" @thread-group)) started)
          (= (aget js/window (str "tg" @thread-group)) started))))

    (defn render! [content]
      (let [content 
            (if (seq? content) 
              (reduce str content)
              content)
            container 
            (aget (.getElementsByClassName js/document "code-output") @dom)]
        (aset container "innerHTML" content))
      content)
</div>

<div class="language-klipse hidden"> 
    (defn dot [x] 
    (str
    "<div style='width:" x "px;"
                "height:" x  "px;"
                "background-color: red;"
                "float: left;"
                "margin: 1px;"
                "'>"
    "</div>"))
</div>

<div data-preamble="(reset! dom 0)(reset-threads! 3)" class="language-klipse"> 
    (go (let [open? (new-thread!)]
            (while (open?)
             (<! (timeout 400))
             (render! (dot 30))
             (<! (timeout 200))
             (render! (dot 20)))))
</div>
<div class="code-output"></div>

(prime? 
This is already a pretty great approach to defining a prime, but lets
see what we can do composing something like this.

(defn nth-prime [n]
 (nth (filter prime? (range)) n))

(sum (map nth-prime (range 5)))
(sum (take 5 (filter prime? (range))))
(sum (take 5 primes))

When choosing tools or frameworks you are rarely getting a huge leap in solving small problems. It is the avoidance of technical debt that should be sought. A problem may be only slightly more difficult to reason about, but it is the combination of reasoning about multiple parts of your system which becomes complex.

(defn prime? [n] (not-any? #(= (mod n %) 0) (range 2 n))
(defn prime? [n] (not-any? #(= (mod n %) 0) (filter prime? (range 2 n))))

(declare prime?)
(def primes 
  (filter 
    (fn [n] (not-any? #(= (mod n %) 0) (drop 2 primes))) 
    (range)))

(defn prime? [n] (not-any? #(= (mod n %) 0) (drop 2 primes)))
(defn prime? [n] (not-any? #(= (mod n %) 0) pri es)) 

Lets wrap it all up by combining macros(destructuring) (functional programming), First-class interop.

(doseq [[x y] (map (juxt identity **) (take n (range)))]
  (if (prime? x)
    (draw-point! [x y] "blue")
    (draw-point! [x y] "red")))

Macros.
Destructuring
We introduce the concept of let.
(let [a 3]
 (+ a 7))
; 10

This is just a way to provide variable scoping. It may looks simple, but therein lies its power. This is a trend in Clojure. Many simple pieces come together to form powerful abstractions.

Now that we know let we can move on to the next step destructuring.
(let [[a b] [4 5]]
  [a b (+ a b)])
; [4 5 9]

Returns a vector of a a,b, and a+b. Pretty simple, but it introduces the idea of destructuring. With a simple sytnax we can very easily build and take apart groups of data.

Here is another neat feature of destructuring. We can deal with infinite and optional arguments, which works great combined with inifite, lazy sequences.

(let [[first second & more] (range)]
  second)

Destrucuturing isn't limited to just lets, we can use it in function arguments as well. We can also combine the techniques above and introduce a feature for destructuring optional hash arguments.  Here we'll write a function to generate the html for a square div. Using (hiccup)[http://github.com/weavejester/hiccup] to change our data structures into html.

(defn square [width & {:keys [[x y] :as pos] color]}]
  (html [:div {:style {:position "absolute"
                       :top (or x 0) :left (or y 0)
                       :width width :height width 
                       :background-color (or color "blue")}]))

We can call this function with expected results in many differnt ways.
; We just the necessary arguments
(square 200 200)
; With a color
(square 200 200 :color "blue")
; Or a position
(square 200 200 :pos [500 400])
; Or both
(square 200 200 :color "red :pos [20 20])
;Or change the order of our map.
(square 200 200 :pos [20 20] :color "red)

We are also provided with nesting. Which lets us pick apart something like a grid and do (determinants)[http://www.purplemath.com/modules/determs.htm] of a 2x2 matrix.

(let [[[a b] [c d]] [[11 12] 
                     [21 22]]]
  (- (* a d) (* c b)))


What follows from this is that we can take these ideas and combined them along with some others features which also allow 



We are going to add one last tweak here to show off the the beauty of if being a macro and not a piece of language syntax.

In an imperative language we would have our if setting a color variable like so. 

```ruby
if(x.isPrime?()) {
  color = "blue"
} else {
 color = "red"
}
```

Instead we are using if to decide which command to execute. If we look closer what we are actually doing is using if to decide which block of code should be passed back up to doseq which will then execute it. So if we look at if this we realize it is composable just like the functions we have written. So let's treat it as such.

(doseq [[x y] (map (juxt identity **) (take n (range)))]
    (draw-point! [x y] (if (prime? x) "red" "blue")))

(defn draw-graph [points]
  (doseq [[x y] points]
    (draw-point! x y (if (prime? x) "red" "blue"))))

(defn draw-graph [
(doseq [x (map(range n)]
  (draw-point! [x (js-func x)] (if prime? x) "red" "blue"))

Now our if code is deciding which block of code to pass back up to the draw-point function, the blocks of code in this case are just strings. draw-point accepts these strings and simply uses them as a color argument. The explanation way be a bit wordy, but the code itself is very simple and quite obvious.


      

(doseq [[x y] (map (juxt identity **) (take n (range)))]
  (if (prime? x)
    (draw-point! [x y] "blue")
    (draw-point! [x y] "red")))



The exclamation point just means there are side effects.

On core.async and local event loops



             (print dialogue)

             Declarating state change graphics.
             We store a list of states as render as a point in the transition.
             It is how to describe a state rather than describing a process.



<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> </link>

<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>


Footnotes
---------


