(ns user
  (:require [clojure.tools.namespace.repl]
            [org.rssys.context.core :as context]))

;; debug print with #p
(require 'hashp.core)

;; debug fns and s-exps with dbg, dbgn, dbg-last
(use 'debux.core)

;; dynamic loading of libraries, e.g. (deps/add-lib 'philoskim/debux {:mvn/version "0.6.3"})
(require '[clj-deps.core :as deps])

;; set global dev flag, which can be checked in runtime
;; (when (resolve 'user/dev-mode) user/dev-mode)
(def dev-mode true)

(println (pr-str {:msg "development mode" :status (if (true? dev-mode) "on" "off")}) \newline)

(defn reload-code
  "Reload system after change some ns. Use in dev mode only"
  ([*ctx]
   (println (format "reloading ns: %s" (.getName *ns*)))
   (use (.getName *ns*) :reload-all)
   (println "stopping all...")
   (context/stop-all *ctx)
   (println "starting all...")
   (context/start-all *ctx))
  ([*ctx component]
   (println (format "reloading ns: %s" (.getName *ns*)))
   (use (.getName *ns*) :reload-all)
   (println "component stopping..")
   (context/stop! *ctx component)
   (println "starting component...")
   (context/start! *ctx component)))
