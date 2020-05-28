(ns my.example.other.mount.config
  (:require [mount.core :as mount :refer [defstate]]))

(defstate ^:dynamic *config*
  :start {:db    {:host "localhost" :port 5432 :user "admin" :password "******"}
          :web   {:host "localhost", :port 8080}
          :cache []}
  :stop nil)

(comment
  (mount/start #'*config*))
