{:title "Interactie core.async"
 :layout :post
 :draft? true
 :tags  ["clojure" "vim" "editor"]}

**Code samples are live and edibtable enabled by [Klipse](http://github.com/viebel/klipse)**

<div class="language-klipse" 
data-external-libs="https://raw.githubusercontent.com/master/clojure/core.async/src/main/clojure/cljs"
style="visibility: hidden;"> 
    (ns example.blog)
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


    (ns example.blog$macros
      (:require-macros [cljs.core.async.macros :refer [go]])
      (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]))

    (defmacro go-loop [& body]
      `(go (let [#open? (new-thread!)]
             (while (#open?)
               ~@body))))

    (ns example.blog)

    (def dom (atom -1))

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

**This repl waits a 1/2 second to re-eval**
<div data-preamble="(reset! dom 0)(reset-threads! 3)" class="language-klipse" data-idle-msec="500"> 
    (go (let [open? (new-thread!)]
            (while (open?)
             (<! (timeout 400))
             (render! (dot 30))
             (<! (timeout 200))
             (render! (dot 20)))))
</div>
<div class="code-output"></div>


<link rel="stylesheet" type="text/css" href="http://app.klipse.tech/css/codemirror.css"> 
</link>

<script>
window.klipse_settings = {
selector: ".language-klipse"
};
</script>
<script src="http://app.klipse.tech/plugin/js/klipse_plugin.js"></script>
