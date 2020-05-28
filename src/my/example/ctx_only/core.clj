(ns my.example.ctx-only.core
  (:require [org.rssys.context.core :as ctx]
            [unifier.response :as r]))

;; Main idea
;; f (state, new-request)

(defn f [*state new-request])











;; context is an Atom
(def *ctx (atom {}))

;; create minimal component
(ctx/create! *ctx {:id :database
                   :config {:host "localhost"
                            :port 5432
                            :user "admin"
                            :password "******"}})

;; context structure
*ctx

(ctx/create! *ctx {:id :http-server :config {:host "localhost" :port 8080}})

*ctx












;; errors
;; no double id
(def *ctx (atom {}))

(def result (ctx/create! *ctx {:id :http-server :config {:host "localhost" :port 8080}}))

(type result)

result

(def result (ctx/create! *ctx {:id :http-server :config {:host "localhost" :port 8080}}))

result

(r/error? result)
(r/success? result)
(r/get-type result)
(r/get-data result)
(r/get-meta result)









;; start and stop
(def *ctx (atom {}))

;; minimal executable component
(ctx/create! *ctx {:id       :database
                   :config   {:host "localhost" :port 5432 :user "admin" :password "******"}
                   :start-fn (fn [cfg] (println "got database config:" cfg) {:conn "conn:localhost:5432"})
                   :stop-fn  (fn [db-obj] (println "disconnecting from database..." db-obj))})

;; fn that prepares :config
(ctx/create! *ctx {:id       :http-server
                   :config   (fn [ctx] (println "preparing http server config...") {:host "localhost" :port 8080})
                   :start-fn (fn [cfg] (println "got http config:" cfg) {:http-server "/"})
                   :stop-fn  (fn [http-obj] (println "shutdown http server..." http-obj))})

;; start independent components in any order
(ctx/start! *ctx :database)
(ctx/start! *ctx :http-server)

;; start is idempotent operation
(ctx/start! *ctx :http-server)

;;  stop independent components in any order
(ctx/stop! *ctx :database)
(ctx/stop! *ctx :http-server)

;; stop is idempotent operation
(ctx/stop! *ctx :http-server)













;; анатомия компонента
(def http-comp (ctx/get-component *ctx :http-server))
http-comp


{:id         :http-server                                   ;; идентификатор
 :config     {:host "localhost", :port 8080},               ;; конфигурация
 :start-fn   (fn [_]),                                      ;; функция старта
 :stop-fn    (fn [_]),                                      ;; функция останова
 :state-obj  {:http-server "/"},                            ;; собственно стейт
 :status     :started,                                      ;; статус компонента
 :start-deps []                                             ;; компоненты от которых зависит старт компонента

 :stop-deps  []                                             ;; компоненты которые нужно остановить перед
                                                            ;; остановкой этого компонента. Управляется библиотекой.
 }





;; управление зависимостями
(def *ctx (atom {}))

;; компонент БД
(ctx/create! *ctx {:id         :db
                   :config     {}
                   :start-deps []
                   :start-fn   (fn [config] (prn "start :db"))
                   :stop-fn    (fn [obj-state] (prn "stop :db"))})

;; компонент кэш, зависит от :db
(ctx/create! *ctx {:id         :cache
                   :config     {}
                   :start-deps [:db]
                   :start-fn   (fn [config] (prn "start :cache"))
                   :stop-fn    (fn [obj-state] (prn "stop :cache"))})

;; компонент веб-сервер, зависит от кэша
(ctx/create! *ctx {:id         :web
                   :config     {}
                   :start-deps [:cache]
                   :start-fn   (fn [config] (prn "start :web"))
                   :stop-fn    (fn [obj-state] (prn "stop :web"))})


;; при запуске компонента автоматически запускаются его зависимости
(ctx/start! *ctx :web)

;; посмотрим, какие компоненты запущены
(ctx/started-ids *ctx)

;; посмотрим как зависимые компоненты записаны в контексте
(ctx/get-component *ctx :db)
(ctx/get-component *ctx :cache)

;; при остановке компонента автоматически останавливаются его зависимости
(ctx/stop! *ctx :db)

;; посмотрим, какие компоненты запущены
(ctx/started-ids *ctx)

;; посмотрим, какие компоненты остановлены
(ctx/stopped-ids *ctx)








;; создаем систему

(def *ctx (atom {}))

;; все зависимости и саму карту системы можно задать либо vector либо set
(def system-map #{

                  {:id         :cfg                         ;; cfg component will prepare config for all context
                   :config     {}
                   :start-deps []
                   :start-fn   (fn [config]
                                 (println "reading config data from OS & JVM environment variables or config file")
                                 {:db    {:host "localhost" :port 1234 :user "sa" :password "*****"}
                                  :cache {:host "127.0.0.1" :user "cache-user" :pwd "***"}
                                  :web   {:host "localhost" :port 8080 :root-context "/main"}})
                   :stop-fn    (fn [obj-state])}


                  {:id         :db
                   :config     (fn [ctx] (-> (ctx/get-component-value ctx :cfg) :state-obj :db))
                   :start-deps [:cfg]
                   :start-fn   (fn [config] (println "starting db" :config config))
                   :stop-fn    (fn [obj-state] (println "stopping db..."))}


                  {:id         :cache
                   :config     (fn [ctx] (-> (ctx/get-component-value ctx :cfg) :state-obj :cache))
                   :start-deps #{:cfg :db}
                   :start-fn   (fn [config] (println "starting cache" :config config))
                   :stop-fn    (fn [obj-state] (println "stopping cache..."))}


                  {:id         :log
                   :config     {:output "stdout"}
                   :start-deps []
                   :start-fn   (fn [config] (println "starting logging" :config config))
                   :stop-fn    (fn [obj-state] (println "stopping logging..."))}


                  {:id         :web
                   :config     (fn [ctx] (-> (ctx/get-component-value ctx :cfg) :state-obj :web))
                   :start-deps #{:cfg :db :cache :log}
                   :start-fn   (fn [config]
                                 (println "starting web" :config config)
                                 (println "pass the whole context as atom to web handler:" *ctx))
                   :stop-fn    (fn [obj-state] (println "stopping web..."))}})

;; создание контекста на основе карты компонентов системы
;; модифицирует ctx
(ctx/build-context *ctx system-map)

;; запускаем систему
(ctx/start-all *ctx)
;; или так
(comment (ctx/start! *ctx :web))

;; смотрим что запущено
(ctx/started-ids *ctx)
(ctx/get-component *ctx :cache)

;; компоненты можно остановить и запустить изолировано
(ctx/isolated-stop! *ctx :cache)
(ctx/started-ids *ctx)
(ctx/isolated-start! *ctx :cache)


;; останавливаем систему
(ctx/stop-all *ctx)

(ctx/stopped-ids *ctx)


;; компоненты в set могуть быть расположены не по порядку
;; контекст построит верный граф старта компонентов
(doseq [i system-map]
  (prn (:id i)))






;; демо веб-проекта




;; остановка индивидуальных компонентов
;; циклические зависимости

