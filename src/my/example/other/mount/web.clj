(ns my.example.other.mount.web
  (:require [mount.core :as mount :refer [defstate] ]))


;; "гвоздями" прибитые импорты объектов состояния из других ns
(defstate ^:dynamic *web*
  :start (do
           (println "starting cache..." (:cache config/*config*))
           {:cache-obj (:cache config/*config*)
            :db db/*db*})

  :stop (do
          (println "disconnect from cache...")))


(comment
  (mount/start #'*web*)
  *cache*
  (mount/stop #'*web*))
