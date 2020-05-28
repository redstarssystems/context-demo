(ns my.example.ctxdemo.system
  (:require [environ.core :refer [env]]
            [org.rssys.context.core :as context]
            [io.pedestal.log :as log]
            [my.example.ctxdemo.web.server :as server]
            [my.example.ctxdemo.config :as config]
            [org.rssys.context.core :as ctx]))


(defn new-context
  "Build a new system context using variables from OS.
  Returns:
    * `atom` - a new system context as map in atom."
  [env]
  (let [*new-ctx   (atom {})
        system-map [{:id         :cfg
                     :config     (zipmap config/expected-env-list ((apply juxt config/expected-env-list) env))
                     :start-deps #{}
                     :start-fn   #(-> % config/coerce-env config/validate-env)
                     :stop-fn    #(do % nil)}

                    ;; add system components here

                    {:id         :web
                     :config     (fn [ctx] (-> ctx :context/components :cfg :state-obj))
                     :start-deps #{:cfg}
                     :start-fn   (fn [config] (server/run (server/app *new-ctx) config))
                     :stop-fn    server/stop}
                    ]
        ]
    (context/build-context *new-ctx system-map)))


(comment
  (def *ctx (new-context env))
  (def *ctx2 (new-context env))
  (context/set-config! *ctx2  :web {:admin-networks ["127.0.0.0/8" "192.168.0.0/16"],
                                :app-region-dc "local dc",
                                :jvm-dump-folder "/tmp/",
                                :app-instance "002",
                                :app-name "my.example.ctxdemo2",
                                :app-group "my platform",
                                :http-port 8081,
                                :http-host "127.0.0.1",
                                :http-disable-fingerprint true})

  (context/start-all *ctx)
  (context/start-all *ctx2)

  (context/stop-all *ctx)
  (user/reload-code *ctx)
  (user/reload-code *ctx :web)
  ((server/app *ctx) {:request-method :get :uri "/monitoring/id" :headers {:accept "application/edn"}})
  ((server/app *ctx) {:request-method :get :uri "/monitoring/id" :headers {:accept "text/plain"}})
  ((server/app *ctx) {:request-method :get :uri "/monitoring/debug" :headers {:accept "application/json"}})
  ((server/app *ctx) {:request-method :get :uri "/monitoring/debug" :headers {:accept "application/transit+json"}})
  ((server/app *ctx) {:request-method :get :uri "/monitoring/heap_dump/download" :query-string "filename=1588888455543.hprof"})
  (require 'clojure.inspector)
  (clojure.inspector/inspect-tree (:context/components @*ctx))
  )

