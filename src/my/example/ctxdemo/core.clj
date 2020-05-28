(ns my.example.ctxdemo.core
  (:gen-class)
  (:require [environ.core :refer [env]]
            [org.rssys.context.core :as context]
            [unifier.response :as r]
            [io.pedestal.log :as log]
            [my.example.ctxdemo.system :as system]))

(defn set-global-exception-hook
  "Catch any uncaught exceptions and print them."
  []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException [_ thread ex]
        (println "uncaught exception" :thread (.getName thread) :desc ex)))))

(defn set-os-signals-hook
  "Catch SIGTERM, SIGINT and other OS signals. Executes given f function. Returns nil."
  [f]
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
                                                    (log/info :msg "Got shutdown signal from OS.")
                                                    (f)
                                                    (shutdown-agents)
                                                    (log/info :msg "System is stopped.")))))

(defn -main
  "entry point to program."
  [& args]

  (set-global-exception-hook)
  ;; use `direnv` utility to load .env file for environ
  (let [*ctx (system/new-context env)]
    (set-os-signals-hook #(context/stop-all *ctx))
    (if (r/success? (context/start-all *ctx))
      (log/info :m "System is started. Press <Ctrl-C> to stop.")
      (do (log/error :m "System is NOT started. Exit...") (System/exit -1)))))
