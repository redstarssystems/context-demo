(ns my.example.ctxdemo.web.pages.monitoring
  (:require [unifier.response :as r]
            [org.rssys.context.core :as context]
            [io.pedestal.log :as log]
            [org.rssys.util.jvm :as jvm]
            [ring.util.response :as response]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def ^:const thread-dump-ext ".thdump.txt")
(def ^:const heap-dump-ext ".hprof")

(defn debug-handler
  "returns request in a EDN form."
  [req]
  (r/as-success
    (dissoc req :reitit.core/match :reitit.core/router)
    {:headers {"Content-Type" "text/plain;charset=utf-8"}}))

(defn id-handler
  "returns id instance data."
  [*ctx req]
  (r/as-success
    (let [config     (-> (context/get-component *ctx :web) :config)
          ctx-fields [:http-host :http-port
                      :app-name :app-instance :app-group :app-region-dc
                      :admin-networks]
          resp-map   (zipmap ctx-fields ((apply juxt ctx-fields) config))
          resp-map   (assoc resp-map :started-components (context/started-ids *ctx))]
      resp-map)))


(defn heap-dump-handler
  "create heap dump"
  [req]
  (let [filename (str "heapdump" heap-dump-ext)
        result   (jvm/heap-dump filename (Boolean/parseBoolean (-> req :params :live)))]
    (log/info :msg "heap dump is complete" :filename filename :size (:size (result)))
    (r/as-created (str "Created Heap dump file: " filename ", size: " (:size (result)) \newline))))

(defn thread-dump-handler
  "Create Thread dump using JVM internals."
  [req]
  (let [filename (str "jvm" thread-dump-ext)
        output   (-> req :params :output)]
    (case output
      "file" (let [result (jvm/thread-dump-jvm filename)]
               (log/info :msg "Thread dump is complete" :filename filename :size (:size result))
               (r/as-created (str "Created Thread dump file: " filename ", size: " (:size result) \newline)))
      "console" (r/as-created (:dump (jvm/thread-dump-jvm))))))


(defn download-dump-handler
  "Download heap dump or thread dump files created by `heap-dump-handler` or `thread-dump-handler`."
  [*ctx req]
  (let [filename ^String (-> req :params :filename)]
    (if (or
          (str/ends-with? filename thread-dump-ext)
          (str/ends-with? filename heap-dump-ext))
      (let [parent-folder (-> (context/get-component *ctx :cfg) :state-obj :jvm-dump-folder)
            resp          (response/file-response (.getName (io/file parent-folder filename)) {:allow-symlinks? false})]
        (assoc resp
          :headers (merge (:headers resp)
                     {"Content-Type"        "application/octet-stream"
                      "Content-Disposition" (str "attachment; filename=\"" filename "\"")})))
      (r/as-forbidden (format "Allowed file formats: *%s,* %s.txt\n" thread-dump-ext heap-dump-ext)))))
