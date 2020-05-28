(ns my.example.other.mount.db
  (:require [mount.core :as mount :refer [defstate]]
            [my.example.other.mount.config :as config]))

;; механизм управления зависимостями неявный, через require ns
;; так сразу и не скажешь, сколько зависимостей у данного ns в части компонентов системы

(defstate ^:dynamic *db*
  :start (do
           (prn "connecting to database" (:db config/*config*))
           {:db-conn "obj"})

  :stop (println "disconnect from database"))

(comment
  (mount/start #'*db*)
  *db*
  (mount/stop #'*db*))
