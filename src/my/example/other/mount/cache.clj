(ns my.example.other.mount.cache
  (:require [mount.core :as mount :refer [defstate] ]
            [my.example.other.mount.db :as db]
            [my.example.other.mount.config :as config]))

;; больше неявных зависимостей


;; "гвоздями" прибитые импорты объектов состояния из других ns
(defstate ^:dynamic *cache*
  :start (do
           (println "starting cache..." (:cache config/*config*))
           {:cache-obj (:cache config/*config*)
            :db db/*db*})

  :stop (do
          (println "disconnect from cache...")))


(comment
  (mount/start #'*cache*)
  *cache*
  (mount/stop #'*cache*))
