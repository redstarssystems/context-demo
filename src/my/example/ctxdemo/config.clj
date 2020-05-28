(ns my.example.ctxdemo.config
  (:require [environ.core :refer [env]]
            [io.pedestal.log :as log]
            [clojure.string :as str]))

;; expected Environment variables
(def expected-env-list [:http-host
                        :http-port
                        :http-disable-fingerprint

                        :app-name
                        :app-instance
                        :app-group
                        :app-region-dc

                        :jvm-dump-folder

                        :admin-networks])

(defn coerce-env
  "Coerce parameters from environment variables map.
  Returns:
    * `env` - map with coerced parameters."
  [env]
  (log/info :m "Coercing system parameters...")
  ;; add your coercing code here
  (assoc env :http-port (Integer/parseInt (:http-port env))
             :admin-networks (into [] (remove str/blank? (str/split (:admin-networks env) #"(\s*\,+\s*)|\s+")))
             :http-disable-fingerprint (Boolean/parseBoolean (:http-disable-fingerprint env))))

(defn validate-env
  "Validate system parameters from environment variables.
  Params:
    * `env` - map with environment variables in environ style.
  Returns:
    * `env` - validated env if success, or Exception with explanation if validation fails."
  [env]
  (log/info :m "Validating system parameters...")
  env)

