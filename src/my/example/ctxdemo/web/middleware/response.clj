(ns my.example.ctxdemo.web.middleware.response
  (:require [unifier.response :as r]
            [jsonista.core :as json]
            [clojure.pprint]
            [cognitect.transit :as transit])
  (:import [java.io ByteArrayOutputStream InputStream File]))

(defn unified-resp->http
  "Middleware converts Unified response to http response."
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (if (r/response? resp)
        (assoc (r/as-http resp)
          :headers (if-let [hdr (-> resp r/get-meta :headers)]
                     hdr
                     {"Content-Type" "text/html;charset=utf-8"})
          :body (r/get-data resp))
        resp))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- edn->transit
  "Convert edn data structure to transit format."
  [format data]
  (let [out    (ByteArrayOutputStream. 4096)
        writer (transit/writer out format {:default-handler (transit/write-handler "object" str)})]
    (transit/write writer data)
    (.toString out)))

(defn- kw-str
  "Convert keyword to string. Other types are ignored."
  [kw]
  (if (keyword? kw)
    (apply str (-> kw str rest))
    kw))

(defn- edn->json
  [data]
  (json/write-value-as-string data (json/object-mapper
                                     {:pretty        true
                                      :encode-key-fn kw-str
                                      :decode-key-fn (comp keyword)})))

(defn- edn->string
  [data]
  (with-out-str (clojure.pprint/pprint data)))

(def accept-formats
  {"application/json"            edn->json
   "text/plain"                  edn->string
   "application/edn"             edn->string
   "application/transit+json"    (partial edn->transit :json)
   "application/transit+msgpack" (partial edn->transit :msgpack)})

(def inverted-accept-formats (clojure.set/map-invert accept-formats))

(defn- convert-data
  "Convert EDN data to one of acceptable formats.
  If no format found then use application/edn by default.
  Returns: {:format _ :data _}"
  [format resp-body]
  (let [tf (get accept-formats format edn->string)]
    {:format (if (string? resp-body)
               "text/plain"
               (get inverted-accept-formats tf))
     :data   (if (string? resp-body)
               resp-body
               (tf resp-body))}))

(defn- map-with-keys? [m & ks]
  (every? #(contains? m %) ks))

(defn convert-response->format
  "Middleware converts EDN response body to requested format.
  Intended only for routes with API handlers (not html content)."
  [handler]
  (fn [request]
    (let [resp (handler request)
          hfn  (fn [h]
                 (or (get h "accept") (get h "Accept") (:accept h) (:Accept h)))]
      (if (r/response? resp)
        (let [old-meta (r/get-meta resp)
              result   (convert-data (-> request :headers hfn) (r/get-data resp))
              resp     (r/set-data resp (:data result))
              rh       (:headers old-meta)
              resp     (r/set-meta resp (assoc-in old-meta [:headers] {"Content-Type" (or
                                                                                        (get rh "Content-Type")
                                                                                        (get rh :content-type)
                                                                                        (str (:format result) ";charset=utf-8"))}))]
          resp)
        (if (map-with-keys? resp :status :body)
          (if (or
                (string? (:body resp))
                (instance? File (:body resp))
                (instance? InputStream (:body resp)))
            resp
            (let [result (convert-data (-> request :headers hfn) (:body resp))]
              (assoc resp :body (:data result) :headers {"Content-Type" (str (:format result) ";charset=utf-8")})))
          resp)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
