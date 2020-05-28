(ns my.example.other.mount.log
  (:require [mount.core :as mount :refer [defstate]]))

;; "гвоздями" прибитые импорты объектов состояния из других ns
(defstate ^:dynamic *log*
  :start (do
           (println "starting log..." )
           {:log-obj "log"})

  :stop (do
          (println "stopping log...")))


(comment
  (mount/start #'*log*)
  *log*
  (mount/stop #'*log*))

