(ns user
  (:require [nextjournal.clerk :as clerk]
            [clojure.repl :as repl]
            [clojure.repl.deps :as deps]
            [tech.v3.dataset :as ds]
          		[tech.v3.io :as io]
          		[tech.v3.libs.parquet :as parquet]))

(defn serve! []
  (clerk/serve! {:browse true :port 6677 :watch-paths ["notebooks"]}))

(defn copy-uri-to-file [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(comment
  (serve!)
  (clerk/halt!)
  (clerk/clear-cache!)
  (clerk/build! {:paths ["notebooks/data-formats.md"]})
  )