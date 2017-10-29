(defproject cryogen "0.1.0"
  :description "Simple static site generator"
  :url "https://github.com/lacarmen/cryogen"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [ring/ring-devel "1.5.0"]
                 [prismatic/schema "1.1.3"]
                 [camel-snake-kebab "0.4.0"]
                 [compojure "1.5.1"]
                 [ring-server "0.4.0"]
                 [cryogen-markdown "0.1.4"]
                 [cryogen-core "0.1.59"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-figwheel "0.5.8"]]
  :resource-paths ["resources"]
  :cljsbuild
  {:builds
   [{:id "post1-dev"
     :source-paths ["src/cljs"]
     :figwheel true
     :compiler
     {:source-map true
      :main "blog.post1"
      :output-dir "resources/public/blog/js/out"
      :asset-path "/blog/js/out"
      :output-to "resources/public/blog/js/blog-dev.js"}}
    {:id "production"
      :source-paths ["src/cljs"]
     :compiler
      {:optimizations :none
       :source-map false
       :modules
       {:blog1
        {:entries #{"blog.post1"}
         :output-to "resources/public/blog/js/post1.js"}}}}]}
  :main cryogen.core
  :ring {:init cryogen.server/init
         :handler cryogen.server/handler})
